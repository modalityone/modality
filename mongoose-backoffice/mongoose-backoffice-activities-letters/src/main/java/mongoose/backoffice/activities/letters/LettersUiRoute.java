package mongoose.backoffice.activities.letters;

import mongoose.backoffice.activities.letters.routing.LettersRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.framework.shared.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class LettersUiRoute extends UiRouteImpl {

    public LettersUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(
                PathBuilder.toRegexPath(LettersRouting.getAnyPath())
                , false
                , LettersActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
