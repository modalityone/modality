package one.modality.base.backoffice.tile;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.extras.theme.Facet;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.pane.ClipPane;
import dev.webfx.extras.util.pane.MonoPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.ui.action.Action;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
public final class Tile extends MonoPane {
    private final Action action;
    private final boolean useHtml;
    private final HtmlText htmlText = new HtmlText();
    private final Text text = new Text();
    private double fontSize;
    private Node graphic;
    private ScalePane scaledGraphic;
    private final Facet luminanceFacet, textFacet;
    private boolean adaptativeFontSize;
    private boolean transparentBackground;

    private final Pane clippedTextGraphicPane = new ClipPane() {

        @Override
        protected void layoutChildren() {
            double width = getWidth(), height = getHeight(), h2 = height / 2, h4 = h2 / 2, h8 = h4 / 2;
            if (adaptativeFontSize)
                setFontSize(Math.min(width * 0.125, h4 * 0.55));
            if (scaledGraphic == null) {
                layoutInArea(getTextNode(), 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
            } else {
                layoutInArea(scaledGraphic, 0, h4, width, h4, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(getTextNode(), 0, h2 + h8, width, h4, 0, HPos.CENTER, VPos.CENTER);
            }
            resizeClip();
        }
        @Override
        protected double computePrefWidth(double height) {
            double prefWidth = getTextNode().prefWidth(height);
            if (scaledGraphic != null)
                prefWidth = Math.max(prefWidth, scaledGraphic.prefWidth(height));
            return prefWidth;
        }
    };

    @Override
    public Orientation getContentBias() {
        return getTextNode().getContentBias();
    }

    public Tile(Action action) {
        this(action, false);
    }

    public Tile(Action action, boolean useHtml) {
        this.action = action;
        this.useHtml = useHtml;
        luminanceFacet = LuminanceTheme.createSecondaryPanelFacet(this)
                .style();
        textFacet = TextTheme.createPrimaryTextFacet(getTextNode())
                .setDisabledProperty(action.disabledProperty())
                .setFillProperty(getTextFillProperty())
                .setFontProperty(useHtml ? htmlText.fontProperty() : text.fontProperty())
                .style();
        setMinHeight(0);
        LayoutUtil.setMaxWidthToInfinite(this);
        LayoutUtil.setMaxHeightToInfinite(this);
        setOnMouseClicked(e -> fireAction());
        setOnMouseEntered(e -> onHover(true));
        setOnMouseExited(e -> onHover(false));
        setCursor(Cursor.HAND);
        htmlText.setMouseTransparent(true);
        FXProperties.runNowAndOnPropertiesChange(this::onActionPropertiesChanged, action.textProperty(), action.graphicProperty());
    }

    private Node getTextNode() {
        return useHtml ? htmlText : text;
    }

    private Property<Paint> getTextFillProperty() {
        return useHtml ? htmlText.fillProperty() : text.fillProperty();
    }

    public Tile setAdaptativeFontSize(boolean adaptativeFontSize) {
        this.adaptativeFontSize = adaptativeFontSize;
        return this;
    }

    public Tile setShadowed(boolean shadowed) {
        luminanceFacet.setShadowed(shadowed);
        return this;
    }

    // Temporary hack flag used for the main frame header tabs to override some settings set by the current theme, i.e.
    // forcing a transparent background when not hovered, and a black color for the text when not selected.
    // TODO: make the Theme configurable to allow this kind of customisation
    private boolean headerTabsThemeHack;

    public Tile setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
        // setTransparentBackground() is actually called only for header tabs, so we activate the hack flag here
        headerTabsThemeHack = true;
        applyTextFillHack();
        applyTransparentBackgroundHack();
        return this;
    }

    private void applyTransparentBackgroundHack() {
        if (transparentBackground && !luminanceFacet.isInverted())
            setBackground(Background.EMPTY);
    }

    private void applyTextFillHack() {
        if (!textFacet.isInverted() && !textFacet.isSelected())
            // Forcing the black value through binding (this will prevent the Theme to change that value)
            getTextFillProperty().bind(new SimpleObjectProperty<>(Color.BLACK));
    }

    private void removeTextFillHack() {
        // Authorizing the Theme again to change that value
        getTextFillProperty().unbind();
    }

    public Tile setSelected(boolean selected) {
        removeTextFillHack();
        textFacet.setSelected(selected);
        if (headerTabsThemeHack) {
            applyTextFillHack();
            applyTransparentBackgroundHack();
        }
        return this;
    }

    public Tile setFontSize(double fontSize) {
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            textFacet.requestedFont(FontDef.font(fontSize));
        }
        return this;
    }

    public void fireAction() {
        action.handle(new ActionEvent());
    }

    private void onHover(boolean hover) {
        removeTextFillHack();
        textFacet.setInverted(hover);
        luminanceFacet.setInverted(hover);
        if (headerTabsThemeHack) {
            applyTextFillHack();
            applyTransparentBackgroundHack();
        }
    }

    private void onActionPropertiesChanged() {
        if (useHtml)
            htmlText.setText("<center style='line-height: 1em'>" + action.getText() + "</center>");
        else
            text.setText(action.getText());
        Node newGraphic = action.getGraphic();
        if (graphic != newGraphic) {
            graphic = action.getGraphic();
            scaledGraphic = graphic == null ? null : new ScalePane(graphic);
        }
        if (scaledGraphic == null)
            clippedTextGraphicPane.getChildren().setAll(getTextNode());
        else
            clippedTextGraphicPane.getChildren().setAll(getTextNode(), scaledGraphic);
        setContent(clippedTextGraphicPane);
        textFacet.setGraphicNode(graphic);
    }

}
