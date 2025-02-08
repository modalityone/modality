package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import one.modality.base.client.css.Fonts;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.browser.BrowserUtil;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.fx.FXCountry;
import one.modality.event.frontoffice.activities.booking.fx.FXOrganizations;
import one.modality.event.frontoffice.activities.booking.map.DynamicMapView;
import one.modality.event.frontoffice.activities.booking.map.MapMarker;
import one.modality.event.frontoffice.activities.booking.map.MapView;
import one.modality.event.frontoffice.activities.booking.map.StaticMapView;

import java.util.stream.Collectors;

public final class OrganizationSelectorView {

    private final static String PLAY_VIDEO_TRIANGLE_SVG = "M 24.2625,45.374999 V 18.625 L 47,32 Z M 32,64 C 14.355,64 0,49.644751 0,32 0,14.355 14.355,0 32,0 49.644751,0 64,14.355 64,32 64,49.644751 49.644751,64 32,64 Z M 32,6.5 C 17.939251,6.5 6.5,17.939251 6.5,32 6.5,46.060751 17.939251,57.5 32,57.5 46.060751,57.5 57.5,46.060751 57.5,32 57.5,17.939251 46.060751,6.5 32,6.5 Z";

    private final ButtonFactoryMixin factoryMixin;
    private final ViewDomainActivityBase activityBase;
    private final FlipPane flipPane = new FlipPane(true);
    private final MapView organizationMapView = new StaticMapView(10);
    private MapView worldMapView;
    private final AspectRatioPane presentationPane = new AspectRatioPane(16d / 9);
    private WebView presentationVideoView; // new WebView() can raise "Not on FX application thread" if this is the first page loaded
    private final Hyperlink websiteLink = GeneralUtility.createHyperlink(BookingI18nKeys.localCentreWebsite, Color.WHITE);
    private final Hyperlink addressLink = GeneralUtility.createHyperlink(BookingI18nKeys.localCentreAddress, Color.WHITE);
    private final Hyperlink phoneLink = GeneralUtility.createHyperlink(BookingI18nKeys.localCentrePhone, Color.WHITE);
    private final Hyperlink emailLink = GeneralUtility.createHyperlink(BookingI18nKeys.localCentreEmail, Color.WHITE);

    public OrganizationSelectorView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {
        this.factoryMixin = factoryMixin;
        this.activityBase = activityBase;

        Layouts.bindAllManagedToVisibleProperty(presentationPane, websiteLink, addressLink, phoneLink, emailLink);

        organizationMapView.placeEntityProperty().bind(FXOrganization.organizationProperty());
        FXProperties.runNowAndOnPropertyChange(this::updateFromOrganization, FXOrganization.organizationProperty());
    }

    public Node getView() {
        // If an organization has already been selected from previous visits, we display that organization
        if (FXOrganizationId.getOrganizationId() != null)
            flipToFrontOrganization();
        else // otherwise we display the world map, so the user can select its organization
            flipToBackWordMap();
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
            websiteLink,
            addressLink,
            phoneLink,
            emailLink
        );

        Hyperlink changeLocation = GeneralUtility.createHyperlink(BookingI18nKeys.ChangeYourLocation, Color.WHITE);
        GeneralUtility.onNodeClickedWithoutScroll(event -> flipToBackWordMap(), changeLocation);

