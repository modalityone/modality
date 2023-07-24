package one.modality.event.frontoffice.activities.terms;

import dev.webfx.extras.cell.collator.grid.GridCollator;
import dev.webfx.extras.util.layout.LayoutUtil;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import one.modality.base.client.icons.ModalityIcons;
import one.modality.ecommerce.client.activity.bookingprocess.BookingProcessPresentationViewActivity;
import one.modality.event.client.controls.sectionpanel.SectionPanelFactory;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationViewActivity
    extends BookingProcessPresentationViewActivity<TermsPresentationModel> {

  private BorderPane termsPanel;

  @Override
  protected void createViewNodes(TermsPresentationModel pm) {
    super.createViewNodes(pm);
    GridCollator termsLetterCollator = new GridCollator("first", "first");
    termsPanel =
        SectionPanelFactory.createSectionPanel(
            ModalityIcons.certificateMonoSvg16JsonUrl, "TermsAndConditions");
    termsPanel.setCenter(LayoutUtil.createVerticalScrollPaneWithPadding(termsLetterCollator));

    termsLetterCollator.visualResultProperty().bind(pm.termsLetterVisualResultProperty());
  }

  @Override
  protected Node assemblyViewNodes() {
    return LayoutUtil.createPadding(
        new VBox(
            10, LayoutUtil.setVGrowable(termsPanel), LayoutUtil.setMaxWidthToInfinite(backButton)));
  }
}
