package one.modality.event.frontoffice.medias;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import java.time.*;

/**
 * @author Bruno Salmon
 */
public final class TimeZoneSwitch {

    private static final ZoneId USER_ZONE_ID = ZoneId.systemDefault();
    private static final TimeZoneSwitch globalTimeZoneSwitch = new TimeZoneSwitch();

    public static TimeZoneSwitch getGlobal() {
        return globalTimeZoneSwitch;
    }

    private final BooleanProperty eventLocalTimeSelectedProperty = new SimpleBooleanProperty();
    private final BooleanProperty eventAndUserTimeZonesHaveSameTimeProperty = new SimpleBooleanProperty();
    private final ObjectProperty<ZoneId> eventZoneIdProperty = FXProperties.newObjectProperty(USER_ZONE_ID,eventZoneId -> {
        boolean sameTime = true;
        if (eventZoneId != null) {
            LocalTime arbitraryLocalTime = LocalTime.of(9, 30); // 09:30
            sameTime = arbitraryLocalTime.equals(convertEventLocalTimeToUserLocalTime(arbitraryLocalTime));
        }
        eventAndUserTimeZonesHaveSameTimeProperty.setValue(sameTime);
        eventLocalTimeSelectedProperty.setValue(sameTime);
    });

    public TimeZoneSwitch() {
    }

    public ZoneId getEventZoneId() {
        return eventZoneIdProperty.getValue();
    }

    public ObjectProperty<ZoneId> eventZoneIdProperty() {
        return eventZoneIdProperty;
    }

    public void setEventZoneId(ZoneId eventZoneId) {
        eventZoneIdProperty.setValue(eventZoneId);
    }

    public Node createTimezoneSwitchBox() {
        Switch timezoneSwitch = new Switch();
        timezoneSwitch.selectedProperty().bindBidirectional(eventLocalTimeSelectedProperty);
        HBox hBox = new HBox(10, I18n.newText(MediasI18nKeys.TimeZoneTime1, eventZoneIdProperty), timezoneSwitch);
        hBox.setAlignment(Pos.CENTER);
        Layouts.bindManagedAndVisiblePropertiesTo(eventAndUserTimeZonesHaveSameTimeProperty.not(), hBox);
        return hBox;
    }

    public ObservableBooleanValue eventLocalTimeSelectedProperty() {
        return eventLocalTimeSelectedProperty;
    }

    public ObservableBooleanValue userLocalTimeSelectedProperty() {
        return eventLocalTimeSelectedProperty.not();
    }

    public LocalTime convertEventLocalTimeToUserLocalTime(LocalTime eventLocalTime) {
        ZoneId eventZoneId = getEventZoneId();
        if (eventZoneId == null)
            return eventLocalTime;
        LocalDate today = LocalDate.now(); // arbitrary date
        ZonedDateTime eventZonedDateTime = ZonedDateTime.of(today, eventLocalTime, eventZoneId);
        ZonedDateTime userZoneDateTime = eventZonedDateTime.withZoneSameInstant(USER_ZONE_ID);
        return userZoneDateTime.toLocalTime();
    }

    public LocalDateTime convertEventLocalDateTimeToUserLocalDateTime(LocalDateTime eventLocalDateTime) {
        ZoneId eventZoneId = getEventZoneId();
        if (eventZoneId == null)
            return eventLocalDateTime;
        ZonedDateTime eventZonedDateTime = eventLocalDateTime.atZone(eventZoneId);
        ZonedDateTime userZonedDateTime = eventZonedDateTime.withZoneSameInstant(USER_ZONE_ID);
        return userZonedDateTime.toLocalDateTime();
    }

    public LocalDate convertEventLocalDateToUserLocalDate(LocalDate eventLocalDate) {
        return convertEventLocalDateTimeToUserLocalDateTime(eventLocalDate.atStartOfDay()).toLocalDate();
    }
}
