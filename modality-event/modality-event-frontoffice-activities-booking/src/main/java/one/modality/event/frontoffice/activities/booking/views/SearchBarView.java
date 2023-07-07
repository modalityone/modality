package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.stack.orm.entity.EntityStore;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.event.frontoffice.activities.booking.BookingUtility;

public class SearchBarView {
    public Node getView() {
        VBox container = new VBox();

        StackPane sp = new StackPane();

        BorderPane searchContainer = new BorderPane();
        Button search = new Button("Search");
        searchContainer.setRight(search);
        searchContainer.setTop(GeneralUtility.createField("City", GeneralUtility.createBindedTextField(FXBooking.keywordsSearchProperty, -1)));

        search.setOnMouseClicked(e -> {
            FXBooking.countryProperty.set("United States");
            FXBooking.cityProperty.set("San Francisco");
            EntityStore store = FXAccount.ownerPM.PERSON.getStore();
            Organization organization = store.createEntity(Organization.class);
            organization.setName("My center");
            FXBooking.displayCenterProperty.setValue(organization);

            BookingUtility.cityAutoComplete(FXBooking.keywordsSearchProperty.get());
        });

        String locationString = FXBooking.countryProperty.get() + ", " + FXBooking.cityProperty.get();

        if (locationString.trim().equals(",")) locationString = "Please select a location";

        Text title = TextUtility.getText("Local Events", 16, StyleUtility.VICTOR_BATTLE_BLACK);
        Text location = TextUtility.getText(locationString, 8, StyleUtility.ELEMENT_GRAY);
        Text clear = TextUtility.getText("Clear filters", 8, StyleUtility.ELEMENT_GRAY);

        VBox titleContainer = GeneralUtility.createVList(0, 0, title, location);

        clear.setOnMouseClicked(e -> {
            FXBooking.cityProperty.set(FXAccount.ownerPM.ADDRESS_CITY.get());
            FXBooking.countryProperty.set(FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName());
            FXBooking.displayCenterProperty.setValue(FXAccount.ownerPM.LOCAL_CENTER.getValue());
        });

        titleContainer.setAlignment(Pos.CENTER);
        container.setAlignment(Pos.CENTER);

        sp.getChildren().addAll(
                titleContainer,
                searchContainer
        );

        container.getChildren().addAll(sp, clear);

        return container;
    }
}
