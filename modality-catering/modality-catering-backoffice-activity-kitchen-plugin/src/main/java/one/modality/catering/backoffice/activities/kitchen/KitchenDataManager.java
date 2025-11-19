package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.reactive.call.query.ReactiveQueryCall;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages data fetching and processing for the Kitchen activity.
 * Encapsulates the reactive query logic and result processing.
 *
 * @author Bruno Salmon
 */
public class KitchenDataManager {

    private static final String MEALS_COUNT_SQL = // language=SQL
        """
            select si.date, i.name, di.code, di.name, count(*), di.ord, di.graphic
             from attendance a
              join scheduled_item si on si.id = a.scheduled_item_id
              join document_line dl on dl.id=a.document_line_id
              join site s on s.id=si.site_id
              join item i on i.id=si.item_id
              join item_family f on f.id=i.family_id
              , ( select i.id,i.code,i.name,i.ord,i.graphic from item i join item_family f on f.id=i.family_id where i.organization_id = $1 and f.code='diet'
                union
                select * from (values (-1, 'Total', null, 10001, null), (-2, '?', null, 10000, null)) vitem(id, code, ord)
                ) di
             where not dl.cancelled
              and s.organization_id = $1
              and f.code = 'meals'
              and si.date between $2 and $3
              and case when di.id=-1 then true
                   when di.id=-2 then not exists(select * from document_line dl2 join item i2 on i2.id=dl2.item_id join item_family f2 on f2.id=i2.family_id where dl2.document_id=dl.document_id and not dl2.cancelled and f2.code='diet')
                   else exists(select * from document_line dl2 where dl2.document_id=dl.document_id and not dl2.cancelled and dl2.item_id=di.id)
                    end
             group by si.date, i.name, di.code, di.name, i.ord, di.ord, di.graphic
             order by si.date, i.ord, di.ord;""";

    private final DataSourceModel dataSourceModel;
    private final ReactiveQueryCall reactiveQueryCall;

    private final ObjectProperty<AttendanceCounts> attendanceCountsProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Set<String>> displayedMealNamesProperty = new SimpleObjectProperty<>(new HashSet<>());
    private final ObjectProperty<Map<String, String>> dietaryOptionSvgsProperty = new SimpleObjectProperty<>(new HashMap<>());

    private final ObjectProperty<LocalDate> startDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<EntityId> organizationIdProperty = new SimpleObjectProperty<>();

