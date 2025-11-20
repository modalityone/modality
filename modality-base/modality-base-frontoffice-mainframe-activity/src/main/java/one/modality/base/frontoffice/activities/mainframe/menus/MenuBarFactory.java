package one.modality.base.frontoffice.activities.mainframe.menus;

import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionBinder;
import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.*;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.brand.BrandI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.crm.shared.services.authn.fx.FXUserName;

/**
 * @author Bruno Salmon
 */
public final class MenuBarFactory {

    private static int menuItemSeq;

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> CollapsePane createMenuBar(String[] menuOperationCodes, AriaToggleGroup<Integer> menuItemGroup, boolean userMenu, MenuBarLayout menuBarLayout, A activity) {
        ToggleButton[] menuItemButtons = RoutingActions.filterRoutingActions(activity, activity, menuOperationCodes)
            .stream().map(action -> {
                ToggleButton menuButton = menuItemGroup.registerItemButton(createMenuButton(action, userMenu, menuBarLayout), ++menuItemSeq, true);
                if (RoutingActions.isCurrentRouteMatchingRoutingAction(action))
                    menuItemGroup.setFiredItem(menuItemSeq);
                return menuButton;
            })
            .toArray(ToggleButton[]::new);
        Region buttonBar;
        if (menuBarLayout == MenuBarLayout.MOBILE_BOTTOM) {
            ScalePane[] scaledMobileButtons = Arrays.map(menuItemButtons, MenuBarFactory::scaleButton, ScalePane[]::new);
            buttonBar = new ColumnsPane(scaledMobileButtons);
        } else if (menuBarLayout != MenuBarLayout.DESKTOP) {
            VBox vBox = new VBox(13, menuItemButtons);
            vBox.setAlignment(Pos.CENTER_LEFT);
            buttonBar = vBox;
        } else {
            HBox hBox = new HBox(13, menuItemButtons);
            hBox.setFillHeight(true);
            FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(hBox);  // to fit like the mount node
            if (userMenu) {
                hBox.getChildren().add(0, createStretchableUsernamePane());
                hBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBar = hBox;
                buttonBar.setPrefHeight(MenuConfig.WEB_USER_MENU_HEIGHT);
            } else {
                hBox.getChildren().add(0, createStretchableBrandPane());
                hBox.setAlignment(Pos.BOTTOM_RIGHT);
                hBox.setMaxHeight(Region.USE_PREF_SIZE);
                buttonBar = new MonoPane(hBox);
                buttonBar.setMinHeight(MenuConfig.WEB_MAIN_MENU_HEIGHT);
                buttonBar.setPrefHeight(MenuConfig.WEB_MAIN_MENU_HEIGHT);
                buttonBar.setMaxHeight(MenuConfig.WEB_MAIN_MENU_HEIGHT);
            }
        }
        buttonBar.getStyleClass().setAll("button-bar"); // to make menuItemButtons square in CSS (remove round corners)
        CollapsePane collapsePane = new CollapsePane(buttonBar);
        Aria.setAriaRole(collapsePane, AriaRole.NAVIGATION);
        collapsePane.getStyleClass().setAll("menu-bar", userMenu ? "user-menu-bar" : "main-menu-bar", menuBarLayout == MenuBarLayout.MOBILE_BOTTOM ? "mobile" : "non-mobile");
        collapsePane.setMaxWidth(Double.MAX_VALUE); // necessary to make the (CSS) border fill the whole page width
        collapsePane.setMinWidth(0); // Temporarily allowing menu shrinking on mobiles to prevent stopping page content shrinking (which is even worse as this crops the content on left and right)
        if (menuBarLayout == MenuBarLayout.DESKTOP) {
            // Binding collapsedProperty with FXCollapseMenu = general case (will be redefined for the user menu to include login)
            collapsePane.setAnimate(false);
            collapsePane.collapsedProperty().bind(FXCollapseMenu.collapseMenuProperty()); // will be redefined in some cases
        } else if (menuBarLayout == MenuBarLayout.MOBILE_BOTTOM) {
            collapsePane.setEffect(new DropShadow());
            collapsePane.setClipEnabled(false);
            // Considering the bottom of the safe area, in particular for OS like iPadOS with a bar at the bottom
            FXProperties.runNowAndOnPropertyChange(sai -> {
                double safeAreaBottom = sai.getBottom();
                // we already have 5 px padding for the menuItemButtons
                collapsePane.setPadding(new Insets(0, 0, Math.max(0, safeAreaBottom - 5), 0));
            }, WebFxKitLauncher.safeAreaInsetsProperty());
        } else if (menuBarLayout == MenuBarLayout.MOBILE_LEFT)
            collapsePane.setCollapseSide(Side.RIGHT);
        else if (menuBarLayout == MenuBarLayout.MOBILE_RIGHT)
            collapsePane.setCollapseSide(Side.LEFT);
        return collapsePane;
    }

