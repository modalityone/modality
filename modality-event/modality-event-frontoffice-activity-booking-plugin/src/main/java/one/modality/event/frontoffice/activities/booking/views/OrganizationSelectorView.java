package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.util.stream.Collectors;

public final class OrganizationSelectorView {

    private final ButtonFactoryMixin factoryMixin;
    private final ViewDomainActivityBase activityBase;
    private final FlipPane flipPane = new FlipPane();

    public OrganizationSelectorView(ButtonFactoryMixin factoryMixin, ViewDomainActivityBase activityBase) {
        this.factoryMixin = factoryMixin;
        this.activityBase = activityBase;
    }

    public Node getView() {
        flipPane.setFront(getOrganizationView());
        flipPane.setBack(getChangeLocationView());
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

        MapView organizationMapView = new MapView(5, 18, 10);
        organizationMapView.entityProperty().bind(FXOrganization.organizationProperty());

        Hyperlink websiteLink = GeneralUtility.createHyperlink("localCentreWebsite", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink addressLink = GeneralUtility.createHyperlink("localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink phoneLink   = GeneralUtility.createHyperlink("localCentrePhone",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        Hyperlink emailLink   = GeneralUtility.createHyperlink("localCentreEmail",   Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);

        FXProperties.runNowAndOnPropertiesChange(e -> {
            Organization organization = FXOrganization.getOrganization();
            if (organization != null) {
                organization.onExpressionLoaded("domainName,latitude,longitude,street,cityName,postCode,country.(name,latitude,longitude,iso_alpha2),phone,email")
                        .onSuccess(ignored -> Platform.runLater(() -> {
                            String domainName = organization.getStringFieldValue("domainName");
                            FXProperties.setEvenIfBound(websiteLink.textProperty(), domainName);
                            websiteLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://" + domainName));
                            FXProperties.setEvenIfBound(addressLink.textProperty(), (String) organization.evaluate("street + ' - ' + cityName + ' ' + postCode + ' - ' + country.name "));
                            Float latitude = organization.getLatitude();
                            Float longitude = organization.getLongitude();
                            if (latitude == null || longitude == null) {
                                addressLink.setOnAction(null);
                                organizationMapView.setMapCenterPoint(null);
                                organizationMapView.getMarkers().clear();
                            } else {
                                addressLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("https://google.com/maps/search/kadampa/@" + latitude + "," + longitude + ",12z"));
                                organizationMapView.setMapCenterPoint(latitude, longitude);
                                organizationMapView.getMarkers().setAll(new MapMarker(latitude, longitude));
                            }
                            String phone = organization.getStringFieldValue("phone");
                            FXProperties.setEvenIfBound(phoneLink.textProperty(), phone);
                            phoneLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("tel://" + phone));
                            String email = organization.getStringFieldValue("email");
                            FXProperties.setEvenIfBound(emailLink.textProperty(), email);
                            emailLink.setOnAction(e2 -> WebFxKitLauncher.getApplication().getHostServices().showDocument("mailto://" + email));
                            FXCountry.setCountry(organization.getCountry());
                        }));
            }
        }, FXOrganization.organizationProperty());

        VBox contactBox = GeneralUtility.createVList(10, 0,
                websiteLink,
                addressLink,
                phoneLink,
                emailLink
        );
        contactBox.setAlignment(Pos.CENTER_LEFT);
        contactBox.setPadding(new Insets(0, 0, 0, 35));

        Node mapAndContactPane = new ColumnsPane(organizationMapView.buildMapNode(), contactBox);

        Hyperlink changeLocation = GeneralUtility.createHyperlink("Change your location", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        changeLocation.setOnMouseClicked(event -> flipPane.flipToBack());

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

    private Node getChangeLocationView() {
        String countryJson = "{class: 'Country', fields: 'latitude,longitude', orderBy: 'name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            countryJson = countryJson.replace("}", ", columns: [{expression: '[image(`images/s16/countries/svg/` + iso_alpha2 + `.svg`),name]'}] }");
        EntityButtonSelector<Country> countryButtonSelector = new EntityButtonSelector<>(
                countryJson, factoryMixin, FXMainFrameDialogArea::getDialogArea, activityBase.getDataSourceModel()
        );
        countryButtonSelector.selectedItemProperty().bindBidirectional(FXCountry.countryProperty());
        Button countryButton = countryButtonSelector.getButton();
        countryButton.setMaxWidth(Region.USE_PREF_SIZE);
        ScalePane countryButtonScalePane = new ScalePane(ScaleMode.FIT_WIDTH, countryButton);
        countryButtonScalePane.setMaxScale(2.5);

        MapView countryMapView = new MapView(4, 7, 5);
        countryMapView.entityProperty().bind(FXCountry.countryProperty());
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Country country = FXCountry.getCountry();
            if (country != null) {
                Float latitude = country.getLatitude();
                Float longitude = country.getLongitude();
                if (latitude != null && longitude != null)
                    countryMapView.setMapCenterPoint(latitude, longitude);
            }
        }, FXCountry.countryProperty());

        VBox countryOrganizationsBox = new VBox(10);
        countryOrganizationsBox.setPadding(new Insets(0, 0, 0, 20));

        ObservableList<Organization> countryOrganizations = FXCollections.observableArrayList();

        ReactiveObjectsMapper.<Organization, Node>createReactiveChain(factoryMixin)
                .always("{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'name', fields: 'type,name,latitude,longitude'}")
                .ifNotNullOtherwiseEmpty(FXCountry.countryProperty(), country -> DqlStatement.where("country=?", country))
                .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.createFactory(Hyperlink::new, this::setOrganizationHyperlink, h -> h))
                .storeEntitiesInto(countryOrganizations)
                .storeMappedObjectsInto(countryOrganizationsBox.getChildren())
                .start();

        ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(countryOrganizationsBox);
        scrollPane.setMaxHeight(600);
        VBox container = new VBox(20,
                I18n.bindI18nProperties(TextUtility.getSubText(null, StyleUtility.RUPAVAJRA_WHITE), "yourLocalCentre"),
                countryButtonScalePane,
                new ColumnsPane(countryMapView.buildMapNode(), scrollPane)
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        countryOrganizations.addListener((InvalidationListener) observable -> {
            countryMapView.getMarkers().setAll(
                    countryOrganizations.stream()
                            .filter(o -> o.getLatitude() != null && o.getLongitude() != null)
                            .map(this::createOrganizationMarker)
                            .collect(Collectors.toList()));
        });

        return container;
    }

    private void setOrganizationHyperlink(Hyperlink h, Organization o) {
        GeneralUtility.setupLabeled(h, "localCentreAddress", Color.WHITE, StyleUtility.MAIN_TEXT_SIZE);
        FXProperties.setEvenIfBound(h.textProperty(), o.getName());
        h.setOnAction(e -> flipBackToOrganization(o));
    }

    private MapMarker createOrganizationMarker(Organization o) {
        MapMarker marker = new MapMarker(o.getLatitude(), o.getLongitude());
        marker.getNode().setOnMousePressed(e -> flipBackToOrganization(o));
        return marker;
    }

    private void flipBackToOrganization(Organization o) {
        FXOrganization.setOrganization(o);
        flipPane.flipToFront();
    }
}
