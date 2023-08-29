// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The OpenJFX executable module of the Modality Back-Office (for desktops &amp; development).
 */
module modality.all.backoffice.application.openjfx {

    // Direct dependencies modules
    requires modality.all.backoffice.application;
    requires modality.ecommerce.payment.custom.buscall;
    requires modality.ecommerce.payment.custom.remote;
    requires webfx.extras.webtext.peers.openjfx;
    requires webfx.kit.openjfx;
    requires webfx.kit.platform.visibility.openjfx;
    requires webfx.platform.boot.java;
    requires webfx.platform.console.java;
    requires webfx.platform.json.java;
    requires webfx.platform.os.java;
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.java;
    requires webfx.platform.storage.java;
    requires webfx.platform.storagelocation.java;
    requires webfx.platform.windowhistory.java;
    requires webfx.platform.windowlocation.java;
    requires webfx.stack.authn.buscall;
    requires webfx.stack.authn.login.buscall;
    requires webfx.stack.authn.login.remote;
    requires webfx.stack.authn.login.ui.gateway.webviewbased.openjfx;
    requires webfx.stack.authn.login.ui.portal;
    requires webfx.stack.authn.remote;
    requires webfx.stack.com.bus.json.client;
    requires webfx.stack.com.bus.json.client.websocket.java;
    requires webfx.stack.com.websocket.java;
    requires webfx.stack.db.query.buscall;
    requires webfx.stack.db.querysubmit.java.jdbc;
    requires webfx.stack.db.submit.buscall;
    requires webfx.stack.orm.dql.query.interceptor;
    requires webfx.stack.orm.dql.querypush.interceptor;
    requires webfx.stack.orm.dql.submit.interceptor;
    requires webfx.stack.session.client;
    requires webfx.stack.ui.fxraiser.json;

}