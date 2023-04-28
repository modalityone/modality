package one.modality.base.client.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.MultiLayerLocalDateLayout;
import dev.webfx.extras.timelayout.TimeLayout;
import dev.webfx.extras.timelayout.TimeWindow;
import dev.webfx.extras.timelayout.canvas.*;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.extras.timelayout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.timelayout.util.YearWeek;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18n;
import javafx.animation.Interpolator;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.time.theme.TimeTheme;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * @author Bruno Salmon
 */
public final class DatedGanttCanvas implements TimeWindow<LocalDate> {

    private final LocalDateGanttLayout<Year> yearsLayer = LocalDateGanttLayout.createYearLocalDateGanttLayout();
    private final LocalDateGanttLayout<YearMonth> monthsLayer = LocalDateGanttLayout.createYearMonthLocalDateGanttLayout();
    private final LocalDateGanttLayout<YearWeek> weeksLayer = LocalDateGanttLayout.createYearWeekLocalDateGanttLayout();
    private final LocalDateGanttLayout<LocalDate> daysLayer = LocalDateGanttLayout.createDayLocalDateGanttLayout();
    private final MultiLayerLocalDateLayout globalLayout = MultiLayerLocalDateLayout.create();
    private final MultiLayerLocalDateCanvasDrawer globalCanvasDrawer = new MultiLayerLocalDateCanvasDrawer(globalLayout);
    private final TimeCanvasPane timeCanvasPane = new TimeCanvasPane(globalLayout, globalCanvasDrawer);
    private final LocalDateCanvasInteractionManager canvasInteractionManager = new LocalDateCanvasInteractionManager(globalCanvasDrawer, globalLayout);
    private final String[] i18nMonths = new String[12];
    private final String[] i18nDaysOfWeek = new String[7];
    private String i18nWeek;
    private double i18nWeekWidth;

    public DatedGanttCanvas() {
        // Adding the layers in the gantt canvas.
        addLayer(daysLayer, this::drawDay);
        addLayer(weeksLayer, this::drawWeek);
        addLayer(monthsLayer, this::drawMonth);
        addLayer(yearsLayer, this::drawYear);
        // Note: the order is important regarding the vertical strips, so they are always displayed "in the background".
        // For example, if days are used to draw the strips, they must be drawn before drawing weeks, months, etc...
        // otherwise these strips would appear on top of them instead of behind.

        // Some layout properties (such as layer visibilities, vertical position, height...) must be set before the layout pass
        globalLayout.addOnBeforeLayout(this::setLayoutPropertiesBeforeLayoutPass);
        // Some draw properties (such as fonts, colors, paddings, radius...) must be set before the draw pass
        globalCanvasDrawer.addOnBeforeDraw(this::setDrawPropertiesBeforeDrawPass);

        // Redrawing the canvas on theme mode changes (because some draw properties depend on the theme)
        ThemeRegistry.addModeChangeListener(this::markCanvasAsDirty);

        // Requesting a new layout pass on some events that impact the layout properties:
        FXProperties.runOnPropertiesChange(this::markLayoutAsDirty,
                // 1) when the layout mode changes (ex: compact mode) => impact the first layer height
                FXLayoutMode.layoutModeProperty(),
                // 2) when the gantt visibility changes (ex: with/without events) => impact layers visibility
                FXGanttVisibility.ganttVisibilityProperty());

        // Updating i18n texts when necessary
        FXProperties.runNowAndOnPropertiesChange(this::updateI18nTexts, I18n.dictionaryProperty());
    }

    public Pane getCanvasContainer() {
        return timeCanvasPane;
    }

    @Override
    public ObjectProperty<LocalDate> timeWindowStartProperty() {
        return globalLayout.timeWindowStartProperty();
    }

    @Override
    public ObjectProperty<LocalDate> timeWindowEndProperty() {
        return globalLayout.timeWindowEndProperty();
    }


    public void setTimeWindow(LocalDate timeWindowStart, LocalDate timeWindowEnd) {
        globalLayout.setTimeWindow(timeWindowStart, timeWindowEnd); // see globalLayout.setOnTimeWindowChanged() callback in constructor
    }

