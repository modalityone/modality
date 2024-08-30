package one.modality.base.frontoffice.activities.account;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class EventBookingsView {

    private Event event;

    private final ImageView eventImageView = new ImageView();
    private final ScalePane eventImageScalePane = new ScalePane(eventImageView);

    private final VBox container = new VBox(10,
        eventImageScalePane
    );

    public EventBookingsView(Event event, List<Document> eventBookings) {
        setEvent(event);
        container.getChildren().addAll(
            Collections.map(eventBookings, b -> new BookingView(b).getView())
        );
    }

    public void setEvent(Event event) {
        this.event = event;
        String imageUrl = event.evaluate("image.url");
        boolean hasImage = imageUrl != null;
        eventImageScalePane.setVisible(hasImage);
        eventImageScalePane.setManaged(hasImage);
        if (hasImage) {
            eventImageView.setImage(new Image(imageUrl, true));
        }
    }

    public Event getEvent() {
        return event;
    }

    public Node getView() {
        return container;
    }

}
