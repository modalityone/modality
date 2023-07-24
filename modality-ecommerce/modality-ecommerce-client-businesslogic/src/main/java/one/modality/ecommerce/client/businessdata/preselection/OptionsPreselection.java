package one.modality.ecommerce.client.businessdata.preselection;

import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.entity.Entities;
import java.util.List;
import one.modality.base.client.aggregates.event.EventAggregate;
import one.modality.base.client.entities.util.Labels;
import one.modality.base.shared.entities.Label;
import one.modality.base.shared.entities.Option;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocumentLine;
import one.modality.hotel.shared.businessdata.time.DateTimeRange;

/**
 * @author Bruno Salmon
 */
public final class OptionsPreselection {

  private final EventAggregate eventAggregate;
  private final Label label;
  private final String i18nKey; // alternative i18n key if label is null
  private final List<OptionPreselection> optionPreselections;

  public OptionsPreselection(
      EventAggregate eventAggregate,
      Label label,
      String i18nKey,
      List<OptionPreselection> optionPreselections) {
    this.eventAggregate = eventAggregate;
    this.label = label;
    this.i18nKey = i18nKey;
    this.optionPreselections = optionPreselections;
  }

  public Label getLabel() {
    return label;
  }

  public List<OptionPreselection> getOptionPreselections() {
    return optionPreselections;
  }

  private WorkingDocument workingDocument;

  public WorkingDocument initializeNewWorkingDocument() {
    if (workingDocument == null) workingDocument = createNewWorkingDocument(null);
    else
      workingDocument.syncPersonDetails(
          eventAggregate.getPersonAggregate().getPreselectionProfilePerson());
    return workingDocument;
  }

  public WorkingDocument getWorkingDocument() {
    if (workingDocument == null) initializeNewWorkingDocument();
    return workingDocument;
  }

  public WorkingDocument createNewWorkingDocument(DateTimeRange workingDocumentDateTimeRange) {
    return new WorkingDocument(
        eventAggregate,
        Collections.map(
            optionPreselections,
            optionPreselection ->
                new WorkingDocumentLine(optionPreselection, workingDocumentDateTimeRange)));
  }

  public WorkingDocumentLine getAccommodationLine() {
    return getWorkingDocument().getAccommodationLine();
  }

  public boolean hasAccommodation() {
    return getAccommodationLine() != null;
  }

  public boolean hasAccommodationExcludingSharing() {
    WorkingDocumentLine accommodationLine = getAccommodationLine();
    return accommodationLine != null
        && Booleans.isFalse(accommodationLine.getItem().isShare_mate());
  }

  public Option getAccommodationOption() {
    WorkingDocumentLine accommodationLine = getAccommodationLine();
    return accommodationLine == null ? null : accommodationLine.getOption();
  }

  public boolean isForceSoldout() {
    Option accommodationOption = getAccommodationOption();
    return accommodationOption != null && Booleans.isTrue(accommodationOption.isForceSoldout());
  }

  public int computePrice() {
    return initializeNewWorkingDocument().applyBusinessRules().computePrice();
  }

  public String getDisplayName() {
    return Labels.instantTranslateLabel(label, i18nKey);
  }

  public String getDisplayName(Object language) {
    return Labels.instantTranslateLabel(label, language, i18nKey);
  }

  public Object getDisplayPrice() {
    return EventPriceFormatter.formatWithCurrency(computePrice(), eventAggregate.getEvent());
  }

  public Object getDisplayAvailability(EventAggregate eventAggregate) {
    QueryResult rs = eventAggregate.getEventAvailabilities();
    if (rs != null) {
      WorkingDocumentLine accommodationLine = getWorkingDocument().getAccommodationLine();
      if (accommodationLine != null) {
        Object sitePk = Entities.getPrimaryKey(accommodationLine.getSite());
        Object itemPk = Entities.getPrimaryKey(accommodationLine.getItem());
        for (int rowIndex = 0; rowIndex < rs.getRowCount(); rowIndex++) {
          if (rs.getValue(rowIndex, 1).equals(sitePk) && rs.getValue(rowIndex, 2).equals(itemPk))
            return rs.getValue(rowIndex, 4);
        }
      }
    }
    return null;
  }

  public void setEventActive() {
    ActiveOptionsPreselectionsByEventStore.setActiveOptionsPreselection(this, eventAggregate);
  }

  @Override
  public String toString() {
    return getDisplayName("en");
  }
}
