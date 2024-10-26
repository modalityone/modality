package one.modality.catering.backoffice.activities.kitchen;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbbreviationGenerator {

    private final Map<String, String> abbreviations = new HashMap<>();

    public AbbreviationGenerator(Collection<String> strings) {
        Map<String, List<String>> capitalizedAcronymInputs = new HashMap<>();
        for (String string : strings) {
            String capitalizedAcronym = generateAcronym(string).toUpperCase();
            if (!capitalizedAcronymInputs.containsKey(capitalizedAcronym)) {
                capitalizedAcronymInputs.put(capitalizedAcronym, new ArrayList<>());
            }
            capitalizedAcronymInputs.get(capitalizedAcronym).add(string);
        }

        for (String string : strings) {
            String acronym = generateAcronym(string);
            if (capitalizedAcronymInputs.get(acronym.toUpperCase()).size() == 1) {
                abbreviations.put(string, acronym);
            } else {
                String complexAcronym = "";
                String[] words = splitIntoWords(string);
                for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                    String sourceWord = words[wordIndex];
                    final int finalWordIndex = wordIndex;
                    Collection<String> acronymTwins = capitalizedAcronymInputs.get(acronym.toUpperCase()).stream()
                            .filter(acronymTwin -> !acronymTwin.equals(string))
                            .map(acronymTwin -> splitIntoWords(acronymTwin)[finalWordIndex])
                            .collect(Collectors.toList());
                    String prefix = findShortestUniquePrefix(sourceWord, acronymTwins);
                    complexAcronym += prefix;
                }
                abbreviations.put(string, complexAcronym);
            }
        }
    }

    private static String findShortestUniquePrefix(String sourceWord, Collection<String> strings) {
        for (int i = 1; i < sourceWord.length(); i++) {
            String prefix = sourceWord.substring(0, i);
            boolean uniquePrefix = true;
            for (String string : strings) {
                if (string.length() >= i && string.substring(0, i).equalsIgnoreCase(prefix)) {
                    uniquePrefix = false;
                    break;
                }
            }
            if (uniquePrefix) {
                return prefix;
            }
        }
        return sourceWord.substring(0, 1);
    }

    private static String generateAcronym(String string) {
        try {
            return Stream.of(splitIntoWords(string))
                    .map(s -> String.valueOf(s.charAt(0)))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            return string;
        }
    }

    private static String[] splitIntoWords(String string) {
        return string.split(" ");
    }

    public String getAbbreviation(String string) {
        return abbreviations.getOrDefault(string, string);
    }
}
