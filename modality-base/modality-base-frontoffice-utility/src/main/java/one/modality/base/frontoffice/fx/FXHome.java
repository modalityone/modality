package one.modality.base.frontoffice.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.MediaPlayer;
import one.modality.base.shared.entities.News;
import one.modality.base.shared.entities.Podcast;

public class FXHome {
    public static final ObservableList<one.modality.base.shared.entities.News> news = FXCollections.observableArrayList();
    public static final ObservableList<Podcast> podcasts = FXCollections.observableArrayList();
    public static MediaPlayer player;
    public static ObjectProperty<News> article = new SimpleObjectProperty<>();
}
