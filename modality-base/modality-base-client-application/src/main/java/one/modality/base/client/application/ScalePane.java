package one.modality.base.client.application;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * @author Bruno Salmon
 */
final class ScalePane extends Pane {

    public enum ScaleMode { WIDTH, HEIGHT, MIN_WIDTH_HEIGHT, MAX_WIDTH_HEIGHT}

    private ScaleMode scaleMode;
    private Node node;
    private boolean canGrow = true, canShrink = true;
    private boolean canScaleX = true, canScaleY = true;
    private boolean alwaysTry = false;

    public ScalePane() {
        this((Node) null);
    }

    public ScalePane(ScaleMode scaleMode) {
        this(scaleMode, null);
    }

    public ScalePane(Node node) {
        this(ScaleMode.MIN_WIDTH_HEIGHT, node);
    }

    public ScalePane(ScaleMode scaleMode, Node node) {
        this.scaleMode = scaleMode;
        setNode(node);
    }

    public void setNode(Node node) {
        this.node = node;
        if (node != null)
            getChildren().setAll(node);
    }

    public Node getNode() {
        return node;
    }

    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
    }

    public void setCanGrow(boolean canGrow) {
        this.canGrow = canGrow;
    }

    public void setCanShrink(boolean canShrink) {
        this.canShrink = canShrink;
    }

    public void setCanScaleX(boolean canScaleX) {
        this.canScaleX = canScaleX;
    }

    public void setCanScaleY(boolean canScaleY) {
        this.canScaleY = canScaleY;
    }

    public void setAlwaysTry(boolean alwaysTry) {
        this.alwaysTry = alwaysTry;
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        boolean tryRescale = !node.isResizable() || alwaysTry;
        double scale = 1;
        if (!tryRescale && node instanceof Region) {
            Region region = (Region) this.node;
            tryRescale = region.minWidth(height) > width || region.maxWidth(height) < width || region.minHeight(width) > height || region.maxHeight(width) < height;
        }
        if (tryRescale) {
            double w = node.prefWidth(height), h = node.prefHeight(width);
            scale = scaleMode == ScaleMode.HEIGHT ? height / h : scaleMode == ScaleMode.WIDTH ? width / w : scaleMode == ScaleMode.MIN_WIDTH_HEIGHT ? Math.min(height / h, width / w) : Math.max(height / h, width / w);
            if (!canShrink && scale < 1 || !canGrow && scale > 1)
                scale = 1;
        }
        node.setScaleX(canScaleX ? scale : 1);
        node.setScaleY(canScaleY ? scale : 1);
        layoutInArea(node, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
}
