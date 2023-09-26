package one.modality.base.backoffice.controls.masterslave.group;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.base.backoffice.controls.masterslave.MasterSlaveView;
import one.modality.base.backoffice.controls.masterslave.MasterTableView;
import one.modality.base.backoffice.controls.masterslave.SlaveTableView;
import one.modality.base.backoffice.controls.masterslave.UiBuilder;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasSelectedMasterProperty;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasSlaveVisibilityCondition;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasSlaveVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.ui.controls.ControlFactoryMixin;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.kit.util.properties.FXProperties;

import java.util.function.Function;

public class GroupMasterSlaveView extends MasterSlaveView {

    public GroupMasterSlaveView() {
        this((Node) null, null, null);
    }

    public GroupMasterSlaveView(UiBuilder groupView, UiBuilder masterView, UiBuilder slaveView) {
        this(groupView.buildUi(), masterView.buildUi(), slaveView.buildUi());
    }

    public GroupMasterSlaveView(Node groupView, Node masterView, Node slaveView) {
        super(masterView, slaveView);
        setGroupView(groupView);
    }

    private final ObjectProperty<Node> groupViewProperty = new SimpleObjectProperty<Node/*GWT*/>() {
        @Override
        protected void invalidated() {
            updateView();
        }
    };

    public ObjectProperty<Node> groupViewProperty() {
        return groupViewProperty;
    }

    public void setGroupView(Node groupView) {
        groupViewProperty().set(groupView);
    }

    public Node getGroupView() {
        return groupViewProperty().get();
    }

    @Override
    protected void updateView() {
        updateSplitPane(isGroupVisible() ? getGroupView() : null, isMasterVisible() ? getMasterView() : null, isSlaveVisible() ? getSlaveView() : null);
    }

    private final BooleanProperty groupVisibleProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            updateView();
        }
    };

    public BooleanProperty groupVisibleProperty() {
        return groupVisibleProperty;
    }

    public boolean isGroupVisible() {
        // Note: groupVisibleProperty is null on first call because not yet initialized (constructor -> set master/slave view -> updateView() -> isGroupVisible()))
        return groupVisibleProperty != null && groupVisibleProperty.get();
    }

    public void setGroupVisible(boolean groupVisible) {
        groupVisibleProperty().setValue(groupVisible);
    }

    private final BooleanProperty masterVisibleProperty = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            updateView();
        }
    };

    public BooleanProperty masterVisibleProperty() {
        return masterVisibleProperty;
    }

    public boolean isMasterVisible() {
        // Note: masterVisibleProperty is null on first call because not yet initialized (constructor -> set master/slave view -> updateView() -> isMasterVisible())
        return masterVisibleProperty == null || masterVisibleProperty.get();
    }

    public void setMasterVisible(boolean masterVisible) {
        masterVisibleProperty().setValue(masterVisible);
    }


    /*==================================================================================================================
    =================================================== Data binding ===================================================
    ==================================================================================================================*/

    @Override
    public <E extends Entity> void doVisibilityBinding(GroupView<E> groupView, ObjectProperty<E> masterSelectedEntityProperty, Function<E, Boolean> additionalSlaveVisibilityCondition) {
        groupVisibleProperty() .bind(FXProperties.compute(groupView.groupDqlStatementProperty(), statement -> statement != null && statement.getGroupBy() != null));
        masterVisibleProperty().bind(FXProperties.combine(groupVisibleProperty(),  groupView.selectedGroupProperty(), (groupVisible, selectedGroup) -> !groupVisible || selectedGroup != null));
        slaveVisibleProperty() .bind(FXProperties.combine(masterVisibleProperty(), masterSelectedEntityProperty, (masterVisible, selectedEntity) -> masterVisible && selectedEntity != null && (additionalSlaveVisibilityCondition == null || additionalSlaveVisibilityCondition.apply(selectedEntity))));
    }


    /*==================================================================================================================
    ============================================== Static factory methods ==============================================
    ==================================================================================================================*/

    public static <E extends Entity, PM extends HasGroupVisualResultProperty & HasMasterVisualResultProperty & HasSelectedMasterProperty<E>>
    GroupMasterSlaveView createAndBind(PM pm, ControlFactoryMixin mixin) {
        UiBuilder slaveView = MasterSlaveView.createAndBindSlaveViewIfApplicable(pm, mixin);
        if (slaveView == null && pm instanceof HasSlaveVisualResultProperty)
            slaveView = SlaveTableView.createAndBind((HasSlaveVisualResultProperty) pm);
        return createAndBind(slaveView, pm, mixin);
    }

    public static <E extends Entity, PM extends HasGroupVisualResultProperty & HasMasterVisualResultProperty & HasSlaveVisualResultProperty & HasSelectedMasterProperty<E>>
    GroupMasterSlaveView createAndBindVisualResult(PM pm, ControlFactoryMixin mixin) {
        return createAndBind(SlaveTableView.createAndBind(pm), pm, mixin);
    }

    public static <E extends Entity, PM extends HasGroupVisualResultProperty & HasMasterVisualResultProperty & HasSelectedMasterProperty<E>>
    GroupMasterSlaveView createAndBind(UiBuilder slaveUiFactory, PM pm, ControlFactoryMixin mixin) {
        return createAndBind(MasterTableView.createAndBind(pm, mixin), slaveUiFactory, pm);
    }

    public static <E extends Entity, PM extends HasGroupVisualResultProperty & HasSelectedMasterProperty<E>>
    GroupMasterSlaveView createAndBind(UiBuilder masterUiFactory, UiBuilder slaveUiFactory, PM pm) {
        return createAndBind(
                GroupView.createAndBind(pm),
                masterUiFactory,
                slaveUiFactory,
                pm);
    }

    public static <E extends Entity> GroupMasterSlaveView createAndBind(GroupView<E> groupView, UiBuilder masterView, UiBuilder slaveView, HasSelectedMasterProperty<E> pm) {
        return createAndBind(groupView, masterView.buildUi(), slaveView == null ? null : slaveView.buildUi(), pm);
    }

    public static  <E extends Entity> GroupMasterSlaveView createAndBind(GroupView<E> groupView, Node masterView, Node slaveView, HasSelectedMasterProperty<E> pm) {
        return createAndBind(groupView, masterView, slaveView, pm.selectedMasterProperty(), pm instanceof HasSlaveVisibilityCondition ? ((HasSlaveVisibilityCondition<E>) pm)::isSlaveVisible : null);
    }

    public static <E extends Entity> GroupMasterSlaveView createAndBind(GroupView<E> groupView, Node masterView, Node slaveView, ObjectProperty<E> masterSelectedEntityProperty, Function<E, Boolean> additionalSlaveVisibilityCondition) {
        GroupMasterSlaveView groupMasterSlaveView = new GroupMasterSlaveView(groupView.buildUi(), masterView, slaveView);
        groupMasterSlaveView.doVisibilityBinding(groupView, masterSelectedEntityProperty, additionalSlaveVisibilityCondition);
        return groupMasterSlaveView;
    }
}
