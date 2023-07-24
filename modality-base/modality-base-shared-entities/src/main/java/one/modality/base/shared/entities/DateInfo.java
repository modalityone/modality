package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface DateInfo
    extends Entity,
        EntityHasEvent,
        EntityHasLabel,
        EntityHasDateTimeRange,
        EntityHasMinDateTimeRange,
        EntityHasMaxDateTimeRange {

  Label getFeesBottomLabel();

  Label getFeesPopupLabel();

  Boolean isForceSoldout();
}
