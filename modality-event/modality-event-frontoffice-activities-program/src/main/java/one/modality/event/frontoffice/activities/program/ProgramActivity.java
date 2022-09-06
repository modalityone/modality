package one.modality.event.frontoffice.activities.program;

import javafx.scene.layout.BorderPane;
import one.modality.base.client.icons.ModalityIcons;
import one.modality.ecommerce.client.businessdata.feesgroup.FeesGroup;
import one.modality.ecommerce.client.businessdata.preselection.OptionsPreselection;
import one.modality.event.client.controls.sectionpanel.SectionPanelFactory;
import one.modality.event.client.controls.bookingcalendar.BookingCalendar;
import one.modality.ecommerce.client.activity.bookingprocess.BookingProcessActivity;
import dev.webfx.stack.ui.util.layout.LayoutUtil;
import dev.webfx.platform.console.Console;

/**
 * @author Bruno Salmon
 */
final class ProgramActivity extends BookingProcessActivity {

    private BookingCalendar bookingCalendar;
    private OptionsPreselection noAccommodationOptionsPreselection;

    @Override
    protected void createViewNodes() {
        super.createViewNodes();
        bookingCalendar = new BookingCalendar(false);
        BorderPane calendarSection = SectionPanelFactory.createSectionPanel(ModalityIcons.calendarMonoSvg16JsonUrl, "Timetable");
        calendarSection.centerProperty().bind(bookingCalendar.calendarNodeProperty());
        verticalStack.getChildren().setAll(calendarSection, LayoutUtil.setMaxWidthToInfinite(backButton));
        showBookingCalendarIfReady();
    }

    private void showBookingCalendarIfReady() {
        if (bookingCalendar != null && noAccommodationOptionsPreselection != null)
            bookingCalendar.createOrUpdateCalendarGraphicFromOptionsPreselection(noAccommodationOptionsPreselection);
    }

    @Override
    protected void startLogic() {
        onEventFeesGroups()
                .onFailure(Console::log)
                .onSuccess(result -> {
                    noAccommodationOptionsPreselection = findNoAccommodationOptionsPreselection(result);
                    showBookingCalendarIfReady();
                });
    }

    private static OptionsPreselection findNoAccommodationOptionsPreselection(FeesGroup[] feesGroups) {
        for (FeesGroup fg : feesGroups) {
            for (OptionsPreselection op : fg.getOptionsPreselections())
                if (!op.hasAccommodation())
                    return op;
        }
        return null;
    }
}
