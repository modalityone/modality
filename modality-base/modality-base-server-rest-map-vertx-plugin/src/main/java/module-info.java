// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.rest.map.vertx.plugin {

    // Direct dependencies modules
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.vertx.web.client;
    requires modality.base.shared.entities;
    requires webfx.platform.boot;
    requires webfx.platform.conf;
    requires webfx.platform.util.http;
    requires webfx.platform.util.vertx;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.server.base.rest.map;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with one.modality.server.base.rest.map.RestMapModuleBooter;

}