package one.modality.booking.frontoffice.bookingpage.pages.prerequisite;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.stack.orm.entity.Entities;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class PrerequisitePage implements BookingFormPage {

    private final Label prerequisiteMessageLabel = BookingElements.createWordingLabel();
    private final CheckBox prerequisiteConfirmCheckBox = new CheckBox();
    private final Label prerequisiteConfirmLabel = new Label();
    // The reason for using an HBox + checkbox + label instead of a simple checkbox is to keep the checkbox on top when
    // the label is multiline. (the standard checkbox is centering it vertically, but we prefer it to be on top)
    private final HBox prerequisiteConfirmHBox = new HBox(5, prerequisiteConfirmCheckBox, prerequisiteConfirmLabel);
    private final VBox container = BookingElements.createFormPageVBox(true,
        prerequisiteMessageLabel,
        prerequisiteConfirmHBox
    );

    public PrerequisitePage(Object prerequisiteMessageI18nKey, Object prerequisiteConfirmI18nKey) {
        I18nControls.bindI18nProperties(prerequisiteMessageLabel, prerequisiteMessageI18nKey);
        BookingElements.wordingLabel(prerequisiteConfirmLabel, false, prerequisiteConfirmI18nKey);
        // A few tweaks:
        // Removing the text center alignement set by wordingLabel()
        prerequisiteConfirmLabel.setTextAlignment(TextAlignment.LEFT);
        // Reproducing the standard checkbox behavior which also reacts on label click
        prerequisiteConfirmLabel.setOnMouseClicked(e -> prerequisiteConfirmCheckBox.setSelected(!prerequisiteConfirmCheckBox.isSelected()));
        // Only on the web version:
        if (UserAgent.isBrowser()) {
            // Removing the default text cursor on the label
            prerequisiteConfirmLabel.setCursor(Cursor.DEFAULT);
            // Preventing the checkbox to shrink horizontally
            prerequisiteConfirmCheckBox.setMinWidth(Region.USE_PREF_SIZE);
            // Removing the HBox spacing (otherwise the space is too large for some reason)
            prerequisiteConfirmHBox.setSpacing(0);
            // Correcting the vertical position of the checkbox so it aligns vertically with the first line of the label
            prerequisiteConfirmCheckBox.setTranslateY(2);
            // Forcing a transparent border to remove the dashed blue border around the focused checkbox
            prerequisiteConfirmCheckBox.setBorder(Border.stroke(Color.TRANSPARENT));
        } else {
            // Correcting the vertical position of the checkbox so it aligns vertically with the first line of the label
            prerequisiteConfirmCheckBox.setTranslateY(-2); // The correction is different for OpenJFX and WebFX
        }
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Prerequisite;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // We use this booking form also for MKMC online empowerment weekends (hardcoded for now)
        return workingBooking.isNewBooking() && Entities.samePrimaryKey(workingBooking.getEvent().getType(), 24);
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {

    }

    @Override
    public ObservableBooleanValue canGoForwardProperty() {
        return prerequisiteConfirmCheckBox.selectedProperty();
    }
}
