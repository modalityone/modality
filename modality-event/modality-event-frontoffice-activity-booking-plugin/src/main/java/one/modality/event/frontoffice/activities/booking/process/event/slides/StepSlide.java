package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.recurringevents.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.fx.FXGuestToBook;
import one.modality.event.client.recurringevents.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.client.recurringevents.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.util.function.Supplier;

abstract class StepSlide implements Supplier<Node> {

    private final BookEventActivity bookEventActivity;
    protected final VBox mainVbox = new VBox();

    StepSlide(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        mainVbox.setAlignment(Pos.TOP_CENTER);
        // Setting a good bottom margin (1/3 screen height), so bottom elements are not stuck at the booking of the screen
        mainVbox.setPadding(new Insets(0, 0, Screen.getPrimary().getVisualBounds().getHeight() / 3, 0));
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

    WorkingBooking getWorkingBooking() {
        return getWorkingBookingProperties().getWorkingBooking();
    }

    DocumentAggregate getDocumentAggregate() {
        return getWorkingBookingProperties().getDocumentAggregate();
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

    void displayPendingPaymentSlide() {
        getBookEventActivity().displayPendingPaymentSlide();
    }

    void displayFailedPaymentSlide() {
        getBookEventActivity().displayFailedPaymentSlide();
    }

    void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        getBookEventActivity().displayCancellationSlide(cancelPaymentResult);
    }

    void displayErrorMessage(String message) {
        getBookEventActivity().displayErrorMessage(message);
    }

    void displayThankYouSlide() {
        getBookEventActivity().displayThankYouSlide();
    }

    void initiateNewPaymentAndDisplayPaymentSlide() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        Object documentPrimaryKey = workingBookingProperties.getWorkingBooking().getDocumentPrimaryKey();
        turnOnWaitMode();
        PaymentService.initiatePayment(
                ClientPaymentUtil.createInitiatePaymentArgument(workingBookingProperties.getBalance(), documentPrimaryKey)
            )
            .onFailure(paymentResult -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                displayErrorMessage("ErrorWhileInitiatingPayment");
                Console.log(paymentResult);
            }))
            .onSuccess(paymentResult -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                HasPersonalDetails buyerDetails = FXUserPerson.getUserPerson();
                if (buyerDetails == null)
                    buyerDetails = FXGuestToBook.getGuestToBook();
                WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, buyerDetails);
                displayPaymentSlide(webPaymentForm);
            }));
    }

    void cancelOrUncancelBookingAndDisplayNextSlide(boolean cancel) {
        WorkingBooking workingBooking = getWorkingBookingProperties().getWorkingBooking();
        if (cancel)
            workingBooking.cancelBooking();
        else
            workingBooking.uncancelBooking();
        turnOnWaitMode();
        workingBooking.submitChanges(cancel ? "Cancelled booking" : "Uncancelled booking")
            .onFailure(ex -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                displayErrorMessage(ex.getMessage());
            }))
            .onSuccess(ignored -> {
                if (cancel)
                    displayCancellationSlide(new CancelPaymentResult(true));
                else
                    getBookEventActivity().loadBookingWithSamePolicy(false)
                        .onComplete(ar -> UiScheduler.runInUiThread(this::turnOffWaitMode));
            });
    }

    void turnOnWaitMode() {
    }

    void turnOffWaitMode() {
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return getBookEventActivity().getRecurringEventSchedule();
    }

    static void turnOnButtonWaitMode(Button... buttons) {
        OperationUtil.turnOnButtonsWaitMode(buttons);
    }

    static void turnOffButtonWaitMode(Button button, String i18nKey) {
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

    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }


}
