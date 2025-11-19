package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.Layouts;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.catering.backoffice.activities.kitchen.DietaryOptionKeyPanel;
import one.modality.catering.backoffice.activities.kitchen.MealsSelectionPane;
import one.modality.catering.client.i18n.CateringI18nKeys;

import java.util.Map;
import java.util.Set;

/**
 * Bottom panel containing generation buttons and key panel.
 * Displays meal selection and dietary option key when data is available.
 *
 * @author Claude Code (Extracted from KitchenActivity)
 */
public final class KitchenBottomPanel extends VBox {

    private static final FontDef NO_DATA_MSG_FONT = FontDef.font(FontWeight.BOLD, 18);

    private final HBox buttonContainer;
    private final Pane keyPane;
    private final Button generateScheduledItemsButton;
    private final Button generateScheduledItemsSqlButton;

    public KitchenBottomPanel() {
        super(5);

        // Create buttons
        generateScheduledItemsButton = I18nControls.newButton(CateringI18nKeys.GenerateScheduledItems);
        generateScheduledItemsSqlButton = I18nControls.newButton(CateringI18nKeys.GenerateScheduledItemsSql);

        // Create button container
        buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.getChildren().addAll(generateScheduledItemsButton, generateScheduledItemsSqlButton);

        // Create key pane (will be populated later)
        keyPane = new HBox();

        // Add to this VBox
        getChildren().addAll(buttonContainer, keyPane);

        // Apply styling
        LuminanceTheme.createBottomPanelFacet(this).setShadowed(true).style();
    }

    /**
     * Updates the key pane with meal selection and dietary options.
     * Shows "no data" message if no dietary options are available.
     */
    public void updateKeyPane(
            Map<String, String> dietaryOptionSvgs,
            Set<String> displayedMealNames,
            MealsSelectionPane mealsSelectionPane,
            DietaryOptionKeyPanel dietaryOptionKeyPanel) {

        if (dietaryOptionSvgs.isEmpty()) {
            // Show "no data" message
            Label noDataLabel = I18nControls.newLabel(CateringI18nKeys.NoMealsData);
            TextTheme.createPrimaryTextFacet(noDataLabel).requestedFont(NO_DATA_MSG_FONT).style();
            noDataLabel.setWrapText(true);
            keyPane.getChildren().setAll(noDataLabel);
        } else {
            // Show meal selection and dietary option key
            keyPane.getChildren().setAll(
                    Layouts.createHGrowable(),
                    mealsSelectionPane,
                    Layouts.createHGrowable(),
                    dietaryOptionKeyPanel,
                    Layouts.createHGrowable());
            dietaryOptionKeyPanel.populate(dietaryOptionSvgs);
            mealsSelectionPane.setDisplayedMealNames(displayedMealNames);
        }
    }

    public Button getGenerateScheduledItemsButton() {
        return generateScheduledItemsButton;
    }

    public Button getGenerateScheduledItemsSqlButton() {
        return generateScheduledItemsSqlButton;
    }
}
