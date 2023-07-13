package one.modality.event.frontoffice.activities.home;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.shared.entities.News;
import one.modality.base.shared.entities.Podcast;
import one.modality.event.frontoffice.activities.home.mapper.NewsToNewsViewMapper;
import one.modality.event.frontoffice.activities.home.mapper.PodcastToPodcastViewMapper;

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

    public static void loadNews(ViewDomainActivityBase activity, ObservableList<Node> newsViews, VBox page) {
        ReactiveObjectsMapper.<News, Node>createPushReactiveChain(activity)
                .always("{class: 'News', fields:'channel, channelNewsId, date, title, excerpt, imageUrl, linkUrl', orderBy: 'date desc'}")
                .always(FXHome.newsLimit, limit -> DqlStatement.limit("?", limit))
                //.storeEntitiesInto(FXHome.news)
                .setIndividualEntityToObjectMapperFactory(news -> new NewsToNewsViewMapper(page, (OperationActionFactoryMixin) activity, activity, news))
                .storeMappedObjectsInto(newsViews)
                .start();
    }

    public static void loadPodcasts(ViewDomainActivityBase activity, ObservableList<Node> podcastViews, VBox page) {
        ReactiveObjectsMapper.<Podcast, Node>createPushReactiveChain(activity)
                .always("{class: 'Podcast', fields:'channel, channelPodcastId, date, title, excerpt, imageUrl, audioUrl', orderBy: 'date desc'}")
                .always(FXHome.podcastLimit, limit -> DqlStatement.limit("?", limit))
                .setIndividualEntityToObjectMapperFactory(podcast -> new PodcastToPodcastViewMapper(page, podcast))
                .storeMappedObjectsInto(podcastViews)
                .start();
    }
}
