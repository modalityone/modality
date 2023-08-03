package one.modality.base.frontoffice.fx;


import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;

import java.util.stream.Collectors;

public class FXBooking {

    public final static Property<Organization> displayCenterProperty = new SimpleObjectProperty<>(null);
    public final static StringProperty countryProperty = new SimpleStringProperty("");
    public final static StringProperty cityProperty = new SimpleStringProperty("");
    public final static StringProperty keywordsSearchProperty = new SimpleStringProperty("");
    public final static StringProperty centerImageProperty = new SimpleStringProperty("");

    public final static BooleanProperty searchDisplayProperty = new SimpleBooleanProperty(false);

}