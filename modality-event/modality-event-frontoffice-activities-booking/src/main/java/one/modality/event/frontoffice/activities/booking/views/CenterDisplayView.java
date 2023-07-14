package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.states.BookingPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.SvgUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class CenterDisplayView {
    public Node getView(VBox page, ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {
        if (FXAccount.ownerPM.PERSON == null) return new VBox();

        VBox container = new VBox();

        container.getChildren().clear();

        EntityButtonSelector<Country> countriesButtonSelector = new EntityButtonSelector<>(
                "{class:'country', orderBy:'name'}",
                factoryMixin, container, activityBase.getDataSourceModel()
        );

        countriesButtonSelector.selectedItemProperty().bindBidirectional(FXAccount.ownerPM.ADDRESS_COUNTRY);

        EntityButtonSelector<Organization> centersButtonSelector = new EntityButtonSelector<>(
                "{class:'organization', orderBy:'name'}",
                factoryMixin, container, activityBase.getDataSourceModel()
        );

        centersButtonSelector.ifNotNullOtherwiseEmpty(countriesButtonSelector.selectedItemProperty(), country -> where("country=?", country));

        centersButtonSelector.selectedItemProperty().bindBidirectional(FXAccount.ownerPM.LOCAL_CENTER);

        WebView mapView = new WebView();
        mapView.getEngine().load("https://maps.googleapis.com/maps/api/js?key=AIzaSyD7f9GOiTp2n4eIGqmUkN3U0RtKVhfsU8k&v=3.exp");

        Image mapImage = new Image("https://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=12&size=400x400&key=AIzaSyAihoCYFho8rqJwnBjxzBlk56SR0uL7_Ks", true);
        ImageView map = new ImageView(mapImage);

        FXBooking.centerImageProperty.addListener(change -> {
            map.setImage(new Image(FXBooking.centerImageProperty.get(), true));
        });


        map.setPreserveRatio(true);
        FXProperties.runNowAndOnPropertiesChange(() -> map.setFitWidth(container.getWidth() * 0.25), container.widthProperty());

        Label addressWeb = GeneralUtility.createLabel(Color.web(StyleUtility.RUPAVAJRA_WHITE), "manjushri.org", StyleUtility.SUB_TEXT_SIZE);
        addressWeb.graphicProperty().set(GeneralUtility.createSvgPath(SvgUtility.ADDRESS_WEB, StyleUtility.RUPAVAJRA_WHITE));

        Label addressLocation = GeneralUtility.createLabel(Color.web(StyleUtility.RUPAVAJRA_WHITE), "Manjushri Kadampa Meditation Centre Conishead Priory, Ulverston LA12 9QQ", StyleUtility.SUB_TEXT_SIZE);
        addressLocation.graphicProperty().set(GeneralUtility.createSvgPath(SvgUtility.ADDRESS_LOCATION, StyleUtility.RUPAVAJRA_WHITE));

        Label addressPhone = GeneralUtility.createLabel(Color.web(StyleUtility.RUPAVAJRA_WHITE), "+44 (0)1229 584029", StyleUtility.SUB_TEXT_SIZE);
        addressPhone.graphicProperty().set(GeneralUtility.createSvgPath(SvgUtility.ADDRESS_PHONE, StyleUtility.RUPAVAJRA_WHITE));

        Label addressEmail = GeneralUtility.createLabel(Color.web(StyleUtility.RUPAVAJRA_WHITE), "info@manjushri.org", StyleUtility.SUB_TEXT_SIZE);
        addressEmail.graphicProperty().set(GeneralUtility.createSvgPath(SvgUtility.ADDRESS_EMAIL, StyleUtility.RUPAVAJRA_WHITE));

        Node location = GeneralUtility.createSplitRow(
                map,
                GeneralUtility.createVList(5, 0,
                        addressWeb,
                        addressLocation,
                        addressPhone,
                        addressEmail
                ),50, 10
        );

        Text changeLocation = TextUtility.getText(BookingPM.CHANGE_CENTER.get() ? "Confirm change" : "Change your location", 10, StyleUtility.RUPAVAJRA_WHITE);
        changeLocation.setOnMouseClicked(event -> {
            if (BookingPM.CHANGE_CENTER.get()) FXAccount.updatePerson(FXAccount.ownerPM);
            BookingPM.CHANGE_CENTER.set(!BookingPM.CHANGE_CENTER.get());
        });
        changeLocation.setTextAlignment(TextAlignment.CENTER);

        Label localCenterText = GeneralUtility.createLabel(Color.web(StyleUtility.VICTOR_BATTLE_BLACK), FXAccount.ownerPM.LOCAL_CENTER.getValue().getName(), StyleUtility.MAIN_TEXT_SIZE);
        localCenterText.setTextAlignment(TextAlignment.CENTER);

        container.getChildren().addAll(
                TextUtility.getSubText("Your Country", StyleUtility.RUPAVAJRA_WHITE),
                BookingPM.CHANGE_CENTER.get() ? countriesButtonSelector.getButton() : GeneralUtility.createLabel(Color.web(StyleUtility.VICTOR_BATTLE_BLACK), FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName(), StyleUtility.MAIN_TEXT_SIZE),
                GeneralUtility.createSpace(20),
                TextUtility.getSubText("Your Local Dharma Center", StyleUtility.RUPAVAJRA_WHITE),
                BookingPM.CHANGE_CENTER.get() ? centersButtonSelector.getButton() : localCenterText,
                GeneralUtility.createSpace(20),
                location,
                GeneralUtility.createSpace(20),
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        container.maxWidthProperty().bind(page.widthProperty());

        return container;
    }
}
