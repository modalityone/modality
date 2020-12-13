package mongoose.shared.entities;

import mongoose.shared.entities.markers.*;
import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface DateInfo extends Entity, EntityHasEvent, EntityHasLabel, EntityHasDateTimeRange, EntityHasMinDateTimeRange, EntityHasMaxDateTimeRange {

    Label getFeesBottomLabel();

    Label getFeesPopupLabel();

    Boolean isForceSoldout();

}
