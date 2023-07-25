package one.modality.crm.backoffice2018.activities.letters;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class LettersActivity extends DomainPresentationActivityImpl<LettersPresentationModel> {

    LettersActivity() {
        super(LettersPresentationViewActivity::new, LettersPresentationLogicActivity::new);
    }
}
