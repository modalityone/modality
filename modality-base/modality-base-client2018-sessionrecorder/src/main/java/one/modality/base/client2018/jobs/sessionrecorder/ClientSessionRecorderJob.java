package one.modality.base.client2018.jobs.sessionrecorder;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.storage.LocalStorage;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.stack.com.bus.Bus;
import dev.webfx.stack.com.bus.BusHook;
import dev.webfx.stack.com.bus.BusService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.client.fx.FXRunId;
import dev.webfx.stack.session.state.client.fx.FXUserPrincipal;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.time.Instant;

/**
 * @author Bruno Salmon
 */
public final class ClientSessionRecorderJob implements ApplicationJob {

    private static ClientSessionRecorderJob INSTANCE;

    private final Bus bus;

    public ClientSessionRecorderJob() {
        this(BusService.bus());
    }

    private ClientSessionRecorderJob(Bus bus) {
        this.bus = bus;
        INSTANCE = this;
    }

    private final UpdateStore store = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
    private Entity sessionAgent, sessionApplication, sessionProcess, sessionConnection, sessionUser;
    private boolean tryRestoreSessionFromLocalStorage = true;

    @Override
    public void onStart() {
        Console.log("User Agent = " + getUserAgent());
        Console.log("application.name = " + getApplicationName());
        Console.log("application.version = " + getApplicationVersion());
        Console.log("application.build.tool = " + getApplicationBuildTool());
        Console.log("application.build.timestamp = " + getApplicationBuildTimestampString());
        Console.log("application.build.number = " + getApplicationBuildNumberString());
        bus.setHook(new BusHook() {
            @Override
            public void handleOpened() {
                onConnectionOpened();
            }

            @Override
            public boolean handlePreClose() {
                onConnectionClosed();
                return true;
            }

            @Override
            public void handlePostClose() {
                onConnectionClosed();
            }
        });
        if (bus.isOpen())
            onConnectionOpened();
        FXUserPrincipal.userPrincipalProperty().addListener((observable, oldValue, userPrincipal) -> {
            if (userPrincipal instanceof ModalityUserPrincipal && INSTANCE != null)
                INSTANCE.recordNewSessionUser(((ModalityUserPrincipal) userPrincipal).getUserPersonId());
        });
    }

    @Override
    public void onStop() {
        onShutdown();
    }

    private void clearSessionInfo() {
        store.cancelChanges();
        sessionAgent = null;
        sessionApplication = null;
        sessionProcess = null;
        sessionConnection = null;
        sessionUser = null;
    }

    private Entity getSessionAgent() {
        if (sessionAgent == null)
            loadOrInsertSessionAgent();
        return sessionAgent;
    }

    private void loadOrInsertSessionAgent() {
        String agentString = getUserAgent();
        if (agentString.length() > 1024)
            agentString = agentString.substring(0, 1024);
        if (tryRestoreSessionFromLocalStorage && agentString.equals(LocalStorage.getItem("sessionAgent.agentString")))
            sessionAgent = recreateSessionEntityFromLocalStorage("SessionAgent", "sessionAgent.id");
        else {
            sessionAgent = insertSessionEntity("SessionAgent", sessionAgent);
            sessionAgent.setFieldValue("agentString", agentString);
        }
    }

    private Entity getSessionApplication() {
        if (sessionApplication == null)
            loadOrInsertSessionApplication();
        return sessionApplication;
    }

    private void loadOrInsertSessionApplication() {
        if (tryRestoreSessionFromLocalStorage
                && getApplicationName().equals(LocalStorage.getItem("sessionApplication.name"))
                && getApplicationVersion().equals(LocalStorage.getItem("sessionApplication.version"))
                && getApplicationBuildTool().equals(LocalStorage.getItem("sessionApplication.buildTool"))
                && getApplicationBuildNumberString().equals(LocalStorage.getItem("sessionApplication.buildNumberString"))
                && getApplicationBuildTimestampString().equals(LocalStorage.getItem("sessionApplication.buildTimestampString")))
            loadSessionApplication();
        else
            insertNewSessionApplication();
    }

    private void loadSessionApplication() {
        sessionApplication = recreateSessionEntityFromLocalStorage("SessionApplication", "sessionApplication.id");
    }

    private void insertNewSessionApplication() {
        sessionApplication = insertSessionEntity("SessionApplication", sessionApplication);
        sessionApplication.setForeignField("agent", getSessionAgent());
        sessionApplication.setFieldValue("name", getApplicationName());
        sessionApplication.setFieldValue("version", getApplicationVersion());
        sessionApplication.setFieldValue("buildTool", getApplicationBuildTool());
        sessionApplication.setFieldValue("buildNumberString", getApplicationBuildNumberString());
        sessionApplication.setFieldValue("buildNumber", getApplicationBuildNumber());
        sessionApplication.setFieldValue("buildTimestampString", getApplicationBuildTimestampString());
        sessionApplication.setFieldValue("buildTimestamp", getApplicationBuildTimestamp());
    }

    private Entity getSessionProcess() {
        if (sessionProcess == null)
            insertSessionProcess();
        return sessionProcess;
    }

    private void insertSessionProcess() {
        sessionProcess = insertSessionEntity("SessionProcess", sessionProcess);
        sessionProcess.setForeignField("application", getSessionApplication());
    }

