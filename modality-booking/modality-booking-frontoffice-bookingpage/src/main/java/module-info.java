// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.frontoffice.bookingpage {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.cloud.image;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.time;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.booking.client.workingbooking;
    requires modality.booking.frontoffice.bookingelements;
    requires modality.booking.frontoffice.bookingform;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.crm.frontoffice.activity.userprofile.plugin;
    requires modality.crm.shared.authn;
    requires modality.ecommerce.client.i18n;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.frontoffice.order;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.client;
    requires modality.ecommerce.policy.service;
    requires modality.ecommerce.shared.pricecalculator;
    requires modality.event.client.lifecycle;
    requires modality.event.frontoffice.activity.book;
    requires webfx.extras.aria;
    requires webfx.extras.async;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.responsive;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time.format;
    requires webfx.extras.util.border;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.extras.validation;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.platform.windowhistory;
    requires webfx.platform.windowlocation;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.booking.frontoffice.bookingpage;
    exports one.modality.booking.frontoffice.bookingpage.cart;
    exports one.modality.booking.frontoffice.bookingpage.components;
    exports one.modality.booking.frontoffice.bookingpage.navigation;
    exports one.modality.booking.frontoffice.bookingpage.pages.audiorecording;
    exports one.modality.booking.frontoffice.bookingpage.pages.closed;
    exports one.modality.booking.frontoffice.bookingpage.pages.payment;
    exports one.modality.booking.frontoffice.bookingpage.pages.personal;
    exports one.modality.booking.frontoffice.bookingpage.pages.prerequisite;
    exports one.modality.booking.frontoffice.bookingpage.pages.summary;
    exports one.modality.booking.frontoffice.bookingpage.pages.teaching;
    exports one.modality.booking.frontoffice.bookingpage.pages.terms;
    exports one.modality.booking.frontoffice.bookingpage.sections;
    exports one.modality.booking.frontoffice.bookingpage.standard;
    exports one.modality.booking.frontoffice.bookingpage.theme;

}