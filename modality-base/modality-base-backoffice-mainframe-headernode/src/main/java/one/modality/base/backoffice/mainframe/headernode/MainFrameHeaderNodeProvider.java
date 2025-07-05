package one.modality.base.backoffice.mainframe.headernode;

import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.ServiceLoader;

public interface MainFrameHeaderNodeProvider {

    String getName();

    Node getHeaderNode(ButtonFactoryMixin buttonFactory, Pane frameContainer, DataSourceModel dataSourceModel);

    static List<MainFrameHeaderNodeProvider> getProviders() {
        return MultipleServiceProviders.getProviders(MainFrameHeaderNodeProvider.class, () -> ServiceLoader.load(MainFrameHeaderNodeProvider.class));
    }
}
