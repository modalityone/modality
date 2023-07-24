package one.modality.base.client.activity.table;

import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import one.modality.base.client.activity.themes.Theme;

/**
 * @author Bruno Salmon
 */
public class GenericTable<PM extends GenericTablePresentationModel> {

    // These height values are the ones for the current GWT implementation of the WebFX data grid.
    private static final int TABLE_HEADER_HEIGHT = 29;
    private static final int TABLE_ROW_HEIGHT = 28;

    private final TextField searchBox;
    private final VisualGrid table;
    private final CheckBox limitCheckBox;

    public GenericTable(PM pm, ButtonFactoryMixin buttonFactory) {
        searchBox = buttonFactory.newTextField("GenericSearch"); // Will set the prompt
        table = new VisualGrid();
        BorderPane.setAlignment(table, Pos.TOP_CENTER);
        limitCheckBox = buttonFactory.newCheckBox("LimitTo100");

        limitCheckBox.textFillProperty().bind(Theme.mainTextFillProperty());

        // Initialization from the presentation model current state
        searchBox.setText(pm.searchTextProperty().getValue());
        limitCheckBox.setSelected(true);
        // searchBox.requestFocus();

        // Binding the UI with the presentation model for further state changes
        // User inputs: the UI state changes are transferred in the presentation model
        pm.searchTextProperty().bind(searchBox.textProperty());
        // pm.limitProperty().bind(Bindings.when(limitCheckBox.selectedProperty()).then(table.heightProperty().divide(36)).otherwise(-1)); // not implemented in webfx-kit-javafxbase-emul
        FXProperties.runNowAndOnPropertiesChange(
                () ->
                        pm.limitProperty()
                                .setValue(
                                        limitCheckBox.isSelected()
                                                ? (table.getHeight()
                                                                - TABLE_HEADER_HEIGHT
                                                                + TABLE_ROW_HEIGHT)
                                                        / TABLE_ROW_HEIGHT
                                                : -1),
                limitCheckBox.selectedProperty(),
                table.heightProperty());
        table.fullHeightProperty().bind(limitCheckBox.selectedProperty());
        // pm.limitProperty().bind(limitCheckBox.selectedProperty());
        pm.genericVisualSelectionProperty().bindBidirectional(table.visualSelectionProperty());
        // User outputs: the presentation model changes are transferred in the UI
        table.visualResultProperty().bind(pm.genericVisualResultProperty());
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    public VisualGrid getTable() {
        return table;
    }

    public CheckBox getLimitCheckBox() {
        return limitCheckBox;
    }

    public Node assemblyViewNodes() {
        return new BorderPane(table, searchBox, null, null, null);
    }

    public void onResume() {
        SceneUtil.autoFocusIfEnabled(searchBox);
    }
}
