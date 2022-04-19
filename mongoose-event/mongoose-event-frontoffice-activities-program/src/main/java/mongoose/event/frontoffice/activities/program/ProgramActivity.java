package mongoose.event.frontoffice.activities.program;

import javafx.scene.layout.BorderPane;
import mongoose.ecommerce.client.businessdata.feesgroup.FeesGroup;
import mongoose.event.client.controls.bookingcalendar.BookingCalendar;
import mongoose.ecommerce.client.activity.bookingprocess.BookingProcessActivity;
import mongoose.ecommerce.client.businessdata.preselection.OptionsPreselection;
import mongoose.base.client.icons.MongooseIcons;
import mongoose.event.client.controls.sectionpanel.SectionPanelFactory;
import dev.webfx.framework.client.ui.util.layout.LayoutUtil;
import dev.webfx.platform.shared.services.log.Logger;

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
        BorderPane calendarSection = SectionPanelFactory.createSectionPanel(MongooseIcons.calendarMonoSvg16JsonUrl, "Timetable");
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
                .onFailure(Logger::log)
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
