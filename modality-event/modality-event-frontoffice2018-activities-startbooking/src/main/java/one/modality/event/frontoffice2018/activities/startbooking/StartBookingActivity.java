package one.modality.event.frontoffice2018.activities.startbooking;

import dev.webfx.platform.console.Console;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import one.modality.base.client.actions.ModalityActions;
import one.modality.base.client.entities.util.Labels;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.client2018.activity.bookingprocess.BookingProcessActivity;
import one.modality.event.frontoffice2018.operations.fees.RouteToFeesRequest;
import one.modality.event.frontoffice2018.operations.options.RouteToOptionsRequest;
import one.modality.event.frontoffice2018.operations.program.RouteToProgramRequest;
import one.modality.event.frontoffice2018.operations.terms.RouteToTermsRequest;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.platform.uischeduler.UiScheduler;

/**
 * @author Bruno Salmon
 */
final class StartBookingActivity extends BookingProcessActivity {

    private final Action bookAction = ModalityActions.newVisitBookAction(this::onBookButtonPressed);
    private final Action feesAction = ModalityActions.newVisitFeesAction(this::onFeesButtonPressed);
    private final Action termsAction = ModalityActions.newVisitTermsAndConditionsAction(this::onTermsButtonPressed);

    private ImageView eventImageView;
    private BorderPane eventImageViewContainer;
    private Label eventTitle;

    @Override
    protected void createViewNodes() {
        super.createViewNodes();
        eventImageViewContainer = LayoutUtil.setMinWidth(new BorderPane(eventImageView = new ImageView()), 0);
        eventTitle = new Label();
        eventTitle.setTextFill(Color.WHITE);
        Button bookButton = newTransparentButton(bookAction);
        Button feesButton = newTransparentButton(feesAction);
        Button termsButton = newTransparentButton(termsAction);
        //Button programButton = newTransparentButton(ModalityActions.newVisitProgramAction(this::onProgramButtonPressed));
        Font eventFont = Font.font("Verdana", 24);
        Font bookButtonFont = Font.font("Verdana", 18);
        Font otherButtonFont = Font.font("Verdana", 12);
        eventTitle.setFont(eventFont);
        bookButton.setFont(bookButtonFont);
        feesButton.setFont(otherButtonFont);
        termsButton.setFont(otherButtonFont);
        //programButton.setFont(otherButtonFont);
        double vGap = 20;
        FlowPane flowPane = new FlowPane(5, vGap, feesButton, termsButton/*, programButton*/);
        flowPane.setAlignment(Pos.CENTER);
        verticalStack.setSpacing(vGap);
        verticalStack.getChildren().setAll(eventImageViewContainer, eventTitle, bookButton, flowPane);
        GridPane goldLayout = LayoutUtil.createGoldLayout(verticalStack, 1.0, 0, null);
        pageContainer.setCenter(verticalScrollPane = LayoutUtil.createVerticalScrollPane(goldLayout));
        goldLayout.minHeightProperty().bind(verticalScrollPane.heightProperty());
    }

    @Override
    protected Node styleUi(Node uiNode) {
        fadeOut();
        onEvent().onComplete(ar -> {
            onEventOptions(); // Anticipating event options loading now (required for options and fees pages)
            UiScheduler.runInUiThread(() -> {
                String imageUrl = null;
                if (ar.succeeded()) {
                    Event event = ar.result();
                    Labels.translateLabel(eventTitle, Labels.bestLabelOrName(event));
                    imageUrl = (String) event.evaluate("buddha.image.url");
                } else
                    Console.log(ar.cause());
                if (imageUrl == null)
                    runFadeInAnimation();
                else {
                    Image image = ImageStore.getOrCreateImage(imageUrl);
                    eventImageView.setImage(image);
                    eventImageView.setPreserveRatio(true);
                    if (image != null)
                        eventImageView.fitWidthProperty().bind(FXProperties.combine(eventImageViewContainer.widthProperty(), image.widthProperty(),
                                (w1, w2) -> Math.min(w1.doubleValue(), w2.doubleValue())));
                    if (image == null || !image.isBackgroundLoading())
                        runFadeInAnimation();
                    else
                        image.heightProperty().addListener(observable -> runFadeInAnimation());
                }
            });
        });
        return super.styleUi(uiNode);
    }

    private void fadeOut() {
        verticalStack.setOpacity(0d);
    }

    private void runFadeInAnimation() {
        Animations.animateProperty(verticalStack.opacityProperty(), 1d);
    }

    private void onProgramButtonPressed() {
        new RouteToProgramRequest(getEventId(), getHistory()).execute();
    }

    private void onTermsButtonPressed() {
        new RouteToTermsRequest(getEventId(), getHistory()).execute();
    }

    private void onFeesButtonPressed() {
        new RouteToFeesRequest(getEventId(), getHistory()).execute();
    }

    private void onBookButtonPressed() {
        new RouteToOptionsRequest(getEventId(), getHistory()).execute();
    }
}
