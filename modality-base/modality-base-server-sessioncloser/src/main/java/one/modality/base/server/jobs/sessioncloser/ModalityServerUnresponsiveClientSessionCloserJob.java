package one.modality.base.server.jobs.sessioncloser;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.db.submit.SubmitService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.push.server.PushServerService;
import dev.webfx.stack.push.server.UnresponsivePushClientListener;

/**
 * @author Bruno Salmon
 */
public final class ModalityServerUnresponsiveClientSessionCloserJob implements ApplicationJob {

    private UnresponsivePushClientListener disconnectListener;

    @Override
    public void onStart() {
        PushServerService.addUnresponsivePushClientListener(
                disconnectListener =
                        clientRunId ->
                                SubmitService.executeSubmit(
                                                SubmitArgument.builder()
                                                        .setLanguage("DQL")
                                                        .setStatement(
                                                                "update SessionConnection set end=now() where process=?")
                                                        .setParameters(clientRunId)
                                                        .setDataSourceId(
                                                                DataSourceModelService
                                                                        .getDefaultDataSourceId())
                                                        .build())
                                        .onFailure(
                                                cause ->
                                                        Console.log(
                                                                "Error while closing session for clientRunId="
                                                                        + clientRunId,
                                                                cause))
                                        .onSuccess(
                                                result ->
                                                        Console.log(
                                                                "Closed session for clientRunId="
                                                                        + clientRunId)));
    }

    @Override
    public void onStop() {
        PushServerService.removeUnresponsivePushClientListener(disconnectListener);
    }
}
