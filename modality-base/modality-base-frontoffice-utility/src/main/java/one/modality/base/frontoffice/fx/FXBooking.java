package one.modality.base.frontoffice.fx;


import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;

import java.util.Objects;
import java.util.stream.Collectors;

public class FXBooking {


    public final static ObservableList<Event> localCenterEvents = FXCollections.observableArrayList();
    public final static ObservableList<Event> nktEvents = FXCollections.observableArrayList();

    public final static Property<Organization> ownerLocalCenterProperty = new SimpleObjectProperty<>();

    static {
        localCenterEvents.addListener((ListChangeListener<Event>) change -> {
            System.out.println(localCenterEvents);
        });

        nktEvents.addListener((ListChangeListener<Event>) change -> {
            System.out.println(nktEvents);
        });
    }
}