package one.modality.booking.frontoffice.bookingpage.sections.generalprogram;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasClassDateSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultSummarySection;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Default summary section for General Program Class booking forms.
 * Shows selected class dates and pricing with full term discount.
 *
 * <p>This section integrates with {@link HasClassDateSelectionSection} to display
 * a dynamic price breakdown based on the selected class dates.</p>
 *
 * @author Claude
 */
public class DefaultGeneralProgramSummarySection extends DefaultSummarySection {

    private final HasClassDateSelectionSection dateSelectionSection;

    // UI components for dynamic updates
    private Label datesLabel;
    private Label subtotalLabel;
    private Label subtotalValue;
    private HBox discountRow;
    private Label discountValue;
    private HBox alreadyPaidRow;
    private Label alreadyPaidValue;
    private Label totalLabel;
    private Label totalValue;
    private VBox priceContentBox;

    // Differential pricing state
    private boolean isModification = false;
    private int initialBookingPrice = 0;

    public DefaultGeneralProgramSummarySection(HasClassDateSelectionSection dateSelectionSection) {
        super();  // This calls buildUI() -> buildPriceBreakdownSection() before dateSelectionSection is set
        this.dateSelectionSection = dateSelectionSection;

        // Now that dateSelectionSection is set, update the content and add listener
        updatePriceContent();
        dateSelectionSection.getSelectedItems().addListener((ListChangeListener<ScheduledItem>) change -> {
            updatePriceContent();
        });
    }

    @Override
    protected VBox buildPriceBreakdownSection() {
        VBox section = new VBox(0);

        // Section header: "Price Breakdown"
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.PriceBreakdown, StyledSectionHeader.ICON_TAG);

        // Content box - static card (no hover effects for informational display)
        VBox contentBox = BookingPageUIBuilder.createPassiveCard();

        // Build GP-specific price breakdown content
        priceContentBox = buildGeneralProgramPriceContent();

