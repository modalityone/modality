package one.modality.event.frontoffice.activities.home;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.entities.News;
import one.modality.base.frontoffice.entities.Podcast;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.event.frontoffice.activities.home.views.NewsView;
import one.modality.event.frontoffice.activities.home.views.PodcastView;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {
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

        VBox newsContainer = new VBox(10);
        VBox podcastContainer = new VBox();

        FXHome.news.forEach(n -> {
            newsContainer.getChildren().add(new NewsView(n).getView(page, this, this));
        });

        FXHome.podcasts.forEach(p -> {
            podcastContainer.getChildren().add(new PodcastView(p).getView(page));
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

        FXProperties.runOnPropertiesChange(() -> GeneralUtility.screenChangeListened(page.getWidth()), page.widthProperty());

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
