package one.modality.event.frontoffice.activities.account;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.event.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyEditRequest;

public class AccountFriendsAndFamilyActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, OperationActionFactoryMixin {
    VBox container = new VBox();
    Button deleteMember;

    private void rebuildMembersList(ObservableList<PersonPM> persons, VBox membersList) {
        membersList.getChildren().clear();

        persons.forEach(personPM -> {
            BorderPane bpL = new BorderPane();
            BorderPane bpR = new BorderPane();
            Separator s = new Separator();

            s.setBorder(new Border(new BorderStroke(Color.web(StyleUtility.SEPARATOR_GRAY), BorderStrokeStyle.NONE, null, BorderStroke.THIN)));

            bpL.setLeft(GeneralUtility.createRadioCheckBoxBySelection(FXAccount.toBeDeletedPerson, personPM.NAME_FULL.get()));
            bpL.setCenter(bpR);

            bpR.setRight(new Button(">"));
            bpR.setCenter(
                    GeneralUtility.createSplitRow(TextUtility.getBindedText(personPM.NAME_FULL, name -> TextUtility.getMainText(name, StyleUtility.VICTOR_BATTLE_BLACK)), TextUtility.getSubText("Kinship"), 75, 0)
            );
            bpR.setBottom(s);

            bpR.setOnMouseClicked(e -> {
                FXAccount.viewedPersonPM.set(personPM.PERSON);
                FXAccount.viewedPersonPM.setASSOCIATE_PM(personPM);
                executeOperation(new RouteToAccountFriendsAndFamilyEditRequest(getHistory()));
            });

            membersList.getChildren().add(bpL);
        });
    }

    public void rebuild() {
        System.out.println(">>>> REBUILD Friends and Family <<<<<");
        container.getChildren().clear();

        VBox membersContainer = new VBox();
        VBox membersList = new VBox();

        rebuildMembersList(FXAccount.getMembersPM(), membersList);

        Button addMember = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "Add Member", 9);
        deleteMember = GeneralUtility.createButton(Color.web(StyleUtility.IMPORTANT_RED), 4, "Delete", 9);

        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().add(GeneralUtility.createHList(10, 0, deleteMember, addMember));

        membersContainer.getChildren().addAll(
                membersList, GeneralUtility.createSpace(100), buttonRow
        );

        addMember.setOnAction(e -> {
            FXAccount.viewedPersonPM.setNew();
            executeOperation(new RouteToAccountFriendsAndFamilyEditRequest(getHistory()));
        });

        container.getChildren().addAll(
                AccountUtility.createAccountHeader("Family or Friends", "Add your family or friends information here", this),
                membersContainer
        );
    }

    @Override
    public Node buildUi() {
        rebuild();

        I18n.dictionaryProperty().addListener(c -> rebuild());
        FXAccount.getMembersPM().addListener((ListChangeListener<PersonPM>) c -> rebuild());

        container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        return GeneralUtility.bindButtonWithPopup(deleteMember, LayoutUtil.createVerticalScrollPane(container), new VBox(), 200);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
