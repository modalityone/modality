package org.modality_project.event.frontoffice.activities.terms;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.modality_project.base.client.icons.MongooseIcons;
import org.modality_project.event.client.controls.sectionpanel.SectionPanelFactory;
import org.modality_project.ecommerce.client.activity.bookingprocess.BookingProcessPresentationViewActivity;
import dev.webfx.framework.client.ui.util.layout.LayoutUtil;
import dev.webfx.extras.cell.collator.grid.GridCollator;

/**
 * @author Bruno Salmon
 */
final class TermsPresentationViewActivity extends BookingProcessPresentationViewActivity<TermsPresentationModel> {

    private BorderPane termsPanel;

    @Override
    protected void createViewNodes(TermsPresentationModel pm) {
        super.createViewNodes(pm);
        GridCollator termsLetterCollator = new GridCollator("first", "first");
        termsPanel = SectionPanelFactory.createSectionPanel(MongooseIcons.certificateMonoSvg16JsonUrl, "TermsAndConditions");
        termsPanel.setCenter(LayoutUtil.createVerticalScrollPaneWithPadding(termsLetterCollator));

        termsLetterCollator.visualResultProperty().bind(pm.termsLetterVisualResultProperty());
    }

    @Override
    protected Node assemblyViewNodes() {
        return LayoutUtil.createPadding(new VBox(10, LayoutUtil.setVGrowable(termsPanel), LayoutUtil.setMaxWidthToInfinite(backButton)));
    }
}