        contentBox.getChildren().add(priceContentBox);
        section.getChildren().addAll(header, contentBox);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        return section;
    }

    private VBox buildGeneralProgramPriceContent() {
        VBox content = new VBox(12);

        // Selected dates list
        datesLabel = new Label();
        datesLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        datesLabel.setWrapText(true);
        content.getChildren().add(datesLabel);

        // Divider
        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.getStyleClass().add("bookingpage-bg-gray");
        VBox.setMargin(divider, new Insets(4, 0, 4, 0));
        content.getChildren().add(divider);

        // Subtotal row
        HBox subtotalRow = new HBox();
        subtotalRow.setAlignment(Pos.CENTER_LEFT);
        subtotalRow.setPadding(new Insets(8, 0, 8, 0));

        subtotalLabel = new Label();
        subtotalLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");

        Region subtotalSpacer = new Region();
        HBox.setHgrow(subtotalSpacer, Priority.ALWAYS);

        subtotalValue = new Label();
        subtotalValue.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        subtotalRow.getChildren().addAll(subtotalLabel, subtotalSpacer, subtotalValue);
        content.getChildren().add(subtotalRow);

        // Discount row (initially hidden)
        discountRow = new HBox();
        discountRow.setAlignment(Pos.CENTER_LEFT);
        discountRow.setPadding(new Insets(0, 0, 8, 0));
        discountRow.setVisible(false);
        discountRow.setManaged(false);

        Label discountLabel = new Label("Full term discount");
        discountLabel.getStyleClass().add("bookingpage-discount-label");

        Region discountSpacer = new Region();
        HBox.setHgrow(discountSpacer, Priority.ALWAYS);

        discountValue = new Label();
        discountValue.getStyleClass().add("bookingpage-discount-value");

        discountRow.getChildren().addAll(discountLabel, discountSpacer, discountValue);
        content.getChildren().add(discountRow);

        // Already paid row (for modifications - initially hidden)
        alreadyPaidRow = new HBox();
        alreadyPaidRow.setAlignment(Pos.CENTER_LEFT);
        alreadyPaidRow.setPadding(new Insets(0, 0, 8, 0));
        alreadyPaidRow.setVisible(false);
        alreadyPaidRow.setManaged(false);

        Label alreadyPaidLabel = I18nControls.newLabel(BookingPageI18nKeys.AlreadyPaid);
        alreadyPaidLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-label-caption");

        Region alreadyPaidSpacer = new Region();
        HBox.setHgrow(alreadyPaidSpacer, Priority.ALWAYS);

        alreadyPaidValue = new Label();
        alreadyPaidValue.getStyleClass().addAll("bookingpage-text-base", "bookingpage-label-caption");

        alreadyPaidRow.getChildren().addAll(alreadyPaidLabel, alreadyPaidSpacer, alreadyPaidValue);
        content.getChildren().add(alreadyPaidRow);

        // Divider before total
        Region totalDivider = new Region();
        totalDivider.setMinHeight(2);
        totalDivider.setMaxHeight(2);
        totalDivider.getStyleClass().add("bookingpage-bg-gray");
        VBox.setMargin(totalDivider, new Insets(4, 0, 0, 0));
        content.getChildren().add(totalDivider);

        // Total row (label text will be updated based on modification state)
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(16, 0, 0, 0));

        totalLabel = new Label("Total");  // Use field for dynamic updates
        totalLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        totalValue = new Label();
        totalValue.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-font-bold", "bookingpage-text-primary");

        totalRow.getChildren().addAll(totalLabel, totalSpacer, totalValue);
        content.getChildren().add(totalRow);

        // Note: Don't call updatePriceContent() here - dateSelectionSection is not yet set during super() constructor
        // It will be called from our constructor after dateSelectionSection is assigned

        return content;
    }

    private void updatePriceContent() {
        // Guard against null during initial construction (super() calls buildUI before our field is set)
        if (dateSelectionSection == null || datesLabel == null) {
            return;
        }

        // Check if this is a modification and calculate initial price
        checkIfModification();

        List<ScheduledItem> selectedItems = dateSelectionSection.getSelectedItems();
        boolean allSelected = dateSelectionSection.isAllDatesSelected();
        int subtotal = dateSelectionSection.getSubtotal();
        int discount = dateSelectionSection.getDiscount();
        int total = dateSelectionSection.getTotalPrice();

            // Update dates label
            if (!selectedItems.isEmpty()) {
                String datesText = formatSelectedDates(selectedItems);
                datesLabel.setText(datesText);
                datesLabel.setVisible(true);
                datesLabel.setManaged(true);
            } else {
                datesLabel.setText("No classes selected");
                datesLabel.setVisible(true);
                datesLabel.setManaged(true);
            }

            // Update subtotal
            int numSelected = selectedItems.size();
            if (allSelected) {
                subtotalLabel.setText("All " + numSelected + " classes (full term)");
            } else {
                subtotalLabel.setText(numSelected + " class" + (numSelected != 1 ? "es" : ""));
            }
            subtotalValue.setText(formatPrice(subtotal));

        // Update discount
        if (discount > 0) {
            discountValue.setText("-" + formatPrice(discount));
            discountRow.setVisible(true);
            discountRow.setManaged(true);
        } else {
            discountRow.setVisible(false);
            discountRow.setManaged(false);
        }

            // Hide "Already paid" row for new bookings
            alreadyPaidRow.setVisible(false);
            alreadyPaidRow.setManaged(false);

            // Show normal total
            totalLabel.setText("Total");
            totalValue.setText(formatPrice(total));
        }

    /**
     * Checks if this is a modification of an existing booking and calculates the initial price.
     */
    private void checkIfModification() {
        if (workingBookingProperties == null) {
            isModification = false;
            initialBookingPrice = 0;
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        if (workingBooking == null || workingBooking.isNewBooking()) {
            isModification = false;
            initialBookingPrice = 0;
            return;
        }

        // This is a modification - calculate initial booking price
        isModification = true;
        DocumentAggregate initialAggregate = workingBooking.getInitialDocumentAggregate();
        if (initialAggregate != null) {
            PriceCalculator initialPriceCalculator = new PriceCalculator(initialAggregate);
            initialBookingPrice = initialPriceCalculator.calculateTotalPrice();
        } else {
            initialBookingPrice = 0;
        }
    }

    private String formatSelectedDates(List<ScheduledItem> items) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
        return items.stream()
            .filter(item -> item.getDate() != null)
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .map(item -> item.getDate().format(formatter))
            .collect(Collectors.joining(", "));
    }

    private String formatPrice(int priceInCents) {
        return PriceFormatter.formatPriceWithCurrencyNoDecimals(priceInCents);
    }

    @Override
    public void refreshPriceBreakdown() {
        // If priceLines were added externally (e.g., PAY_BOOKING flow), render them
        if (!priceLines.isEmpty()) {
            renderExternalPriceLines();
            return;
        }
        // Otherwise, use our GP-specific price content from dateSelectionSection
        updatePriceContent();
    }

    /**
     * Renders externally-added price lines (used for PAY_BOOKING flow).
     * This displays the price lines in the GP-specific UI elements.
     */
    private void renderExternalPriceLines() {
        if (datesLabel == null || subtotalLabel == null || totalValue == null) {
            return;
        }

        // Build dates text from price line descriptions
        StringBuilder datesText = new StringBuilder();
        int total = 0;

        for (PriceLine line : priceLines) {
            if (datesText.length() > 0) {
                datesText.append(", ");
            }
            // Use description (dates) if available, otherwise use name
            String lineText = line.getDescription() != null && !line.getDescription().isEmpty()
                    ? line.getDescription()
                    : line.getName();
            datesText.append(lineText);
            total += line.getAmount();
        }

        // Update UI elements
        datesLabel.setText(datesText.toString());
        datesLabel.setVisible(true);
        datesLabel.setManaged(true);

        // Get class count from dateSelectionSection if available, otherwise count price lines
        int classCount;
        if (dateSelectionSection != null) {
            classCount = dateSelectionSection.getSelectedItems().size();
        } else {
            classCount = priceLines.size();
        }

        // Show class count and total
        subtotalLabel.setText(classCount + " class" + (classCount != 1 ? "es" : ""));
        subtotalValue.setText(formatPrice(total));

        // Hide discount for external price lines
        discountRow.setVisible(false);
        discountRow.setManaged(false);

        // Hide already paid row
        alreadyPaidRow.setVisible(false);
        alreadyPaidRow.setManaged(false);

        // Update total
        totalLabel.setText("Total");
        totalValue.setText(formatPrice(total));
    }
}
