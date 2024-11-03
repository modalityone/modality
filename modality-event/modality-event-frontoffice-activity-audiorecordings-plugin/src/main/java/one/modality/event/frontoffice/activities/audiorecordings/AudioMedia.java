package one.modality.event.frontoffice.activities.audiorecordings;

import one.modality.base.shared.entities.markers.HasAudioUrl;
import one.modality.base.shared.entities.markers.HasMediaInfo;

import java.time.LocalDateTime;

public class AudioMedia implements HasMediaInfo, HasAudioUrl {

    private String url = "";
    private String title = "";
    private String excerpt = "";
    private String imageUrl = "";
    private Long durationMillis = 0L;
    private String lang = "";
    private LocalDateTime date;

    @Override
    public void setAudioUrl(String audioUrl) {
        url = audioUrl;
    }

    @Override
    public String getAudioUrl() {
        return url;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    @Override
    public String getExcerpt() {
        return excerpt;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public Long getDurationMillis() {
        return durationMillis;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public LocalDateTime getDate() {
        return date;
    }
}
