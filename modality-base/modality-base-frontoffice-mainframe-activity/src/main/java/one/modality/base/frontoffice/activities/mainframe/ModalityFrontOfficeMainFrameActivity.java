package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.aria.FXKeyboardNavigationDetected;
import dev.webfx.extras.panes.*;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.panes.transitions.Transition;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionBinder;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.brand.BrandI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.base.client.mainframe.fx.FXMainFrameTransiting;
import one.modality.base.frontoffice.mainframe.fx.FXBackgroundNode;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.mainframe.fx.FXShowFooter;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.crm.shared.services.authn.fx.FXUserName;
import one.modality.event.client.event.fx.FXEventId;

import java.util.List;
import java.util.Objects;

public final class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    private static final boolean ENABLE_OVERLAY_MENU_BAR = false;
    private static final Transition INITIAL_TRANSITION_EFFECT = new CircleTransition();
    private static final Transition PAGE_TRANSITION_EFFECT = null; // maybe go back to new FadeTransition() after Spring Festival

    private static final double LANG_MENU_HEIGHT = 52;
    private static final double LANG_BAR_MENU_HEIGHT = 29;
    private static final double WEB_MAIN_MENU_HEIGHT = 100;
    private static final double WEB_USER_MENU_HEIGHT = 52;

    private static final Config FRONT_OFFICE_CONFIG = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.frontoffice.application");
    private static final String[] LANGUAGES = FRONT_OFFICE_CONFIG.getString("languages").split(",");
    private static final String[] MAIN_MENU_OPERATION_CODES = FRONT_OFFICE_CONFIG.getString("mainMenuOperationCodes").split(",");
    private static final String[] USER_MENU_OPERATION_CODES = FRONT_OFFICE_CONFIG.getString("userMenuOperationCodes").split(",");

    private final BooleanProperty mobileLayoutProperty =
        FXProperties.newBooleanProperty(UserAgent.isNative(), this::onMobileLayoutChange);

    private Pane mainFrameContainer;
    private Node backgroundNode; // can be used to hold a WebView and prevent iFrame reload in the web version
    private final TransitionPane pageTransitionPane = new TransitionPane();
    private CollapsePane overlayMenuBar; // 1 unique instance
    private CollapsePane mountMainMenuButtonBar; // 1 instance per mount node
    private CollapsePane mobileMenuBar; // 1 unique instance
    private ScalePane[] scaledMobileButtons;
    private Pane dialogArea;
    private int firstOverlayChildIndex;

    @Override
    public Node buildUi() {
        // Starting with a circle transition animation for the first activity displayed
        pageTransitionPane.setAnimateFirstContent(true);
        pageTransitionPane.setTransition(INITIAL_TRANSITION_EFFECT);
        // And then a fade transition for later activities
        FXProperties.runOnPropertyChange(transiting -> {
            if (transiting) {
                if (ENABLE_OVERLAY_MENU_BAR)
                    overlayMenuBar.setAnimate(false);
            } else { // Ending a transition
                pageTransitionPane.setTransition(PAGE_TRANSITION_EFFECT) ;
            }
        }, pageTransitionPane.transitingProperty());
        //mountTransitionPane.setKeepsLeavingNodes(true); // Note: activities with video players should call TransitionPane.setKeepsLeavingNode(node, false)
        FXMainFrameTransiting.transitingProperty().bind(pageTransitionPane.transitingProperty());
        mainFrameContainer = new LayoutPane() { // Children are set later in updateMountNode()
            @Override
            protected void layoutChildren(double width, double height) {
                double headerHeight = 0, footerHeight = 0;
                boolean isMobileLayout = mobileLayoutProperty.get();
                if (isMobileLayout) {
                    footerHeight = mobileMenuBar.prefHeight(width);
                    layoutInArea(mobileMenuBar, 0, height - footerHeight, width, footerHeight);
                } else if (ENABLE_OVERLAY_MENU_BAR && mountMainMenuButtonBar != null) {
                    Point2D p = mountMainMenuButtonBar.localToScene(0, 0);
                    layoutInArea(overlayMenuBar, p.getX(), /*p.getY() < 0 ? 0 : p.getY()*/ 0, mountMainMenuButtonBar.getWidth(), mountMainMenuButtonBar.getHeight());
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
            }
        };
        CollapsePane languageMenuBar = createLanguageMenuBar();
        AriaToggleGroup<Integer> overlayMenuItemGroup = new AriaToggleGroup<>(AriaRole.MENUITEM);
        VBox overlayMenuBarContent = new VBox(
            createLanguageMenuBar(),
            createMainMenuButtonBar(overlayMenuItemGroup, false)
        );
        overlayMenuBarContent.setAlignment(Pos.CENTER);
        overlayMenuBarContent.setMaxWidth(Double.MAX_VALUE);
        if (ENABLE_OVERLAY_MENU_BAR) {
            overlayMenuBar = new CollapsePane(overlayMenuBarContent);
            overlayMenuBar.setAnimate(false);
            overlayMenuBar.setVisible(false);
            overlayMenuBar.collapse();
            double[] lastMouseY = {0};
            mainFrameContainer.setOnMouseMoved(e -> {
                double mouseY = e.getY(), mouseX = e.getSceneX();
                if (Math.abs(mouseY - lastMouseY[0]) > 5 && !FXCollapseMenu.isCollapseMenu()) {
                    Node mountNode = getMountNode();
                    ScrollPane scrollPane = getMountNodeEmbeddingScrollPane(mountNode);
                    boolean isPageOnTop = scrollPane == null || scrollPane.getVvalue() == 0;
                    boolean up = mouseY < lastMouseY[0];
                    if (!isPageOnTop && up && mouseY < mainFrameContainer.getHeight() / 3) {
                        if (overlayMenuBar.isCollapsed()) {
                            // Searching for the first button (ugly code...)
                            Node node = overlayMenuBar.getContent(); // should return a vbox;
                            if (node instanceof VBox)
                                node = ((VBox) node).getChildren().get(1); // should return a CollapsePane
                            if (node instanceof CollapsePane)
                                node = ((CollapsePane) node).getContent(); // should return a MonoPane
                            if (node instanceof MonoPane)
                                node = ((MonoPane) node).getContent(); // should return an HBox
                            if (node instanceof Parent)
                                node = ((Parent) node).getChildrenUnmodifiable().get(2); // Should be the index for the first button
                            if (node != null && mouseX >= node.localToScene(0, 0).getX()) {
                                overlayMenuBar.setAnimate(true);
                                overlayMenuBar.expand();
                            }
                        }
                    } else if (!up && mouseY > WEB_MAIN_MENU_HEIGHT) {
                        overlayMenuBar.setAnimate(!isPageOnTop);
                        overlayMenuBar.collapse();
                    }
                }
                lastMouseY[0] = mouseY;
            });
        }

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
                isMobileLayout ? mobileMenuBar : overlayMenuBar); // mobile menu bar (at bottom) or overlay menu bar (in addition to the one inside mountTransitionPane)
            // We call setAll() only if they differ, because setAll() is basically a clear() + addAll() and this causes
            // unnecessary changes in the DOM which in addition cause iFrames to unload
            if (!Objects.equals(children, mainFrameContainer.getChildren()))
                mainFrameContainer.getChildren().setAll(children);
            firstOverlayChildIndex = mainFrameContainer.getChildren().size();
            updateOverlayChildren();
            if (ENABLE_OVERLAY_MENU_BAR && getMountNode() == null)
                overlayMenuBar.collapse();
        }, FXBackgroundNode.backgroundNodeProperty(), mobileLayoutProperty, mountNodeProperty());

        // Temporarily hardcoded footer image
        ImageView footer = new ImageView(new Image(Resource.toUrl("/images/organizations/NKT-IKBU.svg", getClass()), true));
        VBox.setMargin(footer, new Insets(0, 0, 50, 0));

        AriaToggleGroup<Integer> mainAndUserMenuItemGroup = new AriaToggleGroup<>(AriaRole.MENUITEM);
        CollapsePane mainMenuButtonBar = createMainMenuButtonBar(mainAndUserMenuItemGroup, false);
        CollapsePane userMenuButtonBar = createUserMenuButtonBar(mainAndUserMenuItemGroup);
        AriaToggleGroup<Integer> mobileMenuItemGroup = new AriaToggleGroup<>(AriaRole.MENUITEM);
        mobileMenuBar = createMainMenuButtonBar(mobileMenuItemGroup, true);

        MonoPane mountNodeContainer = new MonoPane();

        VBox pageVBox = new VBox(
            languageMenuBar,
            mainMenuButtonBar,
            userMenuButtonBar,
            mountNodeContainer,
            footer
        );
        pageVBox.setAlignment(Pos.CENTER);
        pageVBox.setMaxWidth(Double.MAX_VALUE);

        BorderPane borderPane = new BorderPane(pageVBox);
        ScrollPane scrollPane = Controls.createVerticalScrollPane(borderPane);
        pageTransitionPane.transitToContent(scrollPane);

        if (ENABLE_OVERLAY_MENU_BAR) {
            double[] lastVTopOffset = {0};
            FXProperties.runOnPropertiesChange(() -> {
                mountMainMenuButtonBar = mainMenuButtonBar;
                if (pageTransitionPane.isTransiting())
                    return;
                // Visibility management:
                double vTopOffset = Controls.computeScrollPaneVTopOffset(scrollPane);
                if (vTopOffset <= languageMenuBar.getHeight()) { // Making the overlay menu bar invisible when reaching the top
                    overlayMenuBar.setAnimate(false); // because there is already a web menu on top of that page
                    overlayMenuBar.collapse();
                } else if (vTopOffset > Screen.getPrimary().getBounds().getHeight()) {
                    overlayMenuBar.setVisible(true); // Making it visible when the top one is no more in the view
                    overlayMenuBar.setAnimate(true); // port (however, it will not be showing while it's collapsed)
                }
                // Collapse management:
                // Collapsing the overlay menu if an activity explicitly asked to do so
                if (FXCollapseMenu.isCollapseMenu())
                    overlayMenuBar.collapse();
                    // otherwise if the user scrolled a bit (at least 5 pixels)
                else if (Math.abs(vTopOffset - lastVTopOffset[0]) > 5) {
                    // we expand of collapse the overlay menu depending on the scroll direction
                    if (overlayMenuBar.isAnimate()) // and only when animated (page scrolled down)
                        overlayMenuBar.setCollapsed(vTopOffset > lastVTopOffset[0]); // up = expand, down = collapse
                    lastVTopOffset[0] = vTopOffset;
                }
            }, scrollPane.vvalueProperty(), FXCollapseMenu.collapseMenuProperty());
        }

        Layouts.bindManagedToVisibleProperty(footer);
        // Reacting to the mount node changes:
        FXProperties.runNowAndOnPropertyChange(mountNode -> {
            boolean isLoginPage = mountNode != null && Collections.findFirst(mountNode.getStyleClass(), styleClass -> styleClass.contains("login")) != null;
            if (isLoginPage) {
                FXProperties.setEvenIfBound(footer.visibleProperty(), false);
            } else
                footer.visibleProperty().bind(FXShowFooter.showFooterProperty());
            // Updating the mount node container with the new mount node
            if (mountNode != null && getMountNodeEmbeddingScrollPane(mountNode) == null) {
                if (mountNode instanceof Region mountRegion) {
                    FXProperties.runNowAndOnPropertiesChange(() ->
                            mountRegion.setMinHeight(pageTransitionPane.getMinHeight() - languageMenuBar.getHeight() - mainMenuButtonBar.getHeight() - userMenuButtonBar.getHeight() - footer.getLayoutBounds().getHeight() - VBox.getMargin(footer).getBottom())
                        , pageTransitionPane.minHeightProperty(), languageMenuBar.heightProperty(), mainMenuButtonBar.heightProperty(), userMenuButtonBar.heightProperty(), footer.layoutBoundsProperty());
                }
                registerMountNodeEmbeddingScrollPane(mountNode, scrollPane);
            }

            ToggleButton matchingRouteButton = Collections.findFirst(mainAndUserMenuItemGroup.getToggleButtons(), toggleButton ->
                RoutingActions.isCurrentRouteMatchingRoutingAction(ActionBinder.getNodeAction(toggleButton)));
            mainAndUserMenuItemGroup.setFiredItem(mainAndUserMenuItemGroup.getButtonItem(matchingRouteButton));

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
            updateDialogArea();
        }, mountNodeProperty());

        FXMainFrameOverlayArea.setOverlayArea(mainFrameContainer);
        ObservableLists.runOnListChange(this::updateOverlayChildren, FXMainFrameOverlayArea.getOverlayChildren());

        // Requesting a layout for containerPane on layout mode changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            double footerHeight = Math.max(0.08 * (Math.min(mainFrameContainer.getHeight(), mainFrameContainer.getWidth())), 40);
            Arrays.forEach(scaledMobileButtons, scaledButton -> scaledButton.setPrefHeight(footerHeight));
        }, mainFrameContainer.widthProperty(), mainFrameContainer.heightProperty());

        setupPlayersGlobalConfiguration();

        mainFrameContainer.setOnMouseClicked(e -> {
            if (e.isShiftDown())
                FXProperties.toggleProperty(mobileLayoutProperty);
        });

        mainFrameContainer.getStyleClass().add("main-frame");
        FXProperties.runNowAndOnPropertyChange(keyboardNavigationDetected -> {
            Collections.addIfNotContainsOrRemove(mainFrameContainer.getStyleClass(), keyboardNavigationDetected, "keyboard-navigation-on");
            Collections.addIfNotContainsOrRemove(mainFrameContainer.getStyleClass(), !keyboardNavigationDetected, "keyboard-navigation-off");
        }, FXKeyboardNavigationDetected.keyboardNavigationDetectedProperty());

        return mainFrameContainer;
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
        ModalityFullscreenButton.setupModalityFullscreenButton();
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
                dialogArea.getStyleClass().add("modality-dialog-area");
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

    private CollapsePane createLanguageMenuBar() {
        Insets languageButtonPadding = new Insets(0, 9, 0, 9);
        SegmentedButton<Object> languageSegmentedButton = new SegmentedButton<>();
        Arrays.forEach(LANGUAGES, lang -> {
            ToggleButton toggleButton = languageSegmentedButton.addButtonSegment(lang, lang.toUpperCase());
            toggleButton.setPadding(languageButtonPadding);
        });
        languageSegmentedButton.stateProperty().bindBidirectional(I18n.languageProperty());
        HBox languageBar = languageSegmentedButton.getView(); // Aria role already set by SegmentedButton class
        Aria.setAriaLabel(languageBar, "Language selector");
        Layouts.setFixedHeight(languageBar, LANG_BAR_MENU_HEIGHT);
        languageBar.getStyleClass().setAll("button-bar");
        MonoPane languageSection = new MonoPane(languageBar);
        languageSection.setAlignment(Pos.BOTTOM_LEFT);
        Layouts.setFixedHeight(languageSection, LANG_MENU_HEIGHT);
        FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(languageSection);  // to fit like the mount node
        CollapsePane collapsePane = new CollapsePane(languageSection);
        collapsePane.setAnimate(false);
        //Temporary, while the user language change is not implemented in the settings
        //collapsePane.collapsedProperty().bind(FXLoggedIn.loggedInProperty().or(FXCollapseMenu.collapseMenuProperty()));
        // Showing the language menu (i.e., not collapsing it) when no event is selected (ex: home page), or it's a NKT event
        collapsePane.collapsedProperty().bind(FXProperties.combine(FXOrganizationId.organizationIdProperty(), FXEventId.eventIdProperty(),
            (oId, eId) -> !(eId == null || Entities.samePrimaryKey(oId, 1))));
        collapsePane.setAnimate(true);
        collapsePane.getStyleClass().setAll("menu-bar", "lang-menu-bar", "non-mobile");
        return collapsePane;
    }

    private CollapsePane createMainMenuButtonBar(AriaToggleGroup<Integer> menuItemGroup, boolean mobileLayout) {
        return createMenuButtonBar(MAIN_MENU_OPERATION_CODES, menuItemGroup, false, mobileLayout);
    }

    private CollapsePane createUserMenuButtonBar(AriaToggleGroup<Integer> menuItemGroup) {
        CollapsePane userMenuButtonBar = createMenuButtonBar(USER_MENU_OPERATION_CODES, menuItemGroup, true, false);
        userMenuButtonBar.setAnimate(false);
        userMenuButtonBar.collapsedProperty().bind(FXLoggedIn.loggedInProperty().not().or(FXCollapseMenu.collapseMenuProperty()));
        userMenuButtonBar.setAnimate(true);
        return userMenuButtonBar;
    }

    private int menuItemSeq;

    private CollapsePane createMenuButtonBar(String[] menuOperationCodes, AriaToggleGroup<Integer> menuItemGroup, boolean userMenu, boolean mobileLayout) {
        ToggleButton[] menuItemButtons = RoutingActions.filterRoutingActions(this, this, menuOperationCodes)
            .stream().map(action -> {
                ToggleButton menuButton = menuItemGroup.registerItemButton(createMenuButton(action, userMenu, mobileLayout), ++menuItemSeq, true);
                if (RoutingActions.isCurrentRouteMatchingRoutingAction(action))
                    menuItemGroup.setFiredItem(menuItemSeq);
                return menuButton;
            })
            .toArray(ToggleButton[]::new);
        Region buttonBar;
        if (mobileLayout) {
            scaledMobileButtons = Arrays.map(menuItemButtons, ModalityFrontOfficeMainFrameActivity::scaleButton, ScalePane[]::new);
            buttonBar = new ColumnsPane(scaledMobileButtons);
        } else {
            HBox hBox = new HBox(13, menuItemButtons);
            hBox.setFillHeight(true);
            FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(hBox);  // to fit like the mount node
            if (userMenu) {
                Label userNameLabel = new Label();
                userNameLabel.textProperty().bind(FXUserName.userNameProperty());
                Controls.setupTextWrapping(userNameLabel, false, true);
                Label userInitialsLabel = new Label();
                userInitialsLabel.textProperty().bind(FXUserName.userInitialsProperty());
                LargestFittingChildPane namePane = new LargestFittingChildPane(
                    userNameLabel, // Showing the full username if space is available (ex: on desktops)
                    userInitialsLabel // Otherwise showing only the initials (ex: on small mobiles)
                );
                // Stretching namePane to the maximum available width in the HBox and aligned to the left
                namePane.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(namePane, Priority.ALWAYS);
                namePane.setAlignment(Pos.CENTER_LEFT);
                hBox.getChildren().add(0, namePane);
                hBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBar = hBox;
                buttonBar.setPrefHeight(WEB_USER_MENU_HEIGHT);
            } else {
                LargestFittingChildPane brandPane = new LargestFittingChildPane(
                    createBrandLabel(false, 20), // Long name, large gap
                    createBrandLabel(false, 5), // Long name, small gap
                    createBrandLabel(true, 5) // Short name, small gap
                );
                // Stretching brandPane to the maximum available width in the HBox and aligned to the left
                brandPane.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(brandPane, Priority.ALWAYS);
                brandPane.setAlignment(Pos.CENTER_LEFT);
                hBox.getChildren().add(0, brandPane);
                hBox.setAlignment(Pos.BOTTOM_RIGHT);
                hBox.setMaxHeight(Region.USE_PREF_SIZE);
                buttonBar = new MonoPane(hBox);
                buttonBar.setMinHeight(WEB_MAIN_MENU_HEIGHT);
                buttonBar.setPrefHeight(WEB_MAIN_MENU_HEIGHT);
                buttonBar.setMaxHeight(WEB_MAIN_MENU_HEIGHT);
            }
        }
        buttonBar.getStyleClass().setAll("button-bar"); // to make menuItemButtons square in CSS (remove round corners)
        CollapsePane collapsePane = new CollapsePane(buttonBar);
        Aria.setAriaRole(collapsePane, AriaRole.NAVIGATION);
        collapsePane.getStyleClass().setAll("menu-bar", userMenu ? "user-menu-bar" : "main-menu-bar", mobileLayout ? "mobile" : "non-mobile");
        collapsePane.setMaxWidth(Double.MAX_VALUE); // necessary to make the (CSS) border fill the whole page width
        collapsePane.setMinWidth(0); // Temporarily allowing menu shrinking on mobiles to prevent stopping page content shrinking (which is even worse as this crops the content on left and right)
        // Binding collapsedProperty with FXCollapseMenu = general case (will be redefined for the user menu to include login)
        collapsePane.setAnimate(false);
        collapsePane.collapsedProperty().bind(FXCollapseMenu.collapseMenuProperty()); // will be redefined in some cases
        if (mobileLayout) {
            collapsePane.setEffect(new DropShadow());
            collapsePane.setClipEnabled(false);
            // Considering the bottom of the safe area, in particular for OS like iPadOS with a bar at the bottom
            FXProperties.runNowAndOnPropertyChange(sai -> {
                double safeAreaBottom = sai.getBottom();
                // we already have 5 px padding for the menuItemButtons
                collapsePane.setPadding(new Insets(0, 0, Math.max(0, safeAreaBottom - 5), 0));
            }, WebFxKitLauncher.safeAreaInsetsProperty());
        }
        return collapsePane;
    }

    private Label createBrandLabel(boolean shortVersion, double graphicTextGap) {
        Label brandLabel = I18nControls.newLabel(shortVersion ? BrandI18nKeys.frontOfficeBrandNameAndLogoShort : BrandI18nKeys.frontOfficeBrandNameAndLogo);
        brandLabel.setGraphicTextGap(graphicTextGap);
        brandLabel.getStyleClass().setAll("brand");
        return brandLabel;
    }

    private ToggleButton createMenuButton(Action routeAction, boolean userMenu, boolean mobileLayout) {
        ToggleButton button = ActionBinder.newActionToggleButton(routeAction);
        button.setCursor(Cursor.HAND);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setMaxHeight(Double.MAX_VALUE);
        // We display the icons on top of the text for the main menu, but not for the user menu
        boolean displayIconOnTopOfText = !userMenu;
        if (displayIconOnTopOfText) {
            button.setContentDisplay(ContentDisplay.TOP);
            // All buttons have the same size (because we set maxHeight to MAX_VALUE, so the HBox container will stretch
            // them all to the same height). However, not all icons have the same size, and the default CENTER alignment
            // of the ToggleButton results in having the texts not exactly in the same vertical position, which is not
            // beautiful. To remedy this, we change the vertical alignment BOTTOM. This makes all text aligned at the
            // same bottom line (icons may not be centered on the same line, but this default is much less visible).
            button.setAlignment(Pos.BOTTOM_CENTER);
        }
        button.setGraphicTextGap(mobileLayout ? 0 : 8);
        FXProperties.runNowAndOnPropertyChange(graphic -> {
            if (graphic instanceof SVGPath svgPath) {
                boolean hasStroke = svgPath.getStroke() != null;
                ObjectProperty<Paint> svgColorProperty = hasStroke ? svgPath.strokeProperty() : svgPath.fillProperty();
                if (mobileLayout) {
                    button.textFillProperty().bind(svgColorProperty);
                } else {
                    /* Commented as this was making the Books stroke to null (icon was appearing black)
                    svgColorProperty.bind(button.textFillProperty());
                    */
                    svgColorProperty.set(Color.BLACK); // menu items color hard-code for now
                }
            }
        }, button.graphicProperty());
        button.setPadding(new Insets(5));
        return button;
    }

    private static ScalePane scaleButton(ButtonBase button) {
        ScalePane scalePane = new ScalePane(ScaleMode.FIT_HEIGHT, button);
        scalePane.setStretchWidth(true);
        scalePane.setStretchHeight(true);
        scalePane.visibleProperty().bind(button.visibleProperty());
        scalePane.managedProperty().bind(button.managedProperty()); // Should it be in MonoPane?
        return scalePane;
    }
}
