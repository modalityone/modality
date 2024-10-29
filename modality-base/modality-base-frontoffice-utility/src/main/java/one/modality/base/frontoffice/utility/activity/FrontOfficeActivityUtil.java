package one.modality.base.frontoffice.utility.activity;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.control.ControlUtil;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class FrontOfficeActivityUtil {

    public static final double MAX_PAGE_WIDTH = 1200; // Similar value to website
    public static final double LEFT_RIGHT_PAGE_INSETS = 20;

    public static ScrollPane createActivityPageScrollPane(Region pageContainer, boolean hasVideoPlayers) {
        return createActivityPageScrollPane(pageContainer, hasVideoPlayers, true);
    }

    public static ScrollPane createActivityPageScrollPane(Region pageContainer, boolean hasVideoPlayers, boolean applyLeftRightPageMargins) {
        // Setting a max width for big desktop screens
        pageContainer.setMaxWidth(MAX_PAGE_WIDTH);

        // Embedding the page in a ScrollPane. The page itself is embedded in a BorderPane in order to keep the page
        // centered when it reaches its max width (without the BorderPane, the ScrollPane would position it on left).
        BorderPane borderPane = new BorderPane(pageContainer);
        if (applyLeftRightPageMargins)
            BorderPane.setMargin(pageContainer, FrontOfficeActivityUtil.createLeftRightPageInsets());
        // Also a background is necessary for devices not supporting inverse clipping used in circle animation
        borderPane.setBackground(Background.fill(Color.WHITE));
        ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(borderPane);

        if (hasVideoPlayers) {
            // Ensuring to not keep this activity in the scene graph after transition in order to stop the video players
            // in the browser (in case TransitionPane keepsLeavingNode is enabled)
            TransitionPane.setKeepsLeavingNode(scrollPane, false);
        }

        pageContainer.minHeightProperty().bind(scrollPane.heightProperty());

        return scrollPane;
    }

    public static Insets createLeftRightPageInsets() {
        return createLeftRightPageInsets(0);
    }

    public static Insets createLeftRightPageInsets(double topBottom) {
        return createLeftRightPageInsets(topBottom, topBottom);
    }

    public static Insets createLeftRightPageInsets(double top, double bottom) {
        return new Insets(top, LEFT_RIGHT_PAGE_INSETS, bottom, LEFT_RIGHT_PAGE_INSETS);
    }
}
