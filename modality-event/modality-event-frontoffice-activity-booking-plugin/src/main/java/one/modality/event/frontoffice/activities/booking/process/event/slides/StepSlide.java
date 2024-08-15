package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.fx.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.util.function.Supplier;

abstract class StepSlide implements Supplier<Node> {

    private final BookEventActivity bookEventActivity;
    protected final VBox mainVbox = new VBox();

    StepSlide(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        mainVbox.setAlignment(Pos.TOP_CENTER);
        mainVbox.setPadding(new Insets(0, 0, 80, 0));
        // Also a background is necessary for devices not supporting inverse clipping used in circle animation (ex: iPadOS)
        mainVbox.setBackground(Background.fill(Color.WHITE));
    }

    public Node get() {
        if (mainVbox.getChildren().isEmpty()) {
            buildSlideUi();
        }
        return mainVbox;
    }

    abstract void buildSlideUi();

    void reset() {
        mainVbox.getChildren().clear();
    }

    BookEventActivity getBookEventActivity() {
        return bookEventActivity;
    }

    WorkingBookingProperties getWorkingBookingProperties() {
        return getBookEventActivity().getWorkingBookingProperties();
    }

    Event getEvent() {
        return getWorkingBookingProperties().getEvent();
    }

    void displayBookSlide() {
        getBookEventActivity().displayBookSlide();
    }

    void displayCheckoutSlide() {
        getBookEventActivity().displayCheckoutSlide();
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        getBookEventActivity().displayPaymentSlide(webPaymentForm);
    }

    void displayCancellationSlide() {
        getBookEventActivity().displayCancellationSlide();
    }

    void displayErrorMessage(String message) {
        getBookEventActivity().displayErrorMessage(message);
    }

    void displayThankYouSlide() {
        getBookEventActivity().displayThankYouSlide();
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return getBookEventActivity().getRecurringEventSchedule();
    }

    protected static void turnOnButtonWaitMode(Button... buttons) {
        OperationUtil.turnOnButtonsWaitMode(buttons);
    }

    protected static void turnOffButtonWaitMode(Button button, String i18nKey) {
        OperationUtil.turnOffButtonsWaitMode(button); // but this doesn't reestablish the possible i18n graphic
        // So we reestablish it using i18n
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
    }

    Button createPersonToBookButton() {
        Text personPrefixText = TextUtility.createText("PersonToBook:", Color.GRAY);
        EntityButtonSelector<Person> personSelector = new EntityButtonSelector<Person>(
                "{class: 'Person', alias: 'p', columns: [{expression: `[genderIcon,firstName,lastName]`}], orderBy: 'id'}",
                getBookEventActivity(), FXMainFrameDialogArea::getDialogArea, getBookEventActivity().getDataSourceModel()
        ) { // Overriding the button content to add the "Teacher" prefix text
            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                return new HBox(10, personPrefixText, super.getOrCreateButtonContentFromSelectedItem());
            }
        }.ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()));
        personSelector.selectedItemProperty().bindBidirectional(FXPersonToBook.personToBookProperty());
        Button personButton = Bootstrap.largeButton(personSelector.getButton());
        personButton.setMinWidth(300);
        personButton.setMaxWidth(Region.USE_PREF_SIZE);
        VBox.setMargin(personButton, new Insets(20, 0, 20, 0));
        personButton.visibleProperty().bind(FXModalityUserPrincipal.loggedInProperty());
        personButton.managedProperty().bind(FXModalityUserPrincipal.loggedInProperty());
        return personButton;
    }

}
