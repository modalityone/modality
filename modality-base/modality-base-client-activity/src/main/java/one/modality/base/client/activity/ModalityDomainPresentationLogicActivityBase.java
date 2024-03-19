package one.modality.base.client.activity;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentationlogic.impl.DomainPresentationLogicActivityImpl;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.platform.util.function.Factory;

/**
 * @author Bruno Salmon
 */
public abstract class ModalityDomainPresentationLogicActivityBase<PM>
    extends DomainPresentationLogicActivityImpl<PM>  {

    public ModalityDomainPresentationLogicActivityBase(Factory<PM> presentationModelFactory) {
        super(presentationModelFactory);
    }

    @Override
    protected void updateModelFromContextParameters() {
        super.updateModelFromContextParameters();
        Object activityStateId = getParameter("activityStateId");
        if (activityStateId != null)
            loadActivityState(activityStateId);
    }

    private void loadActivityState(Object activityStateId) {
        // Loading the parameters from the requested activity state
        QueryService.executeQuery(QueryArgument.builder()
                .setLanguage("DQL")
                .setStatement("select parameters from ActivityState where id=?")
                .setParameters(activityStateId)
                .setDataSourceId(getDataSourceId())
                .build())
                .onFailure(Console::log)
                .onSuccess(queryResult -> {
                    // Parsing the read parameters (json string expected) into a Json object
                    ReadOnlyAstObject stateParameters = AST.parseObject(queryResult.getValue(0, 1), "json");
                    // Merging these parameters into the context
                    AstObject contextParams = (AstObject) getParams(); // not beautiful...
                    AST.mergeInto(stateParameters, contextParams);
                    // Updating the presentation model from the new context parameters
                    updatePresentationModelFromContextParameters(getPresentationModel());
                });
    }

}
