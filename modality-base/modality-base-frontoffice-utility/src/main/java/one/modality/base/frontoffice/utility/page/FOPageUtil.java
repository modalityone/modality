package one.modality.base.frontoffice.utility.page;

import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/**
 * @author Bruno Salmon
 */
public final class FOPageUtil {

    public static final double MAX_PAGE_WIDTH = 1440 - 2 * 160;
    public static final double LEFT_RIGHT_PAGE_INSETS = 20;
    public static final double TOP_PAGE_INSETS = 88;
    public static final double BOTTOM_PAGE_INSETS = 160;

    public static <R extends Region> R restrictToMaxPageWidth(R region) {
        region.setMaxWidth(MAX_PAGE_WIDTH);
        return region;
    }

    public static <R extends Region> R restrictToMaxPageWidthAndApplyPageLeftRightPadding(R region) {
        Insets padding = region.getPadding();
        region.setPadding(createLeftRightPageInsets(padding.getTop(), padding.getBottom()));
        return restrictToMaxPageWidth(region);
    }

    public static <R extends Region> R restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(R region) {
        region.setPadding(createLeftRightPageInsets(TOP_PAGE_INSETS, BOTTOM_PAGE_INSETS));
        return restrictToMaxPageWidth(region);
    }

    public static <R extends Region> R restrictToMaxPageWidthAndApplyPageTopBottomPadding(R region) {
        region.setPadding(createTopBottomPageInsets());
        return restrictToMaxPageWidth(region);
    }

    public static Insets createLeftRightPageInsets(double top, double bottom) {
        return new Insets(top, LEFT_RIGHT_PAGE_INSETS, bottom, LEFT_RIGHT_PAGE_INSETS);
    }

    public static Insets createTopBottomPageInsets() {
        return new Insets(TOP_PAGE_INSETS, 0, BOTTOM_PAGE_INSETS, 0);
    }

    public static <R extends Region> R applyTopBottomPagePadding(R region) {
        region.setPadding(createTopBottomPageInsets());
        return region;
    }

    /*
    Old code that I keep to remember the videos player management (not yet managed in new code)
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
*/
}
