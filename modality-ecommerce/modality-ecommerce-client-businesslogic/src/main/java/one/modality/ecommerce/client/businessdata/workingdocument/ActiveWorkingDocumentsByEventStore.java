package one.modality.ecommerce.client.businessdata.workingdocument;

import dev.webfx.stack.orm.entity.EntityId;
import java.util.HashMap;
import java.util.Map;
import one.modality.base.client.aggregates.event.EventAggregate;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import one.modality.ecommerce.client.businessdata.preselection.OptionsPreselection;

/**
 * @author Bruno Salmon
 */
public class ActiveWorkingDocumentsByEventStore {

  private static final Map<EntityId, WorkingDocument> activeWorkingDocumentsByEventMap =
      new HashMap<>();

  public static void setEventActiveWorkingDocument(WorkingDocument workingDocument) {
    setEventActiveWorkingDocument(workingDocument, workingDocument.getEventAggregate());
  }

  public static void setEventActiveWorkingDocument(
      WorkingDocument workingDocument, EventAggregate eventAggregate) {
    setEventActiveWorkingDocument(workingDocument, eventAggregate.getEvent());
  }

  public static void setEventActiveWorkingDocument(WorkingDocument workingDocument, Event event) {
    setEventActiveWorkingDocument(workingDocument, event.getId());
  }

  public static void setEventActiveWorkingDocument(
      WorkingDocument workingDocument, EntityId eventId) {
    activeWorkingDocumentsByEventMap.put(eventId, workingDocument);
  }

  public static WorkingDocument getEventActiveWorkingDocument(EventAggregate eventAggregate) {
    return getEventActiveWorkingDocument(eventAggregate.getEvent());
  }

  public static WorkingDocument getEventActiveWorkingDocument(Event event) {
    return getEventActiveWorkingDocument(event.getId());
  }

  public static WorkingDocument getEventActiveWorkingDocument(EntityId eventId) {
    WorkingDocument workingDocument = activeWorkingDocumentsByEventMap.get(eventId);
    if (workingDocument == null) {
      OptionsPreselection selectedOptionsPreselection =
          ActiveOptionsPreselectionsByEventStore.getActiveOptionsPreselection(
              EventAggregate.get(eventId));
      if (selectedOptionsPreselection != null)
        workingDocument = selectedOptionsPreselection.getWorkingDocument();
    }
    return workingDocument;
  }
}
