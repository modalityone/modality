package one.modality.event.frontoffice2018.activities.terms;

import one.modality.ecommerce.client2018.activity.bookingprocess.BookingProcessPresentationLogicActivity;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.i18n.I18n;

import static dev.webfx.stack.orm.dql.DqlStatement.parse;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationLogicActivity
        extends BookingProcessPresentationLogicActivity<TermsPresentationModel> {

    TermsPresentationLogicActivity() {
        super(TermsPresentationModel::new);
    }

    @Override
    protected void startLogic(TermsPresentationModel pm) {
        ReactiveVisualMapper.createReactiveChain(this)
                .always("{class: 'Letter', where: 'type.terms', limit: '1'}")
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), id -> where("event=?", id))
                .always(I18n.languageProperty(), lang -> parse("{columns: '[`html(" + lang + ")`]'}"))
                .visualizeResultInto(pm.termsLetterVisualResultProperty())
                .start();
    }
}
