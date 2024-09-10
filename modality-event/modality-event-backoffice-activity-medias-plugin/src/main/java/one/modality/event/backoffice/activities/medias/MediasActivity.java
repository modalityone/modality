package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.text.Text;


/**
 * @author Bruno Salmon
 */
public class MediasActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        return new Text("Medias");
    }
}
