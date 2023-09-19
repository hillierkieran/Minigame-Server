package minigames.server.highscore;

import java.util.stream.Collectors;
import java.util.List;


/**
 * Manages the high score functionalities which includes storing and retrieving of high scores 
 * from the HighScoreStorage implementation.
 * <p>
 * This manager is responsible for the logic related to the high score system, such as determining 
 * whether a new score should be recorded or not.
 * </p>
 */
class HighScoreManager {

    private HighScoreStorage storage;


    /**
     * Constructor to create a new instance of the HighScoreManager.
     *
     * @param storage The HighScoreStorage implementation that this manager should use.
     */
    HighScoreManager(HighScoreStorage storage) {
        this.storage = storage;
    }


    void registerGame(String gameName, Boolean isLowerBetter) {
        storage.registerGame(gameName, isLowerBetter);
    }


    boolean isGameRegistered(String gameName) {
        return storage.isGameRegistered(gameName);
    }


    /**
     * Records a new score if it's better than the player's previous best, considering the game's metadata.
     * 
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @param newScore The new score achieved by the player.
     * @throws HighScoreException If game metadata is not found or any other error occurs.
     */
    void recordScore(String playerId, String gameName, int newScore) {
        GameRecord game = storage.getGame(gameName);
        if (game == null) {
            throw new HighScoreException("Game metadata not found for game: " + gameName);
        }

        ScoreRecord previousBest = storage.getScore(playerId, gameName);

        if (previousBest == null) {
            storage.storeScore(playerId, gameName, newScore);
            return;
        }

        boolean shouldRecord;
        if (game.isLowerBetter()) {
            shouldRecord = newScore < previousBest.getScore();
        } else {
            shouldRecord = newScore > previousBest.getScore();
        }

        if (shouldRecord) {
            storage.storeScore(playerId, gameName, newScore);
        }
    }


    /**
     * Retrieves the personal best score of a specific player for a specific game.
     * 
     * @param playerId The ID of the player.
     * @param gameName The name of the game.
     * @return A ScoreRecord object containing the personal best score of the player for the game.
     */
    ScoreRecord getPersonalBest(String playerId, String gameName) {
        return storage.getScore(playerId, gameName);
    }


    /**
     * Retrieves a list of the high scores for a specific game.
     * 
     * @param gameName The name of the game.
     * @return A list of ScoreRecord objects containing the high scores for the game.
     */
    List<ScoreRecord> getHighScores(String gameName) {
        GameRecord game = storage.getGame(gameName);
        if (game == null) {
            throw new HighScoreException(
                "Game metadata not found for game: " + gameName
            );
        }

        List<ScoreRecord> scores = storage.getHighScores(gameName);
        scores.sort((record1, record2) -> {
            if (game.isLowerBetter()) {
                return Integer.compare(record1.getScore(), record2.getScore());
            } else {
                return Integer.compare(record2.getScore(), record1.getScore());
            }
        });

        return scores.stream().collect(Collectors.toList());
    }


    /**
     * Retrieves string listing the high scores for a specific game.
     * 
     * @param gameName The name of the game.
     * @return A string listing all the high scores for the game.
     */
    String getHighScoresToString(String gameName) {
        List<ScoreRecord> scores = getHighScores(gameName);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < scores.size(); i++) {
            ScoreRecord score = scores.get(i);
            result.append(ordinal(i + 1)).append(" ")
                .append(score.getPlayerId()).append(" ")
                .append(score.getScore()).append("\n");
        }

        return result.toString();
    }

    // Convert ints to ordinal strings. eg. 1st, 2nd, 3rd, ...
    private String ordinal(int i) {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod100 - mod10 == 10) {
            return i + "th";
        }
        switch (mod10) {
            case 1:
                return i + "st";
            case 2:
                return i + "nd";
            case 3:
                return i + "rd";
            default:
                return i + "th";
        }
    }


    /**
     * Deletes a given score record from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteScore(String playerId, String gameName) {
        storage.deleteScore(playerId, gameName);
    }


    /**
     * Deletes a given game record and all it's scores from the database.
     * 
     * @param playerId The player ID of the score to be deleted.
     * @param gameName The game name of the score to be deleted.
     */
    public void deleteGame(String gameName) {
        storage.deleteGame(gameName);
    }
}
