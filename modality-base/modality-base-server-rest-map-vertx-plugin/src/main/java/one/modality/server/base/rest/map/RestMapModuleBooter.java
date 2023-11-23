package one.modality.server.base.rest.map;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.vertx.common.VertxInstance;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public class RestMapModuleBooter implements ApplicationModuleBooter {

    private final static String organizationStaticMapUrlTemplate = SourcesConfig.getSourcesRootConfig()
            .childConfigAt("modality.base.server.rest.organizationmap")
            .getString("organizationStaticMapUrl");

    private WebClient webClient;

    @Override
    public String getModuleName() {
        return "modality-base-server-rest-organizationmap-vertx-plugin";
    }

    @Override
    public int getBootLevel() {
        return COMMUNICATION_REGISTER_BOOT_LEVEL;
    }

    @Override
    public void bootModule() {
        webClient = WebClient.create(VertxInstance.getVertx());
        Router router = VertxInstance.getHttpRouter();
        router.route(HttpMethod.GET, "/map/organization/:organizationId")
                .handler(ctx -> {
                    int organizationId = Integer.parseInt(ctx.pathParam("organizationId"));
                    String zoom = ctx.queryParams().contains("zoom") ? ctx.queryParams().get("zoom") : "12";
                    EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                            .<Organization>executeQuery("select latitude,longitude from Organization where id=?", organizationId)
                            .onFailure(err -> ctx.response().setStatusCode(500).send()) // Internal server error
                            .onSuccess(list -> { // on successfully receiving the list (should be a singleton list)
                                Float latitude = null, longitude = null;
                                if (!list.isEmpty()) {
                                    Organization organization = list.get(0);
                                    latitude = organization.getLatitude();
                                    longitude = organization.getLongitude();
                                }
                                forwardToGoogleMap(latitude, longitude, zoom, ctx);
                            });
                });

        router.route(HttpMethod.GET, "/map/country/:countryId")
                .handler(ctx -> {
                    int countryId = Integer.parseInt(ctx.pathParam("countryId"));
                    String zoom = ctx.queryParams().contains("zoom") ? ctx.queryParams().get("zoom") : "5";
                    EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                            .<Country>executeQuery("select latitude,longitude from Country where id=?", countryId)
                            .onFailure(err -> ctx.response().setStatusCode(500).send()) // Internal server error
                            .onSuccess(list -> { // on successfully receiving the list (should be a singleton list)
                                Float latitude = null, longitude = null;
                                if (!list.isEmpty()) {
                                    Country country = list.get(0);
                                    latitude = country.getLatitude();
                                    longitude = country.getLongitude();
                                }
                                forwardToGoogleMap(latitude, longitude, zoom, ctx);
                            });
                });
    }

    private void forwardToGoogleMap(Float latitude, Float longitude, String zoom, RoutingContext ctx) {
        if (latitude == null || longitude == null) {
            ctx.response().setStatusCode(406).send(); // Not acceptable
            return;
        }
        String url = organizationStaticMapUrlTemplate
                .replace("{latitude}",  Float.toString(latitude))
                .replace("{longitude}", Float.toString(longitude))
                .replace("{zoom}", zoom)
                ;
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
    }
}
