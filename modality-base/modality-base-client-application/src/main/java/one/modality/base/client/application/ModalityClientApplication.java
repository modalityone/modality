package one.modality.base.client.application;

import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.ViewDomainActivityContext;
import dev.webfx.stack.routing.activity.ActivityManager;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Bruno Salmon
 */
public class ModalityClientApplication extends Application {

    private final ModalityClientStarterActivity modalityClientStarterActivity;

    public ModalityClientApplication(ModalityClientStarterActivity modalityClientStarterActivity) {
        this.modalityClientStarterActivity = modalityClientStarterActivity;
    }

    @Override
    public void init() {
        ActivityManager.runActivity(modalityClientStarterActivity,
                ViewDomainActivityContext.createViewDomainActivityContext(DataSourceModelService.getDefaultDataSourceModel())
        );
    }

    @Override
    public void start(Stage primaryStage) {
        // On desktops, we open a window with width = 80% screen width and height = 90% screen height
        Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
        double width = screenVisualBounds.getWidth() * 0.8;
        double height = screenVisualBounds.getHeight() * 0.9;
        // We first create a scene with an initial spinner (waiting for the ui router to start)
        Scene scene = DeviceSceneUtil.newScene(Controls.createPageSizeSpinner(), width, height);
        // When the ui router starts, it will set modalityClientStarterActivity.nodeProperty()
        FXProperties.onPropertySet(modalityClientStarterActivity.nodeProperty(), node -> {
            // From that moment on, we bind the scene root to the ui router activity node
            scene.rootProperty().bind(modalityClientStarterActivity.nodeProperty().map(n -> n));
        });
        // Activating focus owner auto scroll
        SceneUtil.installSceneFocusOwnerAutoScroll(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
        // Commented Spinner, as the backoffice status bas has now its own indicators
        //setLoadingSpinnerVisibleConsumer(this::setLoadingSpinnerVisible);
    }

    /*private static void setLoadingSpinnerVisibleConsumer(Consumer<Boolean> consumer) {
        consumeInUiThread(FXProperties.compute(PendingBusCall.pendingCallsCountProperty(), pendingCallsCount -> pendingCallsCount > 0)
                , consumer);
    }

    public static <T> void consumeInUiThread(ObservableValue<T> property, Consumer<T> consumer) {
        FXProperties.consume(property, arg -> UiScheduler.scheduleDeferred(() -> consumer.accept(arg)));
    }


    private ImageView spinner;

    private void setLoadingSpinnerVisible(boolean visible) {
        Scene scene = WebFxKitLauncher.getPrimaryStage().getScene();
        Node root = scene == null ? null : scene.getRoot();
        if (root instanceof Pane) {
            Pane rootPane = (Pane) root;
            if (!visible) {
                rootPane.getChildren().remove(spinner);
            } else if (!rootPane.getChildren().contains(spinner)) {
                if (spinner == null) {
                    spinner = ImageStore.createImageView("one/modality/base/client/images/spinner.gif");
                    spinner.setManaged(false);
                }
                spinner.xProperty().bind(FXProperties.combine(rootPane.widthProperty(), spinner.getImage().widthProperty(), (w1, w2) -> (w1.doubleValue() - w2.doubleValue())/2 ));
                spinner.yProperty().bind(FXProperties.combine(rootPane.heightProperty(), spinner.getImage().heightProperty(), (h1, h2) -> (h1.doubleValue() - h2.doubleValue())/2 ));
                rootPane.getChildren().add(spinner);
            }
        }
    }*/
}
