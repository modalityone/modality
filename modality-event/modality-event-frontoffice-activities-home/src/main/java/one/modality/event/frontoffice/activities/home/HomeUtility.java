package one.modality.event.frontoffice.activities.home;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.shared.entities.News;
import one.modality.base.shared.entities.Podcast;

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

    public static void loadNews(ViewDomainActivityBase activity) {
        ReactiveEntitiesMapper.<News>createPushReactiveChain(activity)
                .always("{class: 'News', fields:'channel, channelNewsId, date, title, excerpt, imageUrl, linkUrl', orderBy: 'date desc'}")
                .always(FXHome.newsLimit, limit -> DqlStatement.limit("?", limit))
                .storeEntitiesInto(FXHome.news)
                .start();
    }

    public static void loadPodcasts(ViewDomainActivityBase activity) {
        ReactiveEntitiesMapper.<Podcast>createPushReactiveChain(activity)
                .always("{class: 'Podcast', fields:'channel, channelPodcastId, date, title, excerpt, imageUrl, audioUrl', orderBy: 'date desc'}")
                .always(FXHome.podcastLimit, limit -> DqlStatement.limit("?", limit))
                .storeEntitiesInto(FXHome.podcasts)
                .start();
    }
}
