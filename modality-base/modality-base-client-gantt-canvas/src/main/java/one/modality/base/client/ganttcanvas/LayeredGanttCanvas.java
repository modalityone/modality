package one.modality.base.client.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.*;
import dev.webfx.extras.timelayout.canvas.ChildCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.LayeredTimeCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.extras.timelayout.util.TimeUtil;
import dev.webfx.extras.timelayout.util.YearWeek;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.gantt.visibility.fx.FXGanttVisibility;
import one.modality.base.client.time.theme.TimeTheme;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.BiConsumer;

/**
 * @author Bruno Salmon
 */
public final class LayeredGanttCanvas implements TimeWindow<LocalDate> {

    private final Canvas canvas = new Canvas();
    private double computedCanvasHeight; // computed by markLayoutAsDirty()
    private final Pane canvasPane = new Pane(canvas) {
        @Override
        protected void layoutChildren() {
            double newCanvasWidth = getWidth();
            double newCanvasHeight = computedCanvasHeight;
            boolean canvasWidthChanged = newCanvasWidth != canvas.getWidth();
            boolean canvasHeightChanged = newCanvasHeight != canvas.getHeight();
            if (canvasWidthChanged || canvasHeightChanged) {
                canvas.setWidth(newCanvasWidth);
                canvas.setHeight(newCanvasHeight);
                layoutInArea(canvas, 0, 0, newCanvasWidth, newCanvasHeight, 0, HPos.LEFT, VPos.TOP);
                if (canvasWidthChanged)
                    relayout();
                else
                    redraw();
            }
        }
    };
    private final GanttLayout<Year, LocalDate> yearsLayout = new GanttLayout<>();
    private final GanttLayout<YearMonth, LocalDate> monthsLayout = new GanttLayout<>();
    private final GanttLayout<YearWeek, LocalDate> weeksLayout = new GanttLayout<>();
    private final GanttLayout<LocalDate, LocalDate> daysLayout = new GanttLayout<>();
    private final LayeredTimeLayout<LocalDate> layeredTimeLayout = LayeredTimeLayout.create();
    private final LayeredTimeCanvasDrawer<LocalDate> layeredTimeCanvasDrawer = new LayeredTimeCanvasDrawer<>(canvas, layeredTimeLayout);
    private long timeWindowDuration;
    private final FontDef MONTH_FONT_DEF = FontDef.font(16);
    private final FontDef DAY_FONT_DEF = FontDef.font(14);

