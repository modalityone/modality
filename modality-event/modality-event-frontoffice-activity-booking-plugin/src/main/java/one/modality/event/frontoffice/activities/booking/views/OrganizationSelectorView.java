package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebView;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.util.stream.Collectors;

public final class OrganizationSelectorView {

    private final static String PLAY_SVG = "M 24.2625,45.374999 V 18.625 L 47,32 Z M 32,64 C 14.355,64 0,49.644751 0,32 0,14.355 14.355,0 32,0 49.644751,0 64,14.355 64,32 64,49.644751 49.644751,64 32,64 Z M 32,6.5 C 17.939251,6.5 6.5,17.939251 6.5,32 6.5,46.060751 17.939251,57.5 32,57.5 46.060751,57.5 57.5,46.060751 57.5,32 57.5,17.939251 46.060751,6.5 32,6.5 Z";

    private final ButtonFactoryMixin factoryMixin;
    private final ViewDomainActivityBase activityBase;
    private final FlipPane flipPane = new FlipPane();
    private final MapView organizationMapView = new StaticMapView(0, 20, 10);
    private final MonoPane presentationPane = new MonoPane();
    private final WebView presentationVideoView = new WebView();
    private final Hyperlink websiteLink = GeneralUtility.createHyperlink("localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink addressLink = GeneralUtility.createHyperlink("localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink phoneLink   = GeneralUtility.createHyperlink("localCentrePhone",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink emailLink   = GeneralUtility.createHyperlink("localCentreEmail",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

    public OrganizationSelectorView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {
        this.factoryMixin = factoryMixin;
        this.activityBase = activityBase;

        organizationMapView.placeEntityProperty().bind(FXOrganization.organizationProperty());

        presentationPane.managedProperty().bind(presentationPane.visibleProperty());
        websiteLink.managedProperty().bind(websiteLink.visibleProperty());
        addressLink.managedProperty().bind(addressLink.visibleProperty());
        phoneLink.managedProperty().bind(phoneLink.visibleProperty());
        emailLink.managedProperty().bind(emailLink.visibleProperty());

        presentationPane.widthProperty().addListener(observable -> presentationPane.setMaxHeight(presentationPane.getWidth() / (16d / 9)));

        FXProperties.runNowAndOnPropertiesChange(e -> updateFromOrganization(), FXOrganization.organizationProperty());
    }

    public Node getView() {
        flipToFrontOrganization();
        //flipToBackWordMap();
        return flipPane;
    }

    private Node getOrganizationView() {
        String organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = organizationJson.replace("}", ", columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }");
        EntityButtonSelector<Organization> organizationButtonSelector = new EntityButtonSelector<>(
                organizationJson, factoryMixin, FXMainFrameDialogArea::getDialogArea, activityBase.getDataSourceModel()
        );
        organizationButtonSelector.selectedItemProperty().bindBidirectional(FXOrganization.organizationProperty());
        Button organizationButton = organizationButtonSelector.getButton();
        organizationButton.setMaxWidth(Region.USE_PREF_SIZE);
        ScalePane organizationButtonScalePane = new ScalePane(ScaleMode.FIT_WIDTH, organizationButton);
        organizationButtonScalePane.setMaxScale(2.5);

        VBox contactBox = GeneralUtility.createVList(10, 0,
                presentationPane,
                websiteLink,
                addressLink,
                phoneLink,
                emailLink
        );
        contactBox.setAlignment(Pos.CENTER_LEFT);
        contactBox.setPadding(new Insets(0, 0, 0, 35));

        Node mapAndContactPane = new ColumnsPane(organizationMapView.buildMapNode(), contactBox);

        Hyperlink changeLocation = GeneralUtility.createHyperlink("Change your location", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        changeLocation.setOnMouseClicked(event -> flipToBackWordMap());

        VBox container = new VBox(20,
                I18n.bindI18nProperties(TextUtility.getSubText(null, StyleUtility.RUPAVAJRA_WHITE), "yourLocalCentre"),
                organizationButtonScalePane,
                GeneralUtility.createSpace(10),
                mapAndContactPane,
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        return container;
    }

    public void onResume() {
        if (UserAgent.isBrowser()) {
            // The issue with the browser is that it completely reloads the iFrame (= HTML mapping for WebView) when
            // reintroduced to the DOM, so the thumb video is appearing with the YouTube play button when the user
            // navigates back to this page. But we prefer in that case to display the organization presentation image
            // with our own video play button.
            updateFromOrganization(); // Will set back the image & play button
        }
    }

    private void updateFromOrganization() {
        Organization organization = FXOrganization.getOrganization();
        if (organization != null) {
            organization.onExpressionLoaded("domainName,latitude,longitude,street,cityName,postCode,country.(name,iso_alpha2,latitude,longitude,north,south,east,west),phone,email")
                    .onSuccess(ignored -> Platform.runLater(() -> {
                        Integer organizationId = Numbers.toInteger(organization.getPrimaryKey());
                        String imageLink = // Temporarily hardcoded
                                // Manjushri KMC
                                organizationId == 151 ? "/images/organizations/ManjushriKMC.png"
                                        // KMC France
                                        : organizationId == 2 ? "/images/organizations/KMCFrance.png"
                                        : null;
                        String videoLink = // Temporarily hardcoded
                                // Manjushri KMC
                                organizationId == 151 ? "https://fast.wistia.net/embed/iframe/z3pqhk7lhs" // "https://www.youtube.com/embed/jwptdnO_f-I?rel=0"
                                        // KMC France
                                        : organizationId == 2 ? "https://www.youtube.com/embed/alIoC9_oD5w?rel=0"
                                        : null;
                        presentationPane.setVisible(imageLink != null || videoLink != null);
                        if (videoLink == null)
                            presentationVideoView.getEngine().load(null); // Unloading possible previous video (stops the video if playing)
                        if (imageLink != null) {
                            ImageView imageView = new ImageView(new Image(imageLink, true));
                            ScalePane scalePane = new ScalePane(ScaleMode.BEST_ZOOM, imageView);
                            presentationPane.setContent(scalePane);
                            if (videoLink != null) {
                                SVGPath playSvgPath = new SVGPath();
                                playSvgPath.setContent(PLAY_SVG);
                                playSvgPath.setFill(Color.WHITE);
                                StackPane stackPane = new StackPane();
                                stackPane.setCursor(Cursor.HAND);
                                presentationPane.setContent(stackPane);
                                boolean isGluon = UserAgent.isNative();
                                if (!isGluon) { // ex: desktop JRE & browser
                                    presentationVideoView.getEngine().load(videoLink);
                                    stackPane.getChildren().setAll(presentationVideoView, scalePane, playSvgPath);
                                    scalePane.setMouseTransparent(true);
                                    playSvgPath.setMouseTransparent(true);
                                    scalePane.setVisible(true);
                                    playSvgPath.setVisible(true);
                                    SceneUtil.runOnceFocusIsInside(presentationVideoView, false, () -> {
                                        scalePane.setVisible(false);
                                        playSvgPath.setVisible(false);
                                    });
                                } else { // ex: mobile app
                                    stackPane.getChildren().setAll(scalePane, playSvgPath);
                                    stackPane.setOnMouseClicked(e -> {
                                        presentationVideoView.getEngine().load(videoLink);
                                        presentationPane.setContent(presentationVideoView);
                                    });
                                }
                            }
                        } else if (videoLink != null) {
                            presentationVideoView.getEngine().load(videoLink);
                            presentationPane.setContent(presentationVideoView);
                        }
                        String domainName = organization.getStringFieldValue("domainName");
                        websiteLink.setVisible(domainName != null);
                        FXProperties.setEvenIfBound(websiteLink.textProperty(), domainName);
                        websiteLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://" + domainName));
                        FXProperties.setEvenIfBound(addressLink.textProperty(), (String) organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                        Float latitude = organization.getLatitude();
                        Float longitude = organization.getLongitude();
                        if (latitude == null || longitude == null) {
                            addressLink.setOnAction(null);
                            organizationMapView.setMapCenter(null);
                            organizationMapView.getMarkers().clear();
                        } else {
                            addressLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://google.com/maps/search/kadampa/@" + latitude + "," + longitude + ",12z"));
                            organizationMapView.setMapCenter(latitude, longitude);
                            organizationMapView.getMarkers().setAll(new MapMarker(organization));
                        }
                        String phone = organization.getStringFieldValue("phone");
                        phoneLink.setVisible(phone != null);
                        FXProperties.setEvenIfBound(phoneLink.textProperty(), phone);
                        phoneLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("tel://" + phone));
                        String email = organization.getStringFieldValue("email");
                        emailLink.setVisible(email != null);
                        FXProperties.setEvenIfBound(emailLink.textProperty(), email);
                        emailLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("mailto://" + email));
                        FXCountry.setCountry(organization.getCountry());
                    }));
        }
    }

    private Node getChangeLocationView() {

        MapView countryMapView = new DynamicMapView();
        countryMapView.placeEntityProperty().bind(FXCountry.countryProperty());
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Country country = FXCountry.getCountry();
            if (country != null) {
                Float latitude = country.getLatitude();
                Float longitude = country.getLongitude();
                if (latitude != null && longitude != null)
                    countryMapView.setMapCenter(latitude, longitude);
            }
        }, FXCountry.countryProperty());

        Hyperlink backLink = GeneralUtility.createHyperlink("Back", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        backLink.setOnAction(e -> flipToFrontOrganization());

        VBox container = new VBox(20,
                countryMapView.buildMapNode(),
                backLink
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        FXProperties.runNowAndOnPropertiesChange(() ->
                recreateOrganizationMarkers(countryMapView), FXCountry.countryProperty());

        ObservableLists.runNowAndOnListChange(x ->
                recreateOrganizationMarkers(countryMapView), FXOrganizations.organizations());

        return container;
    }

    private void recreateOrganizationMarkers(MapView mapView) {
        mapView.getMarkers().setAll(
                FXOrganizations.organizations().stream()
                        .filter(o -> o.getLatitude() != null && o.getLongitude() != null)
                        .map(this::createOrganizationMarker)
                        .collect(Collectors.toList()));
    }

    private MapMarker createOrganizationMarker(Organization o) {
        MapMarker marker = new MapMarker(o);
        marker.setOnAction(() -> flipToFrontOrganization(o));
        if (!Entities.sameId(o.getCountry(), FXCountry.getCountry())) {
            marker.setColours(Color.ORANGE, Color.DARKORANGE);
        }
        return marker;
    }

    private void flipToFrontOrganization(Organization o) {
        FXOrganization.setOrganization(o);
        flipToFrontOrganization();
    }

    private final static boolean INVERT_FLIP = false;

    private void flipToFrontOrganization() {
        if (INVERT_FLIP) {
            if (flipPane.getBack() == null)
                flipPane.setBack(getOrganizationView());
            flipPane.flipToBack();
        } else {
            if (flipPane.getFront() == null)
                flipPane.setFront(getOrganizationView());
            flipPane.flipToFront();
        }
    }

    private void flipToBackWordMap() {
        if (INVERT_FLIP) {
            if (flipPane.getFront() == null)
                flipPane.setFront(getChangeLocationView());
            flipPane.flipToFront();
        } else {
            if (flipPane.getBack() == null)
                flipPane.setBack(getChangeLocationView());
            flipPane.flipToBack();
        }
    }
}