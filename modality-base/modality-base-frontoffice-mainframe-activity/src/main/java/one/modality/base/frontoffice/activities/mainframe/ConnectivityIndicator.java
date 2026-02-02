package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.com.bus.call.PendingBusCall;
import dev.webfx.stack.session.state.client.fx.FXConnected;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

/**
 * @author Bruno Salmon
 */
final class ConnectivityIndicator {

    private final Region connectivityBar = new Region();
    private Timeline connectivityAnimation;
    private String currentConnectivityState; // Track the current state to avoid unnecessary animation resets

    public ConnectivityIndicator() {
        connectivityBar.setMouseTransparent(true);
        FXProperties.runNowAndOnPropertyChange(this::updateConnectivityIndicator, FXConnected.connectedProperty());
        PendingBusCall.addPendingCallsCountHandler(pendingCallsCount -> updateConnectivityIndicator());

    }

    public Region getConnectivityBar() {
        return connectivityBar;
    }

    private void updateConnectivityIndicator() {
        // Determine the new state
        String newState = !FXConnected.isConnected() ? "disconnected" : PendingBusCall.getPendingCallsCount() > 0 ? "pending" : "connected";
        // Only update if the state has actually changed
        if (newState.equals(currentConnectivityState)) {
            return; // State hasn't changed, keep current animation running
        }

        currentConnectivityState = newState;

        // Stop any existing animation
        if (connectivityAnimation != null) {
            connectivityAnimation.stop();
            connectivityAnimation = null;
        }

        if ("disconnected".equals(newState)) {
            // Disconnected state: pulsing red-orange gradient animation
            connectivityBar.setVisible(true);
            connectivityBar.setOpacity(1.0);
            connectivityAnimation = createPulsingAnimation(
                Color.rgb(220, 38, 38),    // Bright red
                Color.rgb(249, 115, 22)    // Orange
            );
            connectivityAnimation.play();
        } else if ("pending".equals(newState)) {
            // Pending call state: shimmer blue-cyan gradient animation
            connectivityBar.setVisible(true);
            connectivityBar.setOpacity(1.0);
            connectivityAnimation = createShimmerAnimation(
                Color.rgb(37, 99, 235),    // Darker blue
                Color.rgb(6, 182, 212)     // Brighter cyan
            );
            connectivityAnimation.play();
        } else {
            // Connected with no pending calls: fade out then hide indicator
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), connectivityBar);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> connectivityBar.setVisible(false));
            fadeOut.play();
        }
    }

    private Timeline createPulsingAnimation(Color color1, Color color2) {
        DoubleProperty animationProgress = new SimpleDoubleProperty(0);

        // Update background whenever the animation progress changes
        animationProgress.addListener((obs, oldVal, newVal) -> {
            double t = newVal.doubleValue();
            Color interpolated = color1.interpolate(color2, t);
            connectivityBar.setBackground(Background.fill(interpolated));
        });

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(animationProgress, 0, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(1),
                new KeyValue(animationProgress, 1, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(2),
                new KeyValue(animationProgress, 0, Interpolator.EASE_BOTH))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    private Timeline createShimmerAnimation(Color color1, Color color2) {
        DoubleProperty animationProgress = new SimpleDoubleProperty(0);

        // Shimmer effect: fast alternating gradient animation
        animationProgress.addListener((obs, oldVal, newVal) -> {
            double t = newVal.doubleValue();

            // Create a gradient that shifts between the two colors
            LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, color1.interpolate(color2, Math.abs(Math.sin(t * Math.PI)))),
                new Stop(1, color2.interpolate(color1, Math.abs(Math.sin(t * Math.PI))))
            );

            connectivityBar.setBackground(Background.fill(gradient));
        });

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(animationProgress, 0, Interpolator.LINEAR)),
            new KeyFrame(Duration.seconds(1.2),
                new KeyValue(animationProgress, 1, Interpolator.LINEAR))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

}
