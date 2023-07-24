package one.modality.ecommerce.client.businesslogic.feesgroup;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import one.modality.base.client.aggregates.event.EventAggregate;
import one.modality.base.client.entities.util.Labels;
import one.modality.base.shared.entities.DateInfo;
import one.modality.base.shared.entities.Option;
import one.modality.base.shared.entities.Site;
import one.modality.ecommerce.client.businessdata.feesgroup.FeesGroup;
import one.modality.ecommerce.client.businessdata.feesgroup.FeesGroupBuilder;
import one.modality.ecommerce.client.businesslogic.option.OptionLogic;

/**
 * @author Bruno Salmon
 */
public final class FeesGroupLogic {

  public static FeesGroup[] createFeesGroups(EventAggregate eventAggregate) {
    List<FeesGroup> feesGroups = new ArrayList<>();
    EntityList<DateInfo> dateInfos = eventAggregate.getEventDateInfos();
    List<Option> defaultOptions = OptionLogic.selectDefaultOptions(eventAggregate);
    List<Option> accommodationOptions =
        eventAggregate.selectOptions(o -> o.isConcrete() && o.isAccommodation());
    if (!dateInfos.isEmpty())
      for (DateInfo dateInfo : dateInfos)
        populateFeesGroups(
            eventAggregate, dateInfo, defaultOptions, accommodationOptions, feesGroups);
    else if (eventAggregate.getEvent() != null) // May happen if event is empty (ie has no option)
    populateFeesGroups(eventAggregate, null, defaultOptions, accommodationOptions, feesGroups);
    return Collections.toArray(feesGroups, FeesGroup[]::new);
  }

  private static void populateFeesGroups(
      EventAggregate eventAggregate,
      DateInfo dateInfo,
      List<Option> defaultOptions,
      List<Option> accommodationOptions,
      List<FeesGroup> feesGroups) {
    Map<Site, List<Option>> accommodationOptionsBySite =
        new LinkedHashMap<>(); // accommodationOptions.stream().collect(Collectors.groupingBy(Option::getSite));
    Collections.forEach(
        accommodationOptions,
        o ->
            accommodationOptionsBySite.computeIfAbsent(o.getSite(), k -> new ArrayList<>()).add(o));
    boolean multiAccommodationSites = accommodationOptionsBySite.size() > 1;
    if (multiAccommodationSites)
      feesGroups.add(
          createFeesGroup(
              eventAggregate,
              null,
              "NoAccommodation",
              dateInfo,
              defaultOptions,
              java.util.Collections.emptyList(),
              true));
    boolean addNoAccommodationOption =
        !multiAccommodationSites && !eventAggregate.getEvent().getName().contains("Overnight");
    for (Map.Entry<Site, List<Option>> entry : accommodationOptionsBySite.entrySet())
      feesGroups.add(
          createFeesGroup(
              eventAggregate,
              entry.getKey(),
              null,
              dateInfo,
              defaultOptions,
              entry.getValue(),
              addNoAccommodationOption));
  }

  private static FeesGroup createFeesGroup(
      EventAggregate eventAggregate,
      Object label,
      String i18nKey,
      DateInfo dateInfo,
      List<Option> defaultOptions,
      List<Option> accommodationOptions,
      boolean addNoAccommodationOption) {
    return new FeesGroupBuilder(eventAggregate)
        .setLabel(Labels.bestLabelOrName(label))
        .setI18nKey(i18nKey)
        .setDateInfo(dateInfo)
        .setDefaultOptions(defaultOptions)
        .setAccommodationOptions(accommodationOptions)
        .setAddNoAccommodationOption(addNoAccommodationOption)
        .build();
  }
}
