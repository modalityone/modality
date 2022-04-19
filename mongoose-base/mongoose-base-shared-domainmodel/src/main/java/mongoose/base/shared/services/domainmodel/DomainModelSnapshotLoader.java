package mongoose.base.shared.services.domainmodel;

import mongoose.base.shared.domainmodel.formatters.DateFormatter;
import mongoose.base.shared.domainmodel.formatters.DateTimeFormatter;
import mongoose.base.shared.domainmodel.formatters.PriceFormatter;
import mongoose.base.shared.domainmodel.functions.AbcNames;
import mongoose.base.shared.domainmodel.functions.DateIntervalFormat;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.framework.shared.orm.domainmodel.DomainModel;
import dev.webfx.framework.shared.orm.entity.EntityFactoryRegistry;
import dev.webfx.framework.shared.orm.expression.terms.function.DomainClassType;
import dev.webfx.framework.shared.orm.expression.terms.function.Function;
import dev.webfx.framework.shared.orm.expression.terms.function.InlineFunction;
import dev.webfx.framework.shared.services.domainmodel.spi.DomainModelLoader;
import dev.webfx.framework.shared.orm.domainmodel.formatter.FormatterRegistry;
import dev.webfx.platform.shared.services.json.Json;
import dev.webfx.platform.shared.services.json.JsonElement;
import dev.webfx.platform.shared.services.query.QueryResult;
import dev.webfx.platform.shared.services.resource.ResourceService;
import dev.webfx.platform.shared.services.serial.SerialCodecManager;
import dev.webfx.platform.shared.async.Batch;
import dev.webfx.platform.shared.async.Future;

/**
 * @author Bruno Salmon
 */
final class DomainModelSnapshotLoader {

    private static DomainModel domainModel;

    static synchronized DomainModel getOrLoadDomainModel() {
        if (domainModel == null)
            domainModel = loadDomainModelFromSnapshot();
        return domainModel;
    }

    private static DomainModel loadDomainModelFromSnapshot() {
        try {
            // Registering formats
            FormatterRegistry.registerFormatter("price", PriceFormatter.INSTANCE);
            FormatterRegistry.registerFormatter("date", DateFormatter.SINGLETON);
            FormatterRegistry.registerFormatter("dateTime", DateTimeFormatter.SINGLETON);
            // Registering entity java classes
            EntityFactoryRegistry.registerProvidedEntityFactories();
            // Loading the model from the resource snapshot
            Future<String> text = ResourceService.getText("mongoose/base/shared/domainmodel/DomainModelSnapshot.json");
            String jsonString = text.result(); // LZString.decompressFromBase64(text.result());
            JsonElement json = Json.parseObject(jsonString);
            Batch<QueryResult> snapshotBatch = SerialCodecManager.decodeFromJson(json);
            DomainModel domainModel = new DomainModelLoader(1).generateDomainModel(snapshotBatch);
            // Registering aggregates TODO: Move this into the framework model (aggregate boolean field of domain class)
            domainModel.getClass("ResourceConfiguration").setAggregate(true);
            // Registering functions
            new AbcNames().register();
            new AbcNames("alphaSearch").register();
            new DateIntervalFormat().register();
            new Function("interpret_brackets", PrimType.STRING).register();
            new Function("compute_dates").register();
            new InlineFunction("searchMatchesDocument", "d", new Type[]{new DomainClassType("Document")}, "d..ref=?searchInteger or d..person_abcNames like ?abcSearchLike or d..person_email like ?searchEmailLike", domainModel.getClass("Document"), domainModel.getParserDomainModelReader()).register();
            new InlineFunction("searchMatchesPerson", "p", new Type[]{new DomainClassType("Person")}, "abcNames(p..firstName + ' ' + p..lastName) like ?abcSearchLike or p..email like ?searchEmailLike", "Person", domainModel.getParserDomainModelReader()).register();
            return domainModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
