package one.modality.base.frontoffice.utility;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.async.Handler;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXApp;

import java.util.List;
import java.util.stream.Collectors;

public class GeneralUtility {

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

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
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

    public static TextField createBindedTextField(StringProperty stringProperty, double limitedWidth) {
        TextField tf = new TextField();

        tf.setBorder(new Border(new BorderStroke(Color.web(StyleUtility.ELEMENT_GRAY), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(0.3))));
        tf.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        tf.setPadding(new Insets(5, 15, 5, 15));
        tf.setFont(Font.font(StyleUtility.MAIN_TEXT_SIZE));

        tf.textProperty().bindBidirectional(stringProperty);
        if (limitedWidth > 0) {
            tf.setMaxWidth(limitedWidth);
        }
        return tf;
    }

    public static String generateStaticMapLinks(Double lat, Double lng, List<Center> centers) {
        return   "https://maps.googleapis.com/maps/api/staticmap?center=" + lat + "," + lng +
                "&markers=color:blue%7Clabel:S%7C" + centers.stream().map(c -> {
                    return c.lat + "," + c.lng;
                }).collect(Collectors.joining("|"))
                + "&zoom=9&size=400x400&key=AIzaSyAihoCYFho8rqJwnBjxzBlk56SR0uL7_Ks";
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

    public static boolean screenChangeListened(double w) {
        double xRatio = FXApp.fontRatio.get();

        if (w < 300) { FXApp.fontRatio.set(1.0); FXApp.widthStage.set(300); }
        else if (w > 2000) { FXApp.fontRatio.set(2.0); FXApp.widthStage.set(2000); }
        else if (300 <= w && w < 600) { FXApp.fontRatio.set(1.2); FXApp.widthStage.set(300); }
        else if (600 <= w && w < 900) { FXApp.fontRatio.set(1.4); FXApp.widthStage.set(600); }
        else if (900 <= w && w < 1200) { FXApp.fontRatio.set(1.6); FXApp.widthStage.set(900); }
        else if (1200 <= w && w < 1500) { FXApp.fontRatio.set(1.8); FXApp.widthStage.set(1200); }
        else if (1500 <= w && w < 1800) { FXApp.fontRatio.set(1.9); FXApp.widthStage.set(1500); }
        else if (1800 <= w && w < 2000) { FXApp.fontRatio.set(2.0); FXApp.widthStage.set(2000); }

        return xRatio != FXApp.fontRatio.get();
    }

    public static Node createCheckBoxDirect(BooleanProperty property, boolean isRadio, boolean isReverse, String label, boolean isDisabled) {
        Rectangle b = createCheckBoxRaw();

        b.setFill((isReverse != property.get()) ? Color.web(StyleUtility.MAIN_OLD_BLUE_NOW_ORANGE) : Color.WHITE);

        property.addListener((observableValue, aBoolean, t1) -> {
            b.setFill((isReverse != property.get()) ? Color.web(StyleUtility.MAIN_OLD_BLUE_NOW_ORANGE) : Color.WHITE);
        });

        if (!isDisabled) b.setOnMouseClicked(e -> property.set(isRadio ? !isReverse : !property.get()));

        return createHList(5,0, b, TextUtility.getMainText(label, StyleUtility.VICTOR_BATTLE_BLACK));
    }

    public static Node createRadioCheckBoxBySelection(StringProperty selectedProperty, String label) {
        Rectangle b = createCheckBoxRaw();

        b.setFill(selectedProperty.get().equals(label) ? Color.web(StyleUtility.MAIN_OLD_BLUE_NOW_ORANGE) : Color.WHITE);

        selectedProperty.addListener((observableValue, aBoolean, t1) -> {
            b.setFill(selectedProperty.get().equals(label) ? Color.web(StyleUtility.MAIN_OLD_BLUE_NOW_ORANGE) : Color.WHITE);
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
        col2.setHalignment(HPos.RIGHT);

        container.getColumnConstraints().addAll(
                col1, col2
        );

        container.setHgap(padding);
        container.setMinWidth(0);

        return container;
    }

    public static HBox createHList(int space, int padding, Node... nodes) {
        HBox container = new HBox();

        container.getChildren().addAll(nodes);

        container.setPadding(new Insets(padding));
        container.setSpacing(space);

        return container;
    }

    public static VBox createVList(int space, int padding, Node... nodes) {
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

    public static Button createButton(Color color, int radius, String label, double fontSize) {
        Button b = new Button(label);
        b.setTextFill(Color.WHITE);
        b.setBackground(new Background(new BackgroundFill(color, new CornerRadii(radius*FXApp.fontRatio.get()), null)));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            double size = fontSize * FXApp.fontRatio.get();
            b.setFont(Font.font(StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(500), size));
            b.setStyle("-fx-font-family: " + StyleUtility.TEXT_FAMILY + "; -fx-font-size: " + size);
        }, FXApp.fontRatio);

        return b;
    }

    public static Label getMainLabel(String content, String color) {
        return createLabel(content, Color.web(color), FontWeight.SEMI_BOLD, StyleUtility.MAIN_TEXT_SIZE);
    }

    public static Label getMainHeaderLabel(String content) {
        return createLabel(content, Color.web(StyleUtility.MAIN_ORANGE), true, 21);
    }

    public static Label getMediumLabel(String content, String color) {
        return createLabel(content, Color.web(color), false, StyleUtility.MEDIUM_TEXT_SIZE);
    }

    public static Label createLabel(String text, Color color, boolean bold, double fontSize) {
        return setupLabeled(new Label(), text, color, bold, fontSize);
    }

    public static Label createLabel(String text, Color color, FontWeight fontWeight, double fontSize) {
        return setupLabeled(new Label(), text, color, fontWeight, fontSize);
    }

    public static Hyperlink createHyperlink(String text, Color color, double fontSize) {
        return setupLabeled(new Hyperlink(), text, color, false, fontSize);
    }

    public static <T extends Labeled> T setupLabeled(T labeled, String text, Color color, boolean bold, double fontSize) {
        return setupLabeled(labeled, text, color, bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize);
    }

    public static <T extends Labeled> T setupLabeled(T labeled, String text, Color color, FontWeight fontWeight, double fontSize) {
        if (text != null)
            I18nControls.bindI18nProperties(labeled, text);
        labeled.setTextFill(color);
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double size = fontSize * FXApp.fontRatio.get();
            labeled.setFont(Font.font(StyleUtility.TEXT_FAMILY, fontWeight, size));
            labeled.setStyle("-fx-font-family: " + StyleUtility.TEXT_FAMILY + "; -fx-font-weight: " + fontWeight.getWeight() + "; -fx-font-size: " + size);
        }, FXApp.fontRatio);

        labeled.setWrapText(true);
        labeled.setLineSpacing(6);
        return labeled;
    }

