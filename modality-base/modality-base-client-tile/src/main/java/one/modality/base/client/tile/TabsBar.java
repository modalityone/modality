package one.modality.base.client.tile;

import dev.webfx.platform.util.Arrays;
import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public class TabsBar<T> {

    private final List<Tab> tabs = new ArrayList<>();
    private Tab selectedTab;
    private final ActionFactory actionFactory;
    private final Consumer<T> valueSetter;

    public TabsBar(ActionFactory actionFactory, Consumer<T> valueSetter) {
        this.actionFactory = actionFactory;
        this.valueSetter = valueSetter;
    }

    public void setTabs(Tab... tabsArray) {
        setTabs(Arrays.asList(tabsArray));
    }

    public void setTabs(Collection<Tab> tabs) {
        this.tabs.clear();
        addTabs(tabs);
    }

    public void addTabs(Tab... tabsArray) {
        addTabs(Arrays.asList(tabsArray));
    }

    public void addTabs(Collection<Tab> tabs) {
        this.tabs.addAll(tabs);
        if (!tabs.isEmpty())
            this.tabs.get(0).fireAction();
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public Tab createTab(Object i18nKey, Supplier<T> valueSupplier) {
        return createTab(i18nKey, valueSupplier, null);
    }

    /**
     * Creates a tab with an optional callback that is invoked every time the tab is selected.
     *
     * @param i18nKey the i18n key for the tab label
     * @param valueSupplier supplier for the tab content (called only once, then cached)
     * @param onSelected callback invoked every time the tab is selected (can be null)
     * @return the created Tab
     */
    public Tab createTab(Object i18nKey, Supplier<T> valueSupplier, Runnable onSelected) {
        Tab[] tab = {null};
        Object[] tabContent = { null };
        Action action = actionFactory.newAction(i18nKey, () -> {
            if (selectedTab != null)
                selectedTab.setSelected(false);
            selectedTab = tab[0].setSelected(true);
            if (tabContent[0] == null)
                tabContent[0] = valueSupplier.get();
            valueSetter.accept((T) tabContent[0]);
            if (onSelected != null)
                onSelected.run();
        });
        return tab[0] = new Tab(action);
    }

    public Tab createTab(Object i18nKey, T value) {
        return createTab(i18nKey, () -> value);
    }

}