        Node organizationMapNode = organizationMapView.getMapNode();
        return OrangeFrame.createOrangeFrame(
            BookingI18nKeys.yourLocalCentre,
            new LayoutPane(organizationButtonScalePane, organizationMapNode, presentationPane, contactBox) {
                @Override
                protected void layoutChildren(double width, double height) {
                    double x = 0, y = 0, w = width, h = organizationButtonScalePane.prefHeight(width);
                    layoutInArea(organizationButtonScalePane, x, y, w, h, Pos.CENTER);
                    double space = Math.min(35, width * 0.03);
                    y += h + space;
                    if (organizationMapNode.isVisible()) {
                        w = width / 2;
                        layoutInArea(organizationMapNode, x, y, w, height - y, Pos.TOP_CENTER);
                        x = w + space;
                        w = width - x;
                        contactBox.setAlignment(Pos.CENTER_LEFT);
                        double fontFactor = GeneralUtility.computeFontFactor(w);
                        GeneralUtility.setLabeledFont(websiteLink, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 16);
                        GeneralUtility.setLabeledFont(addressLink, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 16);
                        GeneralUtility.setLabeledFont(phoneLink, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 16);
                        GeneralUtility.setLabeledFont(emailLink, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 16);
                    } else
                        contactBox.setAlignment(Pos.CENTER);
                    if (presentationPane.isVisible()) {
                        h = presentationPane.prefHeight(w);
                        layoutInArea(presentationPane, x, y, w, h, Pos.TOP_CENTER);
                        y += h + 10;
                    }
                    layoutInArea(contactBox, x, y, w, height - y, Pos.CENTER);
                }
            },
            changeLocation);
    }

    public void onResume() {
        BrowserUtil.setUiRouter(activityBase.getUiRouter());
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
                        organizationId == 151 ? "https://fast.wistia.net/embed/iframe/z3pqhk7lhs?playerColor=EE7130" // "https://www.youtube.com/embed/jwptdnO_f-I?rel=0"
                            // KMC France
                            : organizationId == 2 ? "https://www.youtube.com/embed/alIoC9_oD5w?rel=0"
                            : null;
                    presentationPane.setVisible(imageLink != null || videoLink != null);
                    if (presentationVideoView == null)
                        presentationVideoView = new WebView();
                    if (videoLink == null)
                        presentationVideoView.getEngine().load(null); // Unloading possible previous video (stops the video if playing)
                    if (imageLink != null) {
                        ImageView imageView = new ImageView(new Image(imageLink, true));
                        ScalePane scalePane = new ScalePane(ScaleMode.BEST_ZOOM, imageView);
                        presentationPane.setContent(scalePane);
                        if (videoLink != null) {
                            SVGPath playSvgPath = new SVGPath();
                            playSvgPath.setContent(PLAY_VIDEO_TRIANGLE_SVG);
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
                                GeneralUtility.onNodeClickedWithoutScroll(e -> {
                                    presentationVideoView.getEngine().load(videoLink);
                                    presentationPane.setContent(presentationVideoView);
                                }, stackPane);
                            }
                        }
                    } else if (videoLink != null) {
                        presentationVideoView.getEngine().load(videoLink);
                        presentationPane.setContent(presentationVideoView);
                    }
                    String domainName = organization.getStringFieldValue("domainName");
                    websiteLink.setVisible(domainName != null);
                    FXProperties.setEvenIfBound(websiteLink.textProperty(), domainName);
                    GeneralUtility.onNodeClickedWithoutScroll(e2 ->
                        BrowserUtil.chooseHowToOpenWebsite("https://" + domainName), websiteLink);
                    FXProperties.setEvenIfBound(addressLink.textProperty(), organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                    Float latitude = organization.getLatitude();
                    Float longitude = organization.getLongitude();
                    if (latitude == null || longitude == null) {
                        GeneralUtility.onNodeClickedWithoutScroll(null, addressLink);
                        organizationMapView.setMapCenter(null);
                        organizationMapView.getMarkers().clear();
                        organizationMapView.getMapNode().setVisible(false);
                    } else {
                        GeneralUtility.onNodeClickedWithoutScroll(e2 ->
                                BrowserUtil.openExternalBrowser("https://google.com/maps/search/kadampa/@" + latitude + "," + longitude + ",12z")
                            , addressLink);
                        organizationMapView.setMapCenter(latitude, longitude);
                        organizationMapView.getMarkers().setAll(new MapMarker(organization));
                        organizationMapView.getMapNode().setVisible(true);
                    }
                    String phone = organization.getStringFieldValue("phone");
                    phoneLink.setVisible(phone != null);
                    FXProperties.setEvenIfBound(phoneLink.textProperty(), phone);
                    GeneralUtility.onNodeClickedWithoutScroll(e2 ->
                        BrowserUtil.openExternalBrowser("tel:" + phone), phoneLink);
                    String email = organization.getStringFieldValue("email");
                    emailLink.setVisible(email != null);
                    FXProperties.setEvenIfBound(emailLink.textProperty(), email);
                    GeneralUtility.onNodeClickedWithoutScroll(e2 ->
                        BrowserUtil.openExternalBrowser("mailto:" + email), emailLink);
                    FXCountry.setCountry(organization.getCountry());
                }));
        }
    }

    private Node getChangeLocationView() {
        worldMapView = new DynamicMapView();
        worldMapView.placeEntityProperty().bind(FXCountry.countryProperty());

        Hyperlink backLink = GeneralUtility.createHyperlink(ModalityI18nKeys.Back, Color.WHITE);
        GeneralUtility.onNodeClickedWithoutScroll(event -> flipToFrontOrganization(), backLink);

        FXProperties.runNowAndOnPropertyChange(country -> {
            if (country != null) {
                Float latitude = country.getLatitude();
                Float longitude = country.getLongitude();
                if (latitude != null && longitude != null)
                    worldMapView.setMapCenter(latitude, longitude);
            }
            recreateOrganizationMarkers(worldMapView);
        }, FXCountry.countryProperty());

        ObservableLists.runNowAndOnListChange(x -> recreateOrganizationMarkers(worldMapView)
            , FXOrganizations.organizations());

        return OrangeFrame.createOrangeFrame(
            BookingI18nKeys.findYourLocalCentre,
            worldMapView.getMapNode(),
            backLink
        );
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

    private void flipToFrontOrganization() {
        if (flipPane.getFront() == null)
            flipPane.setFront(getOrganizationView());
        organizationMapView.onBeforeFlip();
        flipPane.flipToFront(organizationMapView::onAfterFlip);
    }

    private void flipToBackWordMap() {
        if (flipPane.getBack() == null)
            flipPane.setBack(getChangeLocationView());
        worldMapView.onBeforeFlip();
        flipPane.flipToBack(worldMapView::onAfterFlip);
    }

}
