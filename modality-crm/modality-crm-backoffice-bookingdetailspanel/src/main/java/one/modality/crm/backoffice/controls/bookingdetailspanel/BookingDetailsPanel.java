package one.modality.crm.backoffice.controls.bookingdetailspanel;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationAction;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.backoffice.controls.masterslave.UiBuilder;
import one.modality.base.backoffice.operations.entities.generic.CopyAllRequest;
import one.modality.base.backoffice.operations.entities.generic.CopySelectionRequest;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.client.presentationmodel.HasSelectedDocumentProperty;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Mail;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.crm.backoffice.operations.entities.mail.ComposeNewMailRequest;
import one.modality.crm.backoffice.operations.entities.mail.OpenMailRequest;
import one.modality.crm.client.controls.personaldetails.BookingPersonalDetailsPanel;
import one.modality.ecommerce.backoffice.operations.entities.document.cart.OpenBookingCartRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings.CancelOtherMultipleBookingsRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings.GetBackCancelledMultipleBookingsDepositRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings.MergeMultipleBookingsOptionsRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings.ToggleMarkMultipleBookingRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.ToggleCancelDocumentRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.ToggleMarkDocumentAsArrivedRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.ToggleMarkDocumentAsReadRequest;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.ToggleMarkDocumentAsWillPayRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.AddNewDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.DeleteDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.EditDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.documentline.ToggleCancelDocumentLineRequest;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.AddNewPaymentRequest;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.AddNewTransferRequest;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.DeletePaymentRequest;
import one.modality.ecommerce.backoffice.operations.entities.moneytransfer.EditPaymentRequest;

import java.util.function.Function;
import java.util.function.Supplier;

