package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

public final class CenterDisplayView {

    public Node getView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {

        String organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC` and exists(select Event where organization=o and endDate > now())', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC` and exists(select Event where organization=o and endDate > now())', orderBy: 'country.name,name', columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }";
        EntityButtonSelector<Organization> centersButtonSelector = new EntityButtonSelector<>(
                organizationJson, factoryMixin, FXMainFrameDialogArea::getDialogArea, activityBase.getDataSourceModel()
        );

        centersButtonSelector.selectedItemProperty().bindBidirectional(FXOrganization.organizationProperty());


        Hyperlink centreWebsiteLink = GeneralUtility.setupLabeled(new Hyperlink(), "localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centreAddressLink = GeneralUtility.setupLabeled(new Hyperlink(),"localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centrePhoneLink = GeneralUtility.setupLabeled(new Hyperlink(), "localCentrePhone", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centreEmailLabel = GeneralUtility.setupLabeled(new Hyperlink(), "localCentreEmail", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

        MapView organizationMapView = new MapView();
        organizationMapView.entityProperty().bind(FXOrganization.organizationProperty());

        FXProperties.runNowAndOnPropertiesChange(e -> {
            Organization organization = FXOrganization.getOrganization();
            if (organization != null) {
                organization.onExpressionLoaded("domainName,latitude,longitude,street,cityName,postCode,country.name,phone,email")
                    .onSuccess(ignored -> Platform.runLater(() -> {
                        String domainName = organization.getStringFieldValue("domainName");
                        FXProperties.setEvenIfBound(centreWebsiteLink.textProperty(), domainName);
                        centreWebsiteLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://" + domainName));
                        FXProperties.setEvenIfBound(centreAddressLink.textProperty(), (String) organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                        centreAddressLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://google.com/maps/search/kadampa/@" + organization.getLatitude() + "," + organization.getLongitude() + ",12z"));
                        String phone = organization.getStringFieldValue("phone");
                        FXProperties.setEvenIfBound(centrePhoneLink.textProperty(), phone);
                        centrePhoneLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("tel://" + phone));
                        String email = organization.getStringFieldValue("email");
                        FXProperties.setEvenIfBound(centreEmailLabel.textProperty(), email);
                        centreEmailLabel.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("mailto://" + email));
                    }));
            }
        }, FXOrganization.organizationProperty());

        VBox centreInfoBox = GeneralUtility.createVList(10, 0,
                centreWebsiteLink,
                centreAddressLink,
                centrePhoneLink,
                centreEmailLabel
        );
        centreInfoBox.setAlignment(Pos.CENTER_LEFT);
        centreInfoBox.setPadding(new Insets(0, 0, 0, 35));

        Node location = new ColumnsPane(
                organizationMapView.buildMapNode(),
                centreInfoBox
        );

        Text changeLocation = TextUtility.getText("Change your location", 10, StyleUtility.RUPAVAJRA_WHITE);
        changeLocation.setOnMouseClicked(event -> {
        });
        changeLocation.setTextAlignment(TextAlignment.CENTER);

        Label localCenterNameLabel = GeneralUtility.createLabel("localCentreName"/* FXAccount.ownerPM.LOCAL_CENTER.getValue().getName()*/, Color.web(StyleUtility.VICTOR_BATTLE_BLACK), StyleUtility.MAIN_TEXT_SIZE);
        localCenterNameLabel.setTextAlignment(TextAlignment.CENTER);

        Button centerButton = centersButtonSelector.getButton();
        centerButton.setMaxWidth(Region.USE_PREF_SIZE);
        ScalePane centerScalePane = new ScalePane(ScaleMode.FIT_WIDTH, centerButton);
        centerScalePane.setMaxScale(2.5);

        VBox container = new VBox(20,
                I18n.bindI18nProperties(TextUtility.getSubText(null, StyleUtility.RUPAVAJRA_WHITE), "yourLocalCentre"),
                centerScalePane,
                GeneralUtility.createSpace(10),
                location,
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        return container;
    }
}
