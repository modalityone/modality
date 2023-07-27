package one.modality.base.frontoffice.fx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.shared.entities.Organization;

import java.util.Arrays;
import java.util.List;

public class FXApp {
    public static ObservableList<Center> centers = FXCollections.observableArrayList();
    public static final ObservableList<Organization> organizations = FXCollections.observableArrayList();
    public static final ObservableList<Center> centersRef = FXCollections.observableArrayList();
    public static DoubleProperty fontRatio = new SimpleDoubleProperty(1.0);
    public static IntegerProperty widthStage = new SimpleIntegerProperty(300);

    static {
        organizations.addListener((ListChangeListener<? super Organization>) change -> {
            organizations.forEach(o -> {
                List<String> toks = Arrays.asList(o.getName().split(" - "));
                String city = null;
                String name = null;

                if (toks.size() == 2) {
                    city = toks.get(1);
                    name = String.join(" ", toks);
                } else {
                    name = toks.get(0);
                }

                Center c = new Center(name, 0.0, 0.0, (String) o.evaluate("type.name"), city);
                c.organization = o;

                centers.add(c);
            });
        });
    }

}
