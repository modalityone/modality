package one.modality.event.frontoffice.activities.home;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.entities.News;
import one.modality.base.frontoffice.entities.Podcast;
import one.modality.base.frontoffice.fx.FXHome;
import dev.webfx.stack.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public class HomeUtility {
    public static void loadCenters(ObservableList<Center> centers) {

        Fetch.fetch("https://kdm.kadampaweb.org/index.php/business/json") // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("KDM size = " + jsonArray.size());
                                for (int i=0; i<jsonArray.size(); i++) {
                                    ReadOnlyJsonObject o = jsonArray.getObject(i);
                                    Center c = new Center(o.getString("name"), o.getDouble("lat"), o.getDouble("lng"), o.getString("type"), o.getString("city"));
                                    centers.add(c);
                                    System.out.println(i);
                                }
                                System.out.println("Centers size = " + centers.size());
                            });
                });
    }

    public static void loadNews() {
        Fetch.fetch("https://kadampa.org/wp-json/wp/v2/posts?per_page=5&lang=" + I18n.getLanguage().toString()) // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("News size = " + jsonArray.size());
                                List<News> news = new ArrayList<>();

                                for (int i=0; i<jsonArray.size(); i++) {
                                    System.out.println(i);
                                    ReadOnlyJsonObject o = jsonArray.getObject(i);
                                    News n = new News(o);
                                    news.add(n);
                                }

                                Platform.runLater(() -> FXHome.news.setAll(news));
                            });
                });
    }

    public static void loadPodcasts() {
        Fetch.fetch("https://kadampa.org/wp-json/wp/v2/podcast?per_page=5") // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("Podcasts size = " + jsonArray.size());
                                List<Podcast> podcasts = new ArrayList<>();

                                for (int i=0; i<jsonArray.size(); i++) {
                                    System.out.println(i);
                                    ReadOnlyJsonObject o = jsonArray.getObject(i);
                                    Podcast p = new Podcast(o);
                                    podcasts.add(p);
                                }

                                Platform.runLater(() -> FXHome.podcasts.setAll(podcasts));
                            });
                });
    }
}
