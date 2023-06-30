package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.entities.Center;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.shared.entities.Organization;

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

        return page;
    }

    @Override
    protected void startLogic() {
        HomeUtility.loadCenters(FXApp.centersRef);

        ReactiveEntitiesMapper.<Organization>createPushReactiveChain(this)
                .always("{class: 'Organization', fields:'name, country, type, closed'}")
                .storeEntitiesInto(FXApp.organizations)
                .start();
    }
}
