package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

/**
 * @author Bruno Salmon
 */
public class RoomsAlterationView {

  private final ResourceConfigurationLoader resourceConfigurationLoader;

  public RoomsAlterationView(AccommodationPresentationModel pm) {
    resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
  }

  public Node buildView() {
    VBox vBox = new VBox(10);
    ObservableLists.bindConverted(
        vBox.getChildren(),
        resourceConfigurationLoader.getResourceConfigurations(),
        this::createRoomNode);
    return LayoutUtil.createVerticalScrollPane(vBox);
  }

  private Node createRoomNode(ResourceConfiguration rc) {
    return new Text(rc.getName());
  }

  public void startLogic(Object mixin) {
    resourceConfigurationLoader.startLogic(mixin);
  }
}
