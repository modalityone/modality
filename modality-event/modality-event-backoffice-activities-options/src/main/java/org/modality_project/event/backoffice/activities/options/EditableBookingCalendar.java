package org.modality_project.event.backoffice.activities.options;

import javafx.scene.Node;
import org.modality_project.ecommerce.client.businessdata.workingdocument.WorkingDocumentLine;
import org.modality_project.event.client.controls.calendargraphic.CalendarClickEvent;
import org.modality_project.event.client.businessdata.calendar.CalendarTimeline;
import org.modality_project.event.client.controls.bookingcalendar.BookingCalendar;
import org.modality_project.base.shared.entities.Option;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;

/**
 * @author Bruno Salmon
 */
public final class EditableBookingCalendar extends BookingCalendar {

    private final Node parentOwner;
    private boolean editMode;

    public EditableBookingCalendar(boolean amendable, Node parentOwner) {
        super(amendable);
        this.parentOwner = parentOwner;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    @Override
    protected void onCalendarClick(CalendarClickEvent event) {
        if (!isEditMode())
            super.onCalendarClick(event);
        else {
            CalendarTimeline calendarTimeline = event.getCalendarTimeline();
            Option option = getCalendarTimelineOption(calendarTimeline);
            if (option != null) {
                DayTimeRangeEditor.showDayTimeRangeEditorDialog(calendarTimeline.getDayTimeRange(),
                        event.getCalendarCell().getEpochDay(),
                        calendarTimeline,
                        (newDayTimeRange, dialogCallback) -> {
                            // Creating an update store
                            UpdateStore store = UpdateStore.create(workingDocument.getEventAggregate().getDataSourceModel());
                            // Creating an instance of Option entity
                            Option updatingOption = store.updateEntity(option);
                            // Updating the option time range
                            updatingOption.setTimeRange(newDayTimeRange.getText());
                            // Asking the update record this change in the database
                            store.submitChanges()
                                    .onFailure(dialogCallback::showException)
                                    .onSuccess(resultBatch -> {
                                        dialogCallback.closeDialog();
                                        // Updating the UI
                                        option.setTimeRange(newDayTimeRange.getText());
                                        createOrUpdateCalendarGraphicFromWorkingDocument(workingDocument, true);
                                    });
                        }, parentOwner
                );
            }
        }
    }

    private static Option getCalendarTimelineOption(CalendarTimeline calendarTimeline) {
        Object source = calendarTimeline.getSource();
        if (source instanceof Option)
            return (Option) source;
        if (source instanceof WorkingDocumentLine)
            return ((WorkingDocumentLine) source).getOption();
        return null;
    }
}
