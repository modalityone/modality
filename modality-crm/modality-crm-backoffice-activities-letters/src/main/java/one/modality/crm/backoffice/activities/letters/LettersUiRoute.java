package one.modality.crm.backoffice.activities.letters;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.crm.backoffice.activities.letters.routing.LettersRouting;

/**
 * @author Bruno Salmon
 */
public final class LettersUiRoute extends UiRouteImpl {

  public LettersUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(LettersRouting.getAnyPath()),
        false,
        LettersActivity::new,
        DomainPresentationActivityContextFinal::new);
  }
}
