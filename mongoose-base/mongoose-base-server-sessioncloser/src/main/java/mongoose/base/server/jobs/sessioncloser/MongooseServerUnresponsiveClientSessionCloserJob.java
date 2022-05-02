package mongoose.base.server.jobs.sessioncloser;

import dev.webfx.framework.server.services.push.PushServerService;
import dev.webfx.framework.server.services.push.UnresponsivePushClientListener;
import dev.webfx.framework.shared.services.datasourcemodel.DataSourceModelService;
import dev.webfx.platform.shared.services.boot.spi.ApplicationJob;
import dev.webfx.platform.shared.services.log.Logger;
import dev.webfx.platform.shared.services.submit.SubmitArgument;
import dev.webfx.platform.shared.services.submit.SubmitService;

/**
 * @author Bruno Salmon
 */
public final class MongooseServerUnresponsiveClientSessionCloserJob implements ApplicationJob {

    private UnresponsivePushClientListener disconnectListener;

    @Override
    public void onStart() {
        PushServerService.addUnresponsivePushClientListener(disconnectListener = pushClientId ->
                SubmitService.executeSubmit(SubmitArgument.builder()
                        .setLanguage("DQL")
                        .setStatement("update SessionConnection set end=now() where process=?")
                        .setParameters(pushClientId)
                        .setDataSourceId(DataSourceModelService.getDefaultDataSourceId())
                        .build())
                        .onFailure(cause -> Logger.log("Error while closing session for pushClientId=" + pushClientId, cause))
                        .onSuccess(result -> Logger.log("Closed session for pushClientId=" + pushClientId)));
    }

    @Override
    public void onStop() {
        PushServerService.removeUnresponsivePushClientListener(disconnectListener);
    }

}
