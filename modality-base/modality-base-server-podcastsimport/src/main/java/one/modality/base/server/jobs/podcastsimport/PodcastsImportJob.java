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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class PodcastsImportJob implements ApplicationJob {

    private static final String PODCAST_FETCH_URL = "https://kadampa.org/wp-json/wp/v2/podcast";
    private static final long IMPORT_PERIODICITY_MILLIS = 3600 * 1000; // 1h
    private Scheduled importTimer;
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private LocalDateTime fetchAfterParameter;


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
        // When this job starts, there is no fetchAfterParameter, so we initialize it with the latest podcast date
        // imported so far in the database.
        if (fetchAfterParameter == null) {
            EntityStore.create(dataSourceModel).<Podcast>executeQuery("select id,date from Podcast order by date desc limit 1")
                    .onFailure(error -> Console.log("Error while reading latest podcast", error))
                    .onSuccess(podcasts -> {
                        if (podcasts.isEmpty()) // Means that there is no podcast in the database
                            fetchAfterParameter = LocalDate.of(2000, 1, 1).atStartOfDay(); // The web service raise an error with dates before 2000
                        else
                            fetchAfterParameter = podcasts.get(0).getDate().atStartOfDay();
                        // Now that fetchAfterParameter is set, we can call importPodcasts() again.
                        importPodcasts();
                    });
            return;
        }
        // Creating the final fetch url with the additional query string (note: the number of podcasts returned by the
        // web service is 10 by default; this could be increased using &per_page=100 - 100 is the maximal value
        // authorized by the web service)
        String fetchUrl = PODCAST_FETCH_URL + "?order=asc&after=" + Dates.formatIso(fetchAfterParameter);
        Fetch.fetch(fetchUrl)
                .onFailure(error -> Console.log("Error while fetching " + fetchUrl, error))
                .onSuccess(response -> {
                    response.jsonArray()
                            .onFailure(error -> Console.log("Error while parsing json array from " + fetchUrl, error))
                            .onSuccess(podcastsJsonArray -> {
                                EntityStore.create(dataSourceModel).<Podcast>executeQuery(
                                                "select channelPodcastId from Podcast where date >= ? order by date limit ?", fetchAfterParameter, podcastsJsonArray.size()
                                        )
                                        .onFailure(e -> Console.log("Error while reading podcasts from database", e))
                                        .onSuccess(podcasts -> {

                                            UpdateStore updateStore = UpdateStore.createAbove(podcasts.getStore());
                                            LocalDateTime[] maxPodcastDate = { fetchAfterParameter };

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
                                                LocalDateTime dateTime = Dates.parseIsoLocalDateTime(podcastJson.getString("date"));
                                                if (dateTime.isAfter(maxPodcastDate[0]))
                                                    maxPodcastDate[0] = dateTime;
                                                p.setDate(LocalDate.from(dateTime));
                                                p.setImageUrl(cleanUrl(podcastJson.getString("episode_featured_image")));
                                                p.setAudioUrl(cleanUrl(podcastJson.getString("player_link")));
                                                try {
                                                    String durationText = podcastJson.getObject("meta").getString("duration");
                                                    Duration duration = Duration.between(LocalTime.MIN, LocalTime.parse(durationText));
                                                    p.setDurationMillis(duration.toMillis());
                                                } catch (Exception e) {
                                                    Console.log("WARNING: No or wrong duration for podcast " + id);
                                                }
                                            }

                                            if (!updateStore.hasChanges())
                                                Console.log("No new podcasts to import");
                                            else
                                                updateStore.submitChanges()
                                                        .onFailure(e -> Console.log("Error while inserting podcasts in database", e))
                                                        .onSuccess(insertBatch -> {
                                                            int newPodcastsCount = insertBatch.getArray().length;
                                                            Console.log(newPodcastsCount + " new podcasts imported in database");
                                                            fetchAfterParameter = maxPodcastDate[0];
                                                            importPodcasts();
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
