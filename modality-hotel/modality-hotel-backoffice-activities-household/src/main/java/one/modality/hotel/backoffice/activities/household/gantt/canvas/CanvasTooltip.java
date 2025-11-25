package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.geometry.MutableBounds;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Canvas-based tooltip implementation for displaying information overlays.
 * This replaces JavaFX Tooltip which doesn't compile well with WebFX/GWT.
 *
 * The tooltip is drawn as an overlay layer on the canvas, positioned near
 * the mouse click location. It auto-hides after a configurable duration.
 *
 * Usage:
 * 1. Create instance with canvas reference
 * 2. Call show() with position and content
 * 3. Register drawTooltip() in canvas's addOnAfterDraw callback
 * 4. Call hide() when needed or let auto-hide work
 *
 * @author Claude Code Assistant
 */
public class CanvasTooltip {

    // Tooltip appearance constants
    private static final double PADDING = 10;
    private static final double RADIUS = 6;
    private static final double LINE_HEIGHT = 16;
    private static final double MAX_WIDTH = 300;
    private static final double TITLE_FONT_SIZE = 13;
    private static final double BODY_FONT_SIZE = 12;
    private static final Color BACKGROUND_COLOR = Color.rgb(50, 50, 50, 0.95);
    private static final Color BORDER_COLOR = Color.rgb(80, 80, 80);
    private static final Color TITLE_COLOR = Color.WHITE;
    private static final Color BODY_COLOR = Color.rgb(220, 220, 220);

    // State
    private boolean visible = false;
    private double tooltipX = 0;
    private double tooltipY = 0;
    private String title;
    private String[] bodyLines;

    // Auto-hide timer
    private Timeline hideTimer;
    private final Duration autoHideDuration;

    // Canvas reference for triggering redraws
    private final Canvas canvas;
    private final Runnable markDirtyCallback;

    // Drawing utilities
    private final BarDrawer backgroundDrawer = new BarDrawer()
            .setBackgroundFill(BACKGROUND_COLOR)
            .setStroke(BORDER_COLOR)
            .setRadius(RADIUS);

    /**
     * Creates a canvas tooltip with default 5 second auto-hide.
     *
     * @param canvas The canvas this tooltip will be drawn on
     * @param markDirtyCallback Callback to mark the canvas as dirty (triggers redraw)
     */
    public CanvasTooltip(Canvas canvas, Runnable markDirtyCallback) {
        this(canvas, markDirtyCallback, Duration.seconds(5));
    }

    /**
     * Creates a canvas tooltip with custom auto-hide duration.
     *
     * @param canvas The canvas this tooltip will be drawn on
     * @param markDirtyCallback Callback to mark the canvas as dirty (triggers redraw)
     * @param autoHideDuration Duration before auto-hide, or null to disable auto-hide
     */
    public CanvasTooltip(Canvas canvas, Runnable markDirtyCallback, Duration autoHideDuration) {
        this.canvas = canvas;
        this.markDirtyCallback = markDirtyCallback;
        this.autoHideDuration = autoHideDuration;
    }

    /**
     * Shows the tooltip at the specified position with title and body content.
     *
     * @param x X position in canvas coordinates (typically mouse X + offset)
     * @param y Y position in canvas coordinates (typically mouse Y)
     * @param title The tooltip title (displayed in bold)
     * @param body The tooltip body text (can contain newlines)
     */
    public void show(double x, double y, String title, String body) {
        this.tooltipX = x;
        this.tooltipY = y;
        this.title = title;
        this.bodyLines = body != null ? body.split("\n") : new String[0];
        this.visible = true;

        // Adjust position to keep tooltip within canvas bounds
        adjustPositionToFitCanvas();

        // Start auto-hide timer
        startAutoHideTimer();

        // Trigger redraw
        if (markDirtyCallback != null) {
            markDirtyCallback.run();
        }
    }

