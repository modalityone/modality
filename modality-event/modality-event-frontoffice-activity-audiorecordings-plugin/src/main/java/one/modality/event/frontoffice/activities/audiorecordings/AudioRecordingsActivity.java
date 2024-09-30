package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Label;

public final class AudioRecordingsActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        return I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.Recordings);
    }
}
