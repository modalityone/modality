package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.markers.EntityHasName;

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

    private static final Font NO_DATA_MSG_FONT = Font.font("Monserrat", FontWeight.BOLD, 24);
    private static final Color NO_DATA_MSG_TEXT_COLOR = Color.web("#0096d6");
    private static final Color TITLE_TEXT_COLOR = Color.web("#0096d6");

    private VBox body = new VBox();
    private ComboBox<Organization> organizationComboBox = new ComboBox<>();
    private MealsSelectionPane mealsSelectionPane = new MealsSelectionPane();
    private DietaryOptionKeyPanel dietaryOptionKeyPanel = new DietaryOptionKeyPanel();
    private MonthSelectionPanel monthSelectionPanel = new MonthSelectionPanel(this::updateAttendanceMonthPanel);
    private AttendanceMonthPanel attendanceMonthPanel;
    private HBox keyPane = new HBox();
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
        Label organizationLabel = new Label("Organization");
        organizationLabel.setTextFill(TITLE_TEXT_COLOR);
        VBox organizationPane = new VBox(organizationLabel, organizationComboBox);
        HBox topPane = new HBox(organizationPane, keyPane);
        body.getChildren().addAll(topPane, monthSelectionPanel, attendanceCountsPanelContainer);
        organizationComboBox.prefWidthProperty().bind(body.widthProperty().divide(3));
        mealsSelectionPane.prefWidthProperty().bind(body.widthProperty().divide(3));
        dietaryOptionKeyPanel.prefWidthProperty().bind(body.widthProperty().divide(3));
        body.setBackground(dev.webfx.stack.ui.util.background.BackgroundFactory.newBackground(Color.WHITE));
        return body;
    }

    private void loadAttendance() {
        Organization organization = organizationComboBox.getValue();
        attendanceCounts = new AttendanceCounts();
        if (organization == null || organization.getId() == null) {
            return;
        }

        attendanceCounts.storeDietaryOptionSvg("Total", "{fill: '#828788', svgPath: 'm 0.971924,10.7805 c 0,-2.85307 1.133386,-5.5893 3.150816,-7.60673 2.01743,-2.01744 4.75366,-3.1508208 7.60676,-3.1508208 2.8531,0 5.5893,1.1333808 7.6067,3.1508208 2.0175,2.01743 3.1508,4.75366 3.1508,7.60673 0,2.8531 -1.1333,5.5893 -3.1508,7.6068 -2.0174,2.0174 -4.7536,3.1508 -7.6067,3.1508 -2.8531,0 -5.58933,-1.1334 -7.60676,-3.1508 C 2.10531,16.3698 0.971924,13.6336 0.971924,10.7805 Z M 11.7295,1.36764 C 9.95688,1.36774 8.22032,1.86836 6.71969,2.81188 5.21906,3.75541 4.01535,5.10349 3.2471,6.70096 2.47885,8.29844 2.17729,10.0804 2.37713,11.8417 c 0.19984,1.7613 0.89295,3.4304 1.99956,4.8151 0.95473,-1.5383 3.05649,-3.1869 7.35281,-3.1869 4.2963,0 6.3967,1.6472 7.3528,3.1869 1.1066,-1.3847 1.7997,-3.0538 1.9995,-4.8151 C 21.2817,10.0804 20.9801,8.29844 20.2119,6.70096 19.4436,5.10349 18.2399,3.75541 16.7393,2.81188 15.2386,1.86836 13.5021,1.36774 11.7295,1.36764 Z m 4.034,6.72357 c 0,1.06991 -0.425,2.09599 -1.1816,2.85249 -0.7565,0.7566 -1.7826,1.1816 -2.8525,1.1816 -1.0699,0 -2.09599,-0.425 -2.85253,-1.1816 C 8.12033,10.1872 7.69531,9.16112 7.69531,8.09121 c 0,-1.0699 0.42502,-2.09599 1.18156,-2.85253 0.75654,-0.75653 1.78263,-1.18155 2.85253,-1.18155 1.0699,0 2.096,0.42502 2.8525,1.18155 0.7566,0.75654 1.1816,1.78263 1.1816,2.85253 z'}");
        Object organizationId = organization.getId().getPrimaryKey();
        LocalDate selectedMonth = monthSelectionPanel.getSelectedMonth();
        LocalDate startDate = LocalDate.of(selectedMonth.getYear(), selectedMonth.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        QueryArgument query = new QueryArgumentBuilder().setStatement(MEAL_COUNT_SQL)
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setParameters(organizationId, organizationId, startDate, endDate)
                .build();

        QueryService.executeQuery(query)
                .onFailure(System.err::println)
                .onSuccess(result -> {
                    Set<String> displayedMealNames = new HashSet<>();
                    LinkedHashMap<String, String> dietaryOptionSvgs = new LinkedHashMap<>();
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
                        if (svg != null)
                            attendanceCounts.storeDietaryOptionSvg(dietaryOptionCode, svg);
                        if (dietaryOptionCode != null && dietaryOptionName != null && svg != null) {
                            String dietaryOptionKeyText = dietaryOptionName + " (" + dietaryOptionCode + ")";
                            dietaryOptionSvgs.put(dietaryOptionKeyText, svg);
                        }
                    }
                    Platform.runLater(() -> {
                        if (dietaryOptionSvgs.isEmpty()) {
                            String monthString = MonthSelectionPanel.buildMonthDisplayText(selectedMonth);
                            String msg = "No meal data for " + monthString + " for " + organization.getName();
                            Label noDataLabel = new Label(msg);
                            noDataLabel.setTextFill(NO_DATA_MSG_TEXT_COLOR);
                            noDataLabel.setFont(NO_DATA_MSG_FONT);
                            noDataLabel.setWrapText(true);
                            keyPane.getChildren().setAll(noDataLabel);
                        } else {
                            keyPane.getChildren().setAll(mealsSelectionPane, dietaryOptionKeyPanel);
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
                            .sorted(Comparator.comparing(EntityHasName::getName))
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
