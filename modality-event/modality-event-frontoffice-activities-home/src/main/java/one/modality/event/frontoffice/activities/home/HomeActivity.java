package one.modality.event.frontoffice.activities.home;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeActivity extends ViewDomainActivityBase {
    @Override
    public Node buildUi() {
        VBox page = new VBox();
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

        FXHome.news.addListener((ListChangeListener<? super News>) change -> {
            FXHome.news.forEach(n -> {
                Text t = TextUtility.getMainText(n.title.toUpperCase(), StyleUtility.MAIN_BLUE);
                t.setWrappingWidth(200);

                Text c = TextUtility.getMainText("A wonderful weekend in which more than 180 people gathered to delight in the yogas of Je Tsongkhapa, understanding a little more his teachings and aspect of Guru Sumati Buddha Heruka. During the event, meditations were guided by kadam Gabby, the Resident Teacher of KMC Girona, and the retreat were guided by gen Kelsang Lochani, the Resident Teacher of KMC Barcelona.", StyleUtility.VICTOR_BATTLE_BLACK);
                c.setWrappingWidth(200);

                Image img = new Image("https://kadampa.org/cdn-cgi/image/width=2048,height=1365,fit=crop,quality=80,format=auto,onerror=redirect,metadata=none/wp-content/uploads/2023/06/Metal-Statues-Project-25.jpg", true);
                ImageView imgV = new ImageView(img);

                imgV.setFitWidth(100);
                imgV.setFitHeight(75);

                Node newsBanner = GeneralUtility.createHList(10, 0, imgV,
                    GeneralUtility.createVList(5, 0,
                        t,
                        TextUtility.getSubText(n.date),
                        c, GeneralUtility.createSpace(20)));

                newsContainer.getChildren().add(newsBanner);
            });
        });

        FXHome.podcasts.addListener((ListChangeListener<? super Podcast>) change -> {
            FXHome.podcasts.forEach(p -> {
                Text t = TextUtility.getMainText(p.title.toUpperCase(), StyleUtility.MAIN_BLUE);
                t.setWrappingWidth(200);

                Text c = TextUtility.getMainText(p.excerpt, StyleUtility.VICTOR_BATTLE_BLACK);
                c.setWrappingWidth(350);

                Image img = new Image(p.image.replace("\\", ""), true);
                ImageView imgV = new ImageView(img);

                imgV.setFitWidth(75);
                imgV.setFitHeight(75);

                MediaPlayer player = new MediaPlayer(new Media(p.link.replace("\\", "")));

                Button play = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Play");
                Button pause = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Pause");

                play.setOnAction(e -> {
                    if (FXHome.player != null) FXHome.player.pause();
                    FXHome.player = player;
                    player.play();
                });

                pause.setOnAction(e -> player.pause());

                Node podcastBanner = GeneralUtility.createVList(10, 0,
                        GeneralUtility.createHList(10, 0, imgV,
                        GeneralUtility.createVList(5, 0,
                                t,
                                GeneralUtility.createHList(10, 0, play, pause))),
                        c, GeneralUtility.createSpace(20));

                podcastContainer.getChildren().add(podcastBanner);
            });
        });

        page.getChildren().addAll(newsContainer, podcastContainer);

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
                .ifNotNullOtherwiseEmpty(FXUserId.userIdProperty(), mup -> DqlStatement.where("frontendAccount=?", ((ModalityUserPrincipal) mup).getUserAccountId())
                )
                .storeEntitiesInto(FXAccount.getPersons())
                .start();
    }
}
