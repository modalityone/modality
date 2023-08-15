package one.modality.base.server.jobs.geoimport;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.JsonArray;
import dev.webfx.platform.json.JsonObject;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Country;

/**
 * @author bjvickers
 */
public class GeoImportJob implements ApplicationJob {

    private static final String GEO_FETCH_URL = "http://api.geonames.org/countryInfoJSON?featureCode=PCLI&username=modality.one";
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

    @Override
    public void onStart() {
        importGeo();
    }

    public void importGeo() {

        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        EntityStore.create(dataSourceModel).<Country>executeQuery("select id,iso_alpha2,latitude,longitude,north,south,east,west,geonameid from Country")

                .onFailure(error -> Console.log(error))
                .onSuccess(countries -> Fetch.fetch(GEO_FETCH_URL)

                                .onFailure(error -> Console.log("Error while fetching " + GEO_FETCH_URL, error))
                                .onSuccess(response -> response.jsonObject()

                                        .onFailure(error -> Console.log("Error while parsing json object from " + GEO_FETCH_URL, error))
                                        .onSuccess(geoJsonObject -> {

                                            JsonArray geonames = (JsonArray) geoJsonObject.getArray("geonames");
                                            for (int i = 0; i < geonames.size(); i++) {

                                                JsonObject geonameCountry = (JsonObject) geonames.getObject(i);
                                                Integer geonameId = geonameCountry.getInteger("geonameId");
                                                Double north = geonameCountry.getDouble("north");
                                                Double south = geonameCountry.getDouble("south");
                                                Double east = geonameCountry.getDouble("east");
                                                Double west = geonameCountry.getDouble("west");

                                                for (Country currentCountry : countries) {

                                                    if (currentCountry.getGeonameid().equals(geonameId)) {
                                                        currentCountry = updateStore.updateEntity(currentCountry);
                                                        currentCountry.setNorth(north.floatValue());
                                                        currentCountry.setSouth(south.floatValue());
                                                        currentCountry.setEast(east.floatValue());
                                                        currentCountry.setWest(west.floatValue());
                                                        break;
                                                    }

                                                }
                                            }

                                            if (!updateStore.hasChanges()) {
                                                Console.log("No Countries to update");
                                            } else {
                                                Console.log("Updating Countries... ");
                                                updateStore.submitChanges().onFailure(Console::log);
                                            }

                                        })));
    }

}
