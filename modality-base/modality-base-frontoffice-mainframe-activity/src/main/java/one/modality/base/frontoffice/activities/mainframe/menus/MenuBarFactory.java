package one.modality.base.frontoffice.activities.mainframe.menus;

import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionBinder;
import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.*;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.brand.BrandI18nKeys;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.crm.shared.services.authn.fx.FXUserName;

/**
 * @author Bruno Salmon
 */
public final class MenuBarFactory {

    private static int menuItemSeq;

    public static ToggleButton createMenuButton(Action routeAction, boolean userMenu, boolean mobileLayout) {
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
                boolean hasFill = svgPath.getFill() != null;
                Collections.addIfNotContainsOrRemove(button.getStyleClass(), hasStroke, "hasStroke");
                Collections.addIfNotContainsOrRemove(button.getStyleClass(), hasFill, "hasFill");
                ObjectProperty<Paint> svgColorProperty = hasStroke ? svgPath.strokeProperty() : svgPath.fillProperty();
                if (mobileLayout) {
                    button.textFillProperty().bind(svgColorProperty);
                } else {
                    /* Commented as this was making the Books stroke to null (icon was appearing black)
                    svgColorProperty.bind(button.textFillProperty()); */
                    svgColorProperty.set(Color.BLACK); // menu items color hard-code for now
                }
            }
        }, button.graphicProperty());
        button.setPadding(new Insets(5));
        return button;
    }

    public static <A extends UiRouteActivityContext<?> & OperationActionFactoryMixin> CollapsePane createMenuBar(String[] menuOperationCodes, AriaToggleGroup<Integer> menuItemGroup, boolean userMenu, boolean mobileLayout, A activity) {
        ToggleButton[] menuItemButtons = RoutingActions.filterRoutingActions(activity, activity, menuOperationCodes)
            .stream().map(action -> {
                ToggleButton menuButton = menuItemGroup.registerItemButton(createMenuButton(action, userMenu, mobileLayout), ++menuItemSeq, true);
                if (RoutingActions.isCurrentRouteMatchingRoutingAction(action))
                    menuItemGroup.setFiredItem(menuItemSeq);
                return menuButton;
            })
            .toArray(ToggleButton[]::new);
        Region buttonBar;
        if (mobileLayout) {
            ScalePane[] scaledMobileButtons = Arrays.map(menuItemButtons, MenuBarFactory::scaleButton, ScalePane[]::new);
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
                buttonBar.setPrefHeight(MenuConfig.WEB_USER_MENU_HEIGHT);
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
                // Routing to the default route (likely the home page) when clicking on the brand pane
                brandPane.setCursor(Cursor.HAND);
                /*brandPane.setOnMouseClicked(e -> {
                    BrowsingHistory history = getHistory();
                    UiRouter uiRouter = getUiRouter();
                    history.push(uiRouter.getDefaultInitialHistoryPath());
                });*/
                hBox.getChildren().add(0, brandPane);
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

    private static Label createBrandLabel(boolean shortVersion, double graphicTextGap) {
        Label brandLabel = I18nControls.newLabel(shortVersion ? BrandI18nKeys.frontOfficeBrandNameAndLogoShort : BrandI18nKeys.frontOfficeBrandNameAndLogo);
        brandLabel.setGraphicTextGap(graphicTextGap);
        brandLabel.getStyleClass().setAll("brand");
        return brandLabel;
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
