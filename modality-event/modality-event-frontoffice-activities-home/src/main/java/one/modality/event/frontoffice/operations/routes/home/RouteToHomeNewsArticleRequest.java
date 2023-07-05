package one.modality.event.frontoffice.operations.routes.home;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.frontoffice.states.GeneralPM;

public class RouteToHomeNewsArticleRequest extends RoutePushRequest implements HasOperationCode {
    public RouteToHomeNewsArticleRequest(BrowsingHistory browsingHistory) {
        super(GeneralPM.HOME_NEWS_ARTICLE_PATH, browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToHomeNewsArticle";
    }
}
