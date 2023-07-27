package one.modality.ecommerce.client2018.activity.bookingprocess;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.shared.entities.Event;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;

/**
 * @author Bruno Salmon
 */
public abstract class BookingProcessActivity
        extends EventDependentViewDomainActivity {

    protected Button backButton;
    protected Button nextButton;

    protected BorderPane pageContainer;
    protected ScrollPane verticalScrollPane;
    protected VBox verticalStack;

    @Override
    public Node buildUi() {
        createViewNodes();
        return styleUi(assemblyViewNodes());
    }

    protected void createViewNodes() {
        if (backButton == null)
            backButton = newTransparentButton("<<Back");
        if (nextButton == null)
            nextButton = newLargeGreenButton( "Next>>");
        backButton.setOnAction(this::onPreviousButtonPressed);
        nextButton.setOnAction(this::onNextButtonPressed);

        pageContainer = new BorderPane(verticalScrollPane = LayoutUtil.createVerticalScrollPaneWithPadding(verticalStack = new VBox(10)));
        verticalStack.setAlignment(Pos.TOP_CENTER);
    }

    protected Node assemblyViewNodes() {
        return pageContainer;
    }

    protected Node styleUi(Node uiNode) {
        if (uiNode instanceof Region)
            FXProperties.runNowAndOnPropertiesChange(() -> onEvent().onComplete(ar -> {
                Event event = ar.result();
                if (event != null) {
                    String css = event.getStringFieldValue("cssClass");
                    if (Strings.startsWith(css,"linear-gradient"))
                        ((Region) uiNode).setBackground(BackgroundFactory.newLinearGradientBackground(css));
                }
            }), eventIdProperty());
        return uiNode;
    }

    private void onPreviousButtonPressed(ActionEvent event) {
        getHistory().goBack();
    }

    protected void onNextButtonPressed(ActionEvent event) { // Should be overridden
    }
}
