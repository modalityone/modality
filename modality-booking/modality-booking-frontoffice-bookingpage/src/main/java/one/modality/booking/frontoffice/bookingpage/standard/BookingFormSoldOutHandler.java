package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.binding.Bindings;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.util.Attendances;
import one.modality.base.shared.entities.util.DocumentLines;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.CompositeBookingFormPage;
import one.modality.booking.frontoffice.bookingpage.sections.accommodation.DefaultAccommodationSoldOutSection;
import one.modality.booking.frontoffice.bookingpage.sections.accommodation.HasAccommodationSelectionSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.booking.frontoffice.bookingpage.util.SoldOutErrorParser;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles sold-out accommodation recovery for booking forms.
 * Extracted from StandardBookingForm to improve maintainability.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Displaying the sold-out recovery page when accommodation becomes unavailable</li>
 *   <li>Building alternative accommodation options</li>
 *   <li>Handling user selection of alternative accommodation</li>
 *   <li>Navigation back to summary after selection</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingFormSoldOutHandler {

    /**
     * Callback interface for sold-out handler to communicate with the form.
     */
    public interface SoldOutFormCallback {
        /** Get the current event */
        Event getEvent();

        /** Get the color scheme for styling */
        BookingFormColorScheme getColorScheme();

        /** Get the working booking properties */
        WorkingBookingProperties getWorkingBookingProperties();

        /** Get the working booking */
        WorkingBooking getWorkingBooking();

        /** Get the callbacks for form-specific hooks */
        StandardBookingFormCallbacks getCallbacks();

        /** Navigate to a special page */
        void navigateToSpecialPage(CompositeBookingFormPage page);

        /** Navigate to summary page */
        void navigateToSummary();

        /** Cancel the booking and exit the form */
        void cancelBookingAndExit();

        /** Update summary page buttons */
        void updateSummaryPageButtons();

        /** Set returned from sold-out recovery flag */
        void setReturnedFromSoldOutRecovery(boolean returned);

        /** Get the booking selection state for date checking */
        BookingSelectionState getSelectionState();

        /** Get the main event start date (first day of main event, not early arrival) */
        LocalDate getMainEventStartDate();

        /** Get the main event end date (last day of main event, not late departure) */
        LocalDate getMainEventEndDate();
    }

    private final SoldOutFormCallback callback;

    /**
     * Creates a new sold-out handler.
     *
     * @param callback The callback interface for communicating with the form
     */
    public BookingFormSoldOutHandler(SoldOutFormCallback callback) {
        this.callback = callback;
    }

    /**
     * Handles SOLD_OUT status by showing a recovery page with alternative options.
     *
     * <p>This method is called when the server returns a SOLDOUT error during booking
     * submission. It shows a user-friendly page that explains what happened and allows
     * the user to select an alternative accommodation option.</p>
     *
     * @param soldOutInfo Information about the sold-out item
     */
    public void handleAccommodationSoldOut(SoldOutErrorParser.SoldOutInfo soldOutInfo) {
        PolicyAggregate policyAggregate = callback.getWorkingBookingProperties().getPolicyAggregate();
        if (policyAggregate == null) {
            return;
        }

        // Reload availabilities from the server to get current availability data
        policyAggregate.reloadAvailabilities()
            .onFailure(error -> {
                // Continue anyway with existing data
                UiScheduler.runInUiThread(() -> showSoldOutRecoveryPage(soldOutInfo, policyAggregate));
            })
            .onSuccess(v -> {
                UiScheduler.runInUiThread(() -> showSoldOutRecoveryPage(soldOutInfo, policyAggregate));
            });
    }

    /**
     * Shows the sold-out recovery page after availabilities have been reloaded.
     */
    private void showSoldOutRecoveryPage(SoldOutErrorParser.SoldOutInfo soldOutInfo, PolicyAggregate policyAggregate) {
        // Allow forms to clean up previous accommodation state before showing alternatives
        StandardBookingFormCallbacks callbacks = callback.getCallbacks();
        if (callbacks != null) {
            callbacks.onEnteringSoldOutRecovery();
        }

        // Find the sold-out item
        EntityStore entityStore = policyAggregate.getEntityStore();
        Site soldOutSite = entityStore.getEntity(Site.class, soldOutInfo.getSitePrimaryKey());
        Item soldOutItem = entityStore.getEntity(Item.class, soldOutInfo.getItemPrimaryKey());

        if (soldOutSite == null || soldOutItem == null) {
            return;
        }

        String soldOutItemName = I18nEntities.translateEntity(soldOutItem);
        int soldOutPrice = 0;

        // Get original price from WorkingBooking document lines
        WorkingBooking workingBooking = callback.getWorkingBooking();
        if (workingBooking != null) {
            // First, try to get stored price from document lines
            soldOutPrice = DocumentLines.filterOfSiteAndItem(workingBooking.getDocumentLines().stream(), soldOutSite, soldOutItem)
                .mapToInt(line -> line.getPriceNet() != null ? line.getPriceNet() : 0)
                .sum();

            // If stored price is 0, calculate dynamically using PriceCalculator
            if (soldOutPrice == 0) {
                PriceCalculator priceCalculator = workingBooking.getLatestBookingPriceCalculator();
                soldOutPrice = DocumentLines.filterOfSiteAndItem(workingBooking.getDocumentLines().stream(), soldOutSite, soldOutItem)
                    .mapToInt(priceCalculator::calculateDocumentLinePrice)
                    .sum();
            }
        }

        // Build list of ALL accommodation options (excluding only the originally selected sold-out item)
        // Other items that are also sold out will show with SOLD OUT ribbon
        List<HasAccommodationSelectionSection.AccommodationOption> alternatives =
            buildAlternativeOptions(policyAggregate, soldOutInfo.getSitePrimaryKey(), soldOutInfo.getItemPrimaryKey());

        // Calculate number of nights from the booked accommodation dates
        int numberOfNights = 0;
        if (workingBooking != null) {
            DocumentAggregate docAggregate = workingBooking.getLastestDocumentAggregate();
            // Count unique dates for accommodation item (the sold-out item)
            numberOfNights = (int) docAggregate.getAttendances().stream()
                .filter(a -> Attendances.isOfSiteAndItem(a, soldOutSite, soldOutItem))
                .map(Attendances::getDate)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        }

        // Create the sold-out recovery section
        DefaultAccommodationSoldOutSection soldOutSection = new DefaultAccommodationSoldOutSection();
        soldOutSection.setColorScheme(callback.getColorScheme());
        soldOutSection.setEventName(callback.getEvent() != null ? callback.getEvent().getName() : "");
        soldOutSection.setOriginalSelection(soldOutItemName, soldOutPrice);
        soldOutSection.setNumberOfNights(numberOfNights);

        // Bind to selection state for date restriction checking
        BookingSelectionState selectionState = callback.getSelectionState();
        if (selectionState != null) {
            soldOutSection.bindToSelectionState(selectionState);
        }

        // Pass main event dates for date restriction checking
        soldOutSection.setMainEventStartDate(callback.getMainEventStartDate());
        soldOutSection.setMainEventEndDate(callback.getMainEventEndDate());

        soldOutSection.setAlternativeOptions(alternatives);

        // Create the recovery page using CompositeBookingFormPage with custom buttons
        CompositeBookingFormPage soldOutPage = createSoldOutRecoveryPage(soldOutSection);

        // Show the page using navigateToSpecialPage for proper integration
        callback.navigateToSpecialPage(soldOutPage);
    }

    /**
     * Creates a CompositeBookingFormPage for the sold-out recovery flow with custom navigation buttons.
     *
     * @param soldOutSection The section containing the sold-out info and alternatives
     * @return A configured CompositeBookingFormPage with Continue and Cancel buttons
     */
    private CompositeBookingFormPage createSoldOutRecoveryPage(DefaultAccommodationSoldOutSection soldOutSection) {
        CompositeBookingFormPage page = new CompositeBookingFormPage(
            BookingPageI18nKeys.AccommodationUpdateNeeded,
            soldOutSection
        );

        // Configure page properties
        page.setStep(false)                    // Don't show in step progress header
            .setHeaderVisible(true)            // Show step navigation header
            .setShowingOwnSubmitButton(false); // Show custom navigation buttons from this page

        StandardBookingFormCallbacks callbacks = callback.getCallbacks();

        // Create custom buttons for this page
        // Continue button - triggers callback with new selection, returns to Summary
        BookingFormButton continueButton = new BookingFormButton(
            BookingPageI18nKeys.ContinueWithNewSelection,
            e -> {
                HasAccommodationSelectionSection.AccommodationOption selectedOption = soldOutSection.getSelectedOption();
                if (selectedOption != null && callbacks != null) {
                    // Get roommate info if collected in the sold-out section
                    StandardBookingFormCallbacks.SoldOutRecoveryRoommateInfo roommateInfo = soldOutSection.getRoommateInfo();
                    callbacks.onAccommodationSoldOutRecovery(selectedOption, roommateInfo, () -> {
                        // Set flag to hide back button on Summary page (no going back after sold-out recovery)
                        callback.setReturnedFromSoldOutRecovery(true);
                        callback.updateSummaryPageButtons();
                        callback.navigateToSummary();
                    });
                }
            },
            "btn-primary booking-form-btn-primary",
            Bindings.not(soldOutSection.validProperty())  // Disable until selection and roommate info valid
        );

        // Cancel Booking button - cancels the booking entirely (left side)
        BookingFormButton cancelButton = new BookingFormButton(
            BookingPageI18nKeys.CancelBooking,
            e -> callback.cancelBookingAndExit(),
            "btn-back booking-form-btn-back"
        );

        // Button order: Cancel (left), Continue (right) - matches other page layouts
        page.setButtons(cancelButton, continueButton);

        return page;
    }

    /**
     * Builds a list of alternative accommodation options, excluding the sold-out item.
     * Always includes a Day Visitor option as a fallback.
     */
    private List<HasAccommodationSelectionSection.AccommodationOption> buildAlternativeOptions(PolicyAggregate policy, Object excludeSiteId, Object excludeItemId) {
        List<HasAccommodationSelectionSection.AccommodationOption> options = new ArrayList<>();

        // Build accommodation options from scheduled items (if available)
        List<ScheduledItem> scheduledItems = policy.getScheduledItems();
        if (scheduledItems != null && !scheduledItems.isEmpty()) {
            // Group scheduled items by Item to find accommodation options
            Map<Object, List<ScheduledItem>> itemGroups =
                ScheduledItems.filterFamily(scheduledItems.stream(), KnownItemFamily.ACCOMMODATION)
                .collect(Collectors.groupingBy(si -> Entities.getPrimaryKey(si.getItem())));

            for (Map.Entry<Object, List<ScheduledItem>> entry : itemGroups.entrySet()) {
                Object itemId = entry.getKey();
                // Skip the sold-out item
                if (Entities.samePrimaryKey(itemId, excludeItemId))
                    continue;

                List<ScheduledItem> itemScheduledItems = entry.getValue();

                if (itemScheduledItems.isEmpty()) continue;

                ScheduledItem firstSi = itemScheduledItems.get(0);
                Item item = firstSi.getItem();

                // Calculate minimum availability across all days
                int minAvailability = itemScheduledItems.stream()
                    .mapToInt(si -> si.getGuestsAvailability() != null ? si.getGuestsAvailability() : 0)
                    .min()
                    .orElse(0);

                HasAccommodationSelectionSection.AvailabilityStatus status =
                    minAvailability <= 0
                        ? HasAccommodationSelectionSection.AvailabilityStatus.SOLD_OUT
                        : minAvailability <= 5
                            ? HasAccommodationSelectionSection.AvailabilityStatus.LIMITED
                            : HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE;

                // Get rate for pricing
                Rate rate = policy.getScheduledItemDailyRate(firstSi);
                int pricePerNight = rate != null && rate.getPrice() != null ? rate.getPrice() : 0;
                boolean perPerson = rate == null || rate.isPerPerson();

                // Get constraint and early/late arrival flags from ItemPolicy
                ItemPolicy itemPolicy = policy.getItemPolicy(item);
                HasAccommodationSelectionSection.ConstraintType constraintType = HasAccommodationSelectionSection.ConstraintType.NONE;
                String constraintLabel = null;
                int minNights = 0;
                // Default to true (allowed) if not specified
                boolean earlyArrivalAllowed = true;
                boolean lateDepartureAllowed = true;

                if (itemPolicy != null) {
                    if (itemPolicy.getMinDay() != null && itemPolicy.getMinDay() > 0) {
                        constraintType = HasAccommodationSelectionSection.ConstraintType.MIN_NIGHTS;
                        minNights = itemPolicy.getMinDay();
                        constraintLabel = I18n.getI18nText(BookingPageI18nKeys.MinNights, minNights);
                    }
                    // Read early/late arrival restrictions (null = allowed)
                    earlyArrivalAllowed = !Boolean.FALSE.equals(itemPolicy.isEarlyAccommodationAllowed());
                    lateDepartureAllowed = !Boolean.FALSE.equals(itemPolicy.isLateAccommodationAllowed());
                }

                HasAccommodationSelectionSection.AccommodationOption option = new HasAccommodationSelectionSection.AccommodationOption(
                    itemId,
                    item,
                    item.getName(),
                    null, // Item doesn't have a description field
                    pricePerNight,
                    status,
                    constraintType,
                    constraintLabel,
                    minNights,
                    false,         // isDayVisitor
                    null,          // imageUrl
                    perPerson,
                    -1,            // preCalculatedTotalPrice
                    earlyArrivalAllowed,
                    lateDepartureAllowed
                );

                options.add(option);
            }
        }

        // Sort accommodation options: available first, then by price
        options.sort(Comparator
            .comparing((HasAccommodationSelectionSection.AccommodationOption o) ->
                o.getAvailability() == HasAccommodationSelectionSection.AvailabilityStatus.SOLD_OUT ? 1 : 0)
            .thenComparingInt(HasAccommodationSelectionSection.AccommodationOption::getPricePerNight));

        // Add Share Accommodation option if configured in the policy
        ItemPolicy sharingAccommodationItemPolicy = policy.getSharingAccommodationItemPolicy();
        if (sharingAccommodationItemPolicy != null) {
            Item sharingItem = sharingAccommodationItemPolicy.getItem();
            if (sharingItem != null) {
                // Get rate for pricing
                Rate shareRate = policy.filterDailyRatesStreamOfSiteAndItem(null, sharingItem)
                    .findFirst()
                    .orElse(null);
                int sharePricePerNight = shareRate != null && shareRate.getPrice() != null ? shareRate.getPrice() : 0;

                // Read early/late arrival restrictions from the sharing item policy
                boolean shareEarlyAllowed = !Boolean.FALSE.equals(sharingAccommodationItemPolicy.isEarlyAccommodationAllowed());
                boolean shareLateAllowed = !Boolean.FALSE.equals(sharingAccommodationItemPolicy.isLateAccommodationAllowed());

                HasAccommodationSelectionSection.AccommodationOption shareAccommodation = new HasAccommodationSelectionSection.AccommodationOption(
                    sharingItem.getPrimaryKey(),
                    sharingItem,
                    sharingItem.getName() != null ? sharingItem.getName() : I18n.getI18nText(BookingPageI18nKeys.ShareAccommodation),
                    I18n.getI18nText(BookingPageI18nKeys.ShareAccommodationDescription),
                    sharePricePerNight,
                    HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE,
                    HasAccommodationSelectionSection.ConstraintType.NONE,
                    null,
                    0,
                    false,          // isDayVisitor = false
                    null,
                    true,           // perPerson
                    -1,             // preCalculatedTotalPrice
                    shareEarlyAllowed,
                    shareLateAllowed
                );
                options.add(shareAccommodation);
            }
        }

        // Always add Day Visitor option at the end as a fallback
        // Day visitors don't stay overnight, so early arrival and late departure don't apply
        HasAccommodationSelectionSection.AccommodationOption dayVisitor = new HasAccommodationSelectionSection.AccommodationOption(
            "DAY_VISITOR",  // special itemId
            null,           // no itemEntity
            I18n.getI18nText(BookingPageI18nKeys.DayVisitor),
            I18n.getI18nText(BookingPageI18nKeys.DayVisitorDescription),
            0,              // pricePerNight = 0 (no accommodation cost)
            HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE,
            HasAccommodationSelectionSection.ConstraintType.NONE,
            null,
            0,
            true,           // isDayVisitor = true
            null,
            true,           // perPerson
            -1,             // preCalculatedTotalPrice
            false,          // earlyArrivalAllowed = false (day visitors stay within event only)
            false           // lateDepartureAllowed = false
        );
        options.add(dayVisitor);

        return options;
    }
}
