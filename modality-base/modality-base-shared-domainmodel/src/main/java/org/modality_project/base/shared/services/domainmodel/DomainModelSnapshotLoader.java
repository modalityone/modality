package org.modality_project.base.shared.services.domainmodel;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.stack.orm.domainmodel.DomainModel;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import dev.webfx.stack.orm.entity.EntityFactoryRegistry;
import dev.webfx.stack.orm.expression.terms.function.DomainClassType;
import dev.webfx.stack.orm.expression.terms.function.Function;
import dev.webfx.stack.orm.expression.terms.function.InlineFunction;
import dev.webfx.stack.orm.domainmodel.service.spi.DomainModelLoader;
import dev.webfx.stack.async.Batch;
import dev.webfx.stack.platform.json.Json;
import dev.webfx.stack.platform.json.JsonElement;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.platform.resource.Resource;
import dev.webfx.stack.com.serial.SerialCodecManager;
import org.modality_project.base.shared.domainmodel.formatters.DateFormatter;
import org.modality_project.base.shared.domainmodel.formatters.DateTimeFormatter;
import org.modality_project.base.shared.domainmodel.formatters.PriceFormatter;
import org.modality_project.base.shared.domainmodel.functions.AbcNames;
import org.modality_project.base.shared.domainmodel.functions.DateIntervalFormat;

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
            String jsonString = Resource.getText("org/modality_project/base/shared/domainmodel/DomainModelSnapshot.json");
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
