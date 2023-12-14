package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.RatioPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.layout.LayoutUtil;
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
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebView;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.util.stream.Collectors;

public final class OrganizationSelectorView {

    private final static String PLAY_VIDEO_TRIANGLE_SVG = "M 24.2625,45.374999 V 18.625 L 47,32 Z M 32,64 C 14.355,64 0,49.644751 0,32 0,14.355 14.355,0 32,0 49.644751,0 64,14.355 64,32 64,49.644751 49.644751,64 32,64 Z M 32,6.5 C 17.939251,6.5 6.5,17.939251 6.5,32 6.5,46.060751 17.939251,57.5 32,57.5 46.060751,57.5 57.5,46.060751 57.5,32 57.5,17.939251 46.060751,6.5 32,6.5 Z";

    private final ButtonFactoryMixin factoryMixin;
    private final ViewDomainActivityBase activityBase;
    private final FlipPane flipPane = new FlipPane(true);
    private final MapView organizationMapView = new StaticMapView(10);
    private MapView worldMapView;
    private final RatioPane presentationPane = new RatioPane(16d/9);
    private final WebView presentationVideoView = new WebView();
    private final Hyperlink websiteLink = GeneralUtility.createHyperlink("localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink addressLink = GeneralUtility.createHyperlink("localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink phoneLink   = GeneralUtility.createHyperlink("localCentrePhone",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
    private final Hyperlink emailLink   = GeneralUtility.createHyperlink("localCentreEmail",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

    public OrganizationSelectorView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {
        this.factoryMixin = factoryMixin;
        this.activityBase = activityBase;

        LayoutUtil.setAllUnmanagedWhenInvisible(presentationPane, websiteLink, addressLink, phoneLink, emailLink);

        organizationMapView.placeEntityProperty().bind(FXOrganization.organizationProperty());
        FXProperties.runNowAndOnPropertiesChange(e -> updateFromOrganization(), FXOrganization.organizationProperty());
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

        Hyperlink changeLocation = GeneralUtility.createHyperlink("Change your location", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        changeLocation.setOnMouseClicked(event -> flipToBackWordMap());

        Node organizationMapNode = organizationMapView.getMapNode();
        return OrangeFrame.createOrangeFrame(
                "yourLocalCentre",
                new Pane(organizationButtonScalePane, organizationMapNode, presentationPane, contactBox) {
                    @Override
                    protected void layoutChildren() {
                        double width = getWidth(), height = getHeight(), x = 0, y = 0, w = width, h = organizationButtonScalePane.prefHeight(width);
                        layoutInArea(organizationButtonScalePane, x, y, w, h, 0, HPos.CENTER, VPos.CENTER);
                        double space = Math.min(35, width * 0.03);
                        y += h + space;
                        if (organizationMapNode.isVisible()) {
                            w = width / 2;
                            layoutInArea(organizationMapNode, x, y, w, height - y, 0, HPos.CENTER, VPos.TOP);
                            x = w + space;
                            w = width - x;
                            contactBox.setAlignment(Pos.CENTER_LEFT);
                        } else
                            contactBox.setAlignment(Pos.CENTER);
                        if (presentationPane.isVisible()) {
                            h = presentationPane.prefHeight(w);
                            layoutInArea(presentationPane, x, y, w, h, 0, HPos.CENTER, VPos.TOP);
                            y += h + 10;
                        }
                        layoutInArea(contactBox, x, y, w, height - y, 0, HPos.CENTER, VPos.CENTER);
                    }
                },
                changeLocation);
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
                        websiteLink.setOnAction(e2 -> chooseHowToOpenUrl("https://" + domainName));
                        FXProperties.setEvenIfBound(addressLink.textProperty(), (String) organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                        Float latitude = organization.getLatitude();
                        Float longitude = organization.getLongitude();
                        if (latitude == null || longitude == null) {
                            addressLink.setOnAction(null);
                            organizationMapView.setMapCenter(null);
                            organizationMapView.getMarkers().clear();
                            organizationMapView.getMapNode().setVisible(false);
                        } else {
                            addressLink.setOnAction(e2 -> BrowserUtil.openExternalBrowser("https://google.com/maps/search/kadampa/@" + latitude + "," + longitude + ",12z"));
                            organizationMapView.setMapCenter(latitude, longitude);
                            organizationMapView.getMarkers().setAll(new MapMarker(organization));
                            organizationMapView.getMapNode().setVisible(true);
                        }
                        String phone = organization.getStringFieldValue("phone");
                        phoneLink.setVisible(phone != null);
                        FXProperties.setEvenIfBound(phoneLink.textProperty(), phone);
                        phoneLink.setOnAction(e2 -> BrowserUtil.openExternalBrowser("tel:" + phone));
                        String email = organization.getStringFieldValue("email");
                        emailLink.setVisible(email != null);
                        FXProperties.setEvenIfBound(emailLink.textProperty(), email);
                        emailLink.setOnAction(e2 -> BrowserUtil.openExternalBrowser("mailto:" + email));
                        FXCountry.setCountry(organization.getCountry());
                    }));
        }
    }

    private void chooseHowToOpenUrl(String url) {
        Hyperlink insideAppLink = GeneralUtility.createHyperlink("openInsideApp", Color.WHITE, 21);
        Hyperlink outsideAppLink = GeneralUtility.createHyperlink("openOutsideApp", Color.WHITE, 21);
        Hyperlink copyLink = GeneralUtility.createHyperlink("copyLink", Color.WHITE, 21);
        VBox vBox = new VBox(30, insideAppLink, outsideAppLink, copyLink);
        vBox.setBorder(Border.stroke(Color.WHITE));
        vBox.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        vBox.setAlignment(Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(vBox, FXMainFrameDialogArea.getDialogArea());
        vBox.setPadding(new Insets(50));
        insideAppLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            BrowserUtil.openInternalBrowser(url, activityBase.getUiRouter());
        });
        outsideAppLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            BrowserUtil.openExternalBrowser(url);
        });
        copyLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            ClipboardContent content = new ClipboardContent();
            content.putString(url);
            Clipboard.getSystemClipboard().setContent(content);
        });
        vBox.requestLayout();
        SceneUtil.runOnceFocusIsOutside(vBox, true, dialogCallback::closeDialog);
    }

    private Node getChangeLocationView() {
        worldMapView = new DynamicMapView();
        worldMapView.placeEntityProperty().bind(FXCountry.countryProperty());
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Country country = FXCountry.getCountry();
            if (country != null) {
                Float latitude = country.getLatitude();
                Float longitude = country.getLongitude();
                if (latitude != null && longitude != null)
                    worldMapView.setMapCenter(latitude, longitude);
            }
        }, FXCountry.countryProperty());

        Hyperlink backLink = GeneralUtility.createHyperlink("Back", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        backLink.setOnAction(e -> flipToFrontOrganization());

        FXProperties.runNowAndOnPropertiesChange(() ->
                recreateOrganizationMarkers(worldMapView), FXCountry.countryProperty());

        ObservableLists.runNowAndOnListChange(x ->
                recreateOrganizationMarkers(worldMapView), FXOrganizations.organizations());

        return OrangeFrame.createOrangeFrame(
                "findYourLocalCentre",
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
