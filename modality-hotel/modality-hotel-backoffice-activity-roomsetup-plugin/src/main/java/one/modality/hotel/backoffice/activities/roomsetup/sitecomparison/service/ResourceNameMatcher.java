package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.service;

import one.modality.base.shared.entities.Resource;

import java.util.*;

/**
 * Service for fuzzy matching resource names between global site and event sites.
 * Uses Levenshtein distance for similarity calculation.
 */
public final class ResourceNameMatcher {

    private static final double DEFAULT_MIN_SCORE = 0.6; // 60% minimum similarity

    /**
     * Calculate similarity between two strings using Levenshtein distance.
     *
     * @return similarity score from 0.0 (no match) to 1.0 (exact match)
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        // Normalize: trim and lowercase for comparison
        String n1 = s1.trim().toLowerCase();
        String n2 = s2.trim().toLowerCase();

        if (n1.equals(n2)) {
            return 1.0;
        }

        if (n1.isEmpty() || n2.isEmpty()) {
            return 0.0;
        }

        int distance = levenshteinDistance(n1, n2);
        int maxLength = Math.max(n1.length(), n2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Find the best matching global resource for an event resource.
     *
     * @param eventResourceName Name of the event site resource
     * @param globalResources List of global site resources to match against
     * @param minScore Minimum similarity score (0.0-1.0)
     * @return Best match with score, or empty if no match above threshold
     */
    public Optional<MatchResult> findBestMatch(
            String eventResourceName,
            List<Resource> globalResources,
            double minScore
    ) {
        if (eventResourceName == null || globalResources == null || globalResources.isEmpty()) {
            return Optional.empty();
        }

        Resource bestMatch = null;
        double bestScore = 0.0;

        for (Resource globalResource : globalResources) {
            String globalName = globalResource.getName();
            if (globalName == null) continue;

            double score = calculateSimilarity(eventResourceName, globalName);
            if (score > bestScore && score >= minScore) {
                bestScore = score;
                bestMatch = globalResource;
            }
        }

        if (bestMatch != null) {
            return Optional.of(new MatchResult(bestMatch, bestScore));
        }

        return Optional.empty();
    }

    /**
     * Suggest matches for all unlinked event resources.
     *
     * @param eventResources Event site resources to find matches for
     * @param globalResources Global site resources to match against
     * @param minScore Minimum similarity score threshold
     * @return Map of event resource to suggested match
     */
    public Map<Resource, MatchResult> suggestMatches(
            List<Resource> eventResources,
            List<Resource> globalResources,
            double minScore
    ) {
        Map<Resource, MatchResult> suggestions = new LinkedHashMap<>();

        if (eventResources == null || globalResources == null) {
            return suggestions;
        }

        // Track which global resources have been suggested to avoid duplicates
        Set<Object> usedGlobalIds = new HashSet<>();

        for (Resource eventResource : eventResources) {
            String eventName = eventResource.getName();
            if (eventName == null) continue;

            // Find best match that hasn't been used yet
            Resource bestMatch = null;
            double bestScore = 0.0;

            for (Resource globalResource : globalResources) {
                Object globalId = globalResource.getId().getPrimaryKey();
                if (usedGlobalIds.contains(globalId)) continue;

                String globalName = globalResource.getName();
                if (globalName == null) continue;

                double score = calculateSimilarity(eventName, globalName);
                if (score > bestScore && score >= minScore) {
                    bestScore = score;
                    bestMatch = globalResource;
                }
            }

            if (bestMatch != null) {
                suggestions.put(eventResource, new MatchResult(bestMatch, bestScore));
                usedGlobalIds.add(bestMatch.getId().getPrimaryKey());
            }
        }

        return suggestions;
    }

    /**
     * Suggest matches using default minimum score.
     */
    public Map<Resource, MatchResult> suggestMatches(
            List<Resource> eventResources,
            List<Resource> globalResources
    ) {
        return suggestMatches(eventResources, globalResources, DEFAULT_MIN_SCORE);
    }

    /**
     * Calculate Levenshtein (edit) distance between two strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Result of a match operation.
     */
    public static final class MatchResult {
        private final Resource resource;
        private final double score;

        public MatchResult(Resource resource, double score) {
            this.resource = resource;
            this.score = score;
        }

        public Resource resource() {
            return resource;
        }

        public double score() {
            return score;
        }

        public int scorePercent() {
            return (int) Math.round(score * 100);
        }
    }
}
