package one.modality.base.frontoffice.fx;


import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;

public class FXBooking {


    public final static ObservableList<Event> localCenterEvents = FXCollections.observableArrayList();
    public final static ObservableList<Event> nktEvents = FXCollections.observableArrayList();

    public final static Property<Organization> displayCenterProperty = new SimpleObjectProperty<>(null);
    public final static StringProperty countryProperty = new SimpleStringProperty("");
    public final static StringProperty cityProperty = new SimpleStringProperty("");
    public final static StringProperty keywordsSearchProperty = new SimpleStringProperty("");
    public final static StringProperty centerImageProperty = new SimpleStringProperty("");

    public final static BooleanProperty searchDisplayProperty = new SimpleBooleanProperty(false);

    static {
        localCenterEvents.addListener((ListChangeListener<Event>) change -> {
            System.out.println(localCenterEvents);
        });

        nktEvents.addListener((ListChangeListener<Event>) change -> {
            System.out.println(nktEvents);
        });
    }
}