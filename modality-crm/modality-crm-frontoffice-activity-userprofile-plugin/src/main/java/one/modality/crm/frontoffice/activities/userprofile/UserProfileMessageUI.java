package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * @author David Hello
 */
final class UserProfileMessageUI {

    private final VBox container = new VBox();
    private final Label title = Bootstrap.textPrimary(Bootstrap.h2(new Label()));
    private final Label infoMessage = Bootstrap.textDanger(new Label());
    private DialogCallback callback;


    public UserProfileMessageUI() {
        title.setPadding(new Insets(50, 0, 80, 0));

        infoMessage.setWrapText(true);
        infoMessage.setPadding(new Insets(0, 20, 30, 20));
        Button closeButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Close));
        closeButton.setOnAction(e2 -> callback.closeDialog());
        VBox.setMargin(closeButton, new Insets(0, 0, 50, 0));
        container.getChildren().setAll(title, infoMessage, closeButton);
        container.setAlignment(Pos.TOP_CENTER);
        container.getStyleClass().add("user-profile-modal-window");
        //container.setPrefWidth(UserProfileActivity.MODAL_WINDOWS_MAX_WIDTH);
        container.setSpacing(20);
        container.setOnMouseClicked(Event::consume);

        container.setBackground(Background.fill(Color.WHITE));
    }

    public VBox getView() {
        return container;
    }

    public void setInfoMessage(Object messageI18nKey, String cssClasses) {
        I18nControls.bindI18nProperties(infoMessage, messageI18nKey);
        infoMessage.getStyleClass().clear();
        infoMessage.getStyleClass().add(cssClasses);
    }
    public void setTitle(Object titleI18nKey) {
        I18nControls.bindI18nProperties(title, titleI18nKey);
    }

    public void resetToInitialState() {
        I18nControls.bindI18nProperties(infoMessage, "");
        infoMessage.getStyleClass().clear();
    }

    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }
}
