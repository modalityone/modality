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
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;

import java.util.List;

public class AccountFriendsAndFamilyActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private void rebuildMembersList(ObservableList<PersonPM> persons, BorderPane membersContainer, VBox membersList, VBox memberInformation, BorderPane container) {
        membersList.getChildren().removeAll(membersList.getChildren());

        persons.forEach(personPM -> {
            BorderPane bpL = new BorderPane();
            BorderPane bpR = new BorderPane();
            Separator s = new Separator();

            s.setBorder(new Border(new BorderStroke(Color.web(StyleUtility.SEPARATOR_GRAY), BorderStrokeStyle.NONE, null, BorderStroke.THIN)));

            bpL.setLeft(GeneralUtility.createRadioCheckBoxBySelection(FXAccount.toBeDeletedPerson, personPM.NAME_FULL.get()));
            bpL.setCenter(bpR);

            bpR.setRight(new Button(">"));
            bpR.setCenter(
                    GeneralUtility.createSplitRow(TextUtility.getBindedText(personPM.NAME_FULL, TextUtility::getMainText), TextUtility.getSubText("Kinship"), 75, 0)
            );
            bpR.setBottom(s);

            bpR.setOnMouseClicked(e -> {
                rebuildMemberInformation(memberInformation);
                container.getChildren().remove(membersContainer);
                container.setCenter(memberInformation);
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

        memberInformation.getChildren().add(LayoutUtil.createVerticalScrollPane(
                (Region) GeneralUtility.createVList(0, 0, nameContainer, AccountUtility.displayInformation(this, this, FXAccount.viewedPersonPM))
        ));


    }

    @Override
    public Node buildUi() {
        BorderPane container = new BorderPane();
        BorderPane membersContainer = new BorderPane();
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

        membersContainer.setCenter(LayoutUtil.createVerticalScrollPane(membersList));
        membersContainer.setBottom(GeneralUtility.createHList(10, 0, addMember, deleteMember));

        addMember.setOnAction(e -> {
            FXAccount.viewedPersonPM.setNew();
            rebuildMemberInformation(memberInformation);
            container.getChildren().remove(membersContainer);
            container.setCenter(memberInformation);
        });

        friendsFamilyHome.setOnAction(e -> {
            container.getChildren().remove(memberInformation);
            container.setCenter(membersContainer);
        });

        container.setTop(friendsFamilyHome);
        container.setCenter(membersContainer);

        container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        return container;
    }

}
