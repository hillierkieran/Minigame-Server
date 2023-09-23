package minigames.server.highscore;

import java.util.stream.Collectors;
import java.util.List;


/**
 * Manages high score operations.
 */
class HighScoreManager {

    private HighScoreStorage storage;


    /**
     * Constructor.
     * @param storage Storage mechanism for high scores.
     */
    HighScoreManager(HighScoreStorage storage) {
        this.storage = storage;
    }


    void registerGame(String gameName, Boolean isLowerBetter) {
        storage.registerGame(gameName, isLowerBetter);
        storage.backupGame();
    }


    boolean isGameRegistered(String gameName) {
        return storage.isGameRegistered(gameName);
    }


    /**
     * Records a new high score if better than previous.
     *
     * @param playerId Player's ID.
     * @param gameName Game's name.
     * @param newScore New score achieved.
     */
    void recordScore(String playerId, String gameName, int newScore) {
        GameRecord game = storage.getGame(gameName);
        if (game == null)
            throw new HighScoreException("Game metadata not found for game: " + gameName);
        ScoreRecord previousBest = storage.getScore(playerId, gameName);
        boolean shouldRecord;
        if (previousBest == null)
            shouldRecord = true;
        else if (game.isLowerBetter())
            shouldRecord = newScore < previousBest.getScore();
        else
            shouldRecord = newScore > previousBest.getScore();
        if (shouldRecord)
            storage.storeScore(playerId, gameName, newScore);
        storage.backupScores();
    }


    /**
     * @param playerId Player's ID.
     * @param gameName Game's name.
     * @return Personal best score for specified game.
     */
    ScoreRecord getPersonalBest(String playerId, String gameName) {
        return storage.getScore(playerId, gameName);
    }


    /**
     * @param gameName Game's name.
     * @return High scores for specified game.
     */
    List<ScoreRecord> getHighScores(String gameName) {
        GameRecord game = storage.getGame(gameName);
        if (game == null)
            throw new HighScoreException( "Game metadata not found for game: " + gameName );
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
     * @param gameName Game's name.
     * @return High scores as a string for specified game.
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


    /** Convert ints to ordinal strings */
    private String ordinal(int i) {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod100 - mod10 == 10) return i + "th";
        switch (mod10) {
            case 1:  return i + "st";
            case 2:  return i + "nd";
            case 3:  return i + "rd";
            default: return i + "th";
        }
    }


    /**
     * Deletes specific score.
     *
     * @param playerId Player's ID.
     * @param gameName Game's name.
     */
    public void deleteScore(String playerId, String gameName) {
        storage.deleteScore(playerId, gameName);
        storage.backupScores();
    }


    /**
     * Deletes game and associated scores.
     *
     * @param gameName Game's name.
     */
    public void deleteGame(String gameName) {
        storage.deleteGame(gameName);
        storage.backupGame();
        storage.backupScores();
    }
}
