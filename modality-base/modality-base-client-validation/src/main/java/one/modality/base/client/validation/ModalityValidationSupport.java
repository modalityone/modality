package one.modality.base.client.validation;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.ui.validation.controlsfx.control.decoration.Decoration;
import dev.webfx.stack.ui.validation.controlsfx.control.decoration.GraphicDecoration;
import dev.webfx.stack.ui.validation.controlsfx.validation.decoration.GraphicValidationDecoration;
import dev.webfx.stack.ui.validation.mvvmfx.ObservableRuleBasedValidator;
import dev.webfx.stack.ui.validation.mvvmfx.ValidationMessage;
import dev.webfx.stack.ui.validation.mvvmfx.Validator;
import dev.webfx.stack.ui.validation.mvvmfx.visualization.ControlsFxVisualizer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Bruno Salmon
 */
public final class ModalityValidationSupport {

    private static final String DEFAULT_REQUIRED_MESSAGE = "This field is required";

    private final List<Validator> validators = new ArrayList<>();
    private final List<Node> validatorErrorDecorationNodes = new ArrayList<>();
    private final BooleanProperty validatingProperty = new SimpleBooleanProperty();
    private Node popOverContentNode;
    private Node popOverOwnerNode;

    public boolean isValid() {
        validatingProperty.setValue(false);
        validatingProperty.setValue(true);
        Validator firstInvalidValidator = firstInvalidValidator();
        if (firstInvalidValidator != null)
            Platform.runLater(() -> {
                popUpOverAutoScroll = true;
                showValidatorErrorPopOver(firstInvalidValidator);
            });
        return firstInvalidValidator == null;
    }

    public void reset() {
        // This will hide the possible validation error popup and other warning icons
        validatingProperty.setValue(false);
    }

    private Validator firstInvalidValidator() {
        return Collections.findFirst(validators, validator -> !validator.getValidationStatus().isValid());
    }

    public void addRequiredInputs(TextInputControl... textInputControls) {
        for (TextInputControl textInputControl : textInputControls)
            addRequiredInput(textInputControl);
    }

    public void addRequiredInput(TextInputControl textInputControl) {
        addRequiredInput(textInputControl, DEFAULT_REQUIRED_MESSAGE);
    }

    public void addRequiredInput(TextInputControl textInputControl, String errorMessage) {
        addRequiredInput(textInputControl.textProperty(), textInputControl, errorMessage);
    }

    public void addRequiredInput(ObservableValue valueProperty, Node inputNode) {
        addRequiredInput(valueProperty, inputNode, DEFAULT_REQUIRED_MESSAGE);
    }

    public void addRequiredInput(ObservableValue valueProperty, Node inputNode, String errorMessage) {
        addValidationRule(Bindings.createBooleanBinding(() -> testNotEmpty(valueProperty.getValue()), valueProperty), inputNode, errorMessage, true);
    }

    private static boolean testNotEmpty(Object value) {
        return value != null && (!(value instanceof String) || !((String) value).trim().isEmpty());
    }

    public ObservableBooleanValue addValidationRule(ObservableValue<Boolean> validProperty, Node node, String errorMessage) {
        return addValidationRule(validProperty, node, errorMessage, false);
    }

