// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.mainframe.activity {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires modality.base.client.brand;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.frontoffice.mainframe.footernode;
    requires modality.base.frontoffice.mainframe.fx;
    requires modality.base.frontoffice.utility;
    requires modality.crm.client.authn.fx;
    requires modality.event.client.mediaview;
    requires webfx.extras.action;
    requires webfx.extras.aria;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.player;
    requires webfx.extras.responsive;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.background;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.kit.util.aria;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.base.frontoffice.activities.mainframe;
    exports one.modality.base.frontoffice.activities.mainframe.menus;
    exports one.modality.base.frontoffice.activities.mainframe.menus.desktop;
    exports one.modality.base.frontoffice.activities.mainframe.menus.mobile;
    exports one.modality.base.frontoffice.activities.mainframe.menus.shared;

}