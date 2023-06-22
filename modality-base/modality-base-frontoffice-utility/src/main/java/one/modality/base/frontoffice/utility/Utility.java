package one.modality.base.frontoffice.utility;

import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class Utility {
    public static <T extends Labeled> T bindI18N(T node, String key) {
        return I18nControls.bindI18nProperties(node, key);
    }

    public static Node createSVGIcon(String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);

        return icon;
    }

    public static Node createSpace(int height) {
        VBox b = new VBox();
        b.setMinHeight(height);
        return b;
    }

    public static Node createCheckBox(BooleanProperty property, boolean isRadio, boolean isReverse, String label, boolean isDisabled) {
        Rectangle b = new Rectangle();
        b.setWidth(20);
        b.setHeight(20);
        b.setArcHeight(4);
        b.setArcWidth(4);
        b.setFill((isReverse != property.get()) ? Color.web(Style.MAIN_BLUE) : Color.WHITE);
        b.setStroke(Color.BLACK);
        property.addListener((observableValue, aBoolean, t1) -> {
            b.setFill((isReverse != property.get()) ? Color.web(Style.MAIN_BLUE) : Color.WHITE);
        });

        if (!isDisabled) b.setOnMouseClicked(e -> property.set(isRadio ? !isReverse : !property.get()));

        return createHList(5,0, b, new Label(label));
    }

    public static Node createSplitRow(Node node1, Node node2, int ratio, int padding) {
        GridPane container = new GridPane();

        container.add(node1, 0, 0);
        container.add(node2, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();

        col1.setPercentWidth(ratio);
        col2.setPercentWidth(100 - ratio);

        container.getColumnConstraints().addAll(
                col1, col2
        );

        container.setHgap(padding);

        return container;
    }

    public static Node createHList(int space, int padding, Node... nodes) {
        HBox container = new HBox();

        container.getChildren().addAll(nodes);

        container.setPadding(new Insets(padding));
        container.setSpacing(space);

        return container;
    }

    public static Node createVList(int space, int padding, Node... nodes) {
        VBox container = new VBox();

        container.getChildren().addAll(nodes);

        container.setPadding(new Insets(padding));
        container.setSpacing(space);

        return container;
    }

    public static Node createField(String labelKey, Node node) {
        VBox field = new VBox();
        Label label = bindI18N(new Label(), labelKey);

        field.getChildren().addAll(
                label, node
        );

        return field;
    }

    public static Node bindButtonWithPopup(Button button, Node container, Node content, int height) {
        StackPane layers = new StackPane();
        AnchorPane overlay = new AnchorPane();
        Button mask = new Button("Exit");

        overlay.setVisible(false);

        AnchorPane.setBottomAnchor(content, -100.0 - height);
        AnchorPane.setLeftAnchor(content,0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(mask, 0.0);
        AnchorPane.setTopAnchor(mask, 0.0);
        AnchorPane.setRightAnchor(mask, 0.0);
        mask.setMaxWidth(1000);
        mask.setMinHeight(1000);
        mask.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));

        overlay.getChildren().addAll(mask, content);

        layers.getChildren().addAll(
                container,
                overlay
        );

        button.setOnAction(e -> {
            overlay.setVisible(true);

            TranslateTransition open = new TranslateTransition(Duration.seconds(2), content);
            open.setByY(-height);
            open.play();

            open.setOnFinished(a -> {
                mask.setOnMouseClicked(o -> {
                    mask.setOnMouseClicked(null);
                    TranslateTransition close = new TranslateTransition(Duration.seconds(2), content);
                    close.setByY(height);
                    close.play();
                    close.setOnFinished(b -> overlay.setVisible(false));
                });
            });
        });

        return layers;
    }
}