    public static SVGPath createSvgPath(String content, String color) {
        SVGPath p = new SVGPath();
        p.setContent(content);
        p.setFill(Color.web(color));

        return p;
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

    public static void roundClipImageView(ImageView imageView) {
        // We will apply a round clip to the imageView
        Rectangle roundClip = new Rectangle();
        imageView.setClip(roundClip);
        // We now need to set the properties of that round clip to always match the width & height of the image view
        // (and set the rectangle arc for the round effect). It's not an obvious task because it's not easy to track
        // the width and height of an ImageView (there is no observable width/height properties). Also (but minor issue)
        // the image may not be set at this stage, or may change later. So first thing is to listen to the image property.
        Unregisterable[] imageUnregisterable = { null }; // Will hold the second listener for a possible un-registration on image change
        FXProperties.runNowAndOnPropertiesChange(() -> {
            // Now we can get the Image value
            Image image = imageView.getImage();
            // We un-register the previous listener if there was one, before creating a new one
            if (imageUnregisterable[0] != null)
                imageUnregisterable[0].unregister();
            boolean[] postponedInAnimationFrame = { false }; // See explanation in code below
            // If an image is set, we listen to the different properties that impact the ImageView size: Image width &
            // height (which may change if the image has not yet finished loading), and ImageView fitWidth & fitHeight
            imageUnregisterable[0] = image == null ? null : FXProperties.runNowAndOnPropertiesChange(new Runnable() {
                @Override
                public void run() {
                    // If a fitWidth or fitHeight has been set, then they are the ImageView width & height
                    double width = imageView.getFitWidth();
                    double height = imageView.getFitHeight();
                    // But if at least one is not set, we will need to rely on the layoutBounds instead. However, the
                    // layoutBounds is not set at this stage in the web version. For this reason, we need to postpone
                    // this runnable in the animation frame before accessing the layoutBounds. This is what we do now:
                    if ((width == 0 || height == 0) && !postponedInAnimationFrame[0]) {
                        UiScheduler.scheduleInAnimationFrame(this);
                        postponedInAnimationFrame[0] = true;
                        return;
                    }
                    // Now we can get the width & height from the layoutBounds if it was not set by fitWidth & fitHeight
                    if (width == 0)
                        width = imageView.getLayoutBounds().getWidth();
                    if (height == 0)
                        height = imageView.getLayoutBounds().getHeight();
                    // Because we now have the width & height of the ImageView, we can finally set the round clip properties
                    roundClip.setWidth(width);
                    roundClip.setHeight(height);
                    double arcWidthHeight = width / 4;
                    roundClip.setArcWidth(arcWidthHeight);
                    roundClip.setArcHeight(arcWidthHeight);
                    // Resetting the postponedInAnimationFrame flag for the next call
                    postponedInAnimationFrame[0] = false;
                }
            }, image.widthProperty(), image.heightProperty(), imageView.fitWidthProperty(), imageView.fitHeightProperty());
        }, imageView.imageProperty());
    }

    public static void onNodeClickedWithoutScroll(Handler<MouseEvent> clickHandler, Node... nodes) {
        double[] screenPressedY = {0};
        Arrays.forEach(nodes, node -> {
            node.setCursor(Cursor.HAND);
            node.setOnMousePressed(e -> screenPressedY[0] = e.getScreenY());
            node.setOnMouseReleased(e -> {
                if (Math.abs(e.getScreenY() - screenPressedY[0]) < 10) // This is to skip the click when the user is actually scrolling on a touch screen such as mobiles
                    clickHandler.handle(e);
            });
        });
    }
}
