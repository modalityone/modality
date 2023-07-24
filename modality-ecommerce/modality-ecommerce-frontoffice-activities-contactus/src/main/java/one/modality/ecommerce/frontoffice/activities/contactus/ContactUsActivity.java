package one.modality.ecommerce.frontoffice.activities.contactus;

import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.routing.uirouter.operations.RouteBackwardRequest;
import dev.webfx.stack.ui.action.Action;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.History;
import one.modality.base.shared.entities.Mail;
import one.modality.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import one.modality.ecommerce.frontoffice.activities.contactus.routing.ContactUsRouting;

/**
 * @author Bruno Salmon
 */
final class ContactUsActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final Action sendAction =
            newAction(
                    "Send",
                    "{url: 'images/svg/mono/send-circle.svg', width: 32, height: 32}",
                    this::send);

    private TextField subjectTextField;
    private TextArea bodyTextArea;

    private Object documentId;
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();

    @Override
    protected void updateModelFromContextParameters() {
        documentId = getParameter("documentId");
    }

    @Override
    public Node buildUi() {
        subjectTextField = newTextField("Subject"); // Will set the prompt
        bodyTextArea = newTextArea("YourMessage"); // Will set the prompt
        initValidation();

        VBox vBox = new VBox(20, subjectTextField, bodyTextArea, newLargeGreenButton(sendAction));

        // Applying the css background of the event if provided and if ui is ready
        UiScheduler.scheduleDeferred(this::applyEventCssBackgroundIfProvided);

        return new BorderPane(LayoutUtil.createVerticalScrollPaneWithPadding(vBox));
    }

    private void initValidation() {
        validationSupport.addRequiredInputs(subjectTextField, bodyTextArea);
    }

    private static final String DOCUMENT_LOAD_QUERY =
            "select <frontoffice_cart>,event.(name,cssClass) from Document where id=?";
    private Document document;

    @Override
    protected void startLogic() {
        // Loading the document in order to prepare
        EntityStore loadStore = EntityStore.create(getDataSourceModel());
        loadStore
                .<Document>executeQuery(DOCUMENT_LOAD_QUERY, documentId)
                .onFailure(Console::log)
                .onSuccess(
                        documents -> {
                            document = documents.get(0);
                            applyEventCssBackgroundIfProvided();
                        });
    }

    private void applyEventCssBackgroundIfProvided() {
        Event event = document == null ? null : document.getEvent();
        if (uiNode != null && event != null) {
            // TODO: capitalize this code with BookingProcessActivity
            String css = event.getStringFieldValue("cssClass");
            if (Strings.startsWith(css, "linear-gradient"))
                ((Region) uiNode).setBackground(BackgroundFactory.newLinearGradientBackground(css));
        }
    }

    private void send() {
        if (!validationSupport.isValid()) return;
        Document doc = document;
        UpdateStore updateStore = UpdateStore.createAbove(doc.getStore());
        Mail mail = updateStore.insertEntity(Mail.class);
        mail.setDocument(doc);
        mail.setFromName(doc.getFullName());
        mail.setFromEmail(doc.getEmail());
        mail.setSubject("[" + doc.getRef() + "] " + subjectTextField.getText());
        String frontofficeUrl =
                Strings.removeSuffix(
                        WindowLocation.getHref(), ContactUsRouting.getContactUsPath(documentId));
        String cartUrl = frontofficeUrl + CartRouting.getCartPath(document);
        // building mail content
        String content =
                bodyTextArea.getText()
                        + "\n-----\n"
                        + doc.getEvent().getName()
                        + " - #"
                        + doc.getRef()
                        + " - <a href=mailto:'"
                        + doc.getEmail()
                        + "'>"
                        + doc.getFullName()
                        + "</a>\n"
                        + "<a href='"
                        + cartUrl
                        + "'>"
                        + cartUrl
                        + "</a>";
        content = Strings.replaceAll(content, "\r", "<br/>");
        content = Strings.replaceAll(content, "\n", "<br/>");
        content = "<html>" + content + "</html>";
        // setting mail content
        mail.setContent(content);
        mail.setOut(
                false); // indicate that this mail is not an outgoing email (sent to booker) but an
                        // ingoing mail (sent to registration team)
        History history = updateStore.insertEntity(History.class); // new server history entry
        history.setDocument(doc);
        history.setMail(mail);
        history.setUsername("online");
        history.setComment("Sent '" + subjectTextField.getText() + "'");
        updateStore
                .submitChanges()
                .onFailure(Console::log)
                .onSuccess(
                        submitResultBatch -> {
                            // Going back (probably to booking cart)
                            new RouteBackwardRequest(getHistory()).execute();
                            // Clearing the fields for the next time visit
                            subjectTextField.clear();
                            bodyTextArea.clear();
                        });
    }
}
