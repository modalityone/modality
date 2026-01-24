package one.modality.ecommerce.policy.service;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Booleans;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class PolicyAggregate {

    // Fields intended for serialisation
    private final String eventQueryBase;
    private final QueryResult eventQueryResult;
    private final String scheduledItemsQueryBase;
    private final QueryResult scheduledItemsQueryResult;
    private final String scheduledBoundariesQueryBase;
    private final QueryResult scheduledBoundariesQueryResult;
    private final String eventPartsQueryBase;
    private final QueryResult eventPartsQueryResult;
    private final String eventSelectionsQueryBase;
    private final QueryResult eventSelectionsQueryResult;
    private final String eventPhasesQueryBase;
    private final QueryResult eventPhasesQueryResult;
    private final String eventPhaseCoveragesQueryBase;
    private final QueryResult eventPhaseCoveragesQueryResult;
    private final String itemFamilyPoliciesQueryBase;
    private final QueryResult itemFamilyPoliciesQueryResult;
    private final String itemPoliciesQueryBase;
    private final QueryResult itemPoliciesQueryResult;
    private final String ratesQueryBase;
    private final QueryResult ratesQueryResult;
    @Deprecated
    private final String bookablePeriodsQueryBase;
    @Deprecated
    private final QueryResult bookablePeriodsQueryResult;
    private final long creationTimeMillis = System.currentTimeMillis();

    // Fields intended for application code
    private Event event;
    private Double secondsToOpeningDateAtLoadingTime;
    private Double secondsToBookingProcessStartAtLoadingTime;
    private EntityStore entityStore;
    private EntityList<ScheduledItem> scheduledItems;
    private EntityList<ScheduledBoundary> scheduledBoundaries;
    private EntityList<EventPart> eventParts;
    private EntityList<EventSelection> eventSelections;
    private EntityList<EventPhase> eventPhases;
    private EntityList<EventPhaseCoverage> phaseCoverages;
    private EntityList<ItemFamilyPolicy> itemFamilyPolicies;
    private EntityList<ItemPolicy> itemPolicies;
    private EntityList<Rate> rates;
    @Deprecated
    private EntityList<BookablePeriod> bookablePeriods;

    public PolicyAggregate(
        String eventQueryBase, QueryResult eventQueryResult,
        String scheduledItemsQueryBase, QueryResult scheduledItemsQueryResult,
        String scheduledBoundariesQueryBase, QueryResult scheduledBoundariesQueryResult,
        String eventPartsQueryBase, QueryResult eventPartsQueryResult,
        String eventSelectionsQueryBase, QueryResult eventSelectionsQueryResult,
        String eventPhasesQueryBase, QueryResult eventPhasesQueryResult,
        String eventPhaseCoveragesQueryBase, QueryResult phaseCoveragesQueryResult,
        String itemFamilyPoliciesQueryBase, QueryResult itemFamilyPoliciesQueryResult,
        String itemPoliciesQueryBase, QueryResult itemPoliciesQueryResult,
        String ratesQueryBase, QueryResult ratesQueryResult,
        String bookablePeriodsQueryBase, QueryResult bookablePeriodsQueryResult
    ) {
        this.eventQueryBase = eventQueryBase;
        this.eventQueryResult = eventQueryResult;
        this.scheduledItemsQueryBase = scheduledItemsQueryBase;
        this.scheduledItemsQueryResult = scheduledItemsQueryResult;
        this.scheduledBoundariesQueryBase = scheduledBoundariesQueryBase;
        this.scheduledBoundariesQueryResult = scheduledBoundariesQueryResult;
        this.eventPartsQueryBase = eventPartsQueryBase;
        this.eventPartsQueryResult = eventPartsQueryResult;
        this.eventSelectionsQueryBase = eventSelectionsQueryBase;
        this.eventSelectionsQueryResult = eventSelectionsQueryResult;
        this.eventPhasesQueryBase = eventPhasesQueryBase;
        this.eventPhasesQueryResult = eventPhasesQueryResult;
        this.eventPhaseCoveragesQueryBase = eventPhaseCoveragesQueryBase;
        this.eventPhaseCoveragesQueryResult = phaseCoveragesQueryResult;
        this.ratesQueryBase = ratesQueryBase;
        this.ratesQueryResult = ratesQueryResult;
        this.itemFamilyPoliciesQueryBase = itemFamilyPoliciesQueryBase;
        this.itemFamilyPoliciesQueryResult = itemFamilyPoliciesQueryResult;
        this.itemPoliciesQueryBase = itemPoliciesQueryBase;
        this.itemPoliciesQueryResult = itemPoliciesQueryResult;
        this.bookablePeriodsQueryBase = bookablePeriodsQueryBase;
        this.bookablePeriodsQueryResult = bookablePeriodsQueryResult;
    }

    public void rebuildEntities(Event event) {
        entityStore = EntityStore.createAbove(event.getStore());
        DataSourceModel dataSourceModel = entityStore.getDataSourceModel();
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(eventQueryBase).getQueryMapping();
        // The event returned by PolicyAggregate is a different instance from the passes event and may contain some
        // additional fields such as termsUrlEn
        this.event = Collections.first(QueryResultToEntitiesMapper.mapQueryResultToEntities(eventQueryResult, queryMapping, entityStore, "events"));
        secondsToOpeningDateAtLoadingTime = this.event.getDoubleFieldValue(Event.secondsToOpeningDateAtLoadingTime);
        secondsToBookingProcessStartAtLoadingTime = this.event.getDoubleFieldValue(Event.secondsToBookingProcessStartAtLoadingTime);
        queryMapping = dataSourceModel.parseAndCompileSelect(scheduledItemsQueryBase).getQueryMapping();
        scheduledItems = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledItemsQueryResult, queryMapping, entityStore, "scheduledItems");
        queryMapping = dataSourceModel.parseAndCompileSelect(scheduledBoundariesQueryBase).getQueryMapping();
        scheduledBoundaries = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledBoundariesQueryResult, queryMapping, entityStore, "scheduledBoundaries");
        queryMapping = dataSourceModel.parseAndCompileSelect(eventPartsQueryBase).getQueryMapping();
        eventParts = QueryResultToEntitiesMapper.mapQueryResultToEntities(eventPartsQueryResult, queryMapping, entityStore, "eventParts");
        queryMapping = dataSourceModel.parseAndCompileSelect(eventSelectionsQueryBase).getQueryMapping();
        eventSelections = QueryResultToEntitiesMapper.mapQueryResultToEntities(eventSelectionsQueryResult, queryMapping, entityStore, "eventSelections");
        queryMapping = dataSourceModel.parseAndCompileSelect(eventPhasesQueryBase).getQueryMapping();
        eventPhases = QueryResultToEntitiesMapper.mapQueryResultToEntities(eventPhasesQueryResult, queryMapping, entityStore, "eventPhases");
        queryMapping = dataSourceModel.parseAndCompileSelect(eventPhaseCoveragesQueryBase).getQueryMapping();
        phaseCoverages = QueryResultToEntitiesMapper.mapQueryResultToEntities(eventPhaseCoveragesQueryResult, queryMapping, entityStore, "phaseCoverages");
        queryMapping = dataSourceModel.parseAndCompileSelect(itemFamilyPoliciesQueryBase).getQueryMapping();
        itemFamilyPolicies = QueryResultToEntitiesMapper.mapQueryResultToEntities(itemFamilyPoliciesQueryResult, queryMapping, entityStore, "itemFamilyPolicies");
        queryMapping = dataSourceModel.parseAndCompileSelect(itemPoliciesQueryBase).getQueryMapping();
        itemPolicies = QueryResultToEntitiesMapper.mapQueryResultToEntities(itemPoliciesQueryResult, queryMapping, entityStore, "itemPolicies");
        queryMapping = dataSourceModel.parseAndCompileSelect(ratesQueryBase).getQueryMapping();
        rates = QueryResultToEntitiesMapper.mapQueryResultToEntities(ratesQueryResult, queryMapping, entityStore, "rates");
        queryMapping = dataSourceModel.parseAndCompileSelect(bookablePeriodsQueryBase).getQueryMapping();
        bookablePeriods = QueryResultToEntitiesMapper.mapQueryResultToEntities(bookablePeriodsQueryResult, queryMapping, entityStore, "bookablePeriods");
        Console.log(scheduledItems.get(2).getGuestsAvailability());
    }

    public Future<Void> reloadAvailabilities() {
        return PolicyService.loadAvailabilities(new LoadPolicyArgument(event))
            .onSuccess(scheduledItemsQueryResult -> {
                DataSourceModel dataSourceModel = entityStore.getDataSourceModel();
                QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(scheduledItemsQueryBase).getQueryMapping();
                scheduledItems = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledItemsQueryResult, queryMapping, entityStore, "scheduledItems");
            })
            .mapEmpty();
    }

    public EntityStore getEntityStore() {
        return entityStore;
    }

    public Event getEvent() {
        return event;
    }

    public Double getSecondsToOpeningDate() {
        return getSecondsNow(secondsToOpeningDateAtLoadingTime);
    }

    public Double getSecondsToBookingProcessStart() {
        return getSecondsNow(secondsToBookingProcessStartAtLoadingTime);
    }

    private Double getSecondsNow(Double secondsAtLoadingTime) {
        if (secondsAtLoadingTime == null) return null;
        return secondsAtLoadingTime - (System.currentTimeMillis() - creationTimeMillis) / 1000.0;
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

    public List<ScheduledItem> filterAccommodationScheduledItems() {
        return filterScheduledItemsOfFamily(KnownItemFamily.ACCOMMODATION);
    }

    public List<ScheduledItem> filterMealsScheduledItems() {
        return filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
    }

    public List<ScheduledItem> filterCeremonyScheduledItems() {
        return filterScheduledItemsOfFamily(KnownItemFamily.CEREMONY);
    }

    public EntityList<ScheduledBoundary> getScheduledBoundaries() {
        return scheduledBoundaries;
    }

    public EntityList<EventPart> getEventParts() {
        return eventParts;
    }

    public EntityList<EventSelection> getEventSelections() {
        return eventSelections;
    }

    public EntityList<EventPhase> getEventPhases() {
        return eventPhases;
    }

    public EntityList<EventPhaseCoverage> getPhaseCoverages() {
        return phaseCoverages;
    }

    public EntityList<ItemFamilyPolicy> getItemFamilyPolicies() {
        return itemFamilyPolicies;
    }

    public EntityList<ItemPolicy> getItemPolicies() {
        return itemPolicies;
    }

    public EventPart getEarlyArrivalPart() {
        // Should be listed first
        EventPart firstPart = Collections.first(getEventParts());
        if (firstPart == null) return null;
        if (firstPart.getStartDate().isBefore(getEvent().getStartDate()))
            return firstPart;
        for (EventSelection eventSelection : getEventSelections()) {
            if (eventSelection.getParts().contains(firstPart))
                return null;
        }
        return firstPart;
    }

    public EventPart getLateDeparturePart() {
        // Should be listed last
        EventPart lastPart = Collections.last(getEventParts());
        if (lastPart == null) return null;
        if (lastPart.getEndDate().isAfter(getEvent().getEndDate()))
            return lastPart;
        for (EventSelection eventSelection : getEventSelections()) {
            if (eventSelection.getParts().contains(lastPart))
                return null;
        }
        return lastPart;
    }

    /**
     * Gets the ItemPolicy for a specific Item.
     *
     * @param item the Item to find the policy for
     * @return the ItemPolicy for the item, or null if none exists
     */
    public ItemPolicy getItemPolicy(Item item) {
        return getItemPolicies().stream()
            .filter(ip -> Entities.samePrimaryKey(ip.getItem(), item))
            .findFirst().orElse(null);
    }

    public List<ItemPolicy> getDietItemPolicies() {
        return getItemPolicies().stream()
            .filter(ip -> ip.getItem().getItemFamilyType() == KnownItemFamily.DIET)
            .collect(Collectors.toList());
    }

    public List<ItemPolicy> getTranslationItemPolicies() {
        return getItemPolicies().stream()
            .filter(ip -> ip.getItem().getItemFamilyType() == KnownItemFamily.TRANSLATION)
            .collect(Collectors.toList());
    }

    public ItemPolicy getSharingAccommodationItemPolicy() {
        return getItemPolicies().stream()
            .filter(ip -> Booleans.isTrue(ip.getItem().isShare_mate()) && ip.getItem().getItemFamilyType() == KnownItemFamily.ACCOMMODATION)
            .findFirst().orElse(null);
    }

    public ItemFamilyPolicy getItemFamilyPolicy(KnownItemFamily knownItemFamily) {
        return getItemFamilyPolicies().stream()
            .filter(ifp -> ifp.getItemFamilyType() == knownItemFamily)
            .findFirst().orElse(null);
    }

    public List<EventPhaseCoverage> getAudioRecordingPhaseCoverages() {
        ItemFamilyPolicy audioRecordingPolicy = getItemFamilyPolicy(KnownItemFamily.AUDIO_RECORDING);
        if (audioRecordingPolicy == null) return Collections.emptyList();
        return audioRecordingPolicy.getEventPhaseCoverages();
    }

    public Map<Item, List<ScheduledItem>> groupScheduledItemsByAudioRecordingItems() {
        return ScheduledItems.groupScheduledItemsByAudioRecordingItems(getScheduledItemsStream());
    }

    private Timeline findMealsTimeline(LocalTime startsAfter, LocalTime startsBefore) {
        return scheduledItems.stream()
            .map(ScheduledItem::getTimeline)
            .distinct()
            .filter(timeline -> {
                if (timeline == null) return false;
                Item item = timeline.getItem();
                if (item == null || !Entities.samePrimaryKey(item.getFamily(), KnownItemFamily.MEALS.getPrimaryKey()))
                    return false;
                LocalTime startTime = timeline.getStartTime();
                return startTime != null && (startsBefore == null || startTime.isBefore(startsBefore)) && (startsAfter == null || startTime.isAfter(startsAfter));
            }).findFirst()
            .orElse(null);
    }

    public Timeline getBreakfastTimeline() {
        return findMealsTimeline(null, LocalTime.of(10, 0));
    }

    public Timeline getLunchTimeline() {
        return findMealsTimeline(LocalTime.of(10, 0), LocalTime.of(15, 0));
    }

    public Timeline getDinnerTimeline() {
        return findMealsTimeline(LocalTime.of(15, 0), null);
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

    public Stream<Rate> filterRatesStreamOfSiteAndItem(Site site, Item item, boolean perDay) {
        return perDay ? filterDailyRatesStreamOfSiteAndItem(site, item) : filterFixedRatesStreamOfSiteAndItem(site, item);
    }

    public Stream<Rate> filterDailyRatesStreamOfSiteAndItem(Site site, Item item) {
        return Rates.filterRatesOfSiteAndItem(getDailyRatesStream(), site, item);
    }

    public Stream<Rate> filterFixedRatesStreamOfSiteAndItem(Site site, Item item) {
        return Rates.filterRatesOfSiteAndItem(getFixedRatesStream(), site, item);
    }

    public Stream<Rate> filterRatesStreamOfSiteAndItemOnTodayAndApplicableOverPeriod(Site site, Item item, boolean perDay, LocalDate startDate, LocalDate endDate) {
        return filterRatesStreamOfSiteAndItem(site, item, perDay)
            .filter(r -> Rates.isOnTodayAndApplicableOverPeriod(r, startDate, endDate));
    }

    public Rate getScheduledItemDailyRate(ScheduledItem scheduledItem) {
        return getSiteItemDailyRateOverPeriod(scheduledItem.getSite(), scheduledItem.getItem(), scheduledItem.getDate(), scheduledItem.getDate());
    }

    public Rate getSiteItemDailyRateOverPeriod(Site site, Item item, Period period) {
        return getSiteItemDailyRateOverPeriod(site, item, period.getStartDate(), period.getEndDate());
    }

    public Rate getSiteItemDailyRateOverPeriod(Site site, Item item, LocalDate startDate, LocalDate endDate) {
        return filterRatesStreamOfSiteAndItemOnTodayAndApplicableOverPeriod(site, item, true, startDate, endDate)
            .findFirst().orElse(null);
    }

    public boolean hasFacilityFees() {
        return Rates.hasFacilityFees(getRatesStream());
    }

    @Deprecated
    public List<BookablePeriod> getBookablePeriods() {
        return bookablePeriods;
    }

    @Deprecated
    public List<BookablePeriod> getBookablePeriods(KnownItemFamily knownItemFamily) {
        return Collections.filter(getBookablePeriods(), bp -> Entities.samePrimaryKey(bp.getStartScheduledItem().getItem().getFamily(), knownItemFamily.getPrimaryKey()));
    }

    @Deprecated
    public List<BookablePeriod> getBookablePeriods(KnownItemFamily knownItemFamily, Object wholePeriodI18nKey) {
        List<BookablePeriod> bookablePeriods = getBookablePeriods(knownItemFamily);
        bookablePeriods.add(createFamilyWholeBookablePeriod(knownItemFamily, wholePeriodI18nKey));
        return bookablePeriods;
    }

    @Deprecated
    public BookablePeriod createFamilyWholeBookablePeriod(KnownItemFamily knownItemFamily, Object i18nKey) {
        List<ScheduledItem> familyScheduledItems = filterScheduledItemsOfFamily(knownItemFamily);
        BookablePeriod wholeBookablePeriod = entityStore.createEntity(BookablePeriod.class);
        wholeBookablePeriod.setEvent(event);
        wholeBookablePeriod.setStartScheduledItem(Collections.first(familyScheduledItems)); // should be the first teaching date
        wholeBookablePeriod.setEndScheduledItem(Collections.last(familyScheduledItems)); // should be the last teaching date
        wholeBookablePeriod.setFieldValue("i18nKey", i18nKey); // Will be recognized by I18nFunction
        return wholeBookablePeriod;
    }

    // The following methods are meant to be used for serialization, not by the application code


    public String getEventQueryBase() {
        return eventQueryBase;
    }

    public QueryResult getEventQueryResult() {
        return eventQueryResult;
    }

    public String getScheduledItemsQueryBase() {
        return scheduledItemsQueryBase;
    }

    public QueryResult getScheduledItemsQueryResult() {
        return scheduledItemsQueryResult;
    }

    public String getScheduledBoundariesQueryBase() {
        return scheduledBoundariesQueryBase;
    }

    public QueryResult getScheduledBoundariesQueryResult() {
        return scheduledBoundariesQueryResult;
    }

    public String getEventPartsQueryBase() {
        return eventPartsQueryBase;
    }

    public QueryResult getEventPartsQueryResult() {
        return eventPartsQueryResult;
    }

    public String getEventSelectionsQueryBase() {
        return eventSelectionsQueryBase;
    }

    public QueryResult getEventSelectionsQueryResult() {
        return eventSelectionsQueryResult;
    }

    public String getEventPhasesQueryBase() {
        return eventPhasesQueryBase;
    }

    public QueryResult getEventPhasesQueryResult() {
        return eventPhasesQueryResult;
    }

    public String getEventPhaseCoveragesQueryBase() {
        return eventPhaseCoveragesQueryBase;
    }

    public QueryResult getEventPhaseCoveragesQueryResult() {
        return eventPhaseCoveragesQueryResult;
    }

    public String getItemFamilyPoliciesQueryBase() {
        return itemFamilyPoliciesQueryBase;
    }

    public QueryResult getItemFamilyPoliciesQueryResult() {
        return itemFamilyPoliciesQueryResult;
    }

    public String getItemPoliciesQueryBase() {
        return itemPoliciesQueryBase;
    }

    public QueryResult getItemPoliciesQueryResult() {
        return itemPoliciesQueryResult;
    }

    public String getRatesQueryBase() {
        return ratesQueryBase;
    }

    public QueryResult getRatesQueryResult() {
        return ratesQueryResult;
    }

    public String getBookablePeriodsQueryBase() {
        return bookablePeriodsQueryBase;
    }

    public QueryResult getBookablePeriodsQueryResult() {
        return bookablePeriodsQueryResult;
    }
}
