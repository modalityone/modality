package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.GeneralUtility;

public class AccountFriendsAndFamilyActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    @Override
    public Node buildUi() {
        VBox container = new VBox();
        VBox membersContainer = new VBox();
        VBox memberInformation = new VBox();
        Button friendsFamilyHome = new Button("Friends and Family");
        double NO_LIMITED_WIDTH = -1;

        memberInformation.getChildren().addAll(
                GeneralUtility.createSplitRow(
                        GeneralUtility.createField("First name", AccountUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_FIRST, NO_LIMITED_WIDTH)),
                        GeneralUtility.createField("Last name", AccountUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_LAST, NO_LIMITED_WIDTH)),
                        50, 10
                ),
                AccountUtility.displayInformation(this, this, FXAccount.viewedPersonPM)
        );

        FXAccount.getMembersPM().forEach(personPM -> {
            Button b = new Button();
            b.textProperty().bind(personPM.NAME_FULL);
            membersContainer.getChildren().add(b);
            b.setOnAction(e -> {
                container.getChildren().remove(membersContainer);
                container.getChildren().add(memberInformation);
                FXAccount.viewedPersonPM.set(personPM.PERSON);
                FXAccount.viewedPersonPM.setASSOCIATE_PM(personPM);
            });
        });

        Button addMember = new Button("Add member");
        Button deleteMember = new Button("Delete selected member");

        membersContainer.getChildren().addAll(
                GeneralUtility.createHList(10, 0, addMember, deleteMember)
        );

        addMember.setOnAction(e -> {
            FXAccount.viewedPersonPM = new PersonPM();
            container.getChildren().remove(membersContainer);
            container.getChildren().add(memberInformation);
        });

        friendsFamilyHome.setOnAction(e -> {
            container.getChildren().remove(memberInformation);
            container.getChildren().add(membersContainer);
        });

        container.getChildren().addAll(
                friendsFamilyHome,
                membersContainer
        );

        return container;
    }

}
