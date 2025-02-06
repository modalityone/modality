package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.ModalityI18nKeys;
import dev.webfx.stack.ui.validation.ValidationSupport;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;

/**
 * @author Bruno Salmon
 */
final class GuestPanel implements MaterialFactoryMixin {

    private final BorderPane container = new BorderPane();
    private final VBox guestTopVBox = new VBox(10);
    private final TextField firstNameTextField = newMaterialTextField(CrmI18nKeys.FirstName);
    private final TextField lastNameTextField = newMaterialTextField(CrmI18nKeys.LastName);
    private final TextField emailTextField = newMaterialTextField(CrmI18nKeys.Email);
    private final Button guestSubmitButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ModalityI18nKeys.Submit));
    private final ValidationSupport validationSupport = new ValidationSupport();

    public GuestPanel() {
        emailTextField.getProperties().put("webfx-input-type", "email");
        emailTextField.getProperties().put("webfx-input-autocomplete", "email");
        Label guestDetailsLabel = Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookingI18nKeys.GuestDetails)));
        guestTopVBox.getChildren().add(guestDetailsLabel);
        guestTopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(guestTopVBox, new Insets(0, 0, 20,0));
        container.setTop(guestTopVBox);
        LayoutUtil.setMaxWidthToInfinite(guestSubmitButton);
        GridPane.setMargin(guestSubmitButton, new Insets(40, 0, 0, 0));
        GridPane guestGridPane = new GridPaneBuilder()
                .addNodeFillingRow(firstNameTextField)
                .addNodeFillingRow(lastNameTextField)
                .addNodeFillingRow(emailTextField)
                .addNodeFillingRow(guestSubmitButton)
                .build();
        guestGridPane.setMaxSize(400, Region.USE_PREF_SIZE);
        guestGridPane.setPadding(new Insets(40));
        guestGridPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), null)));
        guestGridPane.setBorder(new Border(new BorderStroke(Color.gray(0.8), BorderStrokeStyle.SOLID, new CornerRadii(20), BorderStroke.THIN)));
        guestGridPane.setEffect(new DropShadow(10, Color.gray(0.8)));
        guestGridPane.getStyleClass().add("login"); // in order to have same style as login (especially font size)
        container.setCenter(guestGridPane);
        validationSupport.addRequiredInput(firstNameTextField, I18n.i18nTextProperty(CrmI18nKeys.FirstName));
        validationSupport.addRequiredInput(lastNameTextField, I18n.i18nTextProperty(CrmI18nKeys.LastName));
        validationSupport.addEmailValidation(emailTextField, emailTextField, I18n.i18nTextProperty(CrmI18nKeys.FirstName));
    }

    public void addTopNode(Node topNode) {
        guestTopVBox.getChildren().add(topNode);
    }

    public void setOnSubmit(EventHandler<ActionEvent> submitHandler) {
        guestSubmitButton.setOnAction(event -> {
            if (validationSupport.isValid()) {
                submitHandler.handle(event);
            } else {
                Animations.shake(container);
            }
        });
    }

    public void onShowing() {
        guestSubmitButton.setDefaultButton(true);
        //SceneUtil.autoFocusIfEnabled(firstNameTextField);
        UiScheduler.scheduleDelay(500, () -> SceneUtil.autoFocusIfEnabled(firstNameTextField));
    }

    public void onHiding() {
        guestSubmitButton.setDefaultButton(false);
    }

    public void turnOnButtonWaitMode() {
        StepSlide.turnOnButtonWaitMode(guestSubmitButton);
    }

    public void turnOffButtonWaitMode() {
        StepSlide.turnOffButtonWaitMode(guestSubmitButton, ModalityI18nKeys.Submit);
    }

    public Node getContainer() {
        return container;
    }

    public String getFirstName() {
        return firstNameTextField.getText();
    }

    public String getLastName() {
        return lastNameTextField.getText();
    }

    public String getEmail() {
        return emailTextField.getText();
    }
}
