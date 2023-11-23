package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

public final class CenterDisplayView {

    private static final String MARKER_SVG_PATH = "m 8.0408158,27.932173 c 0.2040781,-3.908096 2.3550612,-10.256967 5.4754162,-14.250776 1.699971,-2.177513 2.48363,-4.2978848 2.48363,-5.5856178 A 8.0488411,8.0488411 0 0 0 8.0000002,2.1599999e-7 v 0 A 8.0488411,8.0488411 0 0 0 1.378808e-4,8.0957792 c 0,1.287733 0.7816191992,3.4081048 2.4836307192,5.5856178 3.1203545,3.99585 5.2754194,10.34268 5.475416,14.250776 z";
    private static final String ZOOM_PLUS_SVG_PATH = "M 13,8 H 3 M 8,13 V 3";
    private static final String ZOOM_MINUS_SVG_PATH = "M 13,8 H 3";
    private static final int ZOOM_MIN = 5, ZOOM_MAX = 18, ZOOM_INITIAL = 12;

    private final IntegerProperty zoomProperty = new SimpleIntegerProperty(ZOOM_INITIAL);
    private final Button zoomInButton = createZoomButton(ZOOM_PLUS_SVG_PATH);
    private final Button zoomOutButton = createZoomButton(ZOOM_MINUS_SVG_PATH);

    public Node getView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {

        String organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC` and exists(select Event where organization=o and endDate > now())', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC` and exists(select Event where organization=o and endDate > now())', orderBy: 'country.name,name', columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }";
        EntityButtonSelector<Organization> centersButtonSelector = new EntityButtonSelector<>(
                organizationJson, factoryMixin, FXMainFrameDialogArea::getDialogArea, activityBase.getDataSourceModel()
        );

        centersButtonSelector.selectedItemProperty().bindBidirectional(FXOrganization.organizationProperty());

        ImageView centreStaticMapImageView = new ImageView();
        centreStaticMapImageView.setPreserveRatio(true);

        Hyperlink centreWebsiteLink = GeneralUtility.setupLabeled(new Hyperlink(), "localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centreAddressLink = GeneralUtility.setupLabeled(new Hyperlink(),"localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centrePhoneLink = GeneralUtility.setupLabeled(new Hyperlink(), "localCentrePhone", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink centreEmailLabel = GeneralUtility.setupLabeled(new Hyperlink(), "localCentreEmail", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

        FXProperties.runNowAndOnPropertiesChange(e -> {
            Organization organization = FXOrganization.getOrganization();
            if (organization != null) {
                organization.onExpressionLoaded("domainName,latitude,longitude,street,cityName,postCode,country.name,phone,email")
                    .onSuccess(ignored -> Platform.runLater(() -> {
                        Config webConfig = SourcesConfig.getSourcesRootConfig().childConfigAt("webfx.stack.com.client.websocket");
                        String serverHost = webConfig.getString("serverHost");
                        boolean serverSSL = webConfig.getBoolean("serverSSL");
                        String mapUrl = (serverSSL ? "https://" : "http://") + serverHost + "/map/organization/" + organization.getPrimaryKey() + "?zoom=" + zoomProperty.get();
                        Image image = new Image(mapUrl, true);
                        image.progressProperty().addListener(observable -> {
                            if (image.getProgress() >= 1)
                                centreStaticMapImageView.setImage(image);
                        });
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
        }, FXOrganization.organizationProperty(), zoomProperty);

        VBox centreInfoBox = GeneralUtility.createVList(10, 0,
                centreWebsiteLink,
                centreAddressLink,
                centrePhoneLink,
                centreEmailLabel
        );
        centreInfoBox.setAlignment(Pos.CENTER_LEFT);
        centreInfoBox.setPadding(new Insets(0, 0, 0, 35));

        zoomInButton.setOnAction(e -> incrementZoom( +1));
        zoomOutButton.setOnAction(e -> incrementZoom(-1));
        VBox zoomBar = new VBox(5, zoomInButton, zoomOutButton);
        zoomBar.setAlignment(Pos.BOTTOM_RIGHT);
        zoomBar.setPadding(new Insets(5));

        SVGPath markerSvgPath = new SVGPath();
        markerSvgPath.setContent(MARKER_SVG_PATH);
        markerSvgPath.setFill(Color.web("#EA4335"));
        markerSvgPath.setStroke(Color.web("#DA352D"));
        // We want the bottom tip of the marker to be in the center of the map. The SVG is 16px high, so we need to move
        // it up by 8px (otherwise this will be the point at y = 8px in SVG that will be in the center).
        markerSvgPath.setTranslateY(-8);

        // We add a little white circle on top of the red marker
        Circle markerCircle = new Circle(3.5, Color.WHITE);
        markerCircle.setTranslateY(-14); // Moving it up to the right position

        Node location = new ColumnsPane(
                new StackPane(new ScalePane(ScaleMode.FIT_WIDTH, centreStaticMapImageView), zoomBar, markerSvgPath, markerCircle),
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

        FXProperties.runNowAndOnPropertiesChange(() -> centreStaticMapImageView.setFitWidth(container.getWidth() * 0.25), container.widthProperty());

        return container;
    }

    private void incrementZoom(int deltaZoom) {
        int zoomValue = zoomProperty.get() + deltaZoom;
        zoomValue = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoomValue));
        zoomProperty.set(zoomValue);
        zoomInButton.setDisable(zoomValue == ZOOM_MAX);
        zoomOutButton.setDisable(zoomValue == ZOOM_MIN);
    }

    private static Button createZoomButton(String path) {
        Button b = new Button();
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(path);
        svgPath.setStroke(Color.BLACK);
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        b.setGraphic(svgPath);
        b.setMinSize(24, 24); // For the web version, otherwise the minus button is not square (because minus SVG is not square)
        return b;
    }
}
