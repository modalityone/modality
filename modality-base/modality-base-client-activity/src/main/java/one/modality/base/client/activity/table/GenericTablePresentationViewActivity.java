package one.modality.base.client.activity.table;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import one.modality.base.client.activity.themes.Theme;
import dev.webfx.stack.routing.uirouter.activity.presentation.view.impl.PresentationViewActivityImpl;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.util.scene.SceneUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.extras.visual.controls.grid.VisualGrid;

/**
 * @author Bruno Salmon
 */
public abstract class GenericTablePresentationViewActivity<PM extends GenericTablePresentationModel>
        extends PresentationViewActivityImpl<PM>
        implements ButtonFactoryMixin {

    // These height values are the ones for the current GWT implementation of the WebFX data grid.
    private static final int TABLE_HEADER_HEIGHT = 29;
    private static final int TABLE_ROW_HEIGHT = 28;

    protected TextField searchBox;
    protected VisualGrid table;
    protected CheckBox limitCheckBox;

    @Override
    protected void createViewNodes(PM pm) {
        searchBox = newTextField("GenericSearch"); // Will set the prompt
        table = new VisualGrid();
        BorderPane.setAlignment(table, Pos.TOP_CENTER);
        limitCheckBox = newCheckBox("LimitTo100");

        limitCheckBox.textFillProperty().bind(Theme.mainTextFillProperty());

        // Initialization from the presentation model current state
        searchBox.setText(pm.searchTextProperty().getValue());
        limitCheckBox.setSelected(true);
        //searchBox.requestFocus();

        // Binding the UI with the presentation model for further state changes
        // User inputs: the UI state changes are transferred in the presentation model
        pm.searchTextProperty().bind(searchBox.textProperty());
        //pm.limitProperty().bind(Bindings.when(limitCheckBox.selectedProperty()).then(table.heightProperty().divide(36)).otherwise(-1)); // not implemented in webfx-kit-javafxbase-emul
        FXProperties.runNowAndOnPropertiesChange(() -> pm.limitProperty().setValue(limitCheckBox.isSelected() ? (table.getHeight() - TABLE_HEADER_HEIGHT + TABLE_ROW_HEIGHT) / TABLE_ROW_HEIGHT : -1), limitCheckBox.selectedProperty(), table.heightProperty());
        table.fullHeightProperty().bind(limitCheckBox.selectedProperty());
        //pm.limitProperty().bind(limitCheckBox.selectedProperty());
        pm.genericVisualSelectionProperty().bind(table.visualSelectionProperty());
        // User outputs: the presentation model changes are transferred in the UI
        table.visualResultProperty().bind(pm.genericVisualResultProperty());
    }

    @Override
    protected Node assemblyViewNodes() {
        return new BorderPane(table, searchBox, null, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        SceneUtil.autoFocusIfEnabled(searchBox);
    }
}
