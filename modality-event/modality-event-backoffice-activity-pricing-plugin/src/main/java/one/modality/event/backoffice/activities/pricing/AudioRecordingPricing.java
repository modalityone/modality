package one.modality.event.backoffice.activities.pricing;

import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
final class AudioRecordingPricing extends AbstractItemFamilyPricing {

    private static final ObservableList<Item> ORGANIZATION_RECORDING_ITEMS = FXCollections.observableArrayList();
    static {
        ReactiveEntitiesMapper.<Item>createReactiveChain()
            .setDataSourceModel(DataSourceModelService.getDefaultDataSourceModel())
            //.setStore(EntityStore.create(DataSourceModelService.getDefaultDataSourceModel()))
            .always("{class: 'Item', fields: 'code,name', orderBy: 'id'}")
            .always(DqlStatement.where("!deprecated and family.code = ?", KnownItemFamily.AUDIO_RECORDING.getCode()))
            .always(FXOrganization.organizationProperty(), lang -> DqlStatement.where("organization = ?", lang))
            .storeEntitiesInto(ORGANIZATION_RECORDING_ITEMS)
            .start();
    }

    public AudioRecordingPricing(PolicyAggregate eventPolicy) {
        super(KnownItemFamily.AUDIO_RECORDING, EventPricingI18nKeys.AudioRecordings, eventPolicy);
    }

}
