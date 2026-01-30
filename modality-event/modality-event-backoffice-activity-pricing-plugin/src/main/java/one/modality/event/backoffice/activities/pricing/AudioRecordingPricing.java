package one.modality.event.backoffice.activities.pricing;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.SiteItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.base.shared.knownitems.KnownItemI18nKeys;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.ecommerce.policy.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
final class AudioRecordingPricing extends AbstractItemFamilyPricing {

    private static final ObservableList<Item> ORGANIZATION_RECORDING_ITEMS = FXCollections.observableArrayList();
    static {
        ReactiveEntitiesMapper.<Item>createReactiveChain()
            .setDataSourceModel(DataSourceModelService.getDefaultDataSourceModel())
            //.setStore(EntityStore.create(DataSourceModelService.getDefaultDataSourceModel()))
            .always( // language=JSON5
                "{class: 'Item', fields: 'code,name', orderBy: 'ord,id'}")
            .always(DqlStatement.where("!deprecated and family.code = $1", KnownItemFamily.AUDIO_RECORDING.getCode()))
            .always(FXOrganization.organizationProperty(), lang -> DqlStatement.where("organization = $1", lang))
            .storeEntitiesInto(ORGANIZATION_RECORDING_ITEMS)
            .start();
    }

    public AudioRecordingPricing(PolicyAggregate eventPolicy) {
        super(KnownItemFamily.AUDIO_RECORDING, KnownItemI18nKeys.AudioRecordings, eventPolicy, false);
        Site venue = eventPolicy.getEvent().getVenue();
        ObservableLists.bindConverted(availableSiteItems, ORGANIZATION_RECORDING_ITEMS, item -> new SiteItem(venue, item));
    }

}
