package one.modality.event.client2018.controls.sectionpanel;

import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import one.modality.event.client2018.controls.bookingcalendar.BookingCalendar;
import one.modality.base.client.icons.ModalityIcons;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.extras.cell.collator.NodeCollatorRegistry;

import static dev.webfx.stack.ui.json.JsonImageView.createImageView;

/**
 * @author Bruno Salmon
 */
public final class SectionPanelFactory {

    public static BorderPane createSectionPanel(Object i18nKey) {
        return createSectionPanel(null, i18nKey);
    }

    public static Node createSectionPanel(String iconImageUrl, Object i18nKey, ObservableValue<Node> centerProperty) {
        BorderPane sectionPanel = createSectionPanel(iconImageUrl, i18nKey);
        sectionPanel.centerProperty().bind(centerProperty);
        return sectionPanel;
    }

    public static BorderPane createSectionPanel(Object i18nKey, Node center) {
        BorderPane sectionPanel = createSectionPanel(i18nKey);
        sectionPanel.setCenter(center);
        return sectionPanel;
    }

    public static Node createBookingCalendarSection(BookingCalendar bookingCalendar) {
        return createSectionPanel(ModalityIcons.attendanceIcon16JsonUrl,
                "Attendance",
                bookingCalendar.calendarNodeProperty());
    }

    public static BorderPane createSectionPanel() {
        return new BorderPane();
    }

    public static BorderPane createSectionPanel(String iconImageUrl, Object i18nKey) {
        return createSectionPanelWithHeaderNodes(createImageView(iconImageUrl), I18nControls.bindI18nProperties(new Label(), i18nKey));
    }

    public static BorderPane createSectionPanelWithHeaderNodes(Node... headerNodes) {
        BorderPane sectionPanel = new BorderPane();
        sectionPanel.getStyleClass().add("section-panel");
        sectionPanel.setBorder(BorderFactory.newBorder(Color.grayRgb(0x0d), 5, 1));
        sectionPanel.setBackground(BackgroundFactory.WHITE_BACKGROUND);
        return setSectionPanelHeaderNodes(sectionPanel, headerNodes);
    }

    public static BorderPane setSectionPanelHeaderNodes(BorderPane sectionPanel, Node... headerNodes) {
        return setSectionPanelHeaderNode(sectionPanel, NodeCollatorRegistry.hBoxCollator().collateNodes(headerNodes));
    }

    public static BorderPane setSectionPanelHeaderNode(BorderPane sectionPanel, Node headerNode) {
        if (headerNode instanceof Region) {
            Region headerRegion = (Region) headerNode;
            headerRegion.setBackground(BackgroundFactory.newVerticalLinearGradientBackground("0xF0F0F0", "0xE0E0E0",5));
            headerRegion.setMinHeight(40d);
            headerRegion.setPadding(new Insets(0, 10, 0 , 10));
        }
        sectionPanel.setTop(headerNode);
        return sectionPanel;
    }
}
