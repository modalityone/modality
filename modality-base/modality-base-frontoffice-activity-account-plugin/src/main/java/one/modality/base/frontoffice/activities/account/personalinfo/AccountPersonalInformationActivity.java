package one.modality.base.frontoffice.activities.account.personalinfo;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class AccountPersonalInformationActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, MaterialFactoryMixin {

    @Override
    public Node buildUi() {
        StackPane stackPane = new StackPane();
        Person person = FXUserPerson.getUserPerson();
        PersonalDetailsPanel details = new PersonalDetailsPanel(person.getStore().getDataSourceModel(), new ButtonSelectorParameters().setButtonFactory(this).setDropParent(stackPane).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea));
        UpdateStore updateStore = UpdateStore.createAbove(person.getStore());
        EntityHasPersonalDetails updatingPerson = updateStore.updateEntity(person);
        details.setEditable(true);
        details.syncUiFromModel(updatingPerson);

        BorderPane detailsContainer = details.getContainer();
        stackPane.getChildren().setAll(detailsContainer);
        stackPane.setMaxWidth(Region.USE_PREF_SIZE);

        return ControlUtil.createScalableVerticalScrollPane(stackPane);

        /*VBox vBox = new VBox(
                AccountUtility.createAvatar(),
                AccountUtility.displayInformation(this, this, FXAccount.ownerPM)
        );
        vBox.setBackground(Background.fill(Color.WHITE));
        return ControlUtil.createVerticalScrollPane(vBox);*/
    }
}
