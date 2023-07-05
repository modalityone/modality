package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;

public class AccountSettingsActivity extends ViewDomainActivityBase {
    VBox container;
    Button deleteButton;
    Button deleteContent;

    private static Node createRow(String label, Node field, Node operator) {
        return GeneralUtility.createSplitRow(
                GeneralUtility.createField(label, field),
                operator,
                80,
                10
        );
    }

    public void rebuild(VBox container) {
        container.getChildren().removeAll(container.getChildren());

        Node ukIcon = GeneralUtility.createSVGIcon("M5.34708 -0.833415V-1.66675H4.47208V-0.833415H5.34708ZM8.65283 -0.833415H9.52783V-1.66675H8.65283V-0.833415ZM5.34708 3.33325V4.16659H6.22208V3.33325H5.34708ZM-0.862793 3.33325V2.49992H-1.73779V3.33325H-0.862793ZM-0.862793 6.66659H-1.73779V7.49992H-0.862793V6.66659ZM5.34708 6.66659H6.22208V5.83325H5.34708V6.66659ZM5.34708 10.8333H4.47208V11.6666H5.34708V10.8333ZM8.65283 10.8333V11.6666H9.52783V10.8333H8.65283ZM8.65283 6.66659V5.83325H7.77783V6.66659H8.65283ZM14.8872 6.66659V7.49992H15.7622V6.66659H14.8872ZM14.8872 3.33325H15.7622V2.49992H14.8872V3.33325ZM8.65283 3.33325H7.77783V4.16659H8.65283V3.33325ZM5.34708 -8.13007e-05H8.65283V-1.66675H5.34708V-8.13007e-05ZM6.22208 3.33325V-0.833415H4.47208V3.33325H6.22208ZM-0.862793 4.16659H5.34708V2.49992H-0.862793V4.16659ZM0.012207 6.66659V3.33325H-1.73779V6.66659H0.012207ZM5.34708 5.83325H-0.862793V7.49992H5.34708V5.83325ZM6.22208 10.8333V6.66659H4.47208V10.8333H6.22208ZM8.65283 9.99992H5.34708V11.6666H8.65283V9.99992ZM7.77783 6.66659V10.8333H9.52783V6.66659H7.77783ZM14.8872 5.83325H8.65283V7.49992H14.8872V5.83325ZM14.0122 3.33325V6.66659H15.7622V3.33325H14.0122ZM8.65283 4.16659H14.8872V2.49992H8.65283V4.16659ZM7.77783 -0.833415V3.33325H9.52783V-0.833415H7.77783Z");

        deleteButton = new Button("Permanently delete account");
        deleteContent = new Button("I am a deleter");

        deleteContent.setMinHeight(200);
        deleteContent.setMaxWidth(1000);

        container.getChildren().addAll(
                createRow("Email", new Text("paulmeetboll@gmail.com"), new Button("Change")),
                createRow(
                        "Phone number",
                        GeneralUtility.createHList(10, 10,ukIcon, new Text("0044 745 4125 41")),
                        new Button("Change")
                ),
                createRow("Password", new Text("......."), new Button("Change")),
                createRow("Security",
                        GeneralUtility.createVList(0, 0,
                                new Text("2-step verification"),
                                GeneralUtility.createSplitRow(new Text("Request code on"), new Text("SMS"), 50, 0)),
                        new CheckBox()
                ),
                GeneralUtility.createVList(2, 10,
                        new Text("Notifications"),
                        GeneralUtility.createSplitRow(new Text("In-push notifications"), new CheckBox(), 80, 0),
                        GeneralUtility.createSplitRow(new Text("In-app notifications"), new CheckBox(), 80, 0),
                        GeneralUtility.createSplitRow(new Text("Newsletter emails"), new CheckBox(), 80, 0),
                        GeneralUtility.createSplitRow(new Text("Special events emails"), new CheckBox(), 80, 0)
                ),
                deleteButton
        );
    }

    @Override
    public Node buildUi() {
        container = new VBox();

        rebuild(container);
        I18n.dictionaryProperty().addListener(c -> rebuild(container));

        return GeneralUtility.bindButtonWithPopup(deleteButton, container, deleteContent, 200);
    }
}
