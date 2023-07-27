package one.modality.ecommerce.frontoffice.activities.cart.base;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client2018.aggregates.cart.CartAggregate;
import one.modality.base.client2018.aggregates.event.EventAggregate;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;

/**
 * @author Bruno Salmon
 */
public abstract class CartBasedActivity
        extends ViewDomainActivityBase
        implements ModalityButtonFactoryMixin {

    private final Property<Object> cartUuidProperty = new SimpleObjectProperty<>();

    protected Object getCartUuid() {
        return cartUuidProperty.getValue();
    }

    @Override
    protected void updateModelFromContextParameters() {
        cartUuidProperty.setValue(getParameter("cartUuid"));
    }

    @Override
    public void onStart() {
        super.onStart();
        startLogic();
    }

    protected void startLogic() {
        FXProperties.runOnPropertiesChange(this::onCartUuidChange, cartUuidProperty);
        FXProperties.runOnPropertiesChange(this::onDictionaryChange, I18n.dictionaryProperty());
    }

    protected void onCartUuidChange() {
        loadCart();
    }

    protected void onDictionaryChange() {
        if (cartAggregate().isLoaded())
            onCartLoaded();
    }

    private void loadCart() {
        cartAggregate().onCartDocuments().onSuccess(documents -> onCartLoaded());
    }

    protected void unloadCart() {
        cartAggregate().unload();
    }

    protected void reloadCart() {
        unloadCart();
        loadCart();
    }

    protected void onCartLoaded() {
        // Applying the css background of the event if provided and if ui is ready
        applyEventCssBackgroundIfProvided();
    }

    protected void applyEventCssBackgroundIfProvided() {
        Event event = getEvent();
        if (uiNode != null && event != null) {
            // TODO: capitalize this code with BookingProcessActivity
            String css = event.getStringFieldValue("cssClass");
            if (Strings.startsWith(css,"linear-gradient"))
                ((Region) uiNode).setBackground(BackgroundFactory.newLinearGradientBackground(css));
        }
    }

    protected CartAggregate cartAggregate() {
        return CartAggregate.getOrCreate(getCartUuid(), getDataSourceModel());
    }

    protected EventAggregate eventAggregate() {
        return cartAggregate().getEventAggregate();
    }

    protected Event getEvent() {
        EventAggregate eventAggregate = eventAggregate();
        return eventAggregate == null ? null : eventAggregate.getEvent();
    }

    protected Object getEventId() {
        return getEvent().getPrimaryKey();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Automatically loading the cart if not yet loading or loaded
        if (getCartUuid() != null && !cartAggregate().isLoaded() && !cartAggregate().isLoading())
            loadCart();
    }

    @Override
    protected void refreshDataOnActive() {
        reloadCart();
    }
}