    public ObservableBooleanValue addValidationRule(ObservableValue<Boolean> validProperty, Node node, String errorMessage, boolean required) {
        ObservableRuleBasedValidator validator = new ObservableRuleBasedValidator();
        ObservableBooleanValue rule =
                Bindings.createBooleanBinding(() ->
                    !validatingProperty.get() || validProperty.getValue() || !isShowing(node)
                , validProperty, validatingProperty);
        validator.addRule(rule, ValidationMessage.error(errorMessage));
        validators.add(validator);
        validatorErrorDecorationNodes.add(node);

        if (node instanceof Control) {
            Control control = (Control) node;
            ControlsFxVisualizer validationVisualizer = new ControlsFxVisualizer();
            validationVisualizer.setDecoration(new GraphicValidationDecoration() {
                @Override
                protected Node createErrorNode() {
                    return ImageStore.createImageView(ModalityValidationIcons.validationErrorIcon16Url);
                }

                @Override
                protected Collection<Decoration> createValidationDecorations(dev.webfx.stack.ui.validation.controlsfx.validation.ValidationMessage message) {
                    boolean isTextInput = node instanceof TextInputControl;
                    boolean isButton = node instanceof Button;
                    // isInside flag will determine if we position the decoration inside the node or not (ie outside)
                    boolean isInside;
                    if (isTextInput) // inside for text inputs
                        isInside = true;
                    else { // for others, will be generally outside unless it is stretched to full width by its container
                        Parent parent = node.getParent();
                        while (parent instanceof Pane && !(parent instanceof VBox) && !(parent instanceof HBox))
                            parent = parent.getParent();
                        isInside = parent instanceof VBox && ((VBox) parent).isFillWidth();
                    }
                    double xRelativeOffset = isInside ? -1 : 1; // positioning the decoration inside the control for button and text input
                    double xOffset = isInside && isButton ?  -20 : 0; // moving the decoration before the drop down arrow
                    return java.util.Collections.singletonList(
                            new GraphicDecoration(createDecorationNode(message),
                                    Pos.CENTER_RIGHT,
                                    xOffset,
                                    0,
                                    xRelativeOffset,
                                    0)
                    );
                }

                @Override
                protected Collection<Decoration> createRequiredDecorations(Control target) {
                    return java.util.Collections.singletonList(
                            new GraphicDecoration(ImageStore.createImageView(ModalityValidationIcons.validationRequiredIcon16Url),
                                    Pos.CENTER_LEFT,
                                    -10,
                                    0));
                }
            });
            validationVisualizer.initVisualization(validator.getValidationStatus(), control, required);
            node.getProperties().put("validationVisualizer", validationVisualizer);
        }
        return rule;
    }

    private void showValidatorErrorPopOver(Validator validator) {
        int index = validators.indexOf(validator);
        if (index >= 0) {
            Node decorationNode = validatorErrorDecorationNodes.get(index);
            if (decorationNode != null)
                showValidatorErrorPopOver(validator, decorationNode);
        }
    }

