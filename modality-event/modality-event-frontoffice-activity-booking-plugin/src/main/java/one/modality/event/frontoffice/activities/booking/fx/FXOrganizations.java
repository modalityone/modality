package one.modality.event.frontoffice.activities.booking.fx;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public final class FXOrganizations {

    private final static ObservableList<Organization> organizations = FXCollections.observableArrayList();

    static {
        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .<Organization>executeQuery("select name,type,latitude,longitude,country from Organization where !closed and name!='ISC' order by country.name,name")
                .onFailure(Console::log)
                .onSuccess(list -> Platform.runLater(() -> organizations.setAll(list)));

    }

    public static ObservableList<Organization> organizations() {
        return organizations;
    }

}
