package one.modality.base.frontoffice.fx;


import javafx.beans.property.*;
import one.modality.base.shared.entities.Organization;

public class FXBooking {

    public final static Property<Organization> displayCenterProperty = new SimpleObjectProperty<>(null);
    public final static StringProperty countryProperty = new SimpleStringProperty("");
    public final static StringProperty cityProperty = new SimpleStringProperty("");
    public final static StringProperty keywordsSearchProperty = new SimpleStringProperty("");
    public final static StringProperty centerImageProperty = new SimpleStringProperty("");

    public final static BooleanProperty searchDisplayProperty = new SimpleBooleanProperty(false);

}