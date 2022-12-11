package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class KitchenActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin  {

    private static final String MEAL_COUNT_SQL = "select si.date, i.name, di.code, di.name, count(*), di.ord, di.graphic\n" +
            "from attendance a\n" +
            "  join scheduled_item si on si.id = a.scheduled_item_id\n" +
            "  join document_line dl on dl.id=a.document_line_id\n" +
            "  join site s on s.id=si.site_id\n" +
            "  join item i on i.id=si.item_id\n" +
            "  join item_family f on f.id=i.family_id  \n" +
            "  , ( select i.id,i.code,i.name,i.ord,i.graphic from item i join item_family f on f.id=i.family_id where i.organization_id = $1 and f.code='diet'\n" +
            "    union\n" +
            "    select * from (values (-1, 'Total', null, 10001, null), (-2, '?', null, 10000, null)) vitem(id, code, ord)\n" +
            "    ) di\n" +
            "where not dl.cancelled\n" +
            "  and s.organization_id = $2\n" +
            "  and f.code = 'meals'\n" +
            "  and si.date between $3 and $4\n" +
            "  and case when di.id=-1 then true\n" +
            "       when di.id=-2 then not exists(select * from document_line dl2 join item i2 on i2.id=dl2.item_id join item_family f2 on f2.id=i2.family_id where dl2.document_id=dl.document_id and not dl2.cancelled and f2.code='diet')\n" +
            "       else exists(select * from document_line dl2 where dl2.document_id=dl.document_id and not dl2.cancelled and dl2.item_id=di.id)\n" +
            "        end\n" +
            "group by si.date, i.name, di.code, di.name, i.ord, di.ord, di.graphic\n" +
            "order by si.date, i.ord, di.ord;";

    private VBox body = new VBox();
    private ComboBox<Organization> organizationComboBox = new ComboBox<>();
    private MealsSelectionPane mealsSelectionPane = new MealsSelectionPane();
    private DietaryOptionKeyPanel dietaryOptionKeyPanel = new DietaryOptionKeyPanel();
    private MonthSelectionPanel monthSelectionPanel = new MonthSelectionPanel(this::updateAttendanceMonthPanel);
    private AttendanceMonthPanel attendanceMonthPanel;
    private VBox attendanceCountsPanelContainer = new VBox();
    private AttendanceCounts attendanceCounts;

    public KitchenActivity() {
        mealsSelectionPane.selectedItemsProperty().addListener((observableValue, oldValue, newValue) -> {
            LocalDate selectedMonth = monthSelectionPanel.getSelectedMonth();
            refreshAttendanceMonthPanel(selectedMonth);
        });
    }

    @Override
    public Node buildUi() {
        organizationComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Organization organization) {
                return organization != null ? organization.getName() : null;
            }

            @Override
            public Organization fromString(String s) {
                return null;
            }
        });
        organizationComboBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            mealsSelectionPane.setOrganization(newValue);
            loadAttendance();
        });
        HBox keyPane = new HBox(mealsSelectionPane, dietaryOptionKeyPanel);
        body.getChildren().addAll(organizationComboBox, keyPane, monthSelectionPanel, attendanceCountsPanelContainer);
        return body;
    }

    private void loadAttendance() {
        Organization organization = organizationComboBox.getValue();
        attendanceCounts = new AttendanceCounts();
        if (organization == null || organization.getId() == null) {
            return;
        }
        Object organizationId = organization.getId().getPrimaryKey();
        LocalDate selectedMonth = monthSelectionPanel.getSelectedMonth();
        LocalDate startDate = LocalDate.of(selectedMonth.getYear(), selectedMonth.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        QueryArgument query = new QueryArgumentBuilder().setStatement(MEAL_COUNT_SQL)
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setParameters(organizationId, organizationId, startDate, endDate)
                .build();

        QueryService.executeQuery(query)
                .onFailure(System.out::println)
                .onSuccess(result -> {
                    Set<String> displayedMealNames = new HashSet<>();
                    LinkedHashMap<String, String> dietaryOptionSvgs = new LinkedHashMap();
                    for (int row = 0; row < result.getRowCount(); row++) {
                        String dateString = result.getValue(row, 0);
                        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                        String meal = result.getValue(row, 1);
                        displayedMealNames.add(meal);
                        String dietaryOptionCode = result.getValue(row, 2);
                        String dietaryOptionName = result.getValue(row, 3);
                        int count = result.getValue(row, 4);
                        attendanceCounts.add(date, meal, dietaryOptionCode, count);
                        int dietaryOptionOrdinal = result.getValue(row, 5);
                        attendanceCounts.storeDietaryOptionOrder(dietaryOptionCode, dietaryOptionOrdinal);
                        String svg = result.getValue(row, 6);
                        attendanceCounts.storeDietaryOptionSvg(dietaryOptionCode, svg);
                        if (dietaryOptionCode != null && dietaryOptionName != null && svg != null) {
                            String dietaryOptionKeyText = dietaryOptionName + " (" + dietaryOptionCode + ")";
                            dietaryOptionSvgs.put(dietaryOptionKeyText, svg);
                        }
                    }
                    Platform.runLater(() -> {
                        if (dietaryOptionSvgs.isEmpty()) {
                            dietaryOptionKeyPanel.showNoDataMsg(organization, selectedMonth);
                        } else {
                            dietaryOptionKeyPanel.populate(dietaryOptionSvgs);
                        }
                        refreshAttendanceMonthPanel(selectedMonth);
                        mealsSelectionPane.setDisplayedMealNames(displayedMealNames);
                    });
                });
    }

    private void updateAttendanceMonthPanel(LocalDate month) {
        loadAttendance();
        refreshAttendanceMonthPanel(month);
    }

    private void refreshAttendanceMonthPanel(LocalDate month) {
        List<Item> displayedMeals = mealsSelectionPane.selectedItemsProperty().get();
        AbbreviationGenerator abbreviationGenerator = mealsSelectionPane.getAbbreviationGenerator();
        attendanceMonthPanel = new AttendanceMonthPanel(attendanceCounts, month, displayedMeals, abbreviationGenerator);
        Platform.runLater(() -> attendanceCountsPanelContainer.getChildren().setAll(attendanceMonthPanel));
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
                    Platform.runLater(() -> {
                        organizationComboBox.setItems(FXCollections.observableArrayList(organizationList));
                        if (!organizationList.isEmpty()) {
                            Platform.runLater(() -> organizationComboBox.setValue(organizationList.get(0)));
                        }
                    });
                });
    }

}
