package one.modality.base.server.jobs.newsimport;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.fetch.Response;
import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.Dates;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.News;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class NewsImportJob implements ApplicationJob {

    private static final String NEWS_FETCH_URL = "https://kadampa.org/wp-json/wp/v2/posts";
    private static final String MEDIA_FETCH_URL = "https://kadampa.org/wp-json/wp/v2/media";
    private static final int limit = 100;
    private static final long IMPORT_PERIODICITY_MILLIS = 3600 * 1000; // every 1h
    private Scheduled importTimer;
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();


    @Override
    public void onStart() {
        importNews();
        importTimer = Scheduler.schedulePeriodic(IMPORT_PERIODICITY_MILLIS, this::importNews);
    }

    @Override
    public void onStop() {
        if (importTimer != null)
            importTimer.cancel();
    }

    public void importNews() {
        String fetchUrl = NEWS_FETCH_URL + "?per_page=" + limit + "&lang=en";
        Fetch.fetch(fetchUrl)
                .onFailure(error -> Console.log("Error while fetching " + fetchUrl, error))
                .onSuccess(response -> {
                    response.jsonArray()
                            .onFailure(error -> Console.log("Error while parsing json array from " + fetchUrl, error))
                            .onSuccess(newsJsonArray -> {
                                EntityStore entityStore = EntityStore.create(dataSourceModel);
                                entityStore.<News>executeListQuery("news",
                                                "select channelNewsId from News order by date desc limit ?", limit
                                        )
                                        .onFailure(e -> Console.log("Error while reading news from database", e))
                                        .onSuccess(news -> {

                                            UpdateStore updateStore = UpdateStore.createAbove(entityStore);

                                            List<String> mediaIds = new ArrayList<>();

                                            for (int i = 0; i < newsJsonArray.size(); i++) {
                                                ReadOnlyJsonObject newsJson = newsJsonArray.getObject(i);
                                                String id = newsJson.getString("id");

                                                if (news.stream().anyMatch(p -> Objects.equals(id, p.getChannelNewsId())))
                                                    continue;
                                                String mediaId = newsJson.getString("featured_media");
                                                mediaIds.add(mediaId);
                                            }

                                            Batch<String> mediaIdsReaderBatch = new Batch<>(mediaIds.toArray(new String[0]));
                                            mediaIdsReaderBatch.executeParallel(JsonObject[]::new, mediaId ->
                                                            Fetch.fetch(MEDIA_FETCH_URL + "/" + mediaId).compose(Response::jsonObject))
                                                    .onFailure(e -> Console.log("Error while fetching news medias" , e))
                                                    .onSuccess(mediasJsonBatch -> {
                                                        JsonObject[] mediasJson = mediasJsonBatch.getArray();
                                                        for (int i = 0; i < mediasJson.length; i++) {
                                                            ReadOnlyJsonObject newsJson = newsJsonArray.getObject(i);
                                                            String id = newsJson.getString("id");
                                                            News n = updateStore.insertEntity(News.class);
                                                            n.setChannel(1);
                                                            n.setChannelNewsId(id);
                                                            n.setTitle(getRendered(newsJson.getObject("title")));
                                                            n.setExcerpt(getRendered(newsJson.getObject("excerpt")));
                                                            n.setDate(LocalDate.from(Dates.parseIsoLocalDateTime(newsJson.getString("date"))));
                                                            n.setLinkUrl(cleanUrl(getRendered(newsJson.getObject("guid"))));
                                                            n.setImageUrl(cleanUrl(getRendered(mediasJson[i].getObject("guid"))));
                                                        }

                                                        updateStore.submitChanges()
                                                                .onFailure(e -> Console.log("Error while inserting news in database", e))
                                                                .onSuccess(insertBatch -> {
                                                                    int newNewsCount = insertBatch.getArray().length;
                                                                    Console.log(newNewsCount + " new news imported in database");
                                                                });
                                                    });
                                        });
                            });
                });
    }

    private static String getRendered(ReadOnlyJsonObject json) {
        return json == null ? null : json.getString("rendered");
    }

    private static String cleanUrl(String url) {
        return url == null ? null : url.replace("\\", "");
    }

}
