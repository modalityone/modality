package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.action.ActionBinder;
import dev.webfx.extras.aria.FXKeyboardNavigationDetected;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.LayoutPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.panes.transitions.Transition;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.base.client.mainframe.fx.FXMainFrameTransiting;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuBarFactory;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;
import one.modality.base.frontoffice.activities.mainframe.menus.desktop.DesktopMainMenuBar;
import one.modality.base.frontoffice.activities.mainframe.menus.desktop.DesktopUserMenuBar;
import one.modality.base.frontoffice.activities.mainframe.menus.mobile.BurgerMenu;
import one.modality.base.frontoffice.activities.mainframe.menus.mobile.MobileBottomMainMenuBar;
import one.modality.base.frontoffice.activities.mainframe.menus.mobile.UserMenu;
import one.modality.base.frontoffice.activities.mainframe.menus.shared.LanguageMenuBar;
import one.modality.base.frontoffice.mainframe.footernode.MainFrameFooterNodeProvider;
import one.modality.base.frontoffice.mainframe.fx.FXBackgroundNode;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.mainframe.fx.FXShowFooter;
import one.modality.base.frontoffice.utility.page.FOPageUtil;

import java.util.List;
import java.util.Objects;

import static one.modality.base.frontoffice.activities.mainframe.MainFrameCssSelectors.*;

/**
 * @author Bruno Salmon
 */
