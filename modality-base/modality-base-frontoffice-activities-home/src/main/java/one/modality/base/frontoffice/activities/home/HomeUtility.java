package one.modality.base.frontoffice.activities.home;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.entities.Center;

public class HomeUtility {

        /*Button computeButton = new Button("Compute");

        computeButton.setOnAction(e -> {
            Center.adjustRejectedList(FXApp.centers);
            Center.adjustRejectedList(FXApp.centersRef);

            FXApp.centers = FXCollections.observableArrayList(FXApp.centers.stream().map(c -> {
                List<Center> filtered = FXApp.centersRef.stream()
                        .filter(cc -> Center.isGoodMatch(c, cc))
                        .collect(Collectors.toList());

                if (filtered.size() == 0) return null;

                Center matchedCenter = filtered.get(0);

                matchedCenter.organization = c.organization;

                return matchedCenter;
            }).filter(Objects::nonNull).collect(Collectors.toList()));

            System.out.println("Size of the matches: " + FXApp.centers.size());
        });*/

    public static void loadCenters(ObservableList<Center> centers) {

        Fetch.fetch("https://kdm.kadampaweb.org/index.php/business/json") // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("KDM size = " + jsonArray.size());
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    ReadOnlyJsonObject o = jsonArray.getObject(i);
                                    Center c = new Center(o.getString("name"), o.getDouble("lat"), o.getDouble("lng"), o.getString("type"), o.getString("city"));
                                    centers.add(c);
                                    System.out.println(i);
                                }
                                System.out.println("Centers size = " + centers.size());
                            });
                });
    }

}
