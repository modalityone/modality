package one.modality.event.frontoffice.activities.home;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.SvgUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.event.frontoffice.activities.home.views.NewsView;
import one.modality.event.frontoffice.activities.home.views.PodcastView;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    VBox page = new VBox();
    VBox newsContainer = new VBox();
    VBox podcastContainer = new VBox();

    public void rebuild() {
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

        FXHome.news.addListener((InvalidationListener) change -> {
            newsContainer.getChildren().clear();

            FXHome.news.forEach(n -> {
                NewsView nv = new NewsView(n);
                nv.buildView(page, this, this);
                newsContainer.getChildren().add(nv.getView());
            });
        });

        FXHome.podcasts.addListener((InvalidationListener) change -> {
            podcastContainer.getChildren().clear();

            FXHome.podcasts.forEach(p -> {
                PodcastView pv = new PodcastView(p);
                pv.buildView(page);
                podcastContainer.getChildren().add(new PodcastView(p).getView());
            });
        });

        Button newsLoadMore = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "Load more", StyleUtility.MAIN_TEXT_SIZE);
        newsLoadMore.setOnAction(e -> {
            FXHome.newsLimit.set(FXHome.newsLimit.get() + 3);
        });

        Button podcastLoadMore = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "Load more", StyleUtility.MAIN_TEXT_SIZE);
        podcastLoadMore.setOnAction(e -> {
            FXHome.podcastLimit.set(FXHome.podcastLimit.get() + 3);
        });

        Label podcastLabel = GeneralUtility.createLabel(Color.web(StyleUtility.MAIN_BLUE), "Kadampa Podcast", 21);
        podcastLabel.graphicProperty().set(GeneralUtility.createSvgPath(SvgUtility.KADAMPA_PODCAST, StyleUtility.MAIN_BLUE));

        page.getChildren().addAll(
                newsContainer,
                GeneralUtility.createSpace(20),
                GeneralUtility.centerNode(newsLoadMore),
                GeneralUtility.createSpace(20),
                GeneralUtility.createSpace(100),
                GeneralUtility.centerNode(
                    GeneralUtility.centerNode(podcastLabel)
                ),
                GeneralUtility.createSpace(50),
                podcastContainer,
                GeneralUtility.createSpace(20),
                GeneralUtility.centerNode(podcastLoadMore),
                GeneralUtility.createSpace(20)
        );
    }

    @Override
    public Node buildUi() {
        rebuild();

        FXProperties.runNowAndOnPropertiesChange(e -> {
            HomeUtility.loadPodcasts(this, podcastContainer.getChildren(), page);
            HomeUtility.loadNews(this, newsContainer.getChildren(), page);
        }, I18n.dictionaryProperty());

        FXProperties.runOnPropertiesChange(() -> GeneralUtility.screenChangeListened(page.getWidth()), page.widthProperty());

        return LayoutUtil.createVerticalScrollPane((Region) GeneralUtility.createPaddedContainer(page, 10, 10));
    }

    @Override
    protected void startLogic() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
