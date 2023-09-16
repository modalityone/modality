package one.modality.base.frontoffice.application;

import dev.webfx.extras.flexbox.FlexBox;
import dev.webfx.platform.os.OperatingSystem;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import one.modality.base.client.application.ModalityClientMainFrameActivity;
import one.modality.base.client.application.RoutingActions;

public class ModalityFrontOfficeMainFrameActivity extends ModalityClientMainFrameActivity {

    @Override
    protected Region createMainFrameHeader() {
        return null;
    }

    @Override
    protected Region createMainFrameFooter() {
        return new FlexBox(
                RoutingActions.filterRoutingActions(this, this,
                                "RouteToHome", "RouteToBooking", "RouteToAlerts", "RouteToAccount")
                        .stream().map(this::createRouteButton)
                        .toArray(Node[]::new)
        );
    }

    private Button createRouteButton(Action routeAction) {
        Button button = new Button();
        button.setPrefHeight(80);
        button.setMaxWidth(1000);
        button.setMinWidth(0);
        button.setAlignment(Pos.CENTER);
        button.setBackground(Background.fill(Color.gray(0.92)));
        button.setCursor(Cursor.HAND);
        if (OperatingSystem.isMobile())
            button.setPadding(new Insets(0, 0, 10, 0));
        ActionBinder.bindButtonToAction(button, routeAction);
        return button;
    }
}
