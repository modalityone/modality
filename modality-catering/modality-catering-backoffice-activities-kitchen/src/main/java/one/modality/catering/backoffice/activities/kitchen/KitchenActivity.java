package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.dql.query.ReactiveDqlQuery;
import dev.webfx.stack.orm.reactive.dql.querypush.ReactiveDqlQueryPush;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public class KitchenActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin  {

    private final AttendancePresentationModel pm = new AttendancePresentationModel();

    private final ObjectProperty<VisualResult> organizationVisualResultProperty = new SimpleObjectProperty<>();
    private final Property<AttendanceCounts> attendanceCountsProperty = new SimpleObjectProperty<>();

    private ReactiveVisualMapper<DocumentLine> leftGroupVisualMapper;
    private ReactiveVisualMapper<Attendance> rightAttendanceVisualMapper;

    private HBox body = new HBox();
    private ComboBox<Organization> organizationComboBox = new ComboBox<>();
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
                attendanceCounts = new AttendanceCounts();
                if (newValue != null) {
                    // TODO call loadAttendance
                }
            }
        });
        return body;
    }

    /*private void loadAttendance(Organization organization) {
        ReactiveObjectsMapper.<Attendance, Object>createPushReactiveChain(this)
                //.always("{class: 'Attendance', alias: 'a', columns: `documentLine,documentLine.(site,item),date`, where: 'present', orderBy: 'date'}")
                //.always("{class: 'Attendance', alias: 'a', fields: `documentLine,date`, where: 'present', orderBy: 'date'}")
                .always("{class: 'Attendance', alias: 'a', fields: `documentLine,documentLine.item,date`, where: 'present', orderBy: 'date'}")
                //.always("{class: 'Attendance', alias: 'a', fields: `documentLine,documentLine.(site,item),date`, where: 'present', orderBy: 'date'}")
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(a.[documentLine as dl].(!cancelled)) and documentLine.document.event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(a.[documentLine as dl].(!cancelled)) and documentLine.document.event=? and documentLine.item.family.code = 'meals'", eventId))
                .setIndividualEntityToObjectMapperFactory(AttendanceToObjectMapper::new)
                //.setStore(moneyAccountVisualMapper.getStore())
                .storeMappedObjectsInto(deleteMe)
                .start();
    }*/

    private AttendanceMonthPanel attendanceMonthPanel;

    public KitchenActivity() {
        pm.setEventId(Integer.valueOf(734));
        attendanceCountsProperty.addListener(new ChangeListener<AttendanceCounts>() {
            @Override
            public void changed(ObservableValue<? extends AttendanceCounts> observableValue, AttendanceCounts oldValue, AttendanceCounts newValue) {
                System.out.println(newValue);
            }
        });

    }

    private void refreshUi() {
        body.getChildren().clear();
        body.getChildren().add(organizationComboBox);
        createAttendanceMonthPanel();
        body.getChildren().add(attendanceMonthPanel);
    }

    private void createAttendanceMonthPanel() {
        attendanceMonthPanel = new AttendanceMonthPanel(attendanceCounts, LocalDate.of(2021, 8, 1));
    }

    @Override
    protected void startLogic() {
        // Populate organization combo box
        EntityStore.create(getDataSourceModel())
                .executeQuery("select name from Organization where !closed and name!=`ISC`")
                .onSuccess(organizations -> {
                    List<Organization> organizationList = organizations.stream()
                            .map(entity -> (Organization) entity)
                            .collect(Collectors.toList());
                    organizationComboBox.setItems(FXCollections.observableArrayList(organizationList));
                });


        //int eventId = 734; // TODO don't hard-code this
        pm.eventIdProperty().set(Integer.valueOf(734)); // TODO don't hard-code this

        ObservableList<Object> deleteMe = FXCollections.observableArrayList();
        deleteMe.addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> change) {
                System.out.println("attendanceCounts.getSortedDates() = " + attendanceCounts.getSortedDates());
                refreshUi();
            }
        });
        ReactiveObjectsMapper.<Attendance, Object>createPushReactiveChain(this)
                //.always("{class: 'Attendance', alias: 'a', columns: `documentLine,documentLine.(site,item),date`, where: 'present', orderBy: 'date'}")
                //.always("{class: 'Attendance', alias: 'a', fields: `documentLine,date`, where: 'present', orderBy: 'date'}")
                .always("{class: 'Attendance', alias: 'a', fields: `documentLine,documentLine.item,date`, where: 'present', orderBy: 'date'}")
                //.always("{class: 'Attendance', alias: 'a', fields: `documentLine,documentLine.(site,item),date`, where: 'present', orderBy: 'date'}")
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(a.[documentLine as dl].(!cancelled)) and documentLine.document.event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(a.[documentLine as dl].(!cancelled)) and documentLine.document.event=? and documentLine.item.family.code = 'meals'", eventId))
                .setIndividualEntityToObjectMapperFactory(AttendanceToObjectMapper::new)
                //.setStore(moneyAccountVisualMapper.getStore())
                .storeMappedObjectsInto(deleteMe)
                .start();

        //Query: select site,site.name,item,item.(family,family.code,name,temporal,family.name),count(1) from DocumentLine dl where (!cancelled) and (document.event=?) group by site,item order by item.family.ord,site.ord,item.ord
        /*leftGroupVisualMapper = ReactiveVisualMapper.<DocumentLine>createGroupReactiveChain(this, pm)
                .always("{class: 'DocumentLine', alias: 'dl', fields: `site,site.name,item,item.(family,family.code,name,temporal,family.name),count(1)`, groupBy: `site,item order by item.family.ord,site.ord,item.ord`}")
                // Applying the event condition
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("document.event=?", eventId))
        ;

        //Query: select documentLine,documentLine.(site,item),date,count(1) from Attendance a where ((present) and (documentLine.document.event=?)) and (a.[documentLine as dl].(!cancelled)) group by documentLine.(site,item),date order by date
        rightAttendanceVisualMapper = ReactiveVisualMapper.<Attendance>createReactiveChain(this)
                .always("{class: 'Attendance', alias: 'a', fields: `documentLine,documentLine.(site,item),date`, where: 'present', orderBy: 'date'}")
                //.ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("documentLine.document.event=?", eventId))
                .ifNotNullOtherwiseEmpty(pm.eventIdProperty(), eventId -> where("(a.[documentLine as dl].(!cancelled)) and documentLine.document.event=?", eventId))
        ;

        new AttendanceBuilder(leftGroupVisualMapper, rightAttendanceVisualMapper, leftGroupVisualMapper.visualResultProperty())
                .start();*/

    }

    private AttendanceCounts attendanceCounts = new AttendanceCounts();

    class AttendanceToObjectMapper implements IndividualEntityToObjectMapper<Attendance, Object> {

        AttendanceToObjectMapper(Attendance attendance) {
            System.out.println("attendance = " + attendance);
            System.out.println("attendance.getDate() = " + attendance.getDate());
            DocumentLine documentLine = attendance.getDocumentLine();
            System.out.println("documentLine.getItem().getFamily().getCode() = " + documentLine.getItem().getFamily().getCode());
            String itemCode = getItemCode(attendance);
            attendanceCounts.add(attendance.getDate(), itemCode);
        }

        private String getItemCode(Attendance attendance) {
            Item item = attendance.getDocumentLine().getItem();
            return item.getCode() != null ? item.getCode() : "DIN";
        }

        @Override
        public Object getMappedObject() {
            return null;
        }

        @Override
        public void onEntityChangedOrReplaced(Attendance attendance) {

        }

        @Override
        public void onEntityRemoved(Attendance attendance) {
            String itemCode = getItemCode(attendance);
            attendanceCounts.remove(attendance.getDate(), itemCode);
        }
    }

    @Override
    protected void refreshDataOnActive() {
        leftGroupVisualMapper.refreshWhenActive();
        rightAttendanceVisualMapper.refreshWhenActive();
        //masterVisualMapper.refreshWhenActive();
    }
}