public final class BookingDetailsPanel implements
        OperationActionFactoryMixin,
        HasActiveProperty,
        UiBuilder {

    public static final String REQUIRED_FIELDS = "person_firstName,person_lastName,person_age,person_email,person_organization,person_phone,person_cityName,person_country,person_carer1Name,person_carer2Name,event.startDate"; // event.startDate is required for the personal details panel

    private final ObjectProperty<Document> selectedDocumentProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            detailsPanel.setEditable(false);
            detailsPanel.setEntity(get());
        }
    };
    private final BooleanProperty activeProperty = new SimpleBooleanProperty(true);
    // We don't need access to the data inside that visual result of all bookings, but the purpose of this property is to
    // detect the changes such as server push notifications after pressing the toggle buttons (ex: mark/unmark as arrived)
    // because we need at this point to refresh these buttons through i18n (ex: Mark as arrived => Unmark as arrived)
    private final ObjectProperty<VisualResult> bookingsVisualResultProperty = new SimpleObjectProperty<>();

    private final ButtonFactoryMixin mixin;
    private final DataSourceModel dataSourceModel;
    private final BookingPersonalDetailsPanel detailsPanel;

    public BookingDetailsPanel(ButtonFactoryMixin mixin, DataSourceModel dataSourceModel) {
        this.mixin = mixin;
        this.dataSourceModel = dataSourceModel;
        detailsPanel = new BookingPersonalDetailsPanel(dataSourceModel, new ButtonSelectorParameters().setButtonFactory(mixin).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea));
    }

    @Override
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    public ObjectProperty<Document> selectedDocumentProperty() {
        return selectedDocumentProperty;
    }

    public Document getSelectedDocument() {
        return selectedDocumentProperty.get();
    }

    public void setSelectedDocument(Document document) {
        selectedDocumentProperty.set(document);
    }

    public ObjectProperty<VisualResult> bookingsVisualResultProperty() {
        return bookingsVisualResultProperty;
    }

    @Override
    public Node buildUi() {
        BorderPane container = new BorderPane();
        container.setTop(new FlexPane(10, 2,
                ActionBinder.bindButtonToAction(createFlexButton(), newSelectedDocumentOperationAction(ToggleMarkDocumentAsReadRequest::new)),
                ActionBinder.bindButtonToAction(createFlexButton(), newSelectedDocumentOperationAction(ToggleMarkDocumentAsWillPayRequest::new)),
                ActionBinder.bindButtonToAction(createFlexButton(), newSelectedDocumentOperationAction(ToggleCancelDocumentRequest::new)),
                ActionBinder.bindButtonToAction(createFlexButton(), newSelectedDocumentOperationAction(ToggleMarkDocumentAsArrivedRequest::new))
        ));
        container.setCenter(new TabPane(
                createTab("PersonalDetails", buildPersonalDetailsView()),
                createFilterTab("Options", "{class: 'DocumentLine', columns: `site,item,dates,lockAllocation,resourceConfiguration,comment,price_isCustom,price_net,price_nonRefundable,price_minDeposit,price_deposit`, where: 'document=${selectedDocument}', orderBy: 'item.family.ord,site..ord,item.ord'}"),
                createFilterTab("Payments", "{class: 'MoneyTransfer', columns: `date,method,transactionRef,comment,amount,verified`, where: 'document=${selectedDocument}', orderBy: 'date,id'}"),
                createTab("Comments", buildCommentView()),
                createFilterTab("Cart", "{class: 'Document', columns:`ref,multipleBookingIcon,langIcon,genderIcon,person_firstName,person_lastName,person_age,noteIcon,price_net,price_deposit,price_balance`, where: 'cart=(select cart from Document where id=${selectedDocument})', orderBy: 'ref'}"),
                createFilterTab("MultipleBookings", "{class: 'Document', columns:`ref,multipleBookingIcon,langIcon,genderIcon,person_firstName,person_lastName,person_age,noteIcon,price_deposit,plainOptions`, where: 'multipleBooking=(select multipleBooking from Document where id=${selectedDocument})', orderBy: 'ref'}"),
                createFilterTab("Family", "{class: 'Document', columns:`ref,multipleBookingIcon,langIcon,genderIcon,person_firstName,person_lastName,person_age,noteIcon,price_deposit,plainOptions`, where: 'person_carer1Document=${selectedDocument} or person_carer2Document=${selectedDocument} or id=(select person_carer1Document from Document where id=${selectedDocument}) or id=(select person_carer2Document from Document where id=${selectedDocument})', orderBy: 'ref'}"),
                createFilterTab("Mails", "{class: 'Mail', columns: 'date,subject,transmitted,error', where: 'document=${selectedDocument}', orderBy: 'date desc'}"),
                createFilterTab("History", "{class: 'History', columns: 'date,userDisplay,comment,request', where: 'document=${selectedDocument}', orderBy: 'date desc'}")
        ));
        return container;
    }

    private static Button createFlexButton() {
        Button button = new Button();
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setCursor(Cursor.HAND);
        return button;
    }

    private static Tab createTab(String i18nKey, Node node) {
        Tab tab = I18nControls.bindI18nProperties(new Tab(), i18nKey);
        tab.setContent(node);
        tab.setClosable(false);
        return tab;
    }

    private Tab createFilterTab(String i18nKey, String dqlStatementString) {
        VisualGrid table = new VisualGrid();
        Tab tab = createTab(i18nKey, table);
        // The following is required only for gwt version for any reason (otherwise the table height is not resized when growing)
        FXProperties.runOnPropertiesChange(() -> {
            TabPane tabPane = tab.getTabPane();
            if (tabPane != null)
                tabPane.requestLayout();
        }, table.visualResultProperty());
        // Setting up the reactive visual mapper
        String classOnly = dqlStatementString.substring(0, dqlStatementString.indexOf(',')) + "}";
        ObjectProperty<Entity> selectedEntityProperty = new SimpleObjectProperty<>();
        tab.getProperties().put("selectedEntityProperty", selectedEntityProperty); // used by getTabSelectedEntityProperty()
        ReactiveVisualMapper<Entity> visualMapper = ReactiveVisualMapper.createPushReactiveChain()
                .always(classOnly)
                .ifNotNullOtherwiseEmptyString(selectedDocumentProperty, document -> Strings.replaceAll(dqlStatementString, "${selectedDocument}", document.getPrimaryKey()))
                .bindActivePropertyTo(tab.selectedProperty())
                .setDataSourceModel(dataSourceModel)
                .applyDomainModelRowStyle()
                .visualizeResultInto(table)
                .setSelectedEntityHandler(selectedEntityProperty::set)
                .start();

        Supplier<ActionGroup> contextMenuActionGroupFactory = null;
        switch (i18nKey) {
            case "Options":
                contextMenuActionGroupFactory = () -> newActionGroup(
                        newSelectedDocumentOperationAction(AddNewDocumentLineRequest::new),
                        newSeparatorActionGroup(
                                newTabSelectedDocumentLineOperationAction(EditDocumentLineRequest::new, tab),
                                newTabSelectedDocumentLineOperationAction(ToggleCancelDocumentLineRequest::new, tab),
                                newTabSelectedDocumentLineOperationAction(DeleteDocumentLineRequest::new, tab)
                        ),
                        newTabCopyActionGroup(true, visualMapper)
                );
                break;
            case "Payments":
                contextMenuActionGroupFactory = () -> newActionGroup(
                        newSelectedDocumentOperationAction(AddNewPaymentRequest::new),
                        newSelectedDocumentOperationAction(AddNewTransferRequest::new),
                        newSeparatorActionGroup(
                                newTabSelectedMoneyTransferOperationAction(EditPaymentRequest::new, tab),
                                newTabSelectedMoneyTransferOperationAction(DeletePaymentRequest::new, tab)
                        ),
                        newTabCopyActionGroup(true, visualMapper)
                );
                break;
            case "MultipleBookings":
                contextMenuActionGroupFactory = () -> newActionGroup(
                        newTabSelectedDocumentOperationAction(MergeMultipleBookingsOptionsRequest::new, tab),
                        newTabSelectedDocumentOperationAction(CancelOtherMultipleBookingsRequest::new, tab),
                        newTabSelectedDocumentOperationAction(GetBackCancelledMultipleBookingsDepositRequest::new, tab),
                        newTabSelectedDocumentOperationAction(ToggleMarkMultipleBookingRequest::new, tab)
                );
                break;
            case "Cart":
                contextMenuActionGroupFactory = () -> newActionGroup(
                        newTabSelectedDocumentOperationAction(OpenBookingCartRequest::new, tab)
                );
                break;
            case "Mails":
                contextMenuActionGroupFactory = () -> newActionGroup(
                        newTabSelectedMailOperationAction(OpenMailRequest::new, tab),
                        newSelectedDocumentOperationAction(ComposeNewMailRequest::new),
                        newTabCopyActionGroup(true, visualMapper)
                );
                break;
            case "History":
                contextMenuActionGroupFactory = () -> newTabCopyActionGroup(false, visualMapper);
                break;
        }
        if (contextMenuActionGroupFactory != null)
            mixin.setUpContextMenu(table, contextMenuActionGroupFactory);
        return tab;
    }

    private Node buildPersonalDetailsView() {
        BorderPane container = detailsPanel.getContainer();
        if (false)
            return container;
        ScalePane scalePane = new ScalePane(container);
        scalePane.setCanShrink(false);
        scalePane.setFillWidth(false);
        scalePane.setFillHeight(false);
        scalePane.setScaleRegion(true);
        scalePane.setVAlignment(VPos.TOP);
        scalePane.setStretchWidth(true);
        detailsPanel.enableBigViewButton(() -> scalePane.setContent(container));

        /*ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters().setButtonFactory(mixin).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        mixin.setUpContextMenu(scalePane, () -> newActionGroup(
                newOperationAction(() -> new EditDocumentPersonalDetailsRequest(getSelectedDocument(), buttonSelectorParameters)
                )));
        scalePane.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                executeOperation(new EditDocumentPersonalDetailsRequest(getSelectedDocument(), buttonSelectorParameters));
        });*/

        return scalePane;
    }

    private Node buildCommentView() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        //gridPane.setVgap(10);
        gridPane.setPadding(new Insets(5));

        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100);
        gridPane.getRowConstraints().add(rc);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(33.3333);
        ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
        columnConstraints.add(cc);
        columnConstraints.add(cc);
        columnConstraints.add(cc);

        gridPane.getChildren().setAll(createComment("Person request", "request"), createComment("Registration comment", "comment"), createComment("Special needs", "specialNeeds"));

        return gridPane;
    }

    private int columnIndex;

    private Node createComment(String title, String commentField) {
        TextArea textArea = new TextArea();
        textArea.textProperty().bind(FXProperties.compute(selectedDocumentProperty, document -> document == null ? null : document.getStringFieldValue(commentField)));
        textArea.setEditable(false);
        TitledPane titledPane = new TitledPane(title, textArea);
        titledPane.setCollapsible(false);
        titledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setColumnIndex(titledPane, columnIndex++);
        return titledPane;
    }

    /*==================================================================================================================
    ========================================= OperationAction factory methods ==========================================
    ==================================================================================================================*/

    private OperationAction newSelectedDocumentOperationAction(Function<Document, ?> operationRequestFactory) {
        return newOperationAction(() -> operationRequestFactory.apply(getSelectedDocument()), /* to update the i18n text when the selection change -> */ selectedDocumentProperty, bookingsVisualResultProperty);
    }

    private OperationAction newTabSelectedDocumentOperationAction(Function<Document, ?> operationRequestFactory, Tab tab) {
        ObjectProperty<Document> selectedEntityProperty = getTabSelectedEntityProperty(tab);
        return newOperationAction(() -> operationRequestFactory.apply(selectedEntityProperty.get()), /* to update the i18n text when the selection change -> */ selectedEntityProperty);
    }

    private OperationAction newTabSelectedDocumentLineOperationAction(Function<DocumentLine, ?> operationRequestFactory, Tab tab) {
        ObjectProperty<DocumentLine> selectedEntityProperty = getTabSelectedEntityProperty(tab);
        return newOperationAction(() -> operationRequestFactory.apply(selectedEntityProperty.get()), /* to update the i18n text when the selection change -> */ selectedEntityProperty);
    }

    private OperationAction newTabSelectedMoneyTransferOperationAction(Function<MoneyTransfer, ?> operationRequestFactory, Tab tab) {
        ObjectProperty<MoneyTransfer> selectedEntityProperty = getTabSelectedEntityProperty(tab);
        return newOperationAction(() -> operationRequestFactory.apply(selectedEntityProperty.get()), /* to update the i18n text when the selection change -> */ selectedEntityProperty);
    }

    private OperationAction newTabSelectedMailOperationAction(Function<Mail, ?> operationRequestFactory, Tab tab) {
        ObjectProperty<Mail> selectedEntityProperty = getTabSelectedEntityProperty(tab);
        return newOperationAction(() -> operationRequestFactory.apply(selectedEntityProperty.get()), /* to update the i18n text when the selection change -> */ selectedEntityProperty);
    }

    private <E extends Entity> ObjectProperty<E> getTabSelectedEntityProperty(Tab tab) {
        return (ObjectProperty<E>) tab.getProperties().get("selectedEntityProperty");
    }

    private ActionGroup newTabCopyActionGroup(boolean hasSeparators, ReactiveVisualMapper<Entity> visualMapper) {
        return newActionGroup(null, hasSeparators,
                newOperationAction(() -> new CopySelectionRequest(visualMapper.getSelectedEntities(), visualMapper.getEntityColumns())),
                newOperationAction(() -> new CopyAllRequest(visualMapper.getCurrentEntities(), visualMapper.getEntityColumns()))
        );
    }


    /*==================================================================================================================
    ============================================== Static factory methods ==============================================
    ==================================================================================================================*/

    public static <M extends ButtonFactoryMixin & HasDataSourceModel> BookingDetailsPanel createAndBind(HasSelectedDocumentProperty pm, M mixin) {
        return createAndBind(pm, mixin, mixin.getDataSourceModel());
    }

    public static BookingDetailsPanel createAndBind(HasSelectedDocumentProperty pm, ButtonFactoryMixin mixin, DataSourceModel dataSourceModel) {
        BookingDetailsPanel bookingDetailsPanel = new BookingDetailsPanel(mixin, dataSourceModel);
        bookingDetailsPanel.selectedDocumentProperty().bind(pm.selectedDocumentProperty());
        if (mixin instanceof HasActiveProperty)
            bookingDetailsPanel.activeProperty().bind(((HasActiveProperty) mixin).activeProperty());
        if (pm instanceof HasMasterVisualResultProperty)
            bookingDetailsPanel.bookingsVisualResultProperty().bind(((HasMasterVisualResultProperty) pm).masterVisualResultProperty());
        return bookingDetailsPanel;
    }


    public static BookingDetailsPanel createAndBindIfApplicable(Object pm, Object mixin) {
        if (pm instanceof HasSelectedDocumentProperty && mixin instanceof ButtonFactoryMixin && mixin instanceof HasDataSourceModel)
            return createAndBind((HasSelectedDocumentProperty) pm, (ButtonFactoryMixin & HasDataSourceModel) mixin);
        return null;
    }
}

