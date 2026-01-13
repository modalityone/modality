package one.modality.crm.backoffice.activities.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds detailed usage information for a label, tracking where it's used across different entity types.
 * Now stores actual entity references (ID, name, dates) instead of just counts.
 *
 * @author Claude Code
 */
public class LabelUsageDetails {

    /**
     * Represents a single entity that uses this label.
     */
    public static class EntityReference {
        private final Object id;
        private final String name;
        private final String dateRange; // For events: "startDate - endDate"
        private final String fieldUsed; // Which field uses the label (e.g., "Name", "Short Description")

        public EntityReference(Object id, String name, String dateRange, String fieldUsed) {
            this.id = id;
            this.name = name;
            this.dateRange = dateRange;
            this.fieldUsed = fieldUsed;
        }

        public Object getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDateRange() {
            return dateRange;
        }

        public String getFieldUsed() {
            return fieldUsed;
        }

        public String getDisplayText() {
            StringBuilder sb = new StringBuilder();
            if (name != null && !name.isEmpty()) {
                sb.append(name);
            } else {
                sb.append("ID: ").append(id);
            }
            if (dateRange != null && !dateRange.isEmpty()) {
                sb.append(" (").append(dateRange).append(")");
            }
            if (fieldUsed != null && !fieldUsed.isEmpty()) {
                sb.append(" [").append(fieldUsed).append("]");
            }
            return sb.toString();
        }
    }

    // Entity type display names mapped to their entity references
    // Using LinkedHashMap to preserve insertion order for consistent display
    private final Map<String, List<EntityReference>> usageByEntityType = new LinkedHashMap<>();

    /**
     * Adds an entity reference for a specific entity type.
     *
     * @param entityType The display name for this entity type (e.g., "Events", "Items")
     * @param reference  The entity reference with details
     */
    public void addEntityReference(String entityType, EntityReference reference) {
        usageByEntityType.computeIfAbsent(entityType, k -> new ArrayList<>()).add(reference);
    }

    /**
     * Adds a simple usage count (for backwards compatibility or when details aren't needed).
     *
     * @param entityType The display name for this usage type
     * @param count      The number of usages
     */
    public void addUsage(String entityType, int count) {
        // For simple counts, we just add placeholder references
        if (count > 0) {
            List<EntityReference> refs = usageByEntityType.computeIfAbsent(entityType, k -> new ArrayList<>());
            for (int i = 0; i < count; i++) {
                refs.add(new EntityReference(null, null, null, null));
            }
        }
    }

    /**
     * Gets the total usage count across all entity types.
     */
    public int getTotalCount() {
        return usageByEntityType.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Gets the usage breakdown by entity type (returns counts for display).
     */
    public Map<String, Integer> getUsageByEntityType() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map.Entry<String, List<EntityReference>> entry : usageByEntityType.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    /**
     * Gets the detailed entity references by entity type.
     */
    public Map<String, List<EntityReference>> getDetailedUsage() {
        return usageByEntityType;
    }

    /**
     * Gets entity references for a specific entity type.
     */
    public List<EntityReference> getEntityReferences(String entityType) {
        return usageByEntityType.getOrDefault(entityType, new ArrayList<>());
    }

    /**
     * Checks if there are any usages.
     */
    public boolean hasUsages() {
        return !usageByEntityType.isEmpty();
    }

    /**
     * Checks if this has detailed entity references (with names/IDs) vs just counts.
     */
    public boolean hasDetailedReferences() {
        for (List<EntityReference> refs : usageByEntityType.values()) {
            for (EntityReference ref : refs) {
                if (ref.getId() != null || ref.getName() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clears all usage data.
     */
    public void clear() {
        usageByEntityType.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(getTotalCount()).append("\n");
        for (Map.Entry<String, List<EntityReference>> entry : usageByEntityType.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append("\n");
            for (EntityReference ref : entry.getValue()) {
                if (ref.getId() != null || ref.getName() != null) {
                    sb.append("    - ").append(ref.getDisplayText()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
