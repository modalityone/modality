package one.modality.base.client.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.layout.FXLayoutMode;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.ChildTimeReader;
import dev.webfx.extras.timelayout.LayeredTimeLayout;
import dev.webfx.extras.timelayout.TimeLayout;
import dev.webfx.extras.timelayout.canvas.ChildCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.LayeredTimeCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.extras.timelayout.util.TimeUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.time.theme.TimeTheme;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

/**
 * @author Bruno Salmon
 */
public final class LayeredGanttCanvas {

    private final Canvas canvas = new Canvas();
    private Pane canvasPane;
    private final GanttLayout<YearMonth, LocalDate> monthsLayout = new GanttLayout<>();
    private final GanttLayout<LocalDate, LocalDate> daysLayout = new GanttLayout<>();
    private final LayeredTimeLayout<LocalDate> layeredTimeLayout = LayeredTimeLayout.create();
    private final LayeredTimeCanvasDrawer<LocalDate> layeredTimeCanvasDrawer = new LayeredTimeCanvasDrawer<>(canvas, layeredTimeLayout);
    private final FontDef MONTH_FONT_DEF = FontDef.font(16);
    private final FontDef DAY_FONT_DEF = FontDef.font(14);

    public LayeredGanttCanvas() {
        monthsLayout.setChildTimeReader(new ChildTimeReader<>() {
            @Override
            public LocalDate getStartTime(YearMonth child) {
                return child.atDay(1);
            }

            @Override
            public LocalDate getEndTime(YearMonth child) {
                return child.atEndOfMonth();
            }
        });
        addLayer(monthsLayout, this::drawMonth);

        daysLayout.setFillHeight(true);
        addLayer(daysLayout, this::drawDay);

        // Redrawing the canvas on theme mode changes
        ThemeRegistry.addModeChangeListener(this::markCanvasAsDirty);
        // Recomputing layout on layout mode changes (compact / standard mode)
        FXLayoutMode.layoutModeProperty().addListener(observable -> markLayoutAsDirty());
    }

    public void setTimeWindow(LocalDate timeWindowStart, LocalDate timeWindowEnd) {
        layeredTimeLayout.setTimeWindow(timeWindowStart, timeWindowEnd);
        monthsLayout.getChildren().setAll(TimeUtil.generateThisYearMonths());
        daysLayout.getChildren().setAll(TimeUtil.generateLocalDates(timeWindowStart, timeWindowEnd));
    }

    public <C> void addLayer(TimeLayout<C, LocalDate> timeLayout, ChildCanvasDrawer<C, LocalDate> childCanvasDrawer) {
        layeredTimeLayout.addLayer(timeLayout);
        layeredTimeCanvasDrawer.setLayerChildCanvasDrawer(timeLayout, childCanvasDrawer);
        markLayoutAsDirty();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Pane getCanvasPane() {
        if (canvasPane == null) {
            canvasPane = new Pane(canvas) {
                @Override
                protected void layoutChildren() {
                    double width = getWidth();
                    double height = getHeight();
                    canvas.setWidth(width);
                    canvas.setHeight(height);
                    layoutInArea(canvas, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
                    markCanvasAsDirty();
                }
            };
        }
        return canvasPane;
    }

    public void markLayoutAsDirty() {
        Platform.runLater(() -> {
            boolean compactMode = FXLayoutMode.isCompactMode();
            double y = compactMode ? 40 : 20;
            monthsLayout.setChildFixedHeight(y);
            daysLayout.setTopY(y);
            y += 21; // The height of the day boxes + 1
            layeredTimeLayout.markLayoutAsDirty();
            layeredTimeLayout.layout(canvas.getWidth(), canvas.getHeight());
            ObservableList<TimeLayout<?, LocalDate>> layers = layeredTimeLayout.getLayers();
            for (int i = 2; i < layers.size(); i++) {
                TimeLayout<?, LocalDate> layer = layers.get(i);
                layer.setTopY(y);
                y += layer.getRowsCount() * layer.getChildFixedHeight();
            }
            getCanvasPane().setPrefHeight(y);
            canvasPane.requestLayout();
        });
    }

    public void markCanvasAsDirty() {
        Platform.runLater(() -> layeredTimeCanvasDrawer.draw(true));
    }

    private void drawMonth(YearMonth yearMonth, ChildPosition<LocalDate> p, GraphicsContext gc) {
        Month month = yearMonth.getMonth();
        TimeCanvasUtil.fillStrokeRect(p, TimeTheme.getYearMonthBackgroundColor(yearMonth), TimeTheme.getMonthBorderColor(), gc);
        gc.setFont(TextTheme.getFont(MONTH_FONT_DEF));
        TimeCanvasUtil.fillCenterText(p, month + " " + yearMonth.getYear(), TimeTheme.getMonthTextColor(), gc);
    }

    private void drawDay(LocalDate day, ChildPosition<LocalDate> p, GraphicsContext gc) {
        TimeCanvasUtil.fillStrokeRect(p, TimeTheme.getDayOfWeekCanvasBackgroundColor(day), TimeTheme.getDayOfWeekBorderColor(), gc);
        TimeCanvasUtil.fillStrokeRect(p.getX(), p.getY(), p.getWidth(), 20, TimeTheme.getDayOfWeekBackgroundColor(day), TimeTheme.getDayOfWeekBorderColor(), gc);
        gc.setFont(TextTheme.getFont(DAY_FONT_DEF));
        TimeCanvasUtil.fillText(p.getX(), p.getY(), p.getWidth(), 20, String.valueOf(day.getDayOfMonth()), TimeTheme.getDayOfWeekTextColor(), VPos.CENTER, TextAlignment.CENTER, gc);
    }

}
