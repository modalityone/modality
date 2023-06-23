package one.modality.event.frontoffice.activities.account;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.expression.terms.In;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.TextUtility;

import java.util.List;

public class AccountFriendsAndFamilyActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private void rebuildMembersList(ObservableList<PersonPM> persons, VBox membersContainer, VBox membersList, VBox memberInformation, VBox container) {
        membersList.getChildren().removeAll(membersList.getChildren());

        persons.forEach(personPM -> {
            Button b = new Button();

            BorderPane bpL = new BorderPane();
            BorderPane bpR = new BorderPane();

            bpL.setLeft(GeneralUtility.createRadioCheckBoxBySelection(FXAccount.toBeDeletedPerson, personPM.NAME_FULL.get()));
            bpL.setCenter(bpR);

            bpR.setRight(new Button(">"));
            bpR.setCenter(
                    GeneralUtility.createSplitRow(TextUtility.getBindedText(personPM.NAME_FULL, TextUtility::getMainText), TextUtility.getSubText("Kinship"), 75, 0)
            );

            bpR.setOnMouseClicked(e -> {
                rebuildMemberInformation(memberInformation);
                container.getChildren().remove(membersContainer);
                container.getChildren().add(memberInformation);
                FXAccount.viewedPersonPM.set(personPM.PERSON);
                FXAccount.viewedPersonPM.setASSOCIATE_PM(personPM);
            });

            membersList.getChildren().add(bpL);
        });
    }

    private void rebuildMemberInformation(VBox memberInformation) {
        double NO_LIMITED_WIDTH = -1;

        memberInformation.getChildren().removeAll(memberInformation.getChildren());

        VBox nameContainer = new VBox();
        nameContainer.setPadding(new Insets(20));

        nameContainer.getChildren().add(GeneralUtility.createSplitRow(
                GeneralUtility.createField("First name", AccountUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_FIRST, NO_LIMITED_WIDTH)),
                GeneralUtility.createField("Last name", AccountUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_LAST, NO_LIMITED_WIDTH)),
                50, 10
        ));

        memberInformation.getChildren().addAll(
                nameContainer,
                AccountUtility.displayInformation(this, this, FXAccount.viewedPersonPM)
        );
    }

    @Override
    public Node buildUi() {
        VBox container = new VBox();
        VBox membersContainer = new VBox();
        VBox membersList = new VBox();
        VBox memberInformation = new VBox();
        Button friendsFamilyHome = new Button("Friends and Family");

        rebuildMemberInformation(memberInformation);
        rebuildMembersList(FXAccount.getMembersPM(), membersContainer, membersList, memberInformation, container);

        FXAccount.getMembersPM().addListener((ListChangeListener<PersonPM>) change -> {
            rebuildMembersList(FXAccount.getMembersPM(), membersContainer, membersList, memberInformation, container);
        });

        Button addMember = new Button("Add member");
        Button deleteMember = new Button("Delete selected member");

        membersContainer.getChildren().addAll(
                membersList,
                GeneralUtility.createHList(10, 0, addMember, deleteMember)
        );

        addMember.setOnAction(e -> {
            FXAccount.viewedPersonPM.setNew();
            rebuildMemberInformation(memberInformation);
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

        return LayoutUtil.createVerticalScrollPane(container);
    }

}