    /**
     * Shows the tooltip at the specified screen position, converting to canvas coordinates.
     * This is useful when handling MouseEvent screen coordinates.
     *
     * @param screenX Screen X coordinate
     * @param screenY Screen Y coordinate
     * @param canvasScreenX Canvas screen X position
     * @param canvasScreenY Canvas screen Y position
     * @param title The tooltip title
     * @param body The tooltip body text
     */
    public void showAtScreen(double screenX, double screenY,
                             double canvasScreenX, double canvasScreenY,
                             String title, String body) {
        // Convert screen coordinates to canvas-local coordinates
        double localX = screenX - canvasScreenX + 10; // 10px offset to the right
        double localY = screenY - canvasScreenY;
        show(localX, localY, title, body);
    }

    /**
     * Hides the tooltip immediately.
     */
    public void hide() {
        if (!visible) {
            return;
        }

        visible = false;
        stopAutoHideTimer();

        // Trigger redraw
        if (markDirtyCallback != null) {
            markDirtyCallback.run();
        }
    }

    /**
     * Returns whether the tooltip is currently visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Draws the tooltip on the canvas.
     * This should be called from the canvas's addOnAfterDraw callback
     * to ensure the tooltip is drawn on top of all other content.
     *
     * @param gc The graphics context to draw on
     */
    public void drawTooltip(GraphicsContext gc) {
        if (!visible || title == null) {
            return;
        }

        // Wrap body lines to fit within MAX_WIDTH
        java.util.List<String> wrappedLines = wrapBodyLines(gc);

        // Calculate tooltip dimensions based on wrapped lines
        double width = calculateTooltipWidth(gc, wrappedLines);
        double height = calculateTooltipHeight(wrappedLines.size());

        // Create bounds for the tooltip
        MutableBounds bounds = new MutableBounds();
        bounds.setMinX(tooltipX);
        bounds.setMinY(tooltipY);
        bounds.setWidth(width);
        bounds.setHeight(height);

        // Draw shadow (slight offset, more transparent)
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRoundRect(tooltipX + 3, tooltipY + 3, width, height, RADIUS, RADIUS);

        // Draw background
        backgroundDrawer.drawBar(bounds, gc);

        // Draw title
        gc.setFill(TITLE_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.TOP);
        gc.fillText(title, tooltipX + PADDING, tooltipY + PADDING);

        // Draw wrapped body lines
        gc.setFill(BODY_COLOR);
        gc.setFont(Font.font("System", BODY_FONT_SIZE));

        double lineY = tooltipY + PADDING + LINE_HEIGHT + 4; // Start after title with some spacing
        for (String line : wrappedLines) {
            if (line != null && !line.isEmpty()) {
                gc.fillText(line, tooltipX + PADDING, lineY);
            }
            lineY += LINE_HEIGHT;
        }
    }

    /**
     * Wraps body lines to fit within the maximum tooltip width.
     */
    private java.util.List<String> wrapBodyLines(GraphicsContext gc) {
        java.util.List<String> wrappedLines = new java.util.ArrayList<>();
        double maxTextWidth = MAX_WIDTH - 2 * PADDING;

        gc.setFont(Font.font("System", BODY_FONT_SIZE));

        for (String line : bodyLines) {
            if (line == null || line.isEmpty()) {
                wrappedLines.add("");
                continue;
            }

            // Check if line needs wrapping
            double lineWidth = measureTextWidth(line, gc);
            if (lineWidth <= maxTextWidth) {
                wrappedLines.add(line);
            } else {
                // Wrap long line by words
                wrapLine(line, maxTextWidth, gc, wrappedLines);
            }
        }

        return wrappedLines;
    }

