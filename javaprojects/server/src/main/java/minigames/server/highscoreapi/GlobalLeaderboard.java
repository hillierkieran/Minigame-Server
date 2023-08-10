package minigames.server.highscore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes global rankings based on individual game rankings.
 */
public class GlobalLeaderboard {

    private HighScoreStorage storage;   // Storage to fetch game scores

    /**
     * Constructor
     * @param storage The storage mechanism for retrieving scores
     */
    public GlobalLeaderboard(HighScoreStorage storage) {
        this.storage = storage;
    }

    /**
     * Computes the global scores for all players across all games.
     * @return A sorted map of player IDs to global scores.
     */
    public Map<String, Integer> computeGlobalScores() {

        // Retrieve all scores across all games
        List<ScoreRecord> allScores = storage.retrieveAllScores();

        // Group by game - Map<GameName, List<ScoreRecord>>
        Map<String, List<ScoreRecord>> scoresByGame = allScores.stream()
            .collect(Collectors.groupingBy(ScoreRecord::getGameName));

        // Compute rankings for each game - Map<GameName, Map<playerId, rank>>
        Map<String, Map<String, Integer>> rankingsByGame = new HashMap<>();
        // for each game...
        for (String game : scoresByGame.keySet()) {
            // get each player's rank
            Map<String, Integer> playerRankings = computeRankingsForGame(scoresByGame.get(game));
            rankingsByGame.put(game, playerRankings);
        }

        // Compute global scores - Map<playerID, totalRank>
        Map<String, Integer> globalScores = new HashMap<>();
        // for each game...
        for (Map<String, Integer> rankings : rankingsByGame.values()) {
            // add each player's rank to their total 
            for (String player : rankings.keySet()) {
                globalScores.put(player, globalScores.getOrDefault(player, 0) + rankings.get(player));
            }
        }

        // return ordered list of player's summed rank - LinkedHashMap<playerId, totalRank>
        return globalScores
            .entrySet() // get a set of the map's entries (key-value pairs)
            .stream()   // transform set into a stream
            .sorted(Map.Entry.comparingByValue())   // sort stream of entries by their ascending values (totalRank)
            .collect(Collectors.toMap(  // collect sorted stream back into a map...
                Map.Entry::getKey,      // for each entry, get and use the key (playerId) in the new map.
                Map.Entry::getValue,    // for each entry, get and use the value (totalRank) in the new map.
                (e1, e2) -> e1,         // if there are duplicate keys, keep the first one (e1).
                LinkedHashMap::new));   // specify type of map to create
    }

    /**
     * Computes the rankings for a specific game.
     * @param scores The scores for a game
     * @return A map of player IDs to their rankings for the game.
     */
    private Map<String, Integer> computeRankingsForGame(List<ScoreRecord> scores) {

        // Get game metadata
        String gameName = scores.get(0).getGameName();
        GameMetadata gameMetadata = storage.getGameMetadata(gameName);

        // Sort scores from lowest to highest
        scores.sort(Comparator.comparingInt(ScoreRecord::getScore));

        // Reverse unless lower scores are better
        if (!gameMetadata.isLowerBetter()) {
            Collections.reverse(scores);
        }

        // Map players to their rank
        Map<String, Integer> rankings = new HashMap<>();
        for (int i = 0; i < scores.size(); i++) {
            rankings.put(scores.get(i).getPlayerId(), i + 1);
        }

        return rankings;
    }
}
