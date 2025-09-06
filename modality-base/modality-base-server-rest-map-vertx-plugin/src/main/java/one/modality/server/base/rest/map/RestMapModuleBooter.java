package one.modality.server.base.rest.map;

import dev.webfx.platform.boot.spi.ApplicationModuleBooter;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.util.http.HttpHeaders;
import dev.webfx.platform.util.vertx.VertxInstance;
import dev.webfx.stack.orm.entity.EntityStore;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

import static dev.webfx.platform.util.http.HttpResponseStatus.*;

/**
 * @author Bruno Salmon
 */
public class RestMapModuleBooter implements ApplicationModuleBooter {

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
                    EntityStore.create()
                            .<Organization>executeQuery("select latitude,longitude from Organization where id=?", organizationId)
                            .onFailure(err -> ctx.response().setStatusCode(INTERNAL_SERVER_ERROR_500).send())
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
                    EntityStore.create()
                            .<Country>executeQuery("select latitude,longitude from Country where id=?", countryId)
                            .onFailure(err -> ctx.response().setStatusCode(INTERNAL_SERVER_ERROR_500).send())
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
            ctx.response().setStatusCode(BAD_REQUEST_400).send();
            return;
        }
        String organizationStaticMapUrlTemplate = SourcesConfig.getSourcesRootConfig()
                .childConfigAt("modality.base.server.rest.organizationmap")
                .getString("organizationStaticMapUrl");
        String url = organizationStaticMapUrlTemplate
                .replace("{latitude}",  Float.toString(latitude))
                .replace("{longitude}", Float.toString(longitude))
                .replace("{zoom}", zoom)
                ;
        webClient.getAbs(url)
                .send()
                .onFailure(err -> ctx.response().setStatusCode(SERVICE_UNAVAILABLE_503).send())
                .onSuccess(proxyRes -> {
                    ctx.response()
                            .setStatusCode(proxyRes.statusCode())
                            .putHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                            .headers().addAll(proxyRes.headers());
                    ctx.response().send(proxyRes.body());
                });
    }
}
