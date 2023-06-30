package one.modality.base.frontoffice.utility;

import com.robrua.nlp.bert.Bert;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.nio.file.Paths;

public class GeneralUtility {

    static {
        try(Bert bert = Bert.load(Paths.get("/Users/tylertuan/Documents/modality/modality-base/modality-base-frontoffice-utility/src/main/resources/bert/bert_cased"))) {
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("Kadampa Meditation Center San Francisco"),
                            bert.embedSequence("Kadampa Meditation Center San Francisco")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("KMC San Francisco"),
                            bert.embedSequence("KMC San Francisco")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("Kadampa Meditation Center San Francisco"),
                            bert.embedSequence("KMC San Francisco")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("Kadampa Meditation Center San Francisco"),
                            bert.embedSequence("KMC France")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("Kadampa Meditation Center San Francisco"),
                            bert.embedSequence("France")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("Kadampa Meditation Center San Francisco"),
                            bert.embedSequence("I want to eat ice cream")
                    )
            );
            System.out.println(
                    cosineSimilarity(
                            bert.embedSequence("disco"),
                            bert.embedSequence("ice cream")
                    )
            );
        }
    }

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static <T extends Labeled> T bindI18N(T node, String key) {
        return I18nControls.bindI18nProperties(node, key);
    }

    public static <T extends Text> T bindI18N(T text, String key) {
        return I18n.bindI18nProperties(text, key);
    }

    public static Node createSVGIcon(String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);

        return icon;
    }

    public static Double distance(Double lat1, Double lon1, Double lat2, Double lon2, char unit) {
        if (lat1 == null || lat2 == null || lon1 == null || lon2 == null)
            return null;

        Double theta = lon1 - lon2;
        Double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static Node createSpace(int height) {
        VBox b = new VBox();
        b.setMinHeight(height);
        return b;
    }

    public static void styleSelectButton(EntityButtonSelector buttonSelector) {
        Button b = buttonSelector.getButton();
        b.setBorder(new Border(new BorderStroke(Color.web(StyleUtility.ELEMENT_GRAY), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(0.3))));
        b.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        b.setPadding(new Insets(5, 15, 5, 15));
    }

    public static Rectangle createCheckBoxRaw() {
        Rectangle b = new Rectangle();
        b.setWidth(20);
        b.setHeight(20);
        b.setArcHeight(4);
        b.setArcWidth(4);
        b.setStroke(Color.BLACK);
        b.setStrokeWidth(0.5);

        return b;
    }


    public static Node createCheckBoxDirect(BooleanProperty property, boolean isRadio, boolean isReverse, String label, boolean isDisabled) {
        Rectangle b = createCheckBoxRaw();

        b.setFill((isReverse != property.get()) ? Color.web(StyleUtility.MAIN_BLUE) : Color.WHITE);

        property.addListener((observableValue, aBoolean, t1) -> {
            b.setFill((isReverse != property.get()) ? Color.web(StyleUtility.MAIN_BLUE) : Color.WHITE);
        });

        if (!isDisabled) b.setOnMouseClicked(e -> property.set(isRadio ? !isReverse : !property.get()));

        return createHList(5,0, b, TextUtility.getMainText(label, StyleUtility.VICTOR_BATTLE_BLACK));
    }

    public static Node createRadioCheckBoxBySelection(StringProperty selectedProperty, String label) {
        Rectangle b = createCheckBoxRaw();

        b.setFill(selectedProperty.get().equals(label) ? Color.web(StyleUtility.MAIN_BLUE) : Color.WHITE);

        selectedProperty.addListener((observableValue, aBoolean, t1) -> {
            b.setFill(selectedProperty.get().equals(label) ? Color.web(StyleUtility.MAIN_BLUE) : Color.WHITE);
        });

        b.setOnMouseClicked(e -> { selectedProperty.set(selectedProperty.get().equals(label) ? "" : label); });

        return b;
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

        field.setSpacing(5);
        field.getChildren().addAll(
                TextUtility.getSubText(labelKey), node
        );

        return field;
    }

    public static Button createButton(Color color, int radius, String label) {
        Button b = new Button(label);
        b.setTextFill(Color.WHITE);
        b.setBackground(new Background(new BackgroundFill(color, new CornerRadii(radius), null)));

        return b;
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
