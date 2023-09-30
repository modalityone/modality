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
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
public class Tile extends MonoPane {
    private final Action action;
    private final boolean useHtml;
    private final boolean allowsGraphic;
    private final HtmlText htmlText = new HtmlText();
    private final Text text = new Text();
    private double fontSize;
    private Node graphic;
    private ScalePane scaledGraphic;
    protected final Facet luminanceFacet, textFacet;
    private boolean adaptativeFontSize;

    private final Pane clippedTextGraphicPane = new ClipPane() {

        {
            setContentValignment(VPos.BOTTOM); // for a possible folding animation from bottom to top
        }

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
        this(action, true, true);
    }

    public Tile(Action action, boolean useHtml, boolean allowsGraphic) {
        this.action = action;
        this.useHtml = useHtml;
        this.allowsGraphic = allowsGraphic;
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

    protected Property<Paint> getTextFillProperty() {
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

    protected void onHover(boolean hover) {
        textFacet.setInverted(hover);
        luminanceFacet.setInverted(hover);
    }

    private void onActionPropertiesChanged() {
        if (useHtml)
            htmlText.setText("<center style='line-height: 1em'>" + action.getText() + "</center>");
        else
            text.setText(action.getText());
        Node newGraphic = action.getGraphic();
        if (graphic != newGraphic && allowsGraphic) {
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
