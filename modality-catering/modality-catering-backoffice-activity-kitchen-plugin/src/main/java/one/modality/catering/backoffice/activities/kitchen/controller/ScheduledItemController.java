package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.scene.control.Button;
import one.modality.catering.backoffice.activities.kitchen.model.ScheduledItemGenerator;

import java.time.YearMonth;

/**
 * Controls scheduled item generation operations.
 * Manages button state and delegates to ScheduledItemGenerator.
 *
 * @author Claude Code (Extracted from KitchenActivity)
 */
public final class ScheduledItemController {

    private final ScheduledItemGenerator generator;
    private final Button generateMissingButton;
    private final Button generateFromTimelinesButton;

    public ScheduledItemController(
            ScheduledItemGenerator generator,
            Button generateMissingButton,
            Button generateFromTimelinesButton) {
        this.generator = generator;
        this.generateMissingButton = generateMissingButton;
        this.generateFromTimelinesButton = generateFromTimelinesButton;
    }

    /**
     * Handles the "Generate Missing" button click.
     * Disables button during operation and re-enables on completion.
     */
    public void handleGenerateMissing(EntityId organizationId, YearMonth yearMonth, Runnable onSuccess) {
        if (yearMonth == null || organizationId == null) {
            Console.log("Cannot generate scheduled items: month or organization not selected");
            return;
        }

        // Disable button during generation
        generateMissingButton.setDisable(true);

        generator.generateMissingScheduledItems(organizationId, yearMonth)
                .onFailure(error -> {
                    Console.log("Error during scheduled item generation: " + error.getMessage());
                    generateMissingButton.setDisable(false);
                })
                .onSuccess(count -> {
                    Console.log("Generation complete. Created " + count + " scheduled items");
                    generateMissingButton.setDisable(false);
                    if (count > 0 && onSuccess != null) {
                        onSuccess.run();
                    }
                });
    }

    /**
     * Handles the "Generate from Timelines (SQL)" button click.
     * Disables button during operation and re-enables on completion.
     */
    public void handleGenerateFromTimelines(EntityId organizationId, YearMonth yearMonth, Runnable onSuccess) {
        if (yearMonth == null || organizationId == null) {
            Console.log("Cannot generate scheduled items: month or organization not selected");
            return;
        }

        // Disable button during generation
        generateFromTimelinesButton.setDisable(true);

        generator.generateScheduledItemsFromTimelines(organizationId, yearMonth)
                .onFailure(error -> {
                    Console.log("Error during timeline-based generation: " + error.getMessage());
                    generateFromTimelinesButton.setDisable(false);
                })
                .onSuccess(result -> {
                    Console.log("Timeline-based generation complete");
                    generateFromTimelinesButton.setDisable(false);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
    }
}
