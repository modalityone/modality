package one.modality.event.frontoffice.activities.home.mapper;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.News;
import one.modality.event.frontoffice.activities.home.views.NewsView;

public class NewsToNewsViewMapper implements IndividualEntityToObjectMapper<News, Node> {

    VBox page;
    OperationActionFactoryMixin activityOperation;
    ViewDomainActivityBase activityBase;
    NewsView view;
    News lastNews;

    public NewsToNewsViewMapper(VBox page, OperationActionFactoryMixin activityOperation, ViewDomainActivityBase activityBase, News news) {
        this.page = page;
        this.activityOperation = activityOperation;
        this.activityBase = activityBase;
        onEntityChangedOrReplaced(news);
    }

    @Override
    public Node getMappedObject() {
        return view.getView();
    }

    @Override
    public void onEntityChangedOrReplaced(News news) {
        if (news != lastNews) {
            this.view = new NewsView(lastNews = news);
            this.view.buildView(page, activityOperation, activityBase);
        }
    }

    @Override
    public void onEntityRemoved(News entity) {}
}