    public KitchenDataManager(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        this.reactiveQueryCall = new ReactiveQueryCall();

        // Update query when parameters change
        FXProperties.runOnPropertiesChange(this::updateQueryArgument, startDateProperty, endDateProperty, organizationIdProperty);

        // Process results when they arrive
        FXProperties.runOnPropertyChange(result -> {
            AttendanceCounts attendanceCounts = new AttendanceCounts();
            attendanceCounts.storeDietaryOptionSvg("Total", "{fill: '#828788', svgPath: 'm 0.971924,10.7805 c 0,-2.85307 1.133386,-5.5893 3.150816,-7.60673 2.01743,-2.01744 4.75366,-3.1508208 7.60676,-3.1508208 2.8531,0 5.5893,1.1333808 7.6067,3.1508208 2.0175,2.01743 3.1508,4.75366 3.1508,7.60673 0,2.8531 -1.1333,5.5893 -3.1508,7.6068 -2.0174,2.0174 -4.7536,3.1508 -7.6067,3.1508 -2.8531,0 -5.58933,-1.1334 -7.60676,-3.1508 C 2.10531,16.3698 0.971924,13.6336 0.971924,10.7805 Z M 11.7295,1.36764 C 9.95688,1.36774 8.22032,1.86836 6.71969,2.81188 5.21906,3.75541 4.01535,5.10349 3.2471,6.70096 2.47885,8.29844 2.17729,10.0804 2.37713,11.8417 c 0.19984,1.7613 0.89295,3.4304 1.99956,4.8151 0.95473,-1.5383 3.05649,-3.1869 7.35281,-3.1869 4.2963,0 6.3967,1.6472 7.3528,3.1869 1.1066,-1.3847 1.7997,-3.0538 1.9995,-4.8151 C 21.2817,10.0804 20.9801,8.29844 20.2119,6.70096 19.4436,5.10349 18.2399,3.75541 16.7393,2.81188 15.2386,1.86836 13.5021,1.36774 11.7295,1.36764 Z m 4.034,6.72357 c 0,1.06991 -0.425,2.09599 -1.1816,2.85249 -0.7565,0.7566 -1.7826,1.1816 -2.8525,1.1816 -1.0699,0 -2.09599,-0.425 -2.85253,-1.1816 C 8.12033,10.1872 7.69531,9.16112 7.69531,8.09121 c 0,-1.0699 0.42502,-2.09599 1.18156,-2.85253 0.75654,-0.75653 1.78263,-1.18155 2.85253,-1.18155 1.0699,0 2.096,0.42502 2.8525,1.18155 0.7566,0.75654 1.1816,1.78263 1.1816,2.85253 z'}");

            Set<String> displayedMealNames = new HashSet<>();
            Map<String, String> dietaryOptionSvgs = new HashMap<>();

            for (int row = 0; row < result.getRowCount(); row++) {
                LocalDate date = result.getValue(row, 0);
                String meal = result.getValue(row, 1);
                displayedMealNames.add(meal);
                String dietaryOptionCode = result.getValue(row, 2);
                String dietaryOptionName = result.getValue(row, 3);
                int count = result.getInt(row, 4, 0);
                attendanceCounts.add(date, meal, dietaryOptionCode, count);
                int dietaryOptionOrdinal = result.getInt(row, 5, 0);
                attendanceCounts.storeDietaryOptionOrder(dietaryOptionCode, dietaryOptionOrdinal);
                String svg = result.getValue(row, 6);
                if (svg != null)
                    attendanceCounts.storeDietaryOptionSvg(dietaryOptionCode, svg);
                if (dietaryOptionCode != null && dietaryOptionName != null && svg != null) {
                    String dietaryOptionKeyText = dietaryOptionName + " (" + dietaryOptionCode + ")";
                    dietaryOptionSvgs.put(dietaryOptionKeyText, svg);
                }
            }

            attendanceCountsProperty.set(attendanceCounts);
            displayedMealNamesProperty.set(displayedMealNames);
            dietaryOptionSvgsProperty.set(dietaryOptionSvgs);
        }, reactiveQueryCall.resultProperty());
    }

    private void updateQueryArgument() {
        LocalDate startDate = startDateProperty.get();
        LocalDate endDate = endDateProperty.get();
        EntityId organizationId = organizationIdProperty.get();
        if (startDate == null || endDate == null || organizationId == null)
            return;

        QueryArgument queryArgument = new QueryArgumentBuilder()
                .setStatement(MEALS_COUNT_SQL)
                .setDataSourceId(dataSourceModel.getDataSourceId())
                .setParameters(
                        organizationId.getPrimaryKey(),        // $1
                        startDate,                             // $2
                        endDate                                // $3
                )
                .build();

        reactiveQueryCall.setArgument(queryArgument);
    }

    public void start() {
        reactiveQueryCall.setResultCacheEntry("modality/catering/kitchen/meals-count");
        reactiveQueryCall.start();
    }

    public void bindActivePropertyTo(javafx.beans.value.ObservableValue<Boolean> activeProperty) {
        reactiveQueryCall.bindActivePropertyTo(activeProperty);
    }

    // Getters for properties
    public ObjectProperty<AttendanceCounts> attendanceCountsProperty() {
        return attendanceCountsProperty;
    }

    public AttendanceCounts getAttendanceCounts() {
        return attendanceCountsProperty.get();
    }

    public ObjectProperty<Set<String>> displayedMealNamesProperty() {
        return displayedMealNamesProperty;
    }

    public Set<String> getDisplayedMealNames() {
        return displayedMealNamesProperty.get();
    }

    public ObjectProperty<Map<String, String>> dietaryOptionSvgsProperty() {
        return dietaryOptionSvgsProperty;
    }

    public Map<String, String> getDietaryOptionSvgs() {
        return dietaryOptionSvgsProperty.get();
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDateProperty;
    }

    public void setStartDate(LocalDate startDate) {
        startDateProperty.set(startDate);
    }

    public LocalDate getStartDate() {
        return startDateProperty.get();
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDateProperty;
    }

    public void setEndDate(LocalDate endDate) {
        endDateProperty.set(endDate);
    }

    public LocalDate getEndDate() {
        return endDateProperty.get();
    }

    public ObjectProperty<EntityId> organizationIdProperty() {
        return organizationIdProperty;
    }

    public void setOrganizationId(EntityId organizationId) {
        organizationIdProperty.set(organizationId);
    }
}
