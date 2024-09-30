package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.audiorecordings.routing.AudioRecordingsRouting;

public final class AudioRecordingsUiRoute extends UiRouteImpl {

    public AudioRecordingsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AudioRecordingsRouting.getPath()
                , false
                , AudioRecordingsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
