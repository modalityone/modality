package one.modality.event.frontoffice.activities.home;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.entities.News;
import one.modality.base.frontoffice.entities.Podcast;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.event.frontoffice.operations.routes.home.RouteToHomeNewsArticleRequest;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {
    private VBox page = new VBox();

    private boolean isValidDuration(Duration d) {
        return d != null && !d.isIndefinite() && !d.isUnknown();
    }
    private void bindProgress(MediaPlayer player, ProgressBar bar) {
        var binding =
                Bindings.createDoubleBinding(
                        () -> {
                            var currentTime = player.getCurrentTime();
                            var duration = player.getMedia().getDuration();
                            if (isValidDuration(currentTime) && isValidDuration(duration)) {
                                return currentTime.toMillis() / duration.toMillis();
                            }
                            return ProgressBar.INDETERMINATE_PROGRESS;
                        },
                        player.currentTimeProperty(),
                        player.getMedia().durationProperty());

        bar.progressProperty().bind(binding);
    }

    private void addSeekBehavior(MediaPlayer player, ProgressBar bar) {
        EventHandler<MouseEvent> onClickAndOnDragHandler =
                e -> {
                    var duration = player.getMedia().getDuration();
                    if (isValidDuration(duration)) {
                        var seekTime = duration.multiply(e.getX() / bar.getWidth());
                        player.seek(seekTime);
                        e.consume();
                    }
                };
        bar.addEventHandler(MouseEvent.MOUSE_CLICKED, onClickAndOnDragHandler);
        bar.addEventHandler(MouseEvent.MOUSE_DRAGGED, onClickAndOnDragHandler);
    }


    public void rebuild(VBox page) {
        System.out.println(">>>> REBUILD <<<<<");
        page.getChildren().removeAll(page.getChildren());

        Button compute = new Button("Compute");

        page.getChildren().add(compute);

        compute.setOnAction(e -> {
            Center.adjustRejectedList(FXApp.centers);
            Center.adjustRejectedList(FXApp.centersRef);

            FXApp.centers = FXCollections.observableArrayList(FXApp.centers.stream().map(c -> {
                List<Center> filtered = FXApp.centersRef.stream().filter(new Predicate<Center>() {
                    @Override
                    public boolean test(Center cc) {
                        return Center.isGoodMatch(c, cc);
                    }
                }).collect(Collectors.toList());

                if (filtered.size() == 0) return null;

                Center matchedCenter = filtered.get(0);

                matchedCenter.organization = c.organization;

                return matchedCenter;
            }).filter(new Predicate<Center>() {
                @Override
                public boolean test(Center center) {
                    return center != null;
                }
            }).collect(Collectors.toList()));

            System.out.println("Size of the matches: " + FXApp.centers.size());
        });

        VBox newsContainer = new VBox();
        VBox podcastContainer = new VBox();

        FXHome.news.forEach(n -> {
            Text t = TextUtility.getMainText(n.title.toUpperCase(), StyleUtility.MAIN_BLUE);

            t.setOnMouseClicked(c -> {
                FXHome.article = n;
                executeOperation(new RouteToHomeNewsArticleRequest(getHistory()));
            });

            Text c = TextUtility.getMainText(n.excerpt, StyleUtility.VICTOR_BATTLE_BLACK);

            ImageView imgV = new ImageView();

            Fetch.fetch("https://kadampa.org/wp-json/wp/v2/media/" + n.mediaId) // Expecting a JSON object only
                    .onFailure(error -> Console.log("Fetch failure: " + error))
                    .onSuccess(response -> {
                        Console.log("Fetch success: ok = " + response.ok());
                        response.jsonObject()
                                .onFailure(error -> Console.log("JsonObject failure: " + error))
                                .onSuccess(jsonObject -> {
                                    imgV.setImage(new Image(jsonObject.getObject("guid").getString("rendered").replace("\\", ""), true));
                                });
                    });

            imgV.setFitWidth(100*FXApp.fontRatio.get());
            imgV.setFitHeight(75*FXApp.fontRatio.get());

            VBox vList = GeneralUtility.createVList(5, 0,
                    t,
                    TextUtility.getSubText(n.date),
                    c, GeneralUtility.createSpace(20));
            vList.setMinWidth(0);
            HBox.setHgrow(vList, Priority.ALWAYS);
            vList.widthProperty().addListener((observableValue, number, width) -> {
                double wrappingWidth = width.doubleValue() - 10;
                t.setWrappingWidth(wrappingWidth);
                c.setWrappingWidth(wrappingWidth);
            });

            Node newsBanner = GeneralUtility.createHList(10, 0, imgV, vList);

            newsContainer.getChildren().add(newsBanner);
        });

        FXHome.podcasts.forEach(p -> {
            System.out.println(p.title);
            Text t = TextUtility.getMainText(p.title.toUpperCase(), StyleUtility.MAIN_BLUE);

            Text c = TextUtility.getMainText(p.excerpt, StyleUtility.VICTOR_BATTLE_BLACK);

            Image img = new Image(p.image.replace("\\", ""), true);
            ImageView imgV = new ImageView(img);

            imgV.setFitWidth(75*FXApp.fontRatio.get());
            imgV.setFitHeight(75*FXApp.fontRatio.get());

            MediaPlayer player = new MediaPlayer(new Media(p.link.replace("\\", "")));

            Button play = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Play", 9);
            Button pause = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Pause", 9);
            Button forward = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "--> 30s", 9);
            Button backward = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "<-- 10s>", 9);

            ProgressBar bar = new ProgressBar();
            Text duration = TextUtility.getSubText("", StyleUtility.ELEMENT_GRAY);
            Text current = TextUtility.getSubText("", StyleUtility.ELEMENT_GRAY);

            player.getMedia().durationProperty().addListener(e -> duration.setText("/" + Math.round(player.getMedia().durationProperty().get().toSeconds())));
            player.currentTimeProperty().addListener(e -> current.setText(String.valueOf(Math.round(player.currentTimeProperty().get().toSeconds()))));

            Node barContainer = GeneralUtility.createHList(5,0, bar, current, duration);

            bindProgress(player, bar);
            addSeekBehavior(player, bar);

            play.setOnAction(e -> {
                if (FXHome.player != null) FXHome.player.pause();
                FXHome.player = player;
                player.play();
            });

            forward.setOnAction(e -> {
                player.seek(player.getCurrentTime().add(Duration.seconds(30)));
            });

            backward.setOnAction(e -> {
                player.seek(player.getCurrentTime().subtract(Duration.seconds(10)));
            });

            pause.setOnAction(e -> player.pause());

            VBox vList = GeneralUtility.createVList(5, 0,
                    t,
                    barContainer,
                    GeneralUtility.createHList(10, 0, play, pause, backward, forward));

            vList.setMinWidth(0);
            HBox.setHgrow(vList, Priority.ALWAYS);
            vList.widthProperty().addListener((observableValue, number, width) -> {
                double wrappingWidth = width.doubleValue() - 10;
                t.setWrappingWidth(wrappingWidth);
                c.setWrappingWidth(wrappingWidth);
            });

            Node podcastBanner = GeneralUtility.createVList(10, 0,
                    GeneralUtility.createHList(10, 0, imgV, vList),
                    c, GeneralUtility.createSpace(20));

            podcastContainer.getChildren().add(podcastBanner);
        });

        page.getChildren().addAll(newsContainer, podcastContainer);
    }

    @Override
    public Node buildUi() {
        VBox page = new VBox();

        rebuild(page);

        FXProperties.runNowAndOnPropertiesChange(e -> {
            HomeUtility.loadPodcasts();
            HomeUtility.loadNews();
        }, I18n.dictionaryProperty());


        FXHome.news.addListener((ListChangeListener<News>) change -> rebuild(page));
        FXHome.podcasts.addListener((ListChangeListener<Podcast>) change -> rebuild(page));

        page.widthProperty().addListener((observableValue, number, width) -> {
            if (GeneralUtility.screenChangeListened(width.doubleValue()))
                rebuild(page);
        });

        FXApp.fontRatio.addListener(c -> rebuild(page));

        return LayoutUtil.createVerticalScrollPane((Region) GeneralUtility.createPaddedContainer(page, 10, 10));
    }

    @Override
    protected void startLogic() {
        HomeUtility.loadCenters(FXApp.centersRef);
        HomeUtility.loadNews();
        HomeUtility.loadPodcasts();

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
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