    private void insertSessionConnection() {
        sessionConnection = insertSessionEntity("SessionConnection", sessionConnection);
        sessionConnection.setForeignField("process", getSessionProcess());
    }

    private void onConnectionOpened() {
        listenServerPushCallsIfReady();
        insertSessionConnection();
        executeUpdate();
    }

    private void onConnectionClosed() {
        stopListeningServerPushCalls();
        if (sessionConnection != null) {
            touchSessionEntityEnd(store.updateEntity(sessionConnection));
            executeUpdate();
        }
    }

    private void onShutdown() {
        stopListeningServerPushCalls();
        if (sessionProcess != null) {
            touchSessionEntityEnd(store.updateEntity(sessionProcess));
            if (sessionConnection != null)
                touchSessionEntityEnd(store.updateEntity(sessionConnection));
            if (sessionUser != null)
                touchSessionEntityEnd(store.updateEntity(sessionUser));
            executeUpdate();
        }
    }

    private void recordNewSessionUser(Object userId) {
        createNewSessionUser(userId);
        executeUpdate();
    }

    private void createNewSessionUser(Object userId) {
        sessionUser = insertSessionEntity("SessionUser", sessionUser);
        sessionUser.setForeignField("process", getSessionProcess());
        sessionUser.setForeignField("user", userId);
    }

    private void executeUpdate() {
        boolean newSessionAgent = Entities.isNew(sessionAgent);
        boolean newSessionApplication =  Entities.isNew(sessionApplication);
        store.submitChanges()
                .onSuccess(resultBatch -> {
                    if (newSessionAgent)
                        storeEntityToLocalStorage(sessionAgent, "sessionAgent", "agentString");
                    if (newSessionApplication)
                        storeEntityToLocalStorage(sessionApplication, "sessionApplication", "name", "version", "buildTool", "buildNumberString", "buildTimestampString");
                    listenServerPushCallsIfReady();
                })
                .onFailure(cause -> {
                    if (tryRestoreSessionFromLocalStorage) {
                        Console.log("Client session couldn't be restored (inconsistent state between the local storage and the database) - Restarting a brand-new client session");
                        tryRestoreSessionFromLocalStorage = false;
                        clearSessionInfo();
                        onConnectionOpened();
                    } else
                        Console.log("Client Session Recorder error", cause);
                });
    }

    private void listenServerPushCallsIfReady() {
        if (FXRunId.getRunId() == null && Entities.isNotNew(sessionProcess))
            FXRunId.setRunId(sessionProcess.getPrimaryKey());
    }

    private void stopListeningServerPushCalls() {
        // Resetting the push client id property to null (will be reassigned when connected again). The purpose is to
        // make the reactive expression filters in push mode react when the connection is open again (this property
        // change should make them send the query push info sent to the server again).
        FXRunId.setRunId(null);
    }

    private Entity insertSessionEntity(Object domainClassId, Entity previousEntity) {
        return chainSessionEntities(previousEntity, touchSessionEntityStart(store.insertEntity(domainClassId)));
    }

    private Entity recreateSessionEntityFromLocalStorage(Object domainClassId, String idKey) {
        return store.createEntity(EntityId.create(store.getDomainClass(domainClassId), Integer.parseInt(LocalStorage.getItem(idKey))));
    }

    private void storeEntityToLocalStorage(Entity entity, String entityName, Object... fields) {
        storeEntityFieldToLocalStorage(entityName, "id", entity.getPrimaryKey());
        for (Object field : fields)
            storeEntityFieldToLocalStorage(entityName, field, entity.getFieldValue(field));
    }

    private void storeEntityFieldToLocalStorage(String entityName, Object field, Object value) {
        LocalStorage.setItem(entityName + "." + field, value.toString());
    }

    private static Entity touchSessionEntityStart(Entity entity) {
        return touchSessionEntity(entity, true);
    }

    private static Entity touchSessionEntityEnd(Entity entity) {
        return touchSessionEntity(entity, false);
    }

    private static Entity touchSessionEntity(Entity entity, boolean start) {
        entity.setFieldValue(start ? "start" : "end", Instant.now());
        return entity;
    }

    private Entity chainSessionEntities(Entity previousEntity, Entity nextEntity) {
        if (previousEntity != null && nextEntity != null) {
            previousEntity = store.updateEntity(previousEntity);
            previousEntity.setForeignField("next", nextEntity);
            nextEntity.setForeignField("previous", previousEntity);
            touchSessionEntityEnd(previousEntity);
        }
        return nextEntity;
    }

    private static String getUserAgent() {
        return UserAgent.getUserAgentName();
    }

    private static String getApplicationName() {
        return System.getProperty("application.name", "?");
    }

    private static String getApplicationVersion() {
        return System.getProperty("application.version", "?");
    }

    private static String getApplicationBuildTool() {
        return System.getProperty("application.build.tool", "?");
    }

    private static String getApplicationBuildNumberString() {
        return System.getProperty("application.build.number", "0");
    }

    private static Number getApplicationBuildNumber() {
        try {
            return Integer.valueOf(getApplicationBuildNumberString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getApplicationBuildTimestampString() {
        String timestamp = System.getProperty("application.build.timestamp");
        if (timestamp == null)
            timestamp = Instant.now().toString();
        return timestamp;
    }

    private static Instant getApplicationBuildTimestamp() {
        try {
            return Instant.parse(getApplicationBuildTimestampString());
        } catch (Exception e) {
            return null;
        }
    }
}
