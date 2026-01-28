package one.modality.booking.frontoffice.bookingpage.pages.terms;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Letter;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingform.BookingFormI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

/**
 * @author Bruno Salmon
 */
public final class TermsAndConditionsPage implements BookingFormPage {

    private final ObjectProperty<Letter> termsLetterProperty = new SimpleObjectProperty<>();
    private final HtmlText termsHtmlText = new HtmlText();
    private final CheckBox agree = Bootstrap.strong(I18nControls.newCheckBox(BookingFormI18nKeys.AgreeTermsAndConditions));
    private final VBox container = BookingElements.createFormPageVBox(true,
        termsHtmlText,
        agree
    );

    public TermsAndConditionsPage(BookingForm bookingForm) {
        Event event = ((EventBookingFormSettings) bookingForm.getSettings()).event();
        event.getStore().<Letter>executeQuery("select <frontend_loadEvent> from Letter where event=? and type.terms limit 1", event)
            .inUiThread()
            .onSuccess(letters -> {
                Letter termsLetter = Collections.first(letters);
                termsLetterProperty.set(termsLetter);
                if (termsLetter == null) {
                    Console.log("No terms and conditions found for event " + event.getId());
                } else {
                    I18nEntities.bindTranslatedEntityToTextProperty(termsHtmlText.textProperty(), termsLetter);
                }
            });
        container.setAlignment(Pos.TOP_LEFT);
        Controls.setupTextWrapping(agree, true, false);
        agree.setCursor(Cursor.HAND);
        // Scrolling up when ticking the `agree` checkbox, so the user can easily find the Next button
        FXProperties.runOnPropertyChange(selected -> UiScheduler.scheduleDelay(500, () -> {
            if (selected) {
                ScrollPane scrollPane = Controls.findScrollPaneAncestor(container);
                if (scrollPane != null) {
                    Node content = scrollPane.getContent();
                    Controls.setVerticalScrollNodeWishedPosition(content, VPos.TOP);
                    SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(content, false, true);
                }
            }
        }), agree.selectedProperty());
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingFormI18nKeys.TermsAndConditions;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return agree.selectedProperty();
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return termsLetterProperty.get() != null;
    }
}
