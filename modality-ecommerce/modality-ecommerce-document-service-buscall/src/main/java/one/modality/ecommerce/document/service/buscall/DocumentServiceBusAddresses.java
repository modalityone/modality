package one.modality.ecommerce.document.service.buscall;

/**
 * @author Bruno Salmon
 */
public interface DocumentServiceBusAddresses {

    String LOAD_DOCUMENT_METHOD_ADDRESS = "modality/service/document/loadDocument";

    String LOAD_DOCUMENTS_METHOD_ADDRESS = "modality/service/document/loadDocuments";

    String SUBMIT_DOCUMENT_CHANGES_METHOD_ADDRESS = "modality/service/document/submitDocumentChanges";

    String SUBMIT_DOCUMENT_CHANGES_FINAL_CLIENT_PUSH_ADDRESS = "modality/service/document/push";

    String LEAVE_EVENT_QUEUE_ADDRESS = "modality/service/document/leaveEventQueue";
}
