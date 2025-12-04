package one.modality.hotel.backoffice.activities.roomsetup.util;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Builder class for constructing form layouts with consistent styling.
 * Implements the Builder pattern to simplify form creation in dialogs.
 *
 * <p>Example usage:
 * <pre>{@code
 * GridPane form = new FormBuilder()
 *     .withHgap(16)
 *     .withVgap(16)
 *     .addField("NAME", nameField)
 *     .addField("ZONE", zoneSelector.getButton())
 *     .addField("TYPE", typeSelector.getButton())
 *     .withLabelWidth(120)
 *     .build();
 * }</pre>
 *
 * @author Claude Code
 */
public class FormBuilder {

    private final GridPane form;

    public FormBuilder() {
        this.form = new GridPane();
    }

    /**
     * Builds and returns the configured GridPane.
     */
    public GridPane build() {
        double hgap = 16;
        form.setHgap(hgap);
        double vgap = 16;
        form.setVgap(vgap);

        // Configure column constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        double labelWidth = 120;
        labelCol.setMinWidth(labelWidth);

        ColumnConstraints controlCol = new ColumnConstraints();
        controlCol.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(labelCol, controlCol);

        return form;
    }

}
