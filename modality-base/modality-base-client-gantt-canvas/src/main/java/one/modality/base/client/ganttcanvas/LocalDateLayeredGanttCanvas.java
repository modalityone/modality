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
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18n;
import javafx.animation.Interpolator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.time.theme.TimeTheme;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;

/**
 * @author Bruno Salmon
 */
public final class LocalDateLayeredGanttCanvas implements TimeWindow<LocalDate> {

    private final Canvas canvas = new Canvas();
    private double computedCanvasHeight; // computed by markLayoutAsDirty()
    private final Pane canvasPane = new Pane(canvas) {
        @Override
        protected void layoutChildren() {
            double newCanvasWidth = getWidth();
            double newCanvasHeight = computedCanvasHeight;
            boolean canvasWidthChanged  = newCanvasWidth  != canvas.getWidth();
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
    private final GanttLayout<Year, LocalDate> yearsLayer = GanttLayout.createYearLocalDateGanttLayout();
    private final GanttLayout<YearMonth, LocalDate> monthsLayer = GanttLayout.createYearMonthLocalDateGanttLayout();
    private final GanttLayout<YearWeek, LocalDate> weeksLayer = GanttLayout.createYearWeekLocalDateGanttLayout();
    private final GanttLayout<LocalDate, LocalDate> daysLayer = GanttLayout.createDayLocalDateGanttLayout();
    private final LayeredTimeLayout<LocalDate> globalLayout = LayeredTimeLayout.create();
    private final LayeredTimeCanvasDrawer<LocalDate> globalCanvasDrawer = new LayeredTimeCanvasDrawer<>(canvas, globalLayout);
    private long timeWindowDuration;

    public LocalDateLayeredGanttCanvas() {
        // Adding these layouts as layers in the gantt canvas.
        // Note: the order is important regarding the vertical strips, so they are always displayed "in the background".
        // For example, if days are used to draw the strips, they must be drawn before drawing weeks, months, etc...
        // otherwise these strips would appear on top of them instead of behind them.
        addLayer(daysLayer, this::drawDay);
        addLayer(weeksLayer, this::drawWeek);
        addLayer(monthsLayer, this::drawMonth);
        addLayer(yearsLayer, this::drawYear);

        globalLayout.setOnTimeWindowChanged((start, end) -> {
            timeWindowDuration = ChronoUnit.DAYS.between(start, end);
            markLayoutAsDirty();
            // TODO: investigate if the following can be moved to GanttLayout.createXXX() factory methods
            yearsLayer.getChildren().setAll(TimeUtil.generateYears(Year.from(start), Year.from(end)));
            monthsLayer.getChildren().setAll(TimeUtil.generateYearMonths(YearMonth.from(start), YearMonth.from(end)));
            weeksLayer.getChildren().setAll(TimeUtil.generateYearWeeks(YearWeek.from(start), YearWeek.from(end)));
            daysLayer.getChildren().setAll(TimeUtil.generateLocalDates(start, end));
        });

        // Redrawing the canvas on theme mode changes (because the graphical properties depend on the theme)
        ThemeRegistry.addModeChangeListener(this::markCanvasAsDirty);

        // Recomputing layout on layout mode changes (compact / standard mode)
        FXProperties.runOnPropertiesChange(this::markLayoutAsDirty, FXLayoutMode.layoutModeProperty(), FXGanttVisibility.ganttVisibilityProperty());

        // Updating i18n texts when necessary
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
        return globalLayout.timeWindowStartProperty();
    }

    @Override
    public ObjectProperty<LocalDate> timeWindowEndProperty() {
        return globalLayout.timeWindowEndProperty();
    }

    @Override
    public void setOnTimeWindowChanged(BiConsumer<LocalDate, LocalDate> timeWindowChangedHandler) {
        // Not used so far, but should be implemented
    }

    public void setTimeWindow(LocalDate timeWindowStart, LocalDate timeWindowEnd) {
        globalLayout.setTimeWindow(timeWindowStart, timeWindowEnd); // see globalLayout.setOnTimeWindowChanged() callback in constructor
    }

    public LayeredTimeLayout<LocalDate> getGlobalLayout() {
        return globalLayout;
    }

    public <C> void addLayer(TimeLayout<C, LocalDate> layer, ChildCanvasDrawer<C, LocalDate> layerCanvasDrawer) {
        globalLayout.addLayer(layer);
        layer.getChildren().addListener((ListChangeListener<C>) c -> markLayoutAsDirty());
        layer.selectedChildProperty().addListener(observable -> markCanvasAsDirty());
        globalCanvasDrawer.setLayerChildCanvasDrawer(layer, layerCanvasDrawer);
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
        if (yearsLayer.isVisible()) {
            yearsLayer.setTopY(y);
            yearsLayer.setChildFixedHeight(yearHeight);
            y += yearHeight + vSpacing;
        }
        if (monthsLayer.isVisible()) {
            monthsLayer.setTopY(y);
            monthsLayer.setChildFixedHeight(monthHeight);
            y += monthHeight + vSpacing;
        }
        if (weeksLayer.isVisible()) {
            weeksLayer.setTopY(y);
            weeksLayer.setChildFixedHeight(weekHeight);
            y += weekHeight + vSpacing;
        }
        if (daysLayer.isVisible()) {
            daysLayer.setTopY(y);
            daysLayer.setChildFixedHeight(dayHeight);
            y += dayHeight + vSpacing;
        }
        globalLayout.markLayoutAsDirty();
        globalLayout.layout(canvas.getWidth(), canvas.getHeight());
        if (FXGanttVisibility.showEvents()) {
            ObservableList<TimeLayout<?, LocalDate>> layers = globalLayout.getLayers();
            for (int i = 4; i < layers.size(); i++) {
                TimeLayout<?, LocalDate> layer = layers.get(i);
                layer.setTopY(y);
                y += layer.getRowsCount() * layer.getChildFixedHeight();
            }
            y += vSpacing;
        }
        y = Math.min(y - vSpacing, Screen.getPrimary().getVisualBounds().getHeight());
        computedCanvasHeight = y;
        Animations.animateProperty(canvasPane.prefHeightProperty(), y, wasCanvasPaneManaged && isCanvasPaneManaged);
        globalCanvasDrawer.redraw();
    }

    private Scheduled dirtyCanvasScheduled;

    public void markCanvasAsDirty() {
        if (dirtyCanvasScheduled != null)
            return;
        dirtyCanvasScheduled = UiScheduler.scheduleInAnimationFrame(() -> {
            globalCanvasDrawer.setBackgroundFill(TimeTheme.getCanvasBackgroundColor());
            redraw();
            dirtyCanvasScheduled = null;
        });
    }

    private void redraw() {
        updateLayersDrawingProperties();
        globalCanvasDrawer.redraw();
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
        // Computing heights for day / week / month / year
        boolean compactMode = FXLayoutMode.isCompactMode();
        double nodeHeight = compactMode ? 40 : 35; // Height of the top node
        if (yearsLayer.isVisible()) {
            yearHeight = nodeHeight;
            nodeHeight = 35;
        }
        if (monthsLayer.isVisible()) {
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
        // Computing general graphical properties
        // Day
        dayStroke = TimeTheme.getDayOfWeekBorderColor();
        dayTextFill = TimeTheme.getDayOfWeekTextColor(false);
        daySelectedTextFill = TimeTheme.getDayOfWeekTextColor(true);

        // Week
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

    private double mousePressedX;
    private LocalDate mousePressedStart;
    private long mousePressedDuration;
    private boolean mouseDragged;

    public void makeInteractive() {
        canvas.setOnMousePressed(e -> {
            mousePressedX = e.getX();
            mousePressedStart = getTimeWindowStart();
            mousePressedDuration = ChronoUnit.DAYS.between(mousePressedStart, getTimeWindowEnd());
            mouseDragged = false;
            updateCanvasCursor(e, true);
        });
        canvas.setOnMouseDragged(e -> {
            double deltaX = mousePressedX - e.getX();
            double dayWidth = canvas.getWidth() / (mousePressedDuration + 1);
            long deltaDay = (long) (deltaX / dayWidth);
            if (deltaDay != 0) {
                setTimeWindow(mousePressedStart.plus(deltaDay, ChronoUnit.DAYS), mousePressedDuration);
                mouseDragged = true;
            }
            updateCanvasCursor(e, true);
        });
        // Selecting the event when clicked
        canvas.setOnMouseClicked(e -> {
            if (!mouseDragged) {
                if (selectObjectAt(e.getX(), e.getY()))
                    markCanvasAsDirty();
            }
            updateCanvasCursor(e, false);
            mousePressedStart = null;
        });
        // Changing cursor to hand cursor when hovering an event (to indicate it's clickable)
        canvas.setOnMouseMoved(e -> updateCanvasCursor(e, false));
        canvas.setOnScroll(e -> {
            if (e.isControlDown()) {
                LocalDate start = getTimeWindowStart();
                LocalDate end = getTimeWindowEnd();
                long duration = ChronoUnit.DAYS.between(start, end);
                LocalDate middle = start.plus(duration / 2, ChronoUnit.DAYS);
                if (e.getDeltaY() > 0) // Mouse wheel up => Zoom in
                    duration = (long) (duration / 1.10);
                else // Mouse wheel down => Zoom out
                    duration = Math.max(duration + 1, (long) (duration * 1.10));
                duration = Math.min(duration, 10_000);
                setTimeWindow(middle.minus(duration / 2, ChronoUnit.DAYS), duration);
            }
        });
    }

    private void setTimeWindow(LocalDate start, long duration) {
        setTimeWindow(start, start.plus(duration, ChronoUnit.DAYS));
    }

    private void updateCanvasCursor(MouseEvent e, boolean mouseDown) {
        canvas.setCursor(mouseDown && mouseDragged ? Cursor.CLOSED_HAND : isSelectableObjectPresentAt(e.getX(), e.getY()) ? Cursor.HAND : Cursor.OPEN_HAND);
    }

    private boolean isSelectableObjectPresentAt(double x, double y) {
        return getGlobalLayout().pickChildAt(x, y) != null;
    }

    private boolean selectObjectAt(double x, double y) {
        return getGlobalLayout().selectChildAt(x, y) != null;
    }

    public void bindTimeWindow(Property<LocalDate> startProperty, Property<LocalDate> endProperty, boolean applyInitialValues, boolean bidirectional) {
        if (applyInitialValues)
            setTimeWindow(startProperty.getValue(), endProperty.getValue());
        if (bidirectional) {
            startProperty.bindBidirectional(timeWindowStartProperty());
            endProperty.bindBidirectional(timeWindowEndProperty());
        } else {
            startProperty.bind(timeWindowStartProperty());
            endProperty.bind(timeWindowEndProperty());
        }
    }

    public void setupFXBindings() {
        bindFXGanttTimeWindow();
        bindFXGanttSelection();
    }

    private void bindFXGanttTimeWindow() {
        bindTimeWindow(FXGanttTimeWindow.ganttTimeWindowStartProperty(), FXGanttTimeWindow.ganttTimeWindowEndProperty(), false, true);
    }

    private void bindFXGanttSelection() {
        FXGanttSelection.ganttSelectedObjectProperty().bindBidirectional(getGlobalLayout().selectedChildProperty());
    }

}