public final class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    private static final Transition INITIAL_TRANSITION_EFFECT = new CircleTransition();
    private static final Transition PAGE_TRANSITION_EFFECT = null; // maybe go back to new FadeTransition() after Spring Festival

    private final BooleanProperty mobileLayoutProperty =
        FXProperties.newBooleanProperty(UserAgent.isNative(), this::onMobileLayoutChange);

    private Pane mainFrameContainer;
    private final ConnectivityIndicator connectivityIndicator = new ConnectivityIndicator();
    private Node backgroundNode; // can be used to hold a WebView and prevent iFrame reload in the web version
    private final TransitionPane pageTransitionPane = new TransitionPane();
    private Pane dialogArea;
    private int firstOverlayChildIndex;

    @Override
    public Node buildUi() {
        // Starting with a circle transition animation for the first activity displayed
        pageTransitionPane.setAnimateFirstContent(true);
        pageTransitionPane.setTransition(INITIAL_TRANSITION_EFFECT);
        // And then a fade transition for later activities
        FXProperties.runOnPropertyChange(transiting -> {
            if (!transiting) { // Ending a transition
                pageTransitionPane.setTransition(PAGE_TRANSITION_EFFECT) ;
            }
        }, pageTransitionPane.transitingProperty());
        //mountTransitionPane.setKeepsLeavingNodes(true); // Note: activities with video players should call TransitionPane.setKeepsLeavingNode(node, false)
        FXMainFrameTransiting.transitingProperty().bind(pageTransitionPane.transitingProperty());

        MonoPane desktopMainMenuBar = DesktopMainMenuBar.createDesktopTopMainMenuBar(this);
        MonoPane desktopUserMenuBar = DesktopUserMenuBar.createDesktopTopUserMenuBar(this);
        MonoPane mobileBottomMainMenuBar = MobileBottomMainMenuBar.createMobileBottomMainMenuBar(this);

        mainFrameContainer = new LayoutPane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren(double width, double height) {
                layoutInArea(connectivityIndicator.getConnectivityBar(), 0, 0, width, 5);
                double headerHeight = 0, footerHeight = 0;
                boolean isMobileLayout = mobileLayoutProperty.get();
                if (isMobileLayout) {
                    footerHeight = mobileBottomMainMenuBar.prefHeight(width);
                    layoutInArea(mobileBottomMainMenuBar, 0, height - footerHeight, width, footerHeight);
                }
                double mountNodeY = headerHeight;
                double mountNodeHeight = height - headerHeight - footerHeight;
                pageTransitionPane.setMinHeight(mountNodeHeight);
                layoutInArea(pageTransitionPane, 0, mountNodeY, width, mountNodeHeight);
                if (backgroundNode != null) { // Same position and size as the mount node (if present)
                    layoutInArea(backgroundNode, 0, mountNodeY, width, mountNodeHeight);
                }
                if (dialogArea != null) { // Same position and size as the mount node (if present)
                    layoutInArea(dialogArea, 0, mountNodeY, width, mountNodeHeight);
                }
                for (Node overlayChild : FXMainFrameOverlayArea.getOverlayChildren()) {
                    if (overlayChild.isManaged())
                        layoutInArea(overlayChild, 0, mountNodeY, width, mountNodeHeight);
                }
            }
        };

        // To be aware: if backgroundNode is set to a WebView (which is actually its main purpose), then modifying the
        // mainFrame children again will cause the iFrame to reload in the web version, which is what we want to prevent
        // here: when the user is navigating back to the WebView, we want him to retrieve the WebView in the exact same
        // state as when he left it. So we try to not modify these children anymore once the backgroundNode is set.
        // That's why we encapsulated the mount node inside a container that won't change in that list.
        FXProperties.runNowAndOnPropertiesChange(() -> {
            backgroundNode = FXBackgroundNode.getBackgroundNode();
            boolean isMobileLayout = mobileLayoutProperty.get();
            // Here are the children we need to set for the main frame container
            List<Node> children = Collections.listOfRemoveNulls(
                backgroundNode,      // could be a WebView
                pageTransitionPane,  // contains a standard mount node, or null if we want to display the backgroundNode
                isMobileLayout ? mobileBottomMainMenuBar : null, // mobile menu bar (at bottom) or overlay menu bar (in addition to the one inside mountTransitionPane)
                connectivityIndicator.getConnectivityBar()
            );
            // We call setAll() only if they differ, because setAll() is basically a clear() + addAll() and this causes
            // unnecessary changes in the DOM which in addition cause iFrames to unload
            if (!Objects.equals(children, mainFrameContainer.getChildren()))
                mainFrameContainer.getChildren().setAll(children);
            firstOverlayChildIndex = mainFrameContainer.getChildren().size();
            updateOverlayChildren();
        }, FXBackgroundNode.backgroundNodeProperty(), mobileLayoutProperty, mountNodeProperty());

        MonoPane mountNodeContainer = new MonoPane();
        MonoPane languageMenuBar = LanguageMenuBar.createLanguageMenuBar();
        CollapsePane responsiveTopMenusPane = new CollapsePane();
        responsiveTopMenusPane.collapsedProperty().bind(FXCollapseMenu.collapseMenuProperty()); // will be redefined in some cases

        new ResponsiveDesign(responsiveTopMenusPane)
            .addResponsiveLayout(/* applicability test for small mobile menus mode: */width -> width < 450,
                () -> { // applying mobile view
                    responsiveTopMenusPane.setContent(createTopMobileMenu(languageMenuBar, false));
                }
            ).addResponsiveLayout(/* applicability test for medium mobile menus mode: */width -> width < 600,
                () -> { // applying mobile view
                    responsiveTopMenusPane.setContent(createTopMobileMenu(languageMenuBar, true));
                }
            ).addResponsiveLayout(() -> { // otherwise, applying desktop menus mode
                VBox desktopMenus = new VBox(
                    languageMenuBar,
                    desktopMainMenuBar,
                    desktopUserMenuBar
                );
                desktopMenus.setAlignment(Pos.CENTER);
                if (languageMenuBar.getContent() instanceof MonoPane languageSection) {
                    languageSection.setAlignment(Pos.BOTTOM_LEFT);
                    FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(languageSection);  // to fit like the mount node
                }
                responsiveTopMenusPane.setContent(desktopMenus);
            }).start();

        VBox pageVBox = new VBox(
            responsiveTopMenusPane,
            mountNodeContainer
        );
        pageVBox.setAlignment(Pos.CENTER);
        pageVBox.setMaxWidth(Double.MAX_VALUE);

        MainFrameFooterNodeProvider footerProvider = MainFrameFooterNodeProvider.getProvider();
        Node footer = footerProvider == null ? null : footerProvider.getFooterNode();
        if (footer != null) {
            VBox.setMargin(footer, new Insets(50, 0, 50, 0));
            pageVBox.getChildren().add(footer);
            Layouts.bindManagedToVisibleProperty(footer);
        }

        BorderPane borderPane = new BorderPane(pageVBox);
        ScrollPane scrollPane = Controls.createVerticalScrollPane(borderPane);
        pageTransitionPane.transitToContent(scrollPane);

        // Reacting to the mount node changes:
        FXProperties.runNowAndOnPropertyChange(mountNode -> {
            // Updating the mount node container with the new mount node
            if (mountNode != null && getMountNodeEmbeddingScrollPane(mountNode) == null) {
                if (mountNode instanceof Region mountRegion) {
                    FXProperties.runNowAndOnPropertiesChange(() ->
                            mountRegion.setMinHeight(pageTransitionPane.getMinHeight() - languageMenuBar.getHeight() - desktopMainMenuBar.getHeight() - desktopUserMenuBar.getHeight() - (footer != null && footer.isVisible() ? footer.getLayoutBounds().getHeight() + VBox.getMargin(footer).getTop() + VBox.getMargin(footer).getBottom() : 0))
                        , pageTransitionPane.minHeightProperty(), languageMenuBar.heightProperty(), desktopMainMenuBar.heightProperty(), desktopUserMenuBar.heightProperty(), footer == null ? null : footer.layoutBoundsProperty(), footer == null ? null : footer.visibleProperty());
                }
                registerMountNodeEmbeddingScrollPane(mountNode, scrollPane);
            }

            ToggleButton matchingRouteButton = Collections.findFirst(MenuConfig.mainAndUserMenuItemGroup.getToggleButtons(), toggleButton ->
                RoutingActions.isCurrentRouteMatchingRoutingAction(ActionBinder.getNodeAction(toggleButton)));
            MenuConfig.mainAndUserMenuItemGroup.setFiredItem(MenuConfig.mainAndUserMenuItemGroup.getButtonItem(matchingRouteButton));

            // Transiting to the node (embedded in the scroll pane)
            mountNodeContainer.setContent(mountNode);
            // When the mount node is null, this is to indicate that we want to display the background node instead
            boolean displayBackgroundNode = mountNode == null;
            // We make the background node visible only when we want to display it
            if (backgroundNode != null)
                backgroundNode.setVisible(displayBackgroundNode);
            // Also, when we display the background node, we need to make the mount node container transparent to the
            // mouse (as the background node is behind) to allow the user to interact with it (ex: WebView).
            pageTransitionPane.setMouseTransparent(displayBackgroundNode);
            if (footer != null) {
                boolean isLoginPage = mountNode != null && Collections.findFirst(mountNode.getStyleClass(), styleClass -> styleClass.contains("login")) != null;
                if (isLoginPage || displayBackgroundNode) {
                    FXProperties.setEvenIfBound(footer.visibleProperty(), false);
                } else
                    footer.visibleProperty().bind(FXShowFooter.showFooterProperty());
            }
            updateDialogArea();
        }, mountNodeProperty());

        FXMainFrameOverlayArea.setOverlayArea(mainFrameContainer);
        ObservableLists.runOnListChange(this::updateOverlayChildren, FXMainFrameOverlayArea.getOverlayChildren());

        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double footerHeight = Math.max(0.08 * (Math.min(mainFrameContainer.getHeight(), mainFrameContainer.getWidth())), 40);
            mobileBottomMainMenuBar.getChildren().forEach(menuButton -> {
                if (menuButton instanceof Region region) // Should be the case as they are ScalePane
                    region.setPrefHeight(footerHeight);
            });
        }, mainFrameContainer.widthProperty(), mainFrameContainer.heightProperty());

        setupPlayersGlobalConfiguration();

        mainFrameContainer.setOnMouseClicked(e -> {
            if (e.isShiftDown())
                FXProperties.toggleProperty(mobileLayoutProperty);
        });

        mainFrameContainer.getStyleClass().add(main_frame);
        FXProperties.runNowAndOnPropertyChange(keyboardNavigationDetected -> {
            Collections.addIfNotContainsOrRemove(mainFrameContainer.getStyleClass(), keyboardNavigationDetected, keyboard_navigation_on);
            Collections.addIfNotContainsOrRemove(mainFrameContainer.getStyleClass(), !keyboardNavigationDetected, keyboard_navigation_off);
        }, FXKeyboardNavigationDetected.keyboardNavigationDetectedProperty());

        return mainFrameContainer;
    }

    private HBox createTopMobileMenu(MonoPane languageMenuBar, boolean languageMenuBarCanFit) {
        HBox mobileMenus = new HBox(10, BurgerMenu.createBurgerMenuIcon(this), MenuBarFactory.createStretchableBrandPane());
        if (languageMenuBarCanFit) {
            mobileMenus.getChildren().addAll(languageMenuBar, UserMenu.createUserMenuIcon(null, this));
            if (languageMenuBar.getContent() instanceof MonoPane languageSection) {
                languageSection.setAlignment(Pos.CENTER);
                languageSection.setPadding(Insets.EMPTY);
            }
        } else
            mobileMenus.getChildren().add(UserMenu.createUserMenuIcon(languageMenuBar, this));
        mobileMenus.setAlignment(Pos.CENTER);
        mobileMenus.setPadding(new Insets(10, 0, 10, 0));
        mobileMenus.getStyleClass().addAll(menu_bar, non_mobile); //
        FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(mobileMenus);  // to fit like the mount node
        return mobileMenus;
    }

    private static final String ARBITRARY_EMBEDDING_SCROLL_PANE_PROPERTIES_KEY = "embeddingScrollPane";

    private static void registerMountNodeEmbeddingScrollPane(Node mountNode, ScrollPane scrollPane) {
        mountNode.getProperties().put(ARBITRARY_EMBEDDING_SCROLL_PANE_PROPERTIES_KEY, scrollPane);
    }

    private static ScrollPane getMountNodeEmbeddingScrollPane(Node mountNode) {
        return mountNode == null ? null : (ScrollPane) mountNode.getProperties().get(ARBITRARY_EMBEDDING_SCROLL_PANE_PROPERTIES_KEY);
    }

    private static void setupPlayersGlobalConfiguration() {
        // Setting up the fullscreen button
        ModalityVideoOverlay.setupModalityVideoOverlay();
        // Setting up the player color (actually, only Wistia supports it)
        Players.setGlobalPlayerColor(Brand.getBrandMainColor());
    }

    private void updateDialogArea() {
        if (dialogArea != null)
            mainFrameContainer.getChildren().remove(dialogArea);
        dialogArea = null;
        Node relatedDialogNode = getMountNode();
        if (relatedDialogNode != null) {
            var properties = relatedDialogNode.getProperties();
            String arbitraryKey = "modality-dialogArea";
            dialogArea = (Pane) properties.get(arbitraryKey);
            if (dialogArea == null) {
                properties.put(arbitraryKey, dialogArea = new Pane());
                dialogArea.getStyleClass().add(modality_dialog_area);
                // We request focus on mouse clicked. This is to allow the dropdown dialog in ButtonSelector to automatically
                // close when the user clicks outside (focus change triggers this auto-close mechanism).
                dialogArea.setOnMouseClicked(e -> dialogArea.requestFocus());
                // We automatically show or hide the dialog area, depending on the presence or not of children:
                dialogArea.getChildren().addListener((InvalidationListener) observable -> showOrHideDialogArea());
            } else
                showOrHideDialogArea();
        }
        FXMainFrameDialogArea.setDialogArea(dialogArea);
    }

    private void showOrHideDialogArea() {
        ObservableList<Node> mainFrameChildren = mainFrameContainer.getChildren();
        if (dialogArea.getChildren().isEmpty()) // If the dialog area has no dialogs to show, we hide it
            mainFrameChildren.remove(dialogArea);
        else if (!mainFrameChildren.contains(dialogArea)) { // otherwise if it's not already shown, we show it
            mainFrameChildren.add(firstOverlayChildIndex, dialogArea);
        }
    }

    private void onMobileLayoutChange(boolean mobileLayout) {
        Console.log("mobileLayout = " + mobileLayout);
    }

    private void updateOverlayChildren() {
        ObservableList<Node> mainFrameChildren = mainFrameContainer.getChildren();
        ObservableList<Node> overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
        while (firstOverlayChildIndex < mainFrameChildren.size())
            mainFrameChildren.remove(firstOverlayChildIndex);
        mainFrameChildren.addAll(firstOverlayChildIndex, overlayChildren);
    }

}
