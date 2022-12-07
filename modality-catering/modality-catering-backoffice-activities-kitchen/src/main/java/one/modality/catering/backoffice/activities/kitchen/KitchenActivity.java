package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class KitchenActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin  {

    private final AttendancePresentationModel pm = new AttendancePresentationModel();

    private final Property<AttendanceCounts> attendanceCountsProperty = new SimpleObjectProperty<>();

    private VBox body = new VBox();
    private ComboBox<Organization> organizationComboBox = new ComboBox<>();
    private MonthSelectionPanel monthSelectionPanel = new MonthSelectionPanel(this::updateAttendanceMonthPanel);
    private AttendanceMonthPanel attendanceMonthPanel;
    private VBox attendanceCountsPanelContainer = new VBox();
    private AttendanceCounts attendanceCounts;

    @Override
    public Node buildUi() {
        organizationComboBox.setConverter(new StringConverter<Organization>() {
            @Override
            public String toString(Organization organization) {
                return organization != null ? organization.getName() : null;
            }

            @Override
            public Organization fromString(String s) {
                return null;
            }
        });
        organizationComboBox.valueProperty().addListener(new ChangeListener<Organization>() {
            @Override
            public void changed(ObservableValue<? extends Organization> observableValue, Organization oldValue, Organization newValue) {
                loadAttendance(newValue);
            }
        });
        body.getChildren().addAll(organizationComboBox, monthSelectionPanel, attendanceCountsPanelContainer);
        return body;
    }

    private void loadAttendance(Organization organization) {
        attendanceCounts = new AttendanceCounts();
        if (organization == null || organization.getId() == null) {
            return;
        }
        EntityStore.create(getDataSourceModel())
                .executeQuery("select documentLine,documentLine.item,date from Attendance a where (a.[documentLine as dl].(!cancelled)) and documentLine.document.organization=" + organization.getId().getPrimaryKey() + " and documentLine.item.family.code = 'meals'")
                .onFailure(System.out::println)
                .onSuccess(attendances -> {
                    attendances.stream()
                            .map(entity -> (Attendance) entity)
                            .forEach(attendance -> {
                                String itemCode = getItemCode(attendance);
                                attendanceCounts.add(attendance.getDate(), itemCode);
                            });
                    Platform.runLater(() -> updateAttendanceMonthPanel(LocalDate.now()));
                });
    }

    private String getItemCode(Attendance attendance) {
        Item item = attendance.getDocumentLine().getItem();
        return item.getCode() != null ? item.getCode() : "DIN";
    }

    private void updateAttendanceMonthPanel(LocalDate month) {
        attendanceMonthPanel = new AttendanceMonthPanel(attendanceCounts, month);
        attendanceCountsPanelContainer.getChildren().setAll(attendanceMonthPanel);
    }

    public KitchenActivity() {
        pm.setEventId(Integer.valueOf(734));
        attendanceCountsProperty.addListener(new ChangeListener<AttendanceCounts>() {
            @Override
            public void changed(ObservableValue<? extends AttendanceCounts> observableValue, AttendanceCounts oldValue, AttendanceCounts newValue) {
                System.out.println(newValue);
            }
        });

    }

    @Override
    protected void startLogic() {
        // Populate organization combo box
        EntityStore.create(getDataSourceModel())
                .executeQuery("select name from Organization where !closed and name!=`ISC`")
                .onSuccess(organizations -> {
                    List<Organization> organizationList = organizations.stream()
                            .map(entity -> (Organization) entity)
                            .sorted((org1, org2) -> org1.getName().compareTo(org2.getName()))
                            .collect(Collectors.toList());
                    organizationComboBox.setItems(FXCollections.observableArrayList(organizationList));
                });
    }

}
