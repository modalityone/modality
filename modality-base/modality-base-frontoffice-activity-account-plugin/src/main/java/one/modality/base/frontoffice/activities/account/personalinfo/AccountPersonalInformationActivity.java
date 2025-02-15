package one.modality.base.frontoffice.activities.account.personalinfo;

import dev.webfx.extras.util.control.Controls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

final class AccountPersonalInformationActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, MaterialFactoryMixin {

    @Override
    public Node buildUi() {
        Person person = FXUserPerson.getUserPerson();
        PersonalDetailsPanel details = new PersonalDetailsPanel(person, new ButtonSelectorParameters().setButtonFactory(this).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea));
        details.setValidationEnabled(true);
        return Controls.createScalableVerticalScrollPane(details.getContainer(), true);
    }
}