    private static ToggleButton createMenuButton(Action routeAction, boolean userMenu, MenuBarLayout menuBarLayout) {
        ToggleButton button = ActionBinder.newActionToggleButton(routeAction);
        button.setCursor(Cursor.HAND);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setMaxHeight(Double.MAX_VALUE);
        // We display the icons on top of the text for the main menu, but not for the user menu
        boolean displayIconOnTopOfText = !userMenu && (menuBarLayout == MenuBarLayout.DESKTOP || menuBarLayout == MenuBarLayout.MOBILE_BOTTOM);
        if (displayIconOnTopOfText) {
            button.setContentDisplay(ContentDisplay.TOP);
            // All buttons have the same size (because we set maxHeight to MAX_VALUE, so the HBox container will stretch
            // them all to the same height). However, not all icons have the same size, and the default CENTER alignment
            // of the ToggleButton results in having the texts not exactly in the same vertical position, which is not
            // beautiful. To remedy this, we change the vertical alignment BOTTOM. This makes all text aligned at the
            // same bottom line (icons may not be centered on the same line, but this default is much less visible).
            button.setAlignment(Pos.BOTTOM_CENTER);
        }
        button.setGraphicTextGap(menuBarLayout == MenuBarLayout.MOBILE_BOTTOM ? 0 : 8);
        FXProperties.runNowAndOnPropertyChange(graphic -> {
            if (graphic instanceof SVGPath svgPath) {
                boolean hasStroke = svgPath.getStroke() != null;
                boolean hasFill = svgPath.getFill() != null;
                Collections.addIfNotContainsOrRemove(button.getStyleClass(), hasStroke, "hasStroke");
                Collections.addIfNotContainsOrRemove(button.getStyleClass(), hasFill, "hasFill");
                ObjectProperty<Paint> svgColorProperty = hasStroke ? svgPath.strokeProperty() : svgPath.fillProperty();
                if (menuBarLayout == MenuBarLayout.MOBILE_BOTTOM) {
                    button.textFillProperty().bind(svgColorProperty);
                } else {
                    /* Commented as this was making the Books stroke to null (icon was appearing black)
                    svgColorProperty.bind(button.textFillProperty()); */
                    svgColorProperty.set(Color.BLACK); // menu items color hard-code for now
                }
            }
        }, button.graphicProperty());
        if (menuBarLayout == MenuBarLayout.MOBILE_LEFT || menuBarLayout == MenuBarLayout.MOBILE_RIGHT)
            button.setPadding(new Insets(10, 20, 10, 20));
        else
            button.setPadding(new Insets(5));
        return button;
    }

    public static LargestFittingChildPane createStretchableBrandPane() {
        LargestFittingChildPane brandPane = new LargestFittingChildPane(
            createBrandLabel(false, 20), // Long name, large gap
            createBrandLabel(false, 5), // Long name, small gap
            createBrandLabel(true, 5) // Short name, small gap
        );
        // Stretching brandPane to the maximum available width in the HBox and aligned to the left
        brandPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(brandPane, Priority.ALWAYS);
        brandPane.setAlignment(Pos.CENTER_LEFT);
        /*
        // Routing to the default route (likely the home page) when clicking on the brand pane
        brandPane.setCursor(Cursor.HAND);
        brandPane.setOnMouseClicked(e -> {
            BrowsingHistory history = getHistory();
            UiRouter uiRouter = getUiRouter();
            history.push(uiRouter.getDefaultInitialHistoryPath());
        });
        */
        return brandPane;
    }

    public static MonoPane setupSideMenuIconAndBar(Node menuIcon, CollapsePane sideMenuBar, MenuBarLayout menuBarLayout) {
        MonoPane monoPane = new MonoPane(sideMenuBar);
        monoPane.setEffect(new DropShadow());
        MonoClipPane monoClipPane = new MonoClipPane(monoPane);
        if (menuBarLayout == MenuBarLayout.MOBILE_LEFT) {
            monoClipPane.setPadding(new Insets(0, 50, 0, 0));
            StackPane.setAlignment(monoClipPane, Pos.CENTER_LEFT);
        } else { // MOBILE_RIGHT
            monoClipPane.setPadding(new Insets(0, 0, 0, 50));
            StackPane.setAlignment(monoClipPane, Pos.CENTER_RIGHT);
        }
        sideMenuBar.collapse();
        StackPane stackPane = new StackPane(monoClipPane);
        stackPane.setOnMouseClicked(e -> {
            sideMenuBar.collapse();
            stackPane.setBackground(null);
            FXProperties.onPropertyEquals(sideMenuBar.transitingProperty(), false, x -> {
                FXMainFrameOverlayArea.getOverlayChildren().remove(stackPane);
            });
        });
        return SvgIcons.createButtonPane(menuIcon, () -> {
            stackPane.setBackground(BackgroundFactory.newBackground(Color.gray(0.3, 0.5)));
            FXMainFrameOverlayArea.getOverlayChildren().add(stackPane);
            UiScheduler.scheduleDelay(100, sideMenuBar::expand);
        });

    }

    private static Label createBrandLabel(boolean shortVersion, double graphicTextGap) {
        Label brandLabel = I18nControls.newLabel(shortVersion ? BrandI18nKeys.frontOfficeBrandNameAndLogoShort : BrandI18nKeys.frontOfficeBrandNameAndLogo);
        brandLabel.setGraphicTextGap(graphicTextGap);
        brandLabel.getStyleClass().setAll("brand");
        return brandLabel;
    }

    private static LargestFittingChildPane createStretchableUsernamePane() {
        Label userNameLabel = new Label();
        userNameLabel.textProperty().bind(FXUserName.userNameProperty());
        Controls.setupTextWrapping(userNameLabel, false, true);
        Label userInitialsLabel = new Label();
        userInitialsLabel.textProperty().bind(FXUserName.userInitialsProperty());
        LargestFittingChildPane usernamePane = new LargestFittingChildPane(
            userNameLabel, // Showing the full username if space is available (ex: on desktops)
            userInitialsLabel // Otherwise showing only the initials (ex: on small mobiles)
        );
        // Stretching usernamePane to the maximum available width in the HBox and aligned to the left
        usernamePane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(usernamePane, Priority.ALWAYS);
        usernamePane.setAlignment(Pos.CENTER_LEFT);
        return usernamePane;
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
