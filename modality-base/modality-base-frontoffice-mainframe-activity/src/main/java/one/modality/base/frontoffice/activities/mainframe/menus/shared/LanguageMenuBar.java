package one.modality.base.frontoffice.activities.mainframe.menus.shared;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.platform.util.Arrays;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import one.modality.base.frontoffice.activities.mainframe.menus.MenuConfig;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.utility.page.FOPageUtil;

/**
 * @author Bruno Salmon
 */
public final class LanguageMenuBar {

    public static CollapsePane createLanguageMenuBar() {
        Insets languageButtonPadding = new Insets(0, 9, 0, 9);
        SegmentedButton<Object> languageSegmentedButton = new SegmentedButton<>();
        Arrays.forEach(MenuConfig.LANGUAGES, lang -> {
            ToggleButton toggleButton = languageSegmentedButton.addButtonSegment(lang, lang.toUpperCase());
            toggleButton.setPadding(languageButtonPadding);
        });
        languageSegmentedButton.stateProperty().bindBidirectional(I18n.languageProperty());
        HBox languageBar = languageSegmentedButton.getView(); // Aria role already set by SegmentedButton class
        Aria.setAriaLabel(languageBar, "Language selector");
        Layouts.setFixedHeight(languageBar, MenuConfig.LANG_BAR_MENU_HEIGHT);
        languageBar.getStyleClass().setAll("button-bar");
        MonoPane languageSection = new MonoPane(languageBar);
        languageSection.setAlignment(Pos.BOTTOM_LEFT);
        Layouts.setFixedHeight(languageSection, MenuConfig.LANG_MENU_HEIGHT);
        FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftRightPadding(languageSection);  // to fit like the mount node
        CollapsePane collapsePane = new CollapsePane(languageSection);
        collapsePane.setAnimate(false);
        //Temporary, while the user language change is not implemented in the settings
        collapsePane.collapsedProperty().bind(/*FXLoggedIn.loggedInProperty().or*/(FXCollapseMenu.collapseMenuProperty()));
        // Showing the language menu (i.e., not collapsing it) when no event is selected (ex: home page), or it's a NKT event
        /*collapsePane.collapsedProperty().bind(FXProperties.combine(FXOrganizationId.organizationIdProperty(), FXEventId.eventIdProperty(),
            (oId, eId) -> !(eId == null || Entities.samePrimaryKey(oId, 1))));*/
        collapsePane.setAnimate(true);
        collapsePane.getStyleClass().setAll("menu-bar", "lang-menu-bar", "non-mobile");
        return collapsePane;
    }
}
