package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.i18n.I18n;
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
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.states.BookingPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Organization;

public final class CenterDisplayView {

    public Node getView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {

        EntityButtonSelector<Organization> centersButtonSelector = new EntityButtonSelector<>(
                "{class: 'organization', orderBy: 'name'}",
                factoryMixin, FXMainFrameDialogArea.getDialogArea(), activityBase.getDataSourceModel()
        );

        centersButtonSelector.selectedItemProperty().bindBidirectional(FXAccount.ownerPM.LOCAL_CENTER);

        String staticMapUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.booking").getString("centreStaticMapUrl");
        ImageView map = ImageStore.createImageView(staticMapUrl);
        map.setPreserveRatio(true);

        FXBooking.centerImageProperty.addListener(change -> {
            map.setImage(new Image(FXBooking.centerImageProperty.get(), true));
        });

        Node location = GeneralUtility.createSplitRow(
                map,
                GeneralUtility.createVList(5, 0,
                        GeneralUtility.createLabel("localCentreWebsite", Color.WHITE, StyleUtility.SUB_TEXT_SIZE),
                        GeneralUtility.createLabel("localCentreAddress", Color.WHITE, StyleUtility.SUB_TEXT_SIZE),
                        GeneralUtility.createLabel("localCentrePhone", Color.WHITE, StyleUtility.SUB_TEXT_SIZE),
                        GeneralUtility.createLabel("localCentreEmail", Color.WHITE, StyleUtility.SUB_TEXT_SIZE)
                ),50, 10
        );

        Text changeLocation = TextUtility.getText(BookingPM.CHANGE_CENTER.get() ? "Confirm change" : "Change your location", 10, StyleUtility.RUPAVAJRA_WHITE);
        changeLocation.setOnMouseClicked(event -> {
            if (BookingPM.CHANGE_CENTER.get()) FXAccount.updatePerson(FXAccount.ownerPM);
            BookingPM.CHANGE_CENTER.set(!BookingPM.CHANGE_CENTER.get());
        });
        changeLocation.setTextAlignment(TextAlignment.CENTER);

        Label localCenterNameLabel = GeneralUtility.createLabel("localCentreName"/* FXAccount.ownerPM.LOCAL_CENTER.getValue().getName()*/, Color.web(StyleUtility.VICTOR_BATTLE_BLACK), StyleUtility.MAIN_TEXT_SIZE);
        localCenterNameLabel.setTextAlignment(TextAlignment.CENTER);

        VBox container = new VBox(
                //TextUtility.getSubText("Your Country", StyleUtility.RUPAVAJRA_WHITE),
                //BookingPM.CHANGE_CENTER.get() ? countriesButtonSelector.getButton() : GeneralUtility.createLabel(FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName(), Color.web(StyleUtility.VICTOR_BATTLE_BLACK), StyleUtility.MAIN_TEXT_SIZE),
                GeneralUtility.createSpace(20),
                I18n.bindI18nProperties(TextUtility.getSubText(null, StyleUtility.RUPAVAJRA_WHITE), "yourLocalCentre"),
                BookingPM.CHANGE_CENTER.get() ? centersButtonSelector.getButton() : localCenterNameLabel,
                GeneralUtility.createSpace(20),
                location,
                GeneralUtility.createSpace(20),
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        FXProperties.runNowAndOnPropertiesChange(() -> map.setFitWidth(container.getWidth() * 0.25), container.widthProperty());

        return container;
    }
}
