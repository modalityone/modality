package one.modality.hotel.backoffice.activities.household.dashboard.presenter;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import one.modality.hotel.backoffice.activities.household.dashboard.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for the Dashboard view.
 * Handles business logic for organizing daily activities.
 *
 * @author Claude Code Assistant
 */
public class DashboardPresenter {

    private final IntegerProperty daysToDisplay = new SimpleIntegerProperty(7);
    private final DashboardFilterManager filterManager;

    public DashboardPresenter() {
        this.filterManager = new DashboardFilterManager();
    }

    public IntegerProperty daysToDisplayProperty() {
        return daysToDisplay;
    }

    public DashboardFilterManager getFilterManager() {
        return filterManager;
    }

    /**
     * Generates a list of dates to display based on current settings
     */
    public List<LocalDate> getDateRange(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        int days = daysToDisplay.get();
        for (int i = 0; i < days; i++) {
            dates.add(today.plusDays(i));
        }
        return dates;
    }

    /**
     * Applies appropriate filters to day data based on whether it's today
     */
    public void applyFilters(DayData dayData) {
        if (dayData.isToday()) {
            // Apply filters for today's cleaning and inspection cards
            dayData.getCleaningCards().clear();
            dayData.getCleaningCards().addAll(
                filterManager.applyCleaningFilters(new ArrayList<>(dayData.getCleaningCards()))
            );

            dayData.getInspectionCards().clear();
            dayData.getInspectionCards().addAll(
                filterManager.applyInspectionFilters(new ArrayList<>(dayData.getInspectionCards()))
            );
        }
    }

    /**
     * Determines if filters are active for cleaning section
     */
    public boolean hasActiveCleaningFilters() {
        return !"All".equals(filterManager.cleanStatusFilterProperty().get()) ||
               !"All".equals(filterManager.cleanBuildingFilterProperty().get()) ||
               !"All".equals(filterManager.cleanCheckInFilterProperty().get());
    }

    /**
     * Determines if filters are active for inspection section
     */
    public boolean hasActiveInspectionFilters() {
        return !"All".equals(filterManager.inspectBuildingFilterProperty().get()) ||
               !"All".equals(filterManager.inspectCheckInFilterProperty().get());
    }

    /**
     * Extracts building letter from building name (e.g., "Building A" -> "A")
     */
    public String extractBuildingLetter(String buildingName) {
        if (buildingName == null) return "A";
        if (buildingName.contains("A")) return "A";
        if (buildingName.contains("B")) return "B";
        if (buildingName.contains("C")) return "C";
        return "A";
    }
}
