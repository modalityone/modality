package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.fx.FXBooking;
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

        String centreStaticMapUrlTemplate = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.booking").getString("centreStaticMapUrl");
        ImageView centreStaticMapImageView = new ImageView();
        centreStaticMapImageView.setPreserveRatio(true);

        FXBooking.centerImageProperty.addListener(change -> {
            centreStaticMapImageView.setImage(new Image(FXBooking.centerImageProperty.get(), true));
        });

        Hyperlink centreWebsiteLink = GeneralUtility.setupLabeled(new Hyperlink(), "localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Label centreAddressLabel = GeneralUtility.createLabel("localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Label centrePhoneLabel = GeneralUtility.createLabel("localCentrePhone", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Label centreEmailLabel = GeneralUtility.createLabel("localCentreEmail", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

        FXProperties.runNowAndOnPropertiesChange(e -> {
            Organization organization = FXOrganization.getOrganization();
            if (organization != null) {
                organization.onExpressionLoaded("latitude,longitude,domainName,street,cityName,postCode,country.name,phone,email")
                    .onSuccess(ignored -> Platform.runLater(() -> {
                        String mapUrl = centreStaticMapUrlTemplate.replace("{latitude}", Float.toString(organization.getLatitude())).replace("{longitude}", Float.toString(organization.getLongitude()));
                        centreStaticMapImageView.setImage(new Image(mapUrl, true));
                        String domainName = organization.getStringFieldValue("domainName");
                        FXProperties.setEvenIfBound(centreWebsiteLink.textProperty(), domainName);
                        centreWebsiteLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://" + domainName));
                        FXProperties.setEvenIfBound(centreAddressLabel.textProperty(), (String) organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                        FXProperties.setEvenIfBound(centrePhoneLabel.textProperty(), organization.getStringFieldValue("phone"));
                        FXProperties.setEvenIfBound(centreEmailLabel.textProperty(), organization.getStringFieldValue("email"));
                    }));
            }
        }, FXOrganization.organizationProperty());

        Node location = GeneralUtility.createSplitRow(
                centreStaticMapImageView,
                GeneralUtility.createVList(10, 0,
                        centreWebsiteLink,
                        centreAddressLabel,
                        centrePhoneLabel,
                        centreEmailLabel
                ),50, 10
        );

        Text changeLocation = TextUtility.getText("Change your location", 10, StyleUtility.RUPAVAJRA_WHITE);
        changeLocation.setOnMouseClicked(event -> {
        });
        changeLocation.setTextAlignment(TextAlignment.CENTER);

        Label localCenterNameLabel = GeneralUtility.createLabel("localCentreName"/* FXAccount.ownerPM.LOCAL_CENTER.getValue().getName()*/, Color.web(StyleUtility.VICTOR_BATTLE_BLACK), StyleUtility.MAIN_TEXT_SIZE);
        localCenterNameLabel.setTextAlignment(TextAlignment.CENTER);

        Button button = centersButtonSelector.getButton();
        button.setMaxWidth(Region.USE_PREF_SIZE);
        ScalePane scalePane = new ScalePane(ScaleMode.FIT_WIDTH, button);
        scalePane.setMaxScale(2.5);
        VBox container = new VBox(20,
                //TextUtility.getSubText("Your Country", StyleUtility.RUPAVAJRA_WHITE),
                //BookingPM.CHANGE_CENTER.get() ? countriesButtonSelector.getButton() : GeneralUtility.createLabel(FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName(), Color.web(StyleUtility.VICTOR_BATTLE_BLACK), StyleUtility.MAIN_TEXT_SIZE),
                I18n.bindI18nProperties(TextUtility.getSubText(null, StyleUtility.RUPAVAJRA_WHITE), "yourLocalCentre"),
                scalePane,
                GeneralUtility.createSpace(10),
                location,
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        FXProperties.runNowAndOnPropertiesChange(() -> centreStaticMapImageView.setFitWidth(container.getWidth() * 0.25), container.widthProperty());

        return container;
    }
}
