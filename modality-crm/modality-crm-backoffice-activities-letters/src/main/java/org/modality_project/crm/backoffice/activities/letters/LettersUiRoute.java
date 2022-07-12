package org.modality_project.crm.backoffice.activities.letters;

import org.modality_project.crm.backoffice.activities.letters.routing.LettersRouting;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

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