    public LayeredGanttCanvas() {
        yearsLayout.setInclusiveChildStartTimeReader(year -> year.atDay(1));
        yearsLayout.setInclusiveChildEndTimeReader(year -> year.atMonthDay(MonthDay.of(12, 31)));
        addLayer(yearsLayout, this::drawYear);

        monthsLayout.setInclusiveChildStartTimeReader(yearMonth -> yearMonth.atDay(1));
        monthsLayout.setInclusiveChildEndTimeReader(YearMonth::atEndOfMonth);
        addLayer(monthsLayout, this::drawMonth);

        weeksLayout.setInclusiveChildStartTimeReader(yearWeek -> {
            LocalDate firstYearMonday = LocalDate.of(yearWeek.getYear(), 1, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
            return firstYearMonday.plus(yearWeek.getWeek() - 1, ChronoUnit.WEEKS);
        });
        weeksLayout.setInclusiveChildEndTimeReader(yearWeek -> {
            LocalDate firstYearMonday = LocalDate.of(yearWeek.getYear(), 1, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
            return firstYearMonday.plus(yearWeek.getWeek(), ChronoUnit.WEEKS).minus(1, ChronoUnit.DAYS);
        });
        addLayer(weeksLayout, this::drawWeek);

        addLayer(daysLayout, this::drawDay);

        layeredTimeLayout.setOnTimeWindowChanged((start, end) -> {
            timeWindowDuration = ChronoUnit.DAYS.between(start, end);
            yearsLayout.getChildren().setAll(TimeUtil.generateYears(Year.from(start), Year.from(end)));
            monthsLayout.getChildren().setAll(TimeUtil.generateYearMonths(YearMonth.from(start), YearMonth.from(end)));
            weeksLayout.getChildren().setAll(TimeUtil.generateYearWeeks(YearWeek.from(start), YearWeek.from(end)));
            daysLayout.getChildren().setAll(TimeUtil.generateLocalDates(start, end));
            markLayoutAsDirty();
        });

        // Redrawing the canvas on theme mode changes
        ThemeRegistry.addModeChangeListener(this::markCanvasAsDirty);
        // Recomputing layout on layout mode changes (compact / standard mode)
        FXProperties.runOnPropertiesChange(this::markLayoutAsDirty, FXLayoutMode.layoutModeProperty(), FXGanttVisibility.ganttVisibilityProperty());
        FXProperties.runNowAndOnPropertiesChange(this::onLanguageChanged, I18n.dictionaryProperty());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Pane getCanvasPane() {
        return canvasPane;
    }

    @Override
    public ObjectProperty<LocalDate> timeWindowStartProperty() {
        return layeredTimeLayout.timeWindowStartProperty();
    }

    @Override
    public ObjectProperty<LocalDate> timeWindowEndProperty() {
        return layeredTimeLayout.timeWindowEndProperty();
    }

    @Override
    public void setOnTimeWindowChanged(BiConsumer<LocalDate, LocalDate> timeWindowChangedHandler) {

    }

    public void setTimeWindow(LocalDate timeWindowStart, LocalDate timeWindowEnd) {
        layeredTimeLayout.setTimeWindow(timeWindowStart, timeWindowEnd); // see layeredTimeLayout.setOnTimeWindowChanged() callback in constructor
    }

    public <C> void addLayer(TimeLayout<C, LocalDate> timeLayout, ChildCanvasDrawer<C, LocalDate> childCanvasDrawer) {
        layeredTimeLayout.addLayer(timeLayout);
        layeredTimeCanvasDrawer.setLayerChildCanvasDrawer(timeLayout, childCanvasDrawer);
        markLayoutAsDirty();
    }

    private Scheduled dirtyLayoutScheduled;

    public void markLayoutAsDirty() {
        if (dirtyLayoutScheduled != null)
            return;
        dirtyLayoutScheduled = UiScheduler.scheduleInAnimationFrame(() -> {
            relayout();
            dirtyLayoutScheduled = null;
        });
    }

    private void relayout() {
        boolean wasCanvasPaneManaged = canvasPane.isManaged();
        updateLayersVisibility();
        boolean isCanvasPaneManaged = canvasPane.isManaged();
        boolean compactMode = FXLayoutMode.isCompactMode();
        double y = 0;
        double nodeHeight = compactMode ? 40 : 20;
        if (yearsLayout.isVisible()) {
            yearsLayout.setTopY(y);
            yearsLayout.setChildFixedHeight(nodeHeight);
            y += nodeHeight;
            nodeHeight = 20;
        }
        if (monthsLayout.isVisible()) {
            monthsLayout.setTopY(y);
            monthsLayout.setChildFixedHeight(nodeHeight);
            y += nodeHeight;
            nodeHeight = 20;
        }
        if (weeksLayout.isVisible()) {
            weeksLayout.setTopY(y);
            weeksLayout.setChildFixedHeight(nodeHeight);
            y += nodeHeight;
            nodeHeight = 20;
        }
        if (daysLayout.isVisible()) {
            daysLayout.setTopY(y);
            daysLayout.setChildFixedHeight(nodeHeight);
            y += nodeHeight;
        }
        layeredTimeLayout.markLayoutAsDirty();
        layeredTimeLayout.layout(canvas.getWidth(), canvas.getHeight());
        if (FXGanttVisibility.showEvents()) {
            y += 1;
            ObservableList<TimeLayout<?, LocalDate>> layers = layeredTimeLayout.getLayers();
            for (int i = 4; i < layers.size(); i++) {
                TimeLayout<?, LocalDate> layer = layers.get(i);
                layer.setTopY(y);
                y += layer.getRowsCount() * layer.getChildFixedHeight();
            }
        }
        y = Math.min(y, Screen.getPrimary().getVisualBounds().getHeight());
        computedCanvasHeight = y;
        Animations.animateProperty(canvasPane.prefHeightProperty(), y, wasCanvasPaneManaged && isCanvasPaneManaged);
        layeredTimeCanvasDrawer.redraw();
    }

    private Scheduled dirtyCanvasScheduled;

    public void markCanvasAsDirty() {
        if (dirtyCanvasScheduled != null)
            return;
        dirtyCanvasScheduled = UiScheduler.scheduleInAnimationFrame(() -> {
            redraw();
            dirtyCanvasScheduled = null;
        });
    }

    private void redraw() {
        updateLayersVisibility();
        layeredTimeCanvasDrawer.redraw();
    }

    private double dayWidth, weekWidth, monthWidth;
    private GanttLayout<?, LocalDate> stripLayout;

    private void updateLayersVisibility() {
        boolean isVisible = FXGanttVisibility.isVisible();
        canvasPane.setVisible(isVisible);
        canvasPane.setManaged(isVisible);
        dayWidth = canvas.getWidth() / (timeWindowDuration + 1);
        weekWidth = 7 * dayWidth;
        monthWidth = dayWidth * 365 / 12;
        layeredTimeLayout.getLayers().forEach(l -> l.setVisible(FXGanttVisibility.showEvents()));
        yearsLayout.setVisible(isVisible && monthWidth < 150);
        monthsLayout.setVisible(FXGanttVisibility.showMonths() && monthWidth > 15);
        if (!FXGanttVisibility.showDays()) {
            weeksLayout.setVisible(false);
            daysLayout.setVisible(false);
        } else {
            boolean showDays = dayWidth > 20;
            boolean showWeeks = !showDays && weekWidth > 20;
            weeksLayout.setVisible(showWeeks);
            daysLayout.setVisible(showDays);
        }
        if (daysLayout.isVisible())
            stripLayout = daysLayout;
        else if (weeksLayout.isVisible())
            stripLayout = weeksLayout;
        else if (monthsLayout.isVisible())
            stripLayout = monthsLayout;
        else
            stripLayout = yearsLayout;
    }

    private final String[] i18nMonths = new String[12];
    private final String[] i18nDaysOfWeek = new String[7];
    private String i18nWeek;

    private void onLanguageChanged() {
        for (int i = 0; i < 12; i++)
            i18nMonths[i] = I18n.getI18nText(Month.of(i + 1));
        i18nWeek = I18n.getI18nText("WEEK");
        for (int i = 0; i < 7; i++)
            i18nDaysOfWeek[i] = I18n.getI18nText(DayOfWeek.of(i + 1));
        markCanvasAsDirty();
    }

    private void drawYear(Year year, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == yearsLayout)
            drawStrip(p, TimeTheme.getDayOfWeekCanvasBackgroundColor((Object) null), gc);
        TimeCanvasUtil.fillStrokeRect(p, Color.ALICEBLUE, TimeTheme.getMonthBorderColor(), gc);
        gc.setFont(TextTheme.getFont(MONTH_FONT_DEF));
        TimeCanvasUtil.fillCenterText(p, String.valueOf(year), Color.GRAY, gc);
    }

    private void drawMonth(YearMonth yearMonth, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == monthsLayout)
            drawStrip(p, TimeTheme.getDayOfWeekCanvasBackgroundColor((Object) null), gc);
        Month month = yearMonth.getMonth();
        TimeCanvasUtil.fillStrokeRect(p, TimeTheme.getYearMonthBackgroundColor(yearMonth), TimeTheme.getMonthBorderColor(), gc);
        String text = i18nMonths[month.ordinal()];
        if (!yearsLayout.isVisible())
            text += " " + yearMonth.getYear();
        else if (monthWidth < 40)
            text = text.substring(0, 1);
        else if (monthWidth < 110)
            text = text.substring(0, 3);
        gc.setFont(TextTheme.getFont(MONTH_FONT_DEF));
        TimeCanvasUtil.fillCenterText(p, text, TimeTheme.getMonthTextColor(), gc);
    }

    private void drawWeek(YearWeek yearWeek, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == weeksLayout)
            drawStrip(p, TimeTheme.getDayOfWeekCanvasBackgroundColor((Object) null), gc);
        TimeCanvasUtil.fillStrokeRect(p, Color.ALICEBLUE, TimeTheme.getMonthBorderColor(), gc);
        String text = String.valueOf(yearWeek.getWeek());
        if (weekWidth > 45)
            text = i18nWeek.charAt(0) + text;
        gc.setFont(TextTheme.getFont(MONTH_FONT_DEF));
        TimeCanvasUtil.fillCenterText(p, text, Color.GRAY, gc);
    }

