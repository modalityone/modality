package one.modality.base.client.activity.table;

import dev.webfx.stack.routing.uirouter.activity.presentation.view.impl.PresentationViewActivityImpl;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public abstract class GenericTablePresentationViewActivity<PM extends GenericTablePresentationModel>
        extends PresentationViewActivityImpl<PM>
        implements ButtonFactoryMixin {

    protected GenericTable<PM> genericTable;

    @Override
    protected void createViewNodes(PM pm) {
        genericTable = new GenericTable<>(pm, this);
    }

    @Override
    protected Node assemblyViewNodes() {
        return genericTable.assemblyViewNodes();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (genericTable != null)
            genericTable.onResume(); // will request focus for the search box
    }
}
