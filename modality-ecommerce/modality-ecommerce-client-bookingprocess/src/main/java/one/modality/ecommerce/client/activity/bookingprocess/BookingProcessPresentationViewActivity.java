package one.modality.ecommerce.client.activity.bookingprocess;

import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.routing.uirouter.activity.presentation.view.impl.PresentationViewActivityImpl;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.aggregates.event.EventAggregate;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public abstract class BookingProcessPresentationViewActivity<
        PM extends BookingProcessPresentationModel>
    extends PresentationViewActivityImpl<PM> implements ModalityButtonFactoryMixin {

  protected Button backButton;
  protected Button nextButton;

  @Override
  protected void createViewNodes(PM pm) {
    if (backButton == null) backButton = newTransparentButton("<<Back");
    if (nextButton == null) nextButton = newLargeGreenButton("Next>>");
    backButton.onActionProperty().bind(pm.onPreviousActionProperty());
    nextButton.onActionProperty().bind(pm.onNextActionProperty());
  }

  @Override
  protected Node assemblyViewNodes() {
    return new BorderPane(null, null, null, new HBox(backButton, nextButton), null);
  }

  @Override
  protected Node styleUi(Node uiNode, PM pm) {
    if (uiNode instanceof Region)
      FXProperties.runNowAndOnPropertiesChange(
          () ->
              EventAggregate.getOrCreate(
                      pm.getEventId(), DataSourceModelService.getDefaultDataSourceModel())
                  .onEvent()
                  .onComplete(
                      ar -> {
                        Event event = ar.result();
                        if (event != null) {
                          String css = event.getStringFieldValue("cssClass");
                          if (Strings.startsWith(css, "linear-gradient")) {
                            Background eventBackground =
                                BackgroundFactory.newLinearGradientBackground(css);
                            ((Region) uiNode).setBackground(eventBackground);
                          }
                        }
                      }),
          pm.eventIdProperty());
    return uiNode;
  }
}
