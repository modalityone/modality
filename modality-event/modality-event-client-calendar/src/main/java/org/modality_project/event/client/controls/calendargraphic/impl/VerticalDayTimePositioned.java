package org.modality_project.event.client.controls.calendargraphic.impl;

import org.modality_project.event.client.controls.calendargraphic.HasDayTimeMinuteInterval;

/**
 * @author Bruno Salmon
 */
interface VerticalDayTimePositioned extends HasDayTimeMinuteInterval {

    void setYAndHeight(double y, double height);
}
