package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class UserProfileMessageUI {
    protected VBox container = new VBox();
    private static final int MAX_WIDTH = 486;
    protected Label title = Bootstrap.textPrimary(Bootstrap.h3(new Label()));
    protected Label infoMessage = Bootstrap.textDanger(new Label());
    private final UserProfileActivity parentActivity;

    public UserProfileMessageUI(UserProfileActivity activity) {
        parentActivity = activity;
        container.setPadding(new Insets(100,0,0,0));
        title.setPadding(new Insets(0,0,50,0));

        Hyperlink backHyperlink = I18nControls.newHyperlink(UserProfileI18nKeys.BackToUserProfile);
        backHyperlink.setOnAction(e2-> Platform.runLater(()-> parentActivity.getTransitionPane().transitToContent(parentActivity.getContainer())));

        container.getChildren().setAll(title,infoMessage,backHyperlink);
        container.setMaxWidth(MAX_WIDTH);
        container.setSpacing(20);
    }

    public VBox getView() {
        return container;
    }

    public void setInfoMessage(String messageKey,String cssClasses) {
        I18nControls.bindI18nProperties(infoMessage,messageKey);
        infoMessage.getStyleClass().clear();
        infoMessage.getStyleClass().add(cssClasses);
    }
    public void setTitle(String titleKey) {
        I18nControls.bindI18nProperties(title,titleKey);
    }
}