    private void updateI18nTexts() {
        for (int i = 0; i < 12; i++)
            i18nMonths[i] = I18n.getI18nText(Month.of(i + 1));
        i18nWeek = I18n.getI18nText("WEEK");
        for (int i = 0; i < 7; i++)
            i18nDaysOfWeek[i] = I18n.getI18nText(DayOfWeek.of(i + 1));
        markCanvasAsDirty();
    }

    public void setupFXBindings() {
        bindFXGanttTimeWindow();
        bindFXGanttSelection();
    }

    private void bindFXGanttTimeWindow() {
        bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());
    }

    private void bindFXGanttSelection() {
        FXGanttSelection.ganttSelectedObjectProperty().bindBidirectional(globalLayout.selectedChildProperty());
    }

    public void setInteractive(boolean interactive) {
        canvasInteractionManager.setInteractive(interactive);
    }

    public <C> void addLayer(TimeLayout<C, LocalDate> layer, ChildDrawer<C, LocalDate> layerCanvasDrawer) {
        globalLayout.addLayer(layer);
        globalCanvasDrawer.setLayerChildDrawer(layer, layerCanvasDrawer);
    }

    public void markLayoutAsDirty() { // may be called several times, but only 1 call will happen in the animation frame
        globalLayout.markLayoutAsDirty();
    }

    public void markCanvasAsDirty() { // may be called several times, but only 1 call will happen in the animation frame
        globalCanvasDrawer.markDrawAreaAsDirty();
    }

    // Day graphical properties (general ones, shared by all days)
    private double dayWidth, dayHeight, dayHPadding, dayRadius;
    private Paint /*dayFill,*/ dayStroke, dayTextFill, daySelectedTextFill;
    private Font dayOfWeekFont, dayOfMonthFont;

    // Week graphical properties (general ones, shared by all weeks)
    private double weekWidth, weekHeight, weekHPadding, weekRadius;
    private Paint weekFill, weekSelectedFill, weekStroke, weekTextFill, weekSelectedTextFill;
    private Font weekFont, weekNumberFont;

    // Month graphical properties (general ones, shared by all months)
    private double monthWidth, monthHeight, monthHPadding, monthRadius;
    private Paint /*monthFill,*/ monthStroke, monthTextFill;
    private Font monthFont;

    // Year graphical properties (general ones, shared by all years)
    private double yearWidth, yearHeight, yearHPadding, yearRadius;
    private Paint yearFill, yearSelectedFill, yearStroke, yearTextFill, yearSelectedTextFill;
    private Font yearFont;

    // Strip graphical properties
    private Paint stripStroke;
    private GanttLayout<?, LocalDate> stripLayer;

    private final static Interpolator FONT_SIZE_INTERPOLATOR = Interpolator.SPLINE(0.5, 0.5, 0.1, 1);

    private static double clamp(double min, double value, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private static double fontSize(double minSize, double minFontSize, double maxSize, double maxFontSize, double size) {
        return FONT_SIZE_INTERPOLATOR.interpolate(minFontSize, maxFontSize, clamp(0, (size - minSize) / (maxSize - minSize), 1));
    }

    private static double fontSize(double minWidth, double minWidthFontSize, double maxWidth, double maxWidthFontSize, double minHeight, double minHeightFontSize, double maxHeight, double maxHeightFontSize, double width, double height) {
        double widthFontSize = fontSize(minWidth, minWidthFontSize, maxWidth, maxWidthFontSize, width);
        double heightFontSize = fontSize(minHeight, minHeightFontSize, maxHeight, maxHeightFontSize, height);
        return Math.min(widthFontSize, heightFontSize);
    }

    private static double fontSize(double width, double height) {
        return fontSize(70, 12, 700, 20, 10, 10, 50, 20, width, height);
    }

    private void setLayoutPropertiesBeforeLayoutPass() {
        boolean isVisible = FXGanttVisibility.isVisible();
        timeCanvasPane.setVisible(isVisible);
        timeCanvasPane.setManaged(isVisible);
        // Computing widths for day / week / month / year
        long timeWindowDuration = ChronoUnit.DAYS.between(getTimeWindowStart(), getTimeWindowEnd());
        dayWidth = globalLayout.getWidth() / (timeWindowDuration + 1);
        weekWidth = 7 * dayWidth;
        yearWidth = 365 * dayWidth;
        monthWidth = yearWidth / 12;
        // Computing layers visibility
        globalLayout.getLayers().forEach(l -> l.setVisible(FXGanttVisibility.showEvents()));
        yearsLayer.setVisible(isVisible && weekWidth <= 20);
        monthsLayer.setVisible(FXGanttVisibility.showMonths() && monthWidth > 15);
        if (!FXGanttVisibility.showDays()) {
            weeksLayer.setVisible(false);
            daysLayer.setVisible(false);
        } else {
            boolean showDays = dayWidth > 20;
            boolean showWeeks = !showDays && weekWidth > 20;
            weeksLayer.setVisible(showWeeks);
            daysLayer.setVisible(showDays);
        }

        // Computing heights for day / week / month / year
        boolean compactMode = FXLayoutMode.isCompactMode();
        double nodeHeight = compactMode ? 40 : 35; // Height of the top node
        double vSpacing = 10;
        double y = 0;
        if (yearsLayer.isVisible()) {
            yearHeight = nodeHeight;
            nodeHeight = 35;
            yearsLayer.setTopY(y);
            yearsLayer.setChildFixedHeight(yearHeight);
            y += yearHeight + vSpacing;
        }
        if (monthsLayer.isVisible()) {
            monthHeight = nodeHeight;
            monthsLayer.setTopY(y);
            monthsLayer.setChildFixedHeight(monthHeight);
            y += monthHeight + vSpacing;
        }
        weekHeight = 40;
        if (weeksLayer.isVisible()) {
            weeksLayer.setTopY(y);
            weeksLayer.setChildFixedHeight(weekHeight);
            y += weekHeight + vSpacing;
        }
        dayHeight = 40;
        if (daysLayer.isVisible()) {
            daysLayer.setTopY(y);
            daysLayer.setChildFixedHeight(dayHeight);
            y += dayHeight + vSpacing;
        }
        if (FXGanttVisibility.showEvents()) {
            ObservableList<TimeLayout<?, LocalDate>> layers = globalLayout.getLayers();
            for (int i = 4; i < layers.size(); i++) {
                TimeLayout<?, LocalDate> layer = layers.get(i);
                layer.setTopY(y);
            }
        }
    }

    private void setDrawPropertiesBeforeDrawPass() {
        // Setting stripLayout (which layout will be used as a based to draw the strips on the canvas)
        if (daysLayer.isVisible())
            stripLayer = daysLayer;
        else if (weeksLayer.isVisible())
            stripLayer = weeksLayer;
        else if (monthsLayer.isVisible())
            stripLayer = monthsLayer;
        else
            stripLayer = yearsLayer;
        stripStroke = FXLuminanceMode.isDarkMode() ? Color.gray(0.2) : Color.gray(0.85);
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
        // Computing general graphical properties
        // Day
        dayStroke = TimeTheme.getDayOfWeekBorderColor();
        dayTextFill = TimeTheme.getDayOfWeekTextColor(false);
        daySelectedTextFill = TimeTheme.getDayOfWeekTextColor(true);

        // Week
        boolean compactMode = FXLayoutMode.isCompactMode();
        weekFill = TimeTheme.getWeekBackgroundColor(false);
        weekSelectedFill = TimeTheme.getWeekBackgroundColor(true);
        weekStroke = compactMode ? TimeTheme.getWeekBorderColor() : Color.TRANSPARENT;
        weekTextFill = TimeTheme.getWeekTextColor(false);
        weekSelectedTextFill = TimeTheme.getWeekTextColor(true);

        // Month
        monthStroke = compactMode ? TimeTheme.getMonthBorderColor() : Color.TRANSPARENT;
        monthTextFill = TimeTheme.getMonthTextColor();

        // Year
        yearFill = TimeTheme.getYearBackgroundColor(false);
        yearSelectedFill = TimeTheme.getYearBackgroundColor(true);
        yearStroke = compactMode ? TimeTheme.getYearBorderColor() : Color.TRANSPARENT;
        yearTextFill = TimeTheme.getYearTextColor(false);
        yearSelectedTextFill = TimeTheme.getYearTextColor(true);

        globalCanvasDrawer.setDrawAreaBackgroundFill(TimeTheme.getCanvasBackgroundColor());
    }

    private void drawYear(Year year, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayer == yearsLayer)
            strokeStrip(p, gc);
        boolean selected = Objects.areEquals(yearsLayer.getSelectedChild(), year);
        TimeCanvasUtil.fillStrokeRect(p, yearHPadding, selected ? yearSelectedFill : yearFill, yearStroke, yearRadius, gc);
        gc.setFont(yearFont);
        TimeCanvasUtil.fillCenterText(p, yearHPadding, String.valueOf(year), selected ? yearSelectedTextFill : yearTextFill, gc);
    }

    private void drawMonth(YearMonth yearMonth, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayer == monthsLayer)
            strokeStrip(p, gc);
        Month month = yearMonth.getMonth();
        boolean selected = Objects.areEquals(monthsLayer.getSelectedChild(), yearMonth);
        Color monthFill = TimeTheme.getMonthBackgroundColor(yearMonth, selected);
        TimeCanvasUtil.fillStrokeRect(p, monthHPadding, monthFill, monthStroke, monthRadius, gc);
        String text = i18nMonths[month.ordinal()];
        if (text == null) // May happen if i18n dictionary is not yet loaded
            return;
        boolean m, mmm, yy, yyyy;
        if (yearsLayer.isVisible()) {
            yy = yyyy = false;
            m = monthWidth < 35;
            mmm = monthWidth < 80;
        } else {
            yy = monthWidth < 125;
            yyyy = !yy;
            m = monthWidth < 50;
            mmm = monthWidth < 100;
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
        if (stripLayer == weeksLayer)
            strokeStrip(p, gc);
        boolean selected = Objects.areEquals(weeksLayer.getSelectedChild(), yearWeek);
        TimeCanvasUtil.fillStrokeRect(p, weekHPadding, selected ? weekSelectedFill : weekFill, weekStroke, weekRadius, gc);
        String week = i18nWeek;
        if (week == null) // May happen if i18n dictionary is not yet loaded
            return;
        if (weekWidth - 2 * weekHPadding < i18nWeekWidth + 5)
            week = week.substring(0, 1);
        String weekNumber = (yearWeek.getWeek() < 10 ? "0" : "") + yearWeek.getWeek();
        double h = p.getHeight(), h2 = h / 2, vPadding = h / 16;
        Paint textFill = selected ? weekSelectedTextFill : weekTextFill;
        gc.setFont(weekFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + vPadding, p.getWidth(), h2, weekHPadding, week, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
        gc.setFont(weekNumberFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + h2, p.getWidth(), h2 - vPadding, weekHPadding, weekNumber, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
    }

    private void drawDay(LocalDate day, ChildPosition<LocalDate> p, GraphicsContext gc) {
        if (stripLayer == daysLayer)
            strokeStrip(p, gc);
        boolean selected = Objects.areEquals(daysLayer.getSelectedChild(), day);
        TimeCanvasUtil.fillStrokeRect(p, dayHPadding, TimeTheme.getDayOfWeekBackgroundColor(day, selected), dayStroke, dayRadius, gc);
        String dayOfWeek = i18nDaysOfWeek[day.getDayOfWeek().ordinal()];
        if (dayOfWeek == null) // May happen if i18n dictionary is not yet loaded
            return;
        if (dayWidth < 100)
            dayOfWeek = dayOfWeek.substring(0, 3);
        String dayOfMonth = (day.getDayOfMonth() < 10 ? "0" : "") + day.getDayOfMonth();
        double h = p.getHeight(), h2 = h / 2, vPadding = h / 16;
        Paint textFill = selected ? daySelectedTextFill : dayTextFill;
        gc.setFont(dayOfWeekFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + vPadding, p.getWidth(), h2, dayHPadding, dayOfWeek, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
        gc.setFont(dayOfMonthFont);
        TimeCanvasUtil.fillText(p.getX(), p.getY() + h2, p.getWidth(), h2 - vPadding, dayHPadding, dayOfMonth, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
    }

    private void strokeStrip(ChildPosition<LocalDate> p, GraphicsContext gc) {
        double canvasHeight = gc.getCanvas().getHeight();
        TimeCanvasUtil.strokeRect(p.getX(), 0, p.getWidth(), canvasHeight, 0, canvasHeight > p.getY() + p.getHeight() ? stripStroke : Color.TRANSPARENT, 0, gc);
    }

}