    /**
     * Wraps a single long line into multiple lines by words.
     */
    private void wrapLine(String line, double maxWidth, GraphicsContext gc, java.util.List<String> result) {
        String[] words = line.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            double testWidth = measureTextWidth(testLine, gc);

            if (testWidth <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Current line is full, start a new line
                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, force break it
                    result.add(truncateWord(word, maxWidth, gc));
                    currentLine = new StringBuilder();
                }
            }
        }

        // Add the last line
        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }
    }

    /**
     * Truncates a word that's too long to fit on a single line.
     */
    private String truncateWord(String word, double maxWidth, GraphicsContext gc) {
        for (int i = word.length() - 1; i > 0; i--) {
            String truncated = word.substring(0, i) + "...";
            if (measureTextWidth(truncated, gc) <= maxWidth) {
                return truncated;
            }
        }
        return "...";
    }

    /**
     * Adjusts the tooltip position to ensure it stays within canvas bounds.
     */
    private void adjustPositionToFitCanvas() {
        if (canvas == null) {
            return;
        }

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        // Estimate tooltip size (rough calculation)
        double estimatedWidth = MAX_WIDTH; // Use max width as estimate since we wrap text
        int estimatedLineCount = bodyLines != null ? Math.max(bodyLines.length * 2, 5) : 5; // Estimate wrapped lines
        double estimatedHeight = calculateTooltipHeight(estimatedLineCount);

        // Adjust X position if tooltip would go off the right edge
        if (tooltipX + estimatedWidth > canvasWidth - PADDING) {
            tooltipX = canvasWidth - estimatedWidth - PADDING;
        }

        // Ensure tooltip doesn't go off the left edge
        if (tooltipX < PADDING) {
            tooltipX = PADDING;
        }

        // Adjust Y position if tooltip would go off the bottom edge
        if (tooltipY + estimatedHeight > canvasHeight - PADDING) {
            // Position above the click point instead
            tooltipY = tooltipY - estimatedHeight - 20;
        }

        // Ensure tooltip doesn't go off the top edge
        if (tooltipY < PADDING) {
            tooltipY = PADDING;
        }
    }

    /**
     * Calculates the tooltip width based on wrapped content.
     */
    private double calculateTooltipWidth(GraphicsContext gc, java.util.List<String> wrappedLines) {
        double maxTextWidth = 0;

        // Measure title width
        gc.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        if (title != null) {
            double titleWidth = measureTextWidth(title, gc);
            maxTextWidth = Math.max(maxTextWidth, titleWidth);
        }

        // Measure wrapped body lines width
        gc.setFont(Font.font("System", BODY_FONT_SIZE));
        for (String line : wrappedLines) {
            if (line != null) {
                double lineWidth = measureTextWidth(line, gc);
                maxTextWidth = Math.max(maxTextWidth, lineWidth);
            }
        }

        // Add padding and clamp to max width
        return Math.min(maxTextWidth + 2 * PADDING, MAX_WIDTH);
    }

    /**
     * Calculates the tooltip height based on number of wrapped lines.
     */
    private double calculateTooltipHeight(int wrappedLineCount) {
        // Title height + spacing + body lines height + padding
        return PADDING + LINE_HEIGHT + 4 + (wrappedLineCount * LINE_HEIGHT) + PADDING;
    }

    /**
     * Measures the width of text using the current font.
     */
    private double measureTextWidth(String text, GraphicsContext gc) {
        // WebFX-compatible text measurement
        javafx.geometry.Bounds textBounds = dev.webfx.kit.launcher.WebFxKitLauncher.measureText(text, gc.getFont());
        return textBounds != null ? textBounds.getWidth() : text.length() * 7; // Fallback estimation
    }

    /**
     * Starts the auto-hide timer.
     */
    private void startAutoHideTimer() {
        stopAutoHideTimer();

        if (autoHideDuration != null) {
            hideTimer = new Timeline(new KeyFrame(autoHideDuration, e -> hide()));
            hideTimer.play();
        }
    }

    /**
     * Stops the auto-hide timer.
     */
    private void stopAutoHideTimer() {
        if (hideTimer != null) {
            hideTimer.stop();
            hideTimer = null;
        }
    }
}
