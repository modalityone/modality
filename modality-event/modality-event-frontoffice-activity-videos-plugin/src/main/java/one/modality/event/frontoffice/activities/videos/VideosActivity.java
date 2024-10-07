package one.modality.event.frontoffice.activities.videos;

import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Label;

public final class VideosActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        return I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.VideosMenu);
    }
}
