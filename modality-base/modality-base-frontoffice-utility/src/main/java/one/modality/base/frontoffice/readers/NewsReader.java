package one.modality.base.frontoffice.readers;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.util.Dates;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.News;

import java.time.LocalDate;
import java.util.Objects;

public class NewsReader {
    public static void fetch(DataSourceModel dm) {
        Fetch.fetch("https://kadampa.org/wp-json/wp/v2/posts?per_page=5&lang=" + I18n.getLanguage().toString()) // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonArray()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonArray -> {
                                System.out.println("News size = " + jsonArray.size());

                                EntityStore entityStore = EntityStore.create(dm);
                                Future<EntityList<News>> newsDB = entityStore.<News>executeListQuery(
                                                "news",
                                                "select channelNewsId from News order by id desc limit 5"
                                        )
                                        .onFailure(System.out::println)
                                        .onSuccess(news -> {

                                            UpdateStore updateStore = UpdateStore.createAbove(entityStore);
                                            loop: for (int i=0; i<jsonArray.size(); i++) {
                                                ReadOnlyJsonObject o = jsonArray.getObject(i);
                                                String id = o.getString("id");

                                                for (News n : news) {
                                                    if (Objects.equals(id, n.getChannelNewsId())) {
                                                        continue loop;
                                                    }
                                                }

                                                News n = updateStore.insertEntity(News.class);
                                                n.setChannel(1);
                                                n.setChannelNewsId(id);
                                                n.setTitle(o.getObject("title").getString("rendered"));
                                                n.setExcerpt(o.getObject("excerpt").getString("rendered"));
                                                n.setDate(LocalDate.from(Dates.parseIsoLocalDateTime(o.getString("date"))));
                                                n.setLinkUrl(o.getObject("guid").getString("rendered"));

                                                Fetch.fetch("https://kadampa.org/wp-json/wp/v2/media/" + o.getString("episode_featured_image")) // Expecting a JSON object only
                                                        .onFailure(error -> Console.log("Fetch failure: " + error))
                                                        .onSuccess(response1 -> {
                                                            Console.log("Fetch success: ok = " + response1.ok());
                                                            response1.jsonObject()
                                                                    .onFailure(error -> Console.log("JsonObject failure: " + error))
                                                                    .onSuccess(jsonObject -> {
                                                                        n.setImageUrl(jsonObject.getObject("guid").getString("rendered").replace("\\", ""));
                                                                    });
                                                        });

                                            }

                                            updateStore.submitChanges()
                                                    .onFailure(e -> System.out.println("Fail"))
                                                    .onSuccess(s -> System.out.println("Success"));
                                        });
                            });
                });
    }
}
