package one.modality.base.shared.services.domainmodel;

import dev.webfx.extras.type.ObjectType;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;
import dev.webfx.platform.async.Batch;
import dev.webfx.platform.resource.Resource;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.domainmodel.DomainModel;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import dev.webfx.stack.orm.domainmodel.service.spi.DomainModelLoader;
import dev.webfx.stack.orm.entity.EntityFactoryRegistry;
import dev.webfx.stack.orm.expression.terms.function.DomainClassType;
import dev.webfx.stack.orm.expression.terms.function.Function;
import dev.webfx.stack.orm.expression.terms.function.InlineFunction;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.domainmodel.functions.AbcNames;

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
            // Registering formats (in addition to the default ones in FormatterRegistry)
            FormatterRegistry.registerFormatter("price", PriceFormatter.INSTANCE);
            // Registering entity java classes
            EntityFactoryRegistry.registerProvidedEntityFactories();
            // Loading the model from the resource snapshot
            String jsonString = Resource.getText("one/modality/base/shared/domainmodel/DomainModelSnapshot.json");
            ReadOnlyAstObject json = Json.parseObject(jsonString);
            Batch<QueryResult> snapshotBatch = SerialCodecManager.decodeFromJson(json);
            DomainModel domainModel = new DomainModelLoader(1).generateDomainModel(snapshotBatch);
            // Registering aggregates TODO: Move this into the framework model (aggregate boolean field of domain class)
            domainModel.getClass("ResourceConfiguration").setAggregate(true);
            // Registering functions
            new AbcNames().register();
            new AbcNames("alphaSearch").register();
            new Function("interpret_brackets", PrimType.STRING).register();
            new Function("compute_dates").register();
            new InlineFunction("searchMatchesDocument", "d", new Type[]{new DomainClassType("Document")}, "d..ref=?searchInteger or d..person_abcNames like ?abcSearchLike or d..person_email like ?searchEmailLike", "Document", domainModel.getParserDomainModelReader()).register();
            new InlineFunction("searchMatchesPerson", "p", new Type[]{new DomainClassType("Person")}, "abcNames(p..fullName) like ?abcSearchLike or p..email like ?searchEmailLike", "Person", domainModel.getParserDomainModelReader()).register();
            new InlineFunction("accountCanAccessPersonMedias", "a,p", new Type[]{new ObjectType(Object.class), new DomainClassType("Person")}, "p..frontendAccount=a and p..accountPerson=null or p..accountPerson..frontendAccount=a", "Person", domainModel.getParserDomainModelReader()).register();
            return domainModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
