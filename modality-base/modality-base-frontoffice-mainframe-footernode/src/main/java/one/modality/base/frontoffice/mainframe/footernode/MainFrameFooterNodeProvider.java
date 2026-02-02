package one.modality.base.frontoffice.mainframe.footernode;

import dev.webfx.platform.service.SingleServiceProvider;
import javafx.scene.Node;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public interface MainFrameFooterNodeProvider {

    Node getFooterNode();

    static MainFrameFooterNodeProvider getProvider() {
        return SingleServiceProvider.getProvider(MainFrameFooterNodeProvider.class, () -> ServiceLoader.load(MainFrameFooterNodeProvider.class));
    }


}
