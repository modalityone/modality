package one.modality.booking.backoffice.bookingeditor.multi;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Person;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.backoffice.bookingeditor.family.BookingEditorBase;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class MultiBookingEditor extends BookingEditorBase {

    private final List<BookingEditor> bookingEditors;

    public MultiBookingEditor(WorkingBooking workingBooking, List<BookingEditor> bookingEditors) {
        super(workingBooking);
        this.bookingEditors = bookingEditors;
        // Final subclasses should call this method
        initiateUiAndSyncFromWorkingBooking();
    }

    @Override
    protected void initiateUiAndSyncFromWorkingBooking() {
        // Already done in individual booking editors
    }

    @Override
    public void syncWorkingBookingFromUi() {
        bookingEditors.forEach(bookingEditor -> {
            if (bookingEditor instanceof BookingEditorBase beb) {
                beb.syncWorkingBookingFromUi();
            }
        });
    }

    @Override
    public Node buildUi() {
        VBox vBox = new VBox(20,
            bookingEditors.stream().map(BookingEditor::buildUi).toArray(Node[]::new)
        );
        if (workingBooking.isNewBooking()) {
            vBox.getChildren().add(0, embedInFrame(createPersonToBookButton(), "Person to book"));
        }
        return vBox;
    }

    private Node createPersonToBookButton() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityButtonSelector<Person> personSelector = new EntityButtonSelector<Person>( // language=JSON5
            "{class: 'Person', alias: 'p', columns: [{expression: '[firstName,lastName,`(` + email + `)`]'}], where: 'owner and !removed and frontendAccount.(!backoffice and !disabled)', orderBy: 'firstName,lastName'}",
            new ButtonFactoryMixin() {
            }, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        ) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        }
            // Inline function doesn't work TODO: fix it
            //.setSearchCondition("searchMatchesPerson(p)")
            .setSearchCondition("abcNames(p..fullName) like ?abcSearchLike or lower(p..email) like ?searchEmailLike")
            ;
        FXPersonToBook.personToBookProperty().bind(personSelector.selectedItemProperty());
        return personSelector.getButton();
    }

}
