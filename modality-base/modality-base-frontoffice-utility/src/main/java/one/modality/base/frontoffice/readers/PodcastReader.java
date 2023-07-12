package one.modality.base.frontoffice.readers;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.util.Dates;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.frontoffice.entities.PodcastOld;
import one.modality.base.shared.entities.Podcast;

import java.time.LocalDate;
import java.util.Objects;

public class PodcastReader {
    public static void fetch(DataSourceModel dm) {
        Fetch.fetch("https://kadampa.org/wp-json/wp/v2/podcast?per_page=5") // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("Podcasts size = " + jsonArray.size());

                                EntityStore entityStore = EntityStore.create(dm);
                                Future<EntityList<Podcast>> podcastsDB = entityStore.<Podcast>executeListQuery(
                                        "podcasts",
                                        "select channelPodcastId from Podcast order by id desc limit 5"
                                )
                                        .onFailure(e -> System.out.println(e))
                                        .onSuccess(podcasts -> {

                                    UpdateStore updateStore = UpdateStore.createAbove(entityStore);
                                    loop: for (int i=0; i<jsonArray.size(); i++) {
                                        ReadOnlyJsonObject o = jsonArray.getObject(i);
                                        String id = o.getString("id");
                                        PodcastOld podcast = new PodcastOld(o);

                                        for (Podcast p : podcasts) {
                                            if (Objects.equals(id, p.getChannelPodcastId())) {
                                                continue loop;
                                            }
                                        }

                                        Podcast p = updateStore.insertEntity(Podcast.class);
                                        p.setChannel(2);
                                        p.setChannelPodcastId(id);
                                        p.setTitle(o.getObject("title").getString("rendered"));
                                        p.setExcerpt(o.getObject("excerpt").getString("rendered"));
                                        p.setDate(LocalDate.from(Dates.parseIsoLocalDateTime(o.getString("date"))));
                                        p.setAudioUrl(o.getString("player_link"));
                                        p.setImageUrl(o.getString("episode_featured_image"));
                                    }
                                    updateStore.submitChanges()
                                            .onFailure(e -> System.out.println("Fail"))
                                            .onSuccess(s -> System.out.println("Success"));
                                });
                            });
                });
    }
}
