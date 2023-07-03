package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.util.tuples.Pair;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.utility.GeneralUtility;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookingUtility {
    public static void cityAutoComplete(String key) {
        Fetch.fetch("http://api.geonames.org/searchJSON?featureClass=P&lang=en&maxRows=12&style=full&username=emmanuel.rideau&name_startsWith=" + key.replace(" ", "%20")) // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonObject()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonObject -> {
                                System.out.println(jsonObject);
                                ReadOnlyJsonObject o = jsonObject.getArray("geonames").getObject(0);
                                String name = o.get("asciiName");
                                Double lat = o.getDouble("lat");
                                Double lng = o.getDouble("lng");
                                Console.log("JsonObject Success: " + jsonObject.toJsonString());
                                System.out.println(name);

                                List<Pair<Center, Double>> centersDistance = FXApp.centers.stream().map(c -> {
                                    Double d = GeneralUtility.distance(lat, lng, c.lat, c.lng, 'K');
                                    return new Pair<>(c, d);
                                }).filter(new Predicate<Pair<Center, Double>>() {
                                    @Override
                                    public boolean test(Pair<Center, Double> centerDoublePair) {
                                        return centerDoublePair.get2() != null;
                                    }
                                }).sorted(new Comparator<Pair<Center, Double>>() {
                                    @Override
                                    public int compare(Pair<Center, Double> o1, Pair<Center, Double> o2) {
                                        if (Objects.equals(o1.get2(), o2.get2())) return 0;
                                        return o1.get2() > o2.get2() ? 1 : -1;
                                    }
                                }).collect(Collectors.toList());

                                List<Pair<Center, Double>> centersResult = centersDistance.stream().filter(new Predicate<Pair<Center, Double>>() {
                                    @Override
                                    public boolean test(Pair<Center, Double> centerDoublePair) {
                                        return centerDoublePair.get2() < 50;
                                    }
                                }).collect(Collectors.toList());

                                centersResult.forEach(p -> {
                                    System.out.println(p.get1().organization.getName() + " - " + p.get1().type);
                                    System.out.println(p.get2());
                                    System.out.println(" ");
                                });

                                String mapUrl = GeneralUtility.generateStaticMapLinks(lat, lng, centersResult.stream().map(Pair::get1).collect(Collectors.toList()));

                                FXBooking.centerImageProperty.set(mapUrl);
                            });
                });
    }
}
