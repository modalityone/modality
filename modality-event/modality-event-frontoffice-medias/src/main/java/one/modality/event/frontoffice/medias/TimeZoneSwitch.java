package one.modality.event.frontoffice.medias;

import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.i18n.I18n;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;

import java.time.*;

/**
 * @author Bruno Salmon
 */
public final class TimeZoneSwitch {

    private static final ZoneId EVENT_ZONE_ID = Event.getEventZoneId();
    private static final ZoneId USER_ZONE_ID = ZoneId.systemDefault();
    private static final boolean EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME;

    static {
        LocalTime arbitraryLocalTime = LocalTime.of(9, 30); // 09:30
        EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME = arbitraryLocalTime.equals(convertEventLocalTimeToUserLocalTime(arbitraryLocalTime));
/*
        Console.log("⚛️⚛️⚛️⚛️⚛️ EVENT_ZONE_ID = " + EVENT_ZONE_ID);
        Console.log("⚛️⚛️⚛️⚛️⚛️ USER_ZONE_ID = " + USER_ZONE_ID);
        Console.log("⚛️⚛️⚛️⚛️⚛️ EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME = " + EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME);
*/
    }

    private static final BooleanProperty eventLocalTimeSelectedProperty = new SimpleBooleanProperty(EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME);

    public static Node createTimezoneSwitchBox() {
        if (EVENT_AND_USER_TIME_ZONES_HAVE_SAME_TIME) {
            Node unmanagedInvisibleNode = new Region();
            Layouts.setManagedAndVisibleProperties(unmanagedInvisibleNode, false);
            return unmanagedInvisibleNode;
        }
        Switch timezoneSwitch = new Switch();
        timezoneSwitch.selectedProperty().bindBidirectional(eventLocalTimeSelectedProperty);
        HBox hBox = new HBox(10, I18n.newText(MediasI18nKeys.TimeZoneUK), timezoneSwitch);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    public static ObservableBooleanValue eventLocalTimeSelectedProperty() {
        return eventLocalTimeSelectedProperty;
    }

    public static ObservableBooleanValue userLocalTimeSelectedProperty() {
        return eventLocalTimeSelectedProperty.not();
    }

    public static LocalTime convertEventLocalTimeToUserLocalTime(LocalTime eventLocalTime) {
        LocalDate today = LocalDate.now(); // arbitrary date
        ZonedDateTime eventZonedDateTime = ZonedDateTime.of(today, eventLocalTime, EVENT_ZONE_ID);
        ZonedDateTime userZoneDateTime = eventZonedDateTime.withZoneSameInstant(USER_ZONE_ID);
        return userZoneDateTime.toLocalTime();
    }

    public static LocalDateTime convertEventLocalDateTimeToUserLocalDateTime(LocalDateTime eventLocalDateTime) {
        ZonedDateTime eventZonedDateTime = eventLocalDateTime.atZone(EVENT_ZONE_ID);
        ZonedDateTime userZonedDateTime = eventZonedDateTime.withZoneSameInstant(USER_ZONE_ID);
        return userZonedDateTime.toLocalDateTime();
    }

    public static LocalDate convertEventLocalDateToUserLocalDate(LocalDate eventLocalDate) {
        return convertEventLocalDateTimeToUserLocalDateTime(eventLocalDate.atStartOfDay()).toLocalDate();
    }
}
