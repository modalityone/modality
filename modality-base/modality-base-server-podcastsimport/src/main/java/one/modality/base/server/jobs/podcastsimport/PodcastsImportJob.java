package one.modality.base.server.jobs.podcastsimport;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.Dates;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Podcast;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class PodcastsImportJob implements ApplicationJob {

    private static final String PODCAST_FETCH_URL = "https://kadampa.org/wp-json/wp/v2/podcast";
    private static final int limit = 100;
    private static final long IMPORT_PERIODICITY_MILLIS = 3600 * 1000; // 1h
    private Scheduled importTimer;
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();


    @Override
    public void onStart() {
        importPodcasts();
        importTimer = Scheduler.schedulePeriodic(IMPORT_PERIODICITY_MILLIS, this::importPodcasts);
    }

    @Override
    public void onStop() {
        if (importTimer != null)
            importTimer.cancel();
    }

    public void importPodcasts() {
        String fetchUrl = PODCAST_FETCH_URL + "?per_page=" + limit;
        Fetch.fetch(fetchUrl)
                .onFailure(error -> Console.log("Error while fetching " + fetchUrl, error))
                .onSuccess(response -> {
                    response.jsonArray()
                            .onFailure(error -> Console.log("Error while parsing json array from " + fetchUrl, error))
                            .onSuccess(podcastsJsonArray -> {
                                EntityStore entityStore = EntityStore.create(dataSourceModel);
                                entityStore.<Podcast>executeListQuery("podcasts",
                                                "select channelPodcastId from Podcast order by date desc limit ?", limit
                                        )
                                        .onFailure(e -> Console.log("Error while reading podcasts from database", e))
                                        .onSuccess(podcasts -> {

                                            UpdateStore updateStore = UpdateStore.createAbove(entityStore);

                                            for (int i = 0; i < podcastsJsonArray.size(); i++) {
                                                ReadOnlyJsonObject podcastJson = podcastsJsonArray.getObject(i);
                                                String id = podcastJson.getString("id");

                                                if (podcasts.stream().anyMatch(p -> Objects.equals(id, p.getChannelPodcastId())))
                                                    continue;

                                                Podcast p = updateStore.insertEntity(Podcast.class);
                                                p.setChannel(2);
                                                p.setChannelPodcastId(id);
                                                p.setTitle(getRendered(podcastJson.getObject("title")));
                                                p.setExcerpt(getRendered(podcastJson.getObject("excerpt")));
                                                p.setDate(Dates.parseIsoLocalDate(podcastJson.getString("date")));
                                                p.setAudioUrl(cleanUrl(podcastJson.getString("player_link")));
                                                p.setImageUrl(cleanUrl(podcastJson.getString("episode_featured_image")));
                                            }

                                            updateStore.submitChanges()
                                                    .onFailure(e -> Console.log("Error while inserting podcasts in database", e))
                                                    .onSuccess(insertBatch -> {
                                                        int newPodcastsCount = insertBatch.getArray().length;
                                                        Console.log(newPodcastsCount + " new podcasts imported in database");
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
