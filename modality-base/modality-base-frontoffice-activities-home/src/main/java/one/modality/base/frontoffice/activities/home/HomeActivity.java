package one.modality.base.frontoffice.activities.home;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Handler;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.routing.router.RoutingContext;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import one.modality.base.frontoffice.activities.home.views.NewsView;
import one.modality.base.frontoffice.activities.home.views.PodcastView;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.SvgUtility;
import one.modality.base.shared.entities.News;
import one.modality.base.shared.entities.Podcast;

public final class HomeActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    private final BorderPane homeContainer = new BorderPane();
    private final VBox pageContainer = new VBox(); // The main container inside the vertical scrollbar
    private final VBox newsContainer = new VBox(40);
    private final VBox podcastsContainer = new VBox(20);
    public final IntegerProperty newsLimitProperty = new SimpleIntegerProperty(3);
    public final IntegerProperty podcastsLimitProperty = new SimpleIntegerProperty(3);

    @Override
    public Node buildUi() {
        Text headerText = new Text("KEEP UP TO WHAT'S GOING ON IN THE KADAMPA WORLD");
        headerText.setFill(Color.web(StyleUtility.MAIN_BLUE));
        headerText.setFont(Font.font(StyleUtility.TEXT_FAMILY, FontWeight.BOLD, 32));
        headerText.setStyle("-fx-font-family: " + StyleUtility.TEXT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 32");
        headerText.setWrappingWidth(210);
        ImageView headerImageView = ImageStore.createImageView("https://kadampa.org/cdn-cgi/image//wp-content/uploads/2022/05/VGL.jpg");
        headerImageView.setPreserveRatio(true);
        headerImageView.setFitHeight(600);
        StackPane headerPane = new StackPane(headerImageView, headerText);
        StackPane.setAlignment(headerText, Pos.TOP_LEFT);
        StackPane.setAlignment(headerImageView, Pos.CENTER_RIGHT);
        StackPane.setMargin(headerText, new Insets(150, 0, 0, 0));
        headerPane.setMaxHeight(600);

        Label podcastLabel = GeneralUtility.createLabel("Kadampa Podcast", Color.web(StyleUtility.MAIN_BLUE), 21);
        podcastLabel.setGraphic(GeneralUtility.createSvgPath(SvgUtility.KADAMPA_PODCAST, StyleUtility.MAIN_BLUE));
        VBox.setMargin(podcastLabel, new Insets(100, 0, 50, 0));

        Button loadMoreNewsButton = createLoadMoreButton(newsLimitProperty);
        Button loadMorePodcastsButton = createLoadMoreButton(podcastsLimitProperty);

        ScalePane scalePane = new ScalePane(headerPane);
        scalePane.setBackground(Background.fill(Color.WHITE));

        pageContainer.setAlignment(Pos.CENTER);
        Insets containerMargins = new Insets(30, 20, 10, 20);
        VBox.setMargin(newsContainer, containerMargins);
        VBox.setMargin(podcastsContainer, containerMargins);
        pageContainer.getChildren().setAll(
                scalePane,
                newsContainer,
                loadMoreNewsButton,
                podcastLabel,
                podcastsContainer,
                loadMorePodcastsButton
        );

        FXProperties.runOnPropertiesChange(() -> {
            double width = pageContainer.getWidth();
            double maxHeight = width < 600 ? width : 600;
            scalePane.setMaxHeight(maxHeight);
            headerText.setTranslateX(Math.max(0, (width - 600) * 0.5));
            GeneralUtility.screenChangeListened(width);
        }, pageContainer.widthProperty());

        ScrollPane scrollPane = LayoutUtil.createVerticalScrollPane(pageContainer);

        homeContainer.setCenter(scrollPane);

        getUiRouter().getRouter().route().handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext ctx) {
                homeContainer.setCenter(scrollPane);
                ctx.next();
            }
        });

        return WEB_VIEW_CONTAINER = homeContainer;
    }

    private static Button createLoadMoreButton(IntegerProperty limitProperty) {
        Button loadMoreButton = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "Load more", StyleUtility.MAIN_TEXT_SIZE);
        loadMoreButton.setCursor(Cursor.HAND);
        loadMoreButton.setOnAction(e -> limitProperty.set(limitProperty.get() + 5));
        VBox.setMargin(loadMoreButton, new Insets(30, 0, 30, 0));
        return loadMoreButton;
    }

    @Override
    protected void startLogic() {

        ReactiveObjectsMapper.<News, Node>createPushReactiveChain(this)
                .always("{class: 'News', fields: 'channel, channelNewsId, date, title, excerpt, imageUrl, linkUrl', orderBy: 'date desc, id desc'}")
                .always(newsLimitProperty, limit -> DqlStatement.limit("?", limit))
                .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.createFactory(NewsView::new, NewsView::setNews, NewsView::getView))
                .storeMappedObjectsInto(newsContainer.getChildren())
                .start();

        ReactiveObjectsMapper.<Podcast, Node>createPushReactiveChain(this)
                .always("{class: 'Podcast', fields: 'channel, channelPodcastId, date, title, excerpt, imageUrl, audioUrl, durationMillis', orderBy: 'date desc, id desc'}")
                .always(podcastsLimitProperty, limit -> DqlStatement.limit("?", limit))
                .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.createFactory(PodcastView::new, PodcastView::setPodcast, PodcastView::getView))
                .storeMappedObjectsInto(podcastsContainer.getChildren())
                .start();

        /* TODO: Move this elsewhere

        HomeUtility.loadCenters(FXApp.centersRef);

        ReactiveEntitiesMapper.<Organization>createPushReactiveChain(this)
                .always("{class: 'Organization', fields:'name, country, type, closed'}")
                .storeEntitiesInto(FXApp.organizations)
                .start();

        ReactiveEntitiesMapper.<Person>createPushReactiveChain(this)
                .always("{class: 'Person', fields:'firstName,lastName,birthdate,male,ordained,email,street,cityName,postCode,country,organization,passport,removed', orderBy: 'id'}")
                //.ifInstanceOf(FXUserId.userIdProperty(), ModalityUserPrincipal.class, mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()))
                .ifNotNullOtherwiseEmpty(FXUserId.userIdProperty(), mup -> DqlStatement.where("frontendAccount=?", ((ModalityUserPrincipal) mup).getUserAccountId()))
                .storeEntitiesInto(FXAccount.getPersons())
                .start();
         */
    }

    private static BorderPane WEB_VIEW_CONTAINER;
    private static WebView WEB_VIEW;
    private static String URL;

    public static void browse(String url) {
        if (WEB_VIEW == null)
            WEB_VIEW = new WebView();
        if (!url.equals(URL)) {
            WEB_VIEW.getEngine().load(URL = url);
        }
        WEB_VIEW_CONTAINER.setCenter(WEB_VIEW);
    }
}
