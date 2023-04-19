package one.modality.base.client.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.LayeredTimeLayout;
import dev.webfx.extras.timelayout.TimeLayout;
import dev.webfx.extras.timelayout.TimeWindow;
import dev.webfx.extras.timelayout.canvas.ChildCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.LayeredTimeCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.extras.timelayout.util.TimeUtil;
import dev.webfx.extras.timelayout.util.YearWeek;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import javafx.animation.Interpolator;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    public LayeredGanttCanvas() {
        yearsLayout.setInclusiveChildStartTimeReader(year -> year.atDay(1));
        yearsLayout.setInclusiveChildEndTimeReader(year -> year.atMonthDay(MonthDay.of(12, 31)));

        monthsLayout.setInclusiveChildStartTimeReader(yearMonth -> yearMonth.atDay(1));
        monthsLayout.setInclusiveChildEndTimeReader(YearMonth::atEndOfMonth);

        weeksLayout.setInclusiveChildStartTimeReader(yearWeek -> {
            LocalDate firstYearMonday = LocalDate.of(yearWeek.getYear(), 1, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
            return firstYearMonday.plus(yearWeek.getWeek() - 1, ChronoUnit.WEEKS);
        });
        weeksLayout.setInclusiveChildEndTimeReader(yearWeek -> {
            LocalDate firstYearMonday = LocalDate.of(yearWeek.getYear(), 1, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
            return firstYearMonday.plus(yearWeek.getWeek(), ChronoUnit.WEEKS).minus(1, ChronoUnit.DAYS);
        });

        // Adding these layouts as layers in the gantt canvas.
        // Note: the order is important regarding the vertical strips, so they are always displayed "in the background".
        // For example, if days are used to draw the strips, they must be drawn before drawing weeks, months, etc...
        // otherwise these strips would appear on top of them instead of behind them.
        addLayer(daysLayout, this::drawDay);
        addLayer(weeksLayout, this::drawWeek);
        addLayer(monthsLayout, this::drawMonth);
        addLayer(yearsLayout, this::drawYear);

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
        // Updating texts on i18n dictionary changes
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
        // Not used so far, but should be implemented
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
        updateLayersDrawingProperties();
        boolean isCanvasPaneManaged = canvasPane.isManaged();
        double vSpacing = 10;
        double y = 0;
        if (yearsLayout.isVisible()) {
            yearsLayout.setTopY(y);
            yearsLayout.setChildFixedHeight(yearHeight);
            y += yearHeight + vSpacing;
        }
        if (monthsLayout.isVisible()) {
            monthsLayout.setTopY(y);
            monthsLayout.setChildFixedHeight(monthHeight);
            y += monthHeight + vSpacing;
        }
        if (weeksLayout.isVisible()) {
            weeksLayout.setTopY(y);
            weeksLayout.setChildFixedHeight(weekHeight);
            y += weekHeight + vSpacing;
        }
        if (daysLayout.isVisible()) {
            daysLayout.setTopY(y);
            daysLayout.setChildFixedHeight(dayHeight);
            y += dayHeight + vSpacing;
        }
        layeredTimeLayout.markLayoutAsDirty();
        layeredTimeLayout.layout(canvas.getWidth(), canvas.getHeight());
        if (FXGanttVisibility.showEvents()) {
            //y += 1;
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
            layeredTimeCanvasDrawer.setBackgroundFill(TimeTheme.getCanvasBackgroundColor());
            redraw();
            dirtyCanvasScheduled = null;
        });
    }

    private void redraw() {
        updateLayersDrawingProperties();
        layeredTimeCanvasDrawer.redraw();
    }

    private double dayWidth, dayHeight, dayHPadding, dayRadius;
    private double weekWidth, weekHeight, weekHPadding, weekRadius;
    private double monthWidth, monthHeight, monthHPadding, monthRadius;
    private double yearWidth, yearHeight, yearHPadding, yearRadius;
    private Paint /*dayFill,*/ dayStroke, dayTextFill, weekFill, weekStroke, weekTextFill, /*monthFill,*/ monthStroke, monthTextFill, yearFill, yearStroke, yearTextFill;
    private Font dayOfWeekFont, dayOfMonthFont, weekFont, weekNumberFont, monthFont, yearFont;
    private GanttLayout<?, LocalDate> stripLayout;
    private Paint stripStroke;

    private final static Interpolator FONT_SIZE_INTERPOLATOR = Interpolator.SPLINE(0.5, 0.5, 0.1, 1);

    private static double clamp(double min, double value, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private static double fontSize(double minSize, double minFontSize, double maxSize, double maxFontSize, double size) {
        double fontSize = FONT_SIZE_INTERPOLATOR.interpolate(minFontSize, maxFontSize, clamp(0, (size - minSize) / (maxSize - minSize), 1));
        return fontSize;
    }

    private static double fontSize(double minWidth, double minWidthFontSize, double maxWidth, double maxWidthFontSize, double minHeight, double minHeightFontSize, double maxHeight, double maxHeightFontSize, double width, double height) {
        double widthFontSize = fontSize(minWidth, minWidthFontSize, maxWidth, maxWidthFontSize, width);
        double heightFontSize = fontSize(minHeight, minHeightFontSize, maxHeight, maxHeightFontSize, height);
        double fontSize = Math.min(widthFontSize, heightFontSize);
        return fontSize;
    }

    private static double fontSize(double width, double height) {
        return fontSize(70, 12, 700, 20, 10, 10, 50, 20, width, height);
    }

    private void updateLayersDrawingProperties() {
        boolean isVisible = FXGanttVisibility.isVisible();
        canvasPane.setVisible(isVisible);
        canvasPane.setManaged(isVisible);
        // Computing widths for day / week / month / year
        dayWidth = canvas.getWidth() / (timeWindowDuration + 1);
        weekWidth = 7 * dayWidth;
        yearWidth = 365 * dayWidth;
        monthWidth = yearWidth / 12;
        // Computing layers visibility
        layeredTimeLayout.getLayers().forEach(l -> l.setVisible(FXGanttVisibility.showEvents()));
        yearsLayout.setVisible(isVisible && weekWidth <= 20);
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
        // Setting stripLayout (which layout will be used as a based to draw the strips on the canvas)
        if (daysLayout.isVisible())
            stripLayout = daysLayout;
        else if (weeksLayout.isVisible())
            stripLayout = weeksLayout;
        else if (monthsLayout.isVisible())
            stripLayout = monthsLayout;
        else
            stripLayout = yearsLayout;
        stripStroke = FXLuminanceMode.isDarkMode() ? Color.gray(0.2) : Color.gray(0.85);
        // Computing heights for day / week / month / year
        boolean compactMode = FXLayoutMode.isCompactMode();
        double nodeHeight = compactMode ? 40 : 35; // Height of the top node
        if (yearsLayout.isVisible()) {
            yearHeight = nodeHeight;
            nodeHeight = 35;
        }
        if (monthsLayout.isVisible()) {
            monthHeight = nodeHeight;
        }
        weekHeight = 40;
        dayHeight = 40;
        // Computing fonts for day / week / month / year
        //dayFont = TextTheme.getFont(FontDef.font(14));
        double dayFontSize = fontSize(dayWidth, dayHeight / 2);
        dayOfWeekFont = TextTheme.getFont(FontDef.font(dayFontSize / 3 * 2));
        dayOfMonthFont = TextTheme.getFont(FontDef.font(FontWeight.BOLD, dayFontSize));
        monthFont = TextTheme.getFont(FontDef.font(fontSize(monthWidth, monthHeight)));
        double weekFontSize = fontSize(weekWidth, weekHeight / 2);
        weekFont = TextTheme.getFont(FontDef.font(weekFontSize / 3 * 2));
        weekNumberFont = TextTheme.getFont(FontDef.font(FontWeight.BOLD, weekFontSize));
        yearFont = TextTheme.getFont(FontDef.font(fontSize(yearWidth, yearHeight)));
        i18nWeekWidth = WebFxKitLauncher.measureText(i18nWeek, weekFont).getWidth();
        // Computing hPadding & radius for day / week / month / year
        yearHPadding = yearRadius = 10;
        if (monthWidth < 25) {
            monthHPadding = monthRadius = 0;
        } else {
            monthRadius = 0.5 * Math.min(monthWidth, monthHeight);
            monthHPadding = clamp(3, 0.02 * monthWidth, 10);
        }
        if (weekWidth < 25) {
            weekHPadding = weekRadius = 0;
        } else {
            weekRadius = 0.5 * Math.min(weekWidth, weekHeight);
            weekHPadding = clamp(3, 0.05 * weekWidth, 10);
        }
        if (dayWidth < 30) {
            dayHPadding = dayRadius = 0;
        } else {
            dayRadius = 0.5 * Math.min(dayWidth, dayHeight);
            dayHPadding = clamp(3, 0.05 * dayWidth, 10);
        }
        // Computing fills, strokes & textFills for day / week / month / year
        // dayFill depends on today
        dayStroke = TimeTheme.getDayOfWeekBorderColor();
        dayTextFill = TimeTheme.getDayOfWeekTextColor();
        weekFill = TimeTheme.getWeekBackgroundColor();
        weekStroke = compactMode ? TimeTheme.getMonthBorderColor() : Color.TRANSPARENT;
        weekTextFill = dayTextFill;
        // monthFill depends on today
        monthStroke = compactMode ? TimeTheme.getMonthBorderColor() : Color.TRANSPARENT;
        monthTextFill = TimeTheme.getMonthTextColor();
        yearFill = TimeTheme.getDayOfWeekBackgroundColor(DayOfWeek.MONDAY);
        yearStroke = Color.TRANSPARENT; // Color.LIGHTGRAY; // TimeTheme.getMonthBorderColor();
        yearTextFill = dayTextFill; //Color.GRAY;
    }

    private final String[] i18nMonths = new String[12];
    private final String[] i18nDaysOfWeek = new String[7];
    private String i18nWeek;
    private double i18nWeekWidth;

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
            strokeStrip(p, gc);
        TimeCanvasUtil.fillStrokeRect(p, yearHPadding, yearFill, yearStroke, yearRadius, gc);
        gc.setFont(yearFont);
        TimeCanvasUtil.fillCenterText(p, yearHPadding, String.valueOf(year), yearTextFill, gc);
    }

    private void drawMonth(YearMonth yearMonth, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == monthsLayout)
            strokeStrip(p, gc);
        Month month = yearMonth.getMonth();
        TimeCanvasUtil.fillStrokeRect(p, monthHPadding, TimeTheme.getYearMonthBackgroundColor(yearMonth), monthStroke, monthRadius, gc);
        String text = i18nMonths[month.ordinal()];
        boolean mmm, m, yyyy = !yearsLayout.isVisible(), yy = false;
        if (yyyy) {
            yy = monthWidth < 125;
            mmm = monthWidth < 100;
            m = monthWidth < 50;
        } else {
            mmm = monthWidth < 80;
            m = monthWidth < 35;
        }
        gc.setFont(monthFont);
        if (m)
            text = text.substring(0, 1);
        else if (mmm)
            text = text.substring(0, 3);
        if (yy)
            text += " " + (yearMonth.getYear() % 100);
        else if (yyyy)
            text += " " + yearMonth.getYear();
        TimeCanvasUtil.fillCenterText(p, monthHPadding, text, monthTextFill, gc);
    }

    private void drawWeek(YearWeek yearWeek, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == weeksLayout)
            strokeStrip(p, gc);
        TimeCanvasUtil.fillStrokeRect(p, weekHPadding, weekFill, weekStroke, weekRadius, gc);
        String week = i18nWeek;
        if (weekWidth - 2 * weekHPadding < i18nWeekWidth + 5)
            week = week.substring(0, 1);
        String weekNumber = (yearWeek.getWeek() < 10 ? "0" : "") + yearWeek.getWeek();
        double h = p.getHeight(), h2 = h / 2, vPadding = h / 16;
        gc.setFont(weekFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + vPadding, p.getWidth(), h2, weekHPadding, week, weekTextFill, VPos.CENTER, TextAlignment.CENTER, gc);
        gc.setFont(weekNumberFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + h2, p.getWidth(), h2 - vPadding, weekHPadding, weekNumber, weekTextFill, VPos.CENTER, TextAlignment.CENTER, gc);
    }

    private void drawDay(LocalDate day, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayout == daysLayout)
            strokeStrip(p, gc);
        TimeCanvasUtil.fillStrokeRect(p, dayHPadding, TimeTheme.getDayOfWeekBackgroundColor(day), dayStroke, dayRadius, gc);
        String dayOfWeek = i18nDaysOfWeek[day.getDayOfWeek().ordinal()];
        if (dayWidth < 100)
            dayOfWeek = dayOfWeek.substring(0, 3);
        String dayOfMonth = (day.getDayOfMonth() < 10 ? "0" : "") + day.getDayOfMonth();
        double h = p.getHeight(), h2 = h / 2, vPadding = h / 16;
        gc.setFont(dayOfWeekFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + vPadding, p.getWidth(), h2, dayHPadding, dayOfWeek, dayTextFill, VPos.CENTER, TextAlignment.CENTER, gc);
        gc.setFont(dayOfMonthFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + h2, p.getWidth(), h2 - vPadding, dayHPadding, dayOfMonth, dayTextFill, VPos.CENTER, TextAlignment.CENTER, gc);
    }

    private void strokeStrip(ChildPosition<LocalDate> p, GraphicsContext gc) {
        double canvasHeight = gc.getCanvas().getHeight();
        TimeCanvasUtil.strokeRect(p.getX(), 0, p.getWidth(), canvasHeight, 0, canvasHeight > p.getY() + p.getHeight() ? stripStroke : Color.TRANSPARENT, 0, gc);
    }

}
