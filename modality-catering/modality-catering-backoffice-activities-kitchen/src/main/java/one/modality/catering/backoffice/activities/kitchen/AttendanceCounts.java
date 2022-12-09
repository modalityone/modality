package one.modality.catering.backoffice.activities.kitchen;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceCounts {

    private Map<LocalDate, List<Row>> rows = new HashMap<>();

    public void add(LocalDate date, String meal, String dietaryOption, int count) {
        if (!rows.containsKey(date)) {
            rows.put(date, new ArrayList<>());
        }
        rows.get(date).add(new Row(meal, dietaryOption, count));
    }

    public List<String> getSortedDietaryOptions() {
        return rows.values().stream()
                .flatMap(Collection::stream)
                .map(Row::getDietaryOption)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getCount(LocalDate date, String meal, String dietaryOption) {
        for (Row row : rows.getOrDefault(date, Collections.emptyList())) {
            if (row.getMeal().equals(meal) && row.getDietaryOption().equals(dietaryOption)) {
                return row.getCount();
            }
        }
        return 0;
    }

    private static class Row {
        private String meal;
        private String dietaryOption;
        private int count;

        public Row(String meal, String dietaryOption, int count) {
            this.meal = meal;
            this.dietaryOption = dietaryOption;
            this.count = count;
        }

        public String getMeal() {
            return meal;
        }

        public String getDietaryOption() {
            return dietaryOption;
        }

        public int getCount() {
            return count;
        }
    }
}
