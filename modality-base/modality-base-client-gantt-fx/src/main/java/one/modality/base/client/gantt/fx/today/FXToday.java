package one.modality.base.client.gantt.fx.today;

import dev.webfx.platform.scheduler.Scheduler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXToday {

    private final static ObjectProperty<LocalDate> todayProperty = new SimpleObjectProperty<>();

    static {
        updateTodayAndScheduleTomorrow(LocalDate.now());
    }

    private static void updateTodayAndScheduleTomorrow(LocalDate today) {
        todayProperty.set(today);
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        Scheduler.scheduleDelay(
                // Time between now and tomorrow in milliseconds
                tomorrow.toEpochDay() * 24 * 3600 * 1000 - System.currentTimeMillis(),
                // What to do when tomorrow arrived
                () -> updateTodayAndScheduleTomorrow(tomorrow));
    }


    public static ObjectProperty<LocalDate> todayProperty() {
        return todayProperty;
    }

    public static LocalDate getToday() {
        return todayProperty.get();
    }

    public static boolean isToday(LocalDate date) {
        return Objects.equals(date, getToday());
    }

}
