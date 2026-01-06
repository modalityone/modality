package one.modality.ecommerce.policy.service;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.util.Rates;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class PolicyAggregate {

    // Fields intended for serialisation
    private final String scheduledItemsQueryBase;
    private final QueryResult scheduledItemsQueryResult;
    private final String ratesQueryBase;
    private final QueryResult ratesQueryResult;
    private final String bookablePeriodsQueryBase;
    private final QueryResult bookablePeriodsQueryResult;
    private final String itemPoliciesQueryBase;
    private final QueryResult itemPoliciesQueryResult;

    // Fields intended for application code
    private Event event;
    private EntityStore entityStore;
    private EntityList<ScheduledItem> scheduledItems;
    private EntityList<Rate> rates;
    private EntityList<BookablePeriod> bookablePeriods;
    private EntityList<ItemPolicy> itemPolicies;

    public PolicyAggregate(String scheduledItemsQueryBase, QueryResult scheduledItemsQueryResult,
                           String ratesQueryBase, QueryResult ratesQueryResult,
                           String bookablePeriodsQueryBase, QueryResult bookablePeriodsQueryResult,
                           String itemPoliciesQueryBase, QueryResult itemPoliciesQueryResult
    ) {
        this.scheduledItemsQueryBase = scheduledItemsQueryBase;
        this.scheduledItemsQueryResult = scheduledItemsQueryResult;
        this.ratesQueryBase = ratesQueryBase;
        this.ratesQueryResult = ratesQueryResult;
        this.bookablePeriodsQueryBase = bookablePeriodsQueryBase;
        this.bookablePeriodsQueryResult = bookablePeriodsQueryResult;
        this.itemPoliciesQueryBase = itemPoliciesQueryBase;
        this.itemPoliciesQueryResult = itemPoliciesQueryResult;
    }

    public void rebuildEntities(Event event) {
        this.event = event;
        this.entityStore = EntityStore.createAbove(event.getStore());
        DataSourceModel dataSourceModel = entityStore.getDataSourceModel();
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(scheduledItemsQueryBase).getQueryMapping();
        scheduledItems = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledItemsQueryResult, queryMapping, entityStore, "scheduledItems");
        queryMapping = dataSourceModel.parseAndCompileSelect(ratesQueryBase).getQueryMapping();
        rates = QueryResultToEntitiesMapper.mapQueryResultToEntities(ratesQueryResult, queryMapping, entityStore, "rates");
        queryMapping = dataSourceModel.parseAndCompileSelect(itemPoliciesQueryBase).getQueryMapping();
        itemPolicies = QueryResultToEntitiesMapper.mapQueryResultToEntities(itemPoliciesQueryResult, queryMapping, entityStore, "itemPolicies");
    }

    public EntityStore getEntityStore() {
        return entityStore;
    }

    public Event getEvent() {
        return event;
    }

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public Stream<ScheduledItem> getScheduledItemsStream() {
        return getScheduledItems().stream();
    }

    public List<ScheduledItem> filterScheduledItemsOfFamily(KnownItemFamily knownItemFamily) {
        return ScheduledItems.filterFamily(getScheduledItems(), knownItemFamily);
    }

    public List<ScheduledItem> filterTeachingScheduledItems() {
        return filterScheduledItemsOfFamily(KnownItemFamily.TEACHING);
    }

    public Map<Item, List<ScheduledItem>> groupScheduledItemsByAudioRecordingItems() {
        return ScheduledItems.groupScheduledItemsByAudioRecordingItems(getScheduledItemsStream());
    }

    public List<Rate> getRates() {
        return rates;
    }

    public Stream<Rate> getRatesStream() {
        return getRates().stream();
    }

    public List<Rate> getDailyRates() {
        return Rates.filterDailyRates(getRates());
    }

    public Stream<Rate> getDailyRatesStream() {
        return Rates.filterDailyRates(getRatesStream());
    }

    public Stream<Rate> getFixedRatesStream() {
        return Rates.filterFixedRates(getRatesStream());
    }

    public Rate getDailyRate() {
        List<Rate> dailyRates = getDailyRates();
        int dailyRatesCount = dailyRates.size();
        if (dailyRatesCount > 1) {
            Console.log("⚠️ WARNING: PolicyAggregate.getDailyRate() is meant to be used with single daily rate policies, but this policy has " + dailyRatesCount + " rates.");
        }
        return Collections.first(dailyRates);
    }

    public int getDailyRatePrice() {
        Rate dailyRate = getDailyRate();
        return dailyRate != null ? dailyRate.getPrice() : 0;
    }

    public Stream<Rate> filterRatesStreamOfSiteAndItem(Site site, Item item) {
        return Rates.filterRatesOfSiteAndItem(getRatesStream(), site, item);
    }

    public Stream<Rate> filterDailyRatesStreamOfSiteAndItem(Site site, Item item) {
        return Rates.filterRatesOfSiteAndItem(getDailyRatesStream(), site, item);
    }

    public Stream<Rate> filterFixedRatesStreamOfSiteAndItem(Site site, Item item) {
        return Rates.filterRatesOfSiteAndItem(getFixedRatesStream(), site, item);
    }

    public boolean hasFacilityFees() {
        return Rates.hasFacilityFees(getRatesStream());
    }

    public List<BookablePeriod> getBookablePeriods() {
        return bookablePeriods;
    }

    public List<BookablePeriod> getBookablePeriods(KnownItemFamily knownItemFamily) {
        return Collections.filter(getBookablePeriods(), bp -> Entities.samePrimaryKey(bp.getStartScheduledItem().getItem().getFamily(), knownItemFamily.getPrimaryKey()));
    }

    public List<BookablePeriod> getBookablePeriods(KnownItemFamily knownItemFamily, Object wholePeriodI18nKey) {
        List<BookablePeriod> bookablePeriods = getBookablePeriods(knownItemFamily);
        bookablePeriods.add(createFamilyWholeBookablePeriod(knownItemFamily, wholePeriodI18nKey));
        return bookablePeriods;
    }

    public BookablePeriod createFamilyWholeBookablePeriod(KnownItemFamily knownItemFamily, Object i18nKey) {
        List<ScheduledItem> familyScheduledItems = filterScheduledItemsOfFamily(knownItemFamily);
        BookablePeriod wholeBookablePeriod = entityStore.createEntity(BookablePeriod.class);
        wholeBookablePeriod.setEvent(event);
        wholeBookablePeriod.setStartScheduledItem(Collections.first(familyScheduledItems)); // should be the first teaching date
        wholeBookablePeriod.setEndScheduledItem(Collections.last(familyScheduledItems)); // should be the last teaching date
        wholeBookablePeriod.setFieldValue("i18nKey", i18nKey); // Will be recognized by I18nFunction
        return wholeBookablePeriod;
    }

    public EntityList<ItemPolicy> getItemPolicies() {
        return itemPolicies;
    }

    // The following methods are meant to be used for serialization, not by the application code

    public String getRatesQueryBase() {
        return ratesQueryBase;
    }

    public QueryResult getRatesQueryResult() {
        return ratesQueryResult;
    }

    public String getScheduledItemsQueryBase() {
        return scheduledItemsQueryBase;
    }

    public QueryResult getScheduledItemsQueryResult() {
        return scheduledItemsQueryResult;
    }

    public String getBookablePeriodsQueryBase() {
        return bookablePeriodsQueryBase;
    }

    public QueryResult getBookablePeriodsQueryResult() {
        return bookablePeriodsQueryResult;
    }

    public String getItemPoliciesQueryBase() {
        return itemPoliciesQueryBase;
    }

    public QueryResult getItemPoliciesQueryResult() {
        return itemPoliciesQueryResult;
    }
}
