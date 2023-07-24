package one.modality.crm.backoffice.activities.letters;

import static dev.webfx.stack.orm.dql.DqlStatement.limit;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import one.modality.base.client.activity.eventdependent.EventDependentPresentationLogicActivity;
import one.modality.crm.backoffice.operations.routes.letter.RouteToLetterRequest;

/**
 * @author Bruno Salmon
 */
final class LettersPresentationLogicActivity
    extends EventDependentPresentationLogicActivity<LettersPresentationModel> {

  LettersPresentationLogicActivity() {
    super(LettersPresentationModel::new);
  }

  @Override
  protected void startLogic(LettersPresentationModel pm) {
    ReactiveVisualMapper.createReactiveChain(this)
        .always("{class: 'Letter', where: 'active', orderBy: 'id'}")
        // Condition
        .ifNotNull(pm.eventIdProperty(), eventId -> where("event=?", eventId))
        // Search box condition
        .ifTrimNotEmpty(
            pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
        // Limit condition
        .ifPositive(pm.limitProperty(), l -> limit("?", l))
        .setEntityColumns("[" + "'name'," + "'type'" + "]")
        .applyDomainModelRowStyle()
        .visualizeResultInto(pm.genericVisualResultProperty())
        .setVisualSelectionProperty(pm.genericVisualSelectionProperty())
        .setSelectedEntityHandler(
            letter -> new RouteToLetterRequest(letter, getHistory()).execute())
        .start();
  }
}
