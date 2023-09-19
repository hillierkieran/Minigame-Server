package minigames.server.highscore;

import java.util.*;
import java.util.stream.Collectors;


/**
 * The GlobalLeaderboard class computes global rankings for players across all games.
 * <p>
 * The class provides methods to calculate and retrieve player rankings based on their individual game rankings.
 * Global rankings are determined by aggregating each player's rankings across all games.
 * The aggregate scores are then normalised to account for the number of games a player has played, ensuring 
 * that players are not penalised for playing more games. Players with lower normalised scores are ranked higher.
 * </p>
 */
public class GlobalLeaderboard {

    private HighScoreStorage storage;


    /**
     * Initialises a new instance of the GlobalLeaderboard class.
     *
     * @param storage The storage mechanism used to retrieve game scores and metadata.
     */
    public GlobalLeaderboard(HighScoreStorage storage) {
        this.storage = storage;
    }


    /**
     * Computes and returns the global rankings for all players across all games.
     *
     * @return A map where the key is the player ID and the value is their global rank,
     *         normalised for the number of games they've played.
     */
    public Map<String, Integer> computeGlobalScores() {
        // Retrieve all scores from storage
        List<ScoreRecord> allScores = storage.getAllScores();

        // Group the scores by game and compute rankings for each game
        Map<String, Map<String, Integer>> rankingsByGame = allScores.stream()
            .collect(Collectors.groupingBy(ScoreRecord::getGameName))
            .entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    entry -> computeRankingsForGame(entry.getKey(), entry.getValue())));

        // Aggregate rankings from each game to compute a global score for each player
        // Also count the number of games played by each player for the next step
        Map<String, Integer> globalScores = new HashMap<>();
        Map<String, Integer> gamesPlayed = new HashMap<>();
        rankingsByGame.values().forEach(rankings -> {
            rankings.forEach((player, rank) -> {
                globalScores.put(player, globalScores.getOrDefault(player, 0) + rank);
                gamesPlayed.put(player, gamesPlayed.getOrDefault(player, 0) + 1);
            });
        });

        // Normalise the rankings by dividing a player's global score by the number of games played
        for (String player : globalScores.keySet()) {
            globalScores.put(player, globalScores.get(player) / gamesPlayed.get(player));
        }

        // Convert the aggregated and normalised scores to global ranks
        Map<String, Integer> globalRanks = convertScoresToRanks(globalScores);

        return globalRanks;
    }


    /**
     * Computes the rankings for a specific game based on the game's scores.
     *
     * @param gameName Name of the game for which rankings need to be computed.
     * @param scores A list of scores for the specific game.
     * @return A map where the key is the player ID and the value is their rank for the given game.
     */
    private Map<String, Integer> computeRankingsForGame(String gameName, List<ScoreRecord> scores) {
        // Sort scores based on the game's criteria (lower or higher is better)
        scores.sort((score1, score2) -> 
            storage.getGame(gameName).isLowerBetter() 
            ? Integer.compare(score1.getScore(), score2.getScore())
            : Integer.compare(score2.getScore(), score1.getScore()));
        
        // Assign rankings based on sorted scores
        Map<String, Integer> rankings = new HashMap<>();
        for (int i = 0; i < scores.size(); i++) {
            rankings.put(scores.get(i).getPlayerId(), i + 1);
        }
        return rankings;
    }


    /**
     * Converts the aggregated and normalised global scores into sequential ranks (e.g., 1st, 2nd, 3rd, etc.).
     *
     * @param globalScores A map of player IDs to their normalised aggregated global scores.
     * @return A map of player IDs to their global rank.
     */
    private Map<String, Integer> convertScoresToRanks(Map<String, Integer> globalScores) {
        // Sort players based on their global scores
        List<Map.Entry<String, Integer>> sortedScores = globalScores.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toList());

        // Convert scores to ranks (1st, 2nd, 3rd, etc.)
        Map<String, Integer> globalRanks = new LinkedHashMap<>();
        int currentRank = 1, prevScore = -1;
        for (Map.Entry<String, Integer> entry : sortedScores) {
            globalRanks.put(entry.getKey(), (prevScore != -1 && entry.getValue() == prevScore) ? currentRank : currentRank++);
            prevScore = entry.getValue();
        }
        return globalRanks;
    }
}
