package one.modality.catering.backoffice.activities.kitchen.theme;

import dev.webfx.extras.theme.Facet;
import dev.webfx.stack.i18n.I18n;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * @author Bruno Salmon
 */
public enum TimeFacet {

    YEAR_MONTH_FACET,

    DAY_OF_WEEK_FACET,

    DATE_PANEL_FACET;

    static {
        TimeTheme.register();
    }

    public static Facet createDayOfWeekFacet(DayOfWeek dayOfWeek) {
        Text text = new Text();
        I18n.bindI18nProperties(text, dayOfWeek.name());
        return createDayOfWeekFacet(dayOfWeek, text, new StackPane(text));
    }

    public static Facet createDayOfWeekFacet(DayOfWeek dayOfWeek, Text text, Region container) {
        return new Facet(DAY_OF_WEEK_FACET, container)
                .setLogicValue(dayOfWeek)
                .setTextNode(text)
                .setRounded(true)
                .setBordered(true);
    }

    public static Facet createDatePanelFacet(LocalDate date) {
        Text text = new Text("" + date.getDayOfMonth());
        return createDatePanelFacet(date, text, new StackPane(text));
    }

    public static Facet createDatePanelFacet(LocalDate date, Text text, Region container) {
        return new Facet(DATE_PANEL_FACET, container)
                .setLogicValue(date)
                .setTextNode(text)
                .setRounded(true)
                .setShadowed(true);
    }

    public static Facet createYearMonthFacet(YearMonth yearMonth) {
        Text text = new Text();
        I18n.bindI18nProperties(text, "[" + yearMonth.getMonth().name() + "] " + (yearMonth.getYear() % 1000));
        StackPane stackPane = new StackPane(text);
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        return createYearMonthFacet(yearMonth, text,stackPane);
    }

    public static Facet createYearMonthFacet(YearMonth yearMonth, Text text, Region container) {
        return new Facet(YEAR_MONTH_FACET, container)
                .setLogicValue(yearMonth)
                .setTextNode(text);
    }
}
