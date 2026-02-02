package one.modality.booking.frontoffice.bookingpage.pages.teaching;

import dev.webfx.extras.panes.LargestFittingChildPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.entity.Entities;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Region;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class TeachingPeriodOption {

    private final RadioButton radioButton = BookingElements.optionLabel(new RadioButton());
    private final Label longPeriodLabel = BookingElements.createPeriodLabel();
    private final Label shortPeriodLabel = BookingElements.createPeriodLabel();
    private final LargestFittingChildPane periodLabel = new LargestFittingChildPane(longPeriodLabel, shortPeriodLabel);
    private final Label priceLabel = BookingElements.createPriceLabel();

    TeachingPeriodOption(BookablePeriod bookablePeriod, WorkingBooking workingBooking) {
        //radioButton.setMinWidth(Region.USE_PREF_SIZE);
        radioButton.setMinWidth(0);
        Controls.setupTextWrapping(radioButton, true, false);
        I18nEntities.bindTranslatedEntityToTextProperty(radioButton, bookablePeriod);
        I18nEntities.bindExpressionToTextProperty(longPeriodLabel,  bookablePeriod, "dateIntervalFormat(startScheduledItem.date, endScheduledItem.date) + ' (' + (endScheduledItem.date - startScheduledItem.date + 1) + ' [days])'");
        I18nEntities.bindExpressionToTextProperty(shortPeriodLabel, bookablePeriod, "dateIntervalFormat(startScheduledItem.date, endScheduledItem.date)");
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        LocalDate startDate = bookablePeriod.getStartScheduledItem().getDate();
        LocalDate endDate = bookablePeriod.getEndScheduledItem().getDate();
        Item item = bookablePeriod.getStartScheduledItem().getItem();
        List<ScheduledItem> bookableScheduledItems = Collections.filter(policyAggregate.getScheduledItems(),
            si -> Entities.sameId(si.getItem(), item) && Times.isBetween(si.getDate(), startDate, endDate));
        BookingElements.setupPeriodOption(bookableScheduledItems, priceLabel, radioButton.selectedProperty(), workingBooking);
    }

    RadioButton getRadioButton() {
        return radioButton;
    }

    Region getPeriodLabel() {
        return periodLabel;
    }

    Label getPriceLabel() {
        return priceLabel;
    }

}