    private void drawDay(LocalDate day, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == daysLayout)
            drawStrip(p, TimeTheme.getDayOfWeekCanvasBackgroundColor(day), gc);
        TimeCanvasUtil.fillStrokeRect(p, TimeTheme.getDayOfWeekBackgroundColor(day), TimeTheme.getDayOfWeekBorderColor(), gc);
        String text = String.valueOf(day.getDayOfMonth());
        if (dayWidth > 110)
            text = i18nDaysOfWeek[day.getDayOfWeek().ordinal()] + " " + text;
        else if (dayWidth > 60)
            text = i18nDaysOfWeek[day.getDayOfWeek().ordinal()].substring(0, 3) + " " + text;
        else if (dayWidth > 35)
            text = i18nDaysOfWeek[day.getDayOfWeek().ordinal()].charAt(0) + " " + text;
        gc.setFont(TextTheme.getFont(DAY_FONT_DEF));
        TimeCanvasUtil.fillText(p.getX(), p.getY(), p.getWidth(), 20, text, TimeTheme.getDayOfWeekTextColor(), VPos.CENTER, TextAlignment.CENTER, gc);
    }

    private void drawStrip(ChildPosition<LocalDate> p, Paint fill, GraphicsContext gc) {
        TimeCanvasUtil.fillStrokeRect(p.getX(), p.getY(), p.getWidth(), gc.getCanvas().getHeight() - p .getY(), fill, TimeTheme.getDayOfWeekBorderColor(), gc);
    }

}
