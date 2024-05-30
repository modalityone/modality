package one.modality.base.backoffice.ganttcanvas;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.canvas.layer.ChildDrawer;
import dev.webfx.extras.canvas.pane.CanvasPane;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.YearWeek;
import dev.webfx.extras.time.layout.MultiLayerLocalDateLayout;
import dev.webfx.extras.time.layout.TimeLayout;
import dev.webfx.extras.time.layout.canvas.MultiLayerLocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.GanttLayout;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.projector.AnimatedTimeProjector;
import dev.webfx.extras.time.projector.TimeProjector;
import dev.webfx.extras.time.window.TimeWindow;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18n;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
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
    private final CanvasPane canvasPane = TimeCanvasUtil.createTimeCanvasPane(globalLayout, globalCanvasDrawer);
    private final BarDrawer yearBarDrawer = new BarDrawer();
    private final BarDrawer monthBarDrawer = new BarDrawer();
    private final BarDrawer weekBarDrawer = new BarDrawer();
    private final BarDrawer dayBarDrawer = new BarDrawer();
    private final String[] i18nMonths = new String[12];
    private final String[] i18nDaysOfWeek = new String[7];
    private String i18nWeek;
    private double i18nWeekWidth;
    // Layout properties computed by setLayoutPropertiesBeforeLayoutPass() and used by setDrawPropertiesBeforeDrawPass():
    private double yearWidth, yearHeight, monthWidth, monthHeight, weekWidth, weekHeight, dayWidth, dayHeight;
    // Draw properties computed by setDrawPropertiesBeforeDrawPass() and used by draw methods:
    private Paint yearFill, yearSelectedFill, yearTextFill, yearSelectedTextFill, weekFill, weekSelectedFill, weekTextFill, weekSelectedTextFill, dayTextFill, daySelectedTextFill;
    // Strip properties:
    private GanttLayout<?, LocalDate> stripLayer; // depending on zoom level, strips may be days, months, weeks or years
    private Paint stripStroke;
    private Timeline animationTimeline; // used for horizontal animation on time window change

    public DatedGanttCanvas() {
        // Setting an animatedTimeProjector on the global layout to allow possible horizontal animation
        TimeProjector<LocalDate> untranslatedTimeProjector = daysLayer.getTimeProjector();
        AnimatedTimeProjector<LocalDate> animatedTimeProjector = new AnimatedTimeProjector<>(untranslatedTimeProjector);
        globalLayout.setTimeProjector(animatedTimeProjector);

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
                FXGanttVisibility.ganttVisibilityProperty()
        );
        FXProperties.runOnPropertiesChange(this::markCanvasAsDirty, FXGanttHighlight.ganttHighlightedDayProperty());

        // Updating i18n texts when necessary
        FXProperties.runNowAndOnPropertiesChange(this::updateI18nTexts, I18n.dictionaryProperty());

        FXGanttHighlight.addDayHighlight(daysLayer, globalCanvasDrawer);

        // Animation management on time window change (starting animation timeline)
        timeWindowEndProperty().addListener(new InvalidationListener() { // end changes after start (so both are updated at this point)
            private LocalDate lastTimeWindowStart;
            @Override
            public void invalidated(Observable observable) {
                LocalDate newTimeWindowStart = getTimeWindowStart();
                if (lastTimeWindowStart != null) { // ignoring first call
                    // We compute the horizontal shift
                    double xStartBeforeChange = getTimeProjector().timeToX(lastTimeWindowStart, true, false);
                    double xStartAfterChange = untranslatedTimeProjector.timeToX(newTimeWindowStart, true, false);
                    double deltaX = xStartAfterChange - xStartBeforeChange;
                    // We translate the animated time projector to the opposite amount so the time window looks unchanged to the user for now
                    animatedTimeProjector.setTranslateX(-deltaX);
                    // Stopping a possible previous animation
                    if (animationTimeline != null)
                        animationTimeline.stop();
                    // Animating the horizontal translation to go back to 0 (where the time window was asked to start).
                    animationTimeline = Animations.animateProperty(animatedTimeProjector.translateXProperty(), 0);
                }
                lastTimeWindowStart = newTimeWindowStart;
            }
        });

        // Animation management on time window change (during animation timeline)
        animatedTimeProjector.translateXProperty().addListener(observable -> {
            // Getting the start and end of the appearing time window to the user at this time of the animation timeline
            LocalDate start = animatedTimeProjector.xToTime(0);
            LocalDate end = animatedTimeProjector.xToTime(canvasPane.getWidth());
            // Populating the days, weeks, months & years over that appearing time window
            LocalDateGanttLayout.populateDayLocalDateGanttLayout(daysLayer, start, end); // 0
            LocalDateGanttLayout.populateYearWeekLocalDateGanttLayout(weeksLayer, start, end); // 1
            LocalDateGanttLayout.populateYearMonthLocalDateGanttLayout(monthsLayer, start, end); //2
            LocalDateGanttLayout.populateYearLocalDateGanttLayout(yearsLayer, start, end); // 3
            // For the following layers (such as events), we don't repopulate, but just invalidate objects so their
            // layout position is recomputed with the updated animated time projector.
            for (int i = 4, n = globalLayout.getLayers().size(); i < n; i++) {
                TimeLayout<?, LocalDate> layer = globalLayout.getLayers().get(i);
                for (int j = 0, m = layer.getChildren().size(); j < m; j++) {
                    layer.getChildBounds(j).invalidateObject();
                }
            }
        });
    }

    public Canvas getCanvas() {
        return globalCanvasDrawer.getCanvas();
    }

    public Pane getCanvasContainer() {
        return canvasPane;
    }

    public TimeProjector<LocalDate> getTimeProjector() {
        return globalLayout.getTimeProjector();
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

    public DatedGanttCanvas setInteractive(boolean interactive) {
        globalCanvasDrawer.setInteractive(interactive);
        return this;
    }

    public DatedGanttCanvas setDateSelectionEnabled(boolean dateSelectionEnabled) {
        daysLayer.setSelectionEnabled(dateSelectionEnabled);
        weeksLayer.setSelectionEnabled(dateSelectionEnabled);
        monthsLayer.setSelectionEnabled(dateSelectionEnabled);
        yearsLayer.setSelectionEnabled(dateSelectionEnabled);
        return this;
    }

    public <C> void addLayer(TimeLayout<C, LocalDate> layer, ChildDrawer<C> layerCanvasDrawer) {
        globalLayout.addLayer(layer);
        globalCanvasDrawer.setLayerChildDrawer(layer, layerCanvasDrawer);
    }

    public void markLayoutAsDirty() { // may be called several times, but only 1 call will happen in the animation frame
        globalLayout.markLayoutAsDirty();
    }

    public void markCanvasAsDirty() { // may be called several times, but only 1 call will happen in the animation frame
        globalCanvasDrawer.markDrawAreaAsDirty();
    }

    private void setLayoutPropertiesBeforeLayoutPass() { // Called only once before the layout pass
        // Computing widths for day / week / month / year
        long timeWindowDuration = ChronoUnit.DAYS.between(getTimeWindowStart(), getTimeWindowEnd());
        dayWidth = globalLayout.getWidth() / (timeWindowDuration + 1);
        weekWidth = 7 * dayWidth;
        yearWidth = 365 * dayWidth;
        monthWidth = yearWidth / 12;

        // Setting global visibility
        boolean isVisible = FXGanttVisibility.isVisible();
        canvasPane.setVisible(isVisible);
        canvasPane.setManaged(isVisible);

        // Setting layers visibility
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

    private void setDrawPropertiesBeforeDrawPass() { // Called only once before the draw pass
        // ============= Global canvas background =============
        globalCanvasDrawer.setDrawAreaBackgroundFill(TimeTheme.getCanvasBackgroundColor());

        boolean compactMode = FXLayoutMode.isCompactMode();

        // ============= Year draw properties =============
        // 1) Properties that apply to all years => set only once here (remain identical for all drawYear() calls)
        yearBarDrawer
                .setTextFont(TextTheme.getFont(FontDef.font(fontSize(yearWidth, yearHeight))))
                .sethPadding(10)
                .setRadius(10)
                .setStroke(compactMode ? TimeTheme.getYearBorderColor() : null);
        // 2) Properties that depend on the year selection => drawYear() will set the correct value in yearBarDrawer on each call
        yearFill = TimeTheme.getYearBackgroundColor(false);
        yearSelectedFill = TimeTheme.getYearBackgroundColor(true);
        yearTextFill = TimeTheme.getYearTextColor(false);
        yearSelectedTextFill = TimeTheme.getYearTextColor(true);

        // ============= Month draw properties =============
        // 1) Properties that apply to all months => set only once here (remain identical for all drawMonth() calls)
        monthBarDrawer
                .setStroke(compactMode ? TimeTheme.getMonthBorderColor() : null)
                .setTextFill(TimeTheme.getMonthTextColor())
                .setTextFont(TextTheme.getFont(FontDef.font(fontSize(monthWidth, monthHeight))))
                .setRadius(monthWidth < 25 ? 0 : 0.5 * Math.min(monthWidth, monthHeight))
                .sethPadding(monthWidth < 25 ? 0 : clamp(3, 0.02 * monthWidth, 10));
        // 2) Properties that depend on the months => can't be precomputed because they may vary for each month
        // (ex: each month may have a different color in varied palette mode)

        // ============= Week draw properties =============
        // 1) Properties that apply to all weeks => set only once here (remain identical for all drawWeek() calls)
        double weekFontSize = fontSize(weekWidth, weekHeight / 2);
        Font weekFont = TextTheme.getFont(FontDef.font(weekFontSize / 3 * 2));
        weekBarDrawer
                .setTopTextFont(weekFont) // top = week word
                .setBottomTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, weekFontSize))) // bottom = week number
                .setStroke(compactMode ? TimeTheme.getWeekBorderColor() : null)
                .setRadius(weekWidth < 25 ? 0 : 0.5 * Math.min(weekWidth, weekHeight))
                .sethPadding(weekWidth < 25 ? 0 : clamp(3, 0.05 * weekWidth, 10));
        i18nWeekWidth = WebFxKitLauncher.measureText(i18nWeek, weekFont).getWidth();
        // 2) Properties that depend on the week selection => drawWeek() will set the correct value in weekBarDrawer on each call
        weekFill = TimeTheme.getWeekBackgroundColor(false);
        weekSelectedFill = TimeTheme.getWeekBackgroundColor(true);
        weekTextFill = TimeTheme.getWeekTextColor(false);
        weekSelectedTextFill = TimeTheme.getWeekTextColor(true);

        // ============= Day draw properties =============
        // 1) Properties that apply to all days => set only once here (remain identical for all drawDay() calls)
        double dayOfMonthFontSize = fontSize(dayWidth, dayHeight / 2);
        dayBarDrawer
                .setTopTextFont(TextTheme.getFont(FontDef.font(0.6 * dayOfMonthFontSize))) // top = day of week => smaller
                .setBottomTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, dayOfMonthFontSize))) // bottom = day of month
                .setStroke(TimeTheme.getDayOfWeekBorderColor())
                .setRadius(dayWidth < 30 ? 0 : 0.5 * Math.min(dayWidth, dayHeight))
                .sethPadding(dayWidth < 30 ? 0 : clamp(3, 0.05 * dayWidth, 10));
        // 2) Properties that depend on the day selection => drawDay() will set the correct value in dayBarDrawer on each call
        dayTextFill = TimeTheme.getDayOfWeekTextColor(false);
        daySelectedTextFill = TimeTheme.getDayOfWeekTextColor(true);

        // ============= Strip draw properties =============
        if (daysLayer.isVisible())
            stripLayer = daysLayer;
        else if (weeksLayer.isVisible())
            stripLayer = weeksLayer;
        else if (monthsLayer.isVisible())
            stripLayer = monthsLayer;
        else
            stripLayer = yearsLayer;
        stripStroke = FXLuminanceMode.isDarkMode() ? Color.gray(0.2) : Color.gray(0.85);
    }

    // method to draw 1 strip - may be called many times during the draw pass
    private void strokeStrip(Bounds b, GraphicsContext gc) {
        double canvasHeight = gc.getCanvas().getHeight();
        BarDrawer.strokeRect(b.getMinX(), 0, b.getWidth(), canvasHeight, 0, canvasHeight > b.getMaxY() ? stripStroke : Color.TRANSPARENT, 0, gc);
    }

    // method to draw 1 year - may be called many times during the draw pass
    private void drawYear(Year year, Bounds b, GraphicsContext gc) {
        if (stripLayer == yearsLayer)
            strokeStrip(b, gc);

        boolean selected = Objects.areEquals(yearsLayer.getSelectedChild(), year);

        yearBarDrawer
                .setBackgroundFill(selected ? yearSelectedFill : yearFill)
                .setTextFill(selected ? yearSelectedTextFill : yearTextFill)
                .setMiddleText(String.valueOf(year))
                .drawBar(b, gc);
    }

    // method to draw 1 month - may be called many times during the draw pass
    private void drawMonth(YearMonth yearMonth, Bounds b, GraphicsContext gc) {
        if (stripLayer == monthsLayer)
            strokeStrip(b, gc);

        Month month = yearMonth.getMonth();
        boolean selected = Objects.areEquals(monthsLayer.getSelectedChild(), yearMonth);
        String text = i18nMonths[month.ordinal()];
        if (text != null) { // null may happen if i18n dictionary is not yet loaded
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
            if (m)
                text = text.substring(0, 1);
            else if (mmm)
                text = text.substring(0, 3);
            if (yy)
                text += " " + (yearMonth.getYear() % 100);
            else if (yyyy)
                text += " " + yearMonth.getYear();
        }

        monthBarDrawer
                .setBackgroundFill(TimeTheme.getMonthBackgroundColor(yearMonth, selected))
                .setMiddleText(text)
                .drawBar(b, gc);
    }

    // method to draw 1 week - may be called many times during the draw pass
    private void drawWeek(YearWeek yearWeek, Bounds b, GraphicsContext gc) {
        if (stripLayer == weeksLayer)
            strokeStrip(b, gc);

        boolean selected = Objects.areEquals(weeksLayer.getSelectedChild(), yearWeek);
        String week = i18nWeek;
        if (week != null) { // null may happen if i18n dictionary is not yet loaded
            if (dayBarDrawer.getTextAreaWidth(b) < i18nWeekWidth)
                week = week.substring(0, 1);
        }
        String weekNumber = (yearWeek.getWeek() < 10 ? "0" : "") + yearWeek.getWeek();

        weekBarDrawer
                .setBackgroundFill(selected ? weekSelectedFill : weekFill)
                .setTopText(week)
                .setBottomText(weekNumber)
                .setTextFill(selected ? weekSelectedTextFill : weekTextFill)
                .drawBar(b, gc);
    }

    private static final Image TODAY_IMAGE = new Image(Resource.toUrl("images/s32/sun.png", DatedGanttCanvas.class), true);


    // method to draw 1 day - may be called many times during the draw pass
    private void drawDay(LocalDate day, Bounds b, GraphicsContext gc) {
        if (stripLayer == daysLayer)
            strokeStrip(b, gc);

        boolean today = Objects.areEquals(day, LocalDate.now());

        boolean highlighted = Objects.areEquals(day, FXGanttHighlight.getGanttHighlightedDay());

        boolean selected = Objects.areEquals(day, daysLayer.getSelectedChild());
        String dayOfWeek = i18nDaysOfWeek[day.getDayOfWeek().ordinal()];
        if (dayOfWeek != null) { // null may happen if i18n dictionary is not yet loaded
            if (dayWidth < 100)
                dayOfWeek = dayOfWeek.substring(0, 3);
        }
        String dayOfMonth = (day.getDayOfMonth() < 10 ? "0" : "") + day.getDayOfMonth();

        dayBarDrawer
                .setBackgroundFill(TimeTheme.getDayOfWeekBackgroundColor(day, selected, highlighted))
                .setIcon(today ? TODAY_IMAGE : null, Pos.TOP_RIGHT, HPos.CENTER, VPos.CENTER)
                .setTopText(dayOfWeek)
                .setBottomText(dayOfMonth)
                .setTextFill(selected ? daySelectedTextFill : dayTextFill)
                .drawBar(b, gc);
    }

    // private static utility methods

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

}
