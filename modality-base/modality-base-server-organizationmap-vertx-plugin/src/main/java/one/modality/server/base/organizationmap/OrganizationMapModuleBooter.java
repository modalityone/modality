package one.modality.server.base.organizationmap;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.vertx.common.VertxInstance;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public class OrganizationMapModuleBooter implements ApplicationModuleBooter {

    @Override
    public String getModuleName() {
        return "modality-base-server-organizationmap-vertx-plugin";
    }

    @Override
    public int getBootLevel() {
        return COMMUNICATION_REGISTER_BOOT_LEVEL;
    }

    @Override
    public void bootModule() {
        String organizationStaticMapUrlTemplate = SourcesConfig.getSourcesRootConfig()
                .childConfigAt("modality.base.server.organizationmap")
                .getString("organizationStaticMapUrl");
        WebClient webClient = WebClient.create(VertxInstance.getVertx());
        Router router = VertxInstance.getHttpRouter();
        router.route(HttpMethod.GET, "/organization-map/:organizationId")
                .handler(ctx -> {
                    int organizationId = Integer.parseInt(ctx.pathParam("organizationId"));
                    EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                            .<Organization>executeQuery("select latitude,longitude from Organization where id=?", organizationId)
                            .onFailure(err -> ctx.response().setStatusCode(500).send()) // Internal server error
                            .onSuccess(list -> { // on successfully receiving the list (should be a singleton list)
                                if (list.isEmpty()) {
                                    ctx.response().setStatusCode(406).send(); // Not acceptable
                                    return;
                                }
                                Organization organization = list.get(0);
                                Float latitude = organization.getLatitude();
                                Float longitude = organization.getLongitude();
                                if (latitude == null || longitude == null) {
                                    ctx.response().setStatusCode(406).send(); // Not acceptable
                                    return;
                                }
                                String url = organizationStaticMapUrlTemplate
                                        .replace("{latitude}",  Float.toString(latitude))
                                        .replace("{longitude}", Float.toString(longitude));
                                webClient.getAbs(url)
                                        .send()
                                        .onFailure(err -> ctx.response().setStatusCode(503).send()) // Service unavailable
                                        .onSuccess(proxyRes -> {
                                            HttpServerResponse res = ctx.response();
                                            res.setStatusCode(proxyRes.statusCode());
                                            res.headers().addAll(proxyRes.headers());
                                            res.putHeader("cache-control", "public, max-age=86400");
                                            res.send(proxyRes.body());
                                        });
                            });
                });
    }
}