    private void showValidatorErrorPopOver(Validator validator, Node errorDecorationNode) {
        ValidationMessage errorMessage = Collections.first(validator.getValidationStatus().getErrorMessages());
        if (errorMessage != null) {
            Label label = new Label(errorMessage.getMessage());
            label.setPadding(new Insets(8));
            label.setFont(Font.font("Verdana", 11.5));
            label.setTextFill(Color.WHITE);
            label.setBackground(BackgroundFactory.newBackground(Color.RED, 5, 2));
            label.setBorder(BorderFactory.newBorder(Color.WHITE, 5, 2));
            Rectangle diamond = new Rectangle(10, 10, Color.RED);
            diamond.getTransforms().add(new Rotate(45, 5, 5));
            diamond.layoutYProperty().bind(FXProperties.compute(label.heightProperty(), n -> n.doubleValue() - 7));
            diamond.setLayoutX(20d);
            popOverContentNode = new Group(label, diamond);
            //popOverContentNode.setOpacity(0.75);
            //popOverContentNode.setEffect(new DropShadow());
            showPopOver(errorDecorationNode);
            // Removing the error pop over when the status is valid again
            validator.getValidationStatus().validProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean valid) {
                    if (valid) {
                        observable.removeListener(this);
                        popOverOwnerNode = null;
                        hidePopOver();
                    }
                }
            });
        }
    }

    private void showPopOver(Node node) {
        popOverOwnerNode = node;
        showPopOverNow();
        if (!node.getProperties().containsKey("popOverListen")) {
            node.getProperties().put("popOverListen", true);
            node.sceneProperty().addListener(observable -> {
                if (popOverOwnerNode == node) {
                    showPopOverNow();
                }
            });
            node.parentProperty().addListener(observable -> {
                if (popOverOwnerNode == node) {
                    showPopOverNow();
                }
            });
        }
    }

    public void addEmailValidation(TextField emailInput, Node where, String errorMessage) {
        // Define the email pattern
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailPattern);
        // Create the validation rule
        addValidationRule(
                Bindings.createBooleanBinding(
                        () -> pattern.matcher(emailInput.getText()).matches(),
                        emailInput.textProperty()
                ),
                where,
                errorMessage
        );
    }

    public void addNonEmptyValidation(TextField textField, Node where,String errorMessage) {
        // Create the validation rule
        addValidationRule(
                Bindings.createBooleanBinding(
                        () -> !textField.getText().trim().isEmpty(),
                        textField.textProperty()
                ),
                where,
                errorMessage);
    }

    public void addDateValidation(TextField textField,String dateFormat, Node where, String errorMessage) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        // Create the validation rule
        addValidationRule(
                Bindings.createBooleanBinding(() -> {
                    try {
                        dateFormatter.parse(textField.getText().trim());
                        return true;
                    } catch (DateTimeParseException e) {
                        return false;
                    }}, textField.textProperty()),
                where,
                errorMessage
        );
    }

    public void addLegalAgeValidation(TextField textField, String dateFormat, int legalAge, Node where, String errorMessage) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        // Create the validation rule
        addValidationRule(
                Bindings.createBooleanBinding(() -> {
                    try {
                        LocalDate birthDate = LocalDate.parse(textField.getText().trim(), dateFormatter);
                        LocalDate now = LocalDate.now();
                        return birthDate.plusYears(legalAge).isBefore(now) || birthDate.plusYears(legalAge).isEqual(now);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                }, textField.textProperty()),
                where,
                errorMessage
        );
    }


/*
    private PopOver popOver;
    private void showPopOverNow() {
        if (popOver != null && popOver.getOwnerNode() != popOverOwnerNode) {
            popOver.hide();
            popOver = null;
        }
        if (popOver == null && isShowing(popOverOwnerNode)) {
            popOver = new PopOver();
            popOver.setContentNode(popOverContentNode);
            popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
            //Platform.runLater(() -> {
                popOver.show(popOverOwnerNode, -(popOverOwnerNode instanceof ImageView ? ((ImageView) popOverOwnerNode).getImage().getHeight() : 0) + 4);
            //});
        }
    }
*/

    private GraphicDecoration popOverDecoration;
    private Node popOverDecorationTarget;
    private boolean popUpOverAutoScroll;

    private void showPopOverNow() {
        Platform.runLater(() -> {
            hidePopOver();
            if (isShowing(popOverOwnerNode)) {
                popOverDecorationTarget = popOverOwnerNode;
                popOverDecoration = new GraphicDecoration(popOverContentNode, 0, -1, 0, -1);
                popOverDecoration.applyDecoration(popOverDecorationTarget);
                if (popUpOverAutoScroll) {
                    SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(popOverDecorationTarget);
                    SceneUtil.autoFocusIfEnabled(popOverOwnerNode);
                    popUpOverAutoScroll = false;
                }
            }
        });
    }

    private void hidePopOver() {
        UiScheduler.runInUiThread(() -> {
            if (popOverDecoration != null) {
                popOverDecoration.removeDecoration(popOverDecorationTarget);
                popOverDecoration = null;
            }
        });
    }

    private static boolean isShowing(Node node) {
        if (!node.isVisible())
            return false;
        Parent parent = node.getParent();
        if (parent != null)
            return isShowing(parent);
        Scene scene = node.getScene();
        return scene != null && scene.getRoot() == node;
    }

    public void addPasswordValidation(TextField passwordInput, Label passwordLabel, String errorMessage) {
        addValidationRule(
                Bindings.createBooleanBinding(
                        () -> passwordInput.getText().length() >= 8,
                        passwordInput.textProperty()
                ),
                passwordLabel,
                errorMessage
        );
    }
}
