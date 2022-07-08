package org.modality_project.event.client.controls.calendargraphic.impl;

import org.modality_project.event.client.controls.calendargraphic.HasEpochDay;

/**
 * @author Bruno Salmon
 */
interface HorizontalDayPositioned extends HasEpochDay {

    void setXAndWidth(double x, double width);
}
