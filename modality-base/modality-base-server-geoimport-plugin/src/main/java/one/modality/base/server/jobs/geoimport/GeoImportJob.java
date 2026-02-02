package one.modality.base.server.jobs.geoimport;

import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.json.JsonFetch;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Country;

/**
 * @author bjvickers
 */
public final class GeoImportJob implements ApplicationJob {

    private static final String GEO_FETCH_URL = "http://api.geonames.org/countryInfoJSON?featureCode=PCLI&username=modality.one";

    @Override
    public void onStart() {
        importGeo();
    }

    public void importGeo() {
        UpdateStore updateStore = UpdateStore.create();
        // TODO: rename geonameid to geonamesId in the domain model
        EntityStore.create().<Country>executeQuery("select id,iso_alpha2,latitude,longitude,north,south,east,west,geonameid from Country")
            .onFailure(Console::error)
            .onSuccess(countries -> JsonFetch.fetchJsonObject(GEO_FETCH_URL)
                .onFailure(error -> Console.error("Error while fetching " + GEO_FETCH_URL, error))
                .onSuccess(geoJsonObject -> {

                    ReadOnlyAstArray geonames = geoJsonObject.getArray("geonames");
                    for (int i = 0; i < geonames.size(); i++) {

                        ReadOnlyAstObject geonamesCountry = geonames.getObject(i);
                        Integer geonamesId = geonamesCountry.getInteger("geonameId");
                        Double north = geonamesCountry.getDouble("north");
                        Double south = geonamesCountry.getDouble("south");
                        Double east = geonamesCountry.getDouble("east");
                        Double west = geonamesCountry.getDouble("west");

                        for (Country currentCountry : countries) {

                            if (currentCountry.getGeonameid().equals(geonamesId)) {
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
                        updateStore.submitChanges().onFailure(Console::error);
                    }
                }));
    }

}
