package org.modality_project.base.shared.entities;

import dev.webfx.stack.framework.shared.orm.entity.Entity;
import org.modality_project.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface DateInfo extends Entity, EntityHasEvent, EntityHasLabel, EntityHasDateTimeRange, EntityHasMinDateTimeRange, EntityHasMaxDateTimeRange {

    Label getFeesBottomLabel();

    Label getFeesPopupLabel();

    Boolean isForceSoldout();

}
