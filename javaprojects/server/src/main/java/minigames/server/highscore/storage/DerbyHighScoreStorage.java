package minigames.server.highscore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import minigames.server.database.*;


/**
 * Derby Database implementation of HighScoreStorage.
 */
public class DerbyHighScoreStorage implements HighScoreStorage {

    private DerbyDatabase database;
    private GameTable gameTable;
    private ScoreTable scoreTable;


    /**
     * @param database Database for operations.
     */
    public DerbyHighScoreStorage(DerbyDatabase database) {
        if (!(database instanceof DerbyDatabase)) {
            throw new IllegalArgumentException(
                "Provided database is not an instance of DerbyDatabase"
            );
        }
        this.database = database;
        this.gameTable = new GameTable(this.database);
        this.scoreTable = new ScoreTable(this.database, this.gameTable);
    }


    /**
     * For testing only.
     * @param gameTable Game table reference.
     * @param scoreTable Score table reference.
     */
    DerbyHighScoreStorage(DatabaseTable gameTable, DatabaseTable scoreTable) {
        this.gameTable  = (GameTable)  gameTable;
        this.scoreTable = (ScoreTable) scoreTable;
    }


    /**
     * Inserts or updates a given game.
     *
     * @param gameName Game name.
     * @param isLowerBetter Game's score ordering preference.
     */
    @Override
    public void registerGame(String gameName, Boolean isLowerBetter) {
        GameRecord prevRecord = getGame(gameName);
        GameRecord gameRecord = new GameRecord(gameName, isLowerBetter);
        if (prevRecord == null) {
            gameTable.create(gameRecord);
        } else if (Boolean.compare(prevRecord.isLowerBetter(), isLowerBetter) != 0) {
            gameTable.update(gameRecord);
        }
    }


    /**
     * Checks if a given game exists in the database.
     *
     * @param gameName Game name.
     * @return True if exists, false otherwise.
     */
    @Override
    public boolean isGameRegistered(String gameName) {
        return getGame(gameName) != null;
    }


    /**
     * Inserts or updates a given score.
     *
     * @param playerId Player ID.
     * @param gameName Game name.
     * @param score Score value.
     */
    @Override
    public void storeScore(String playerId, String gameName, int score) {
        ScoreRecord prevRecord = getScore(playerId, gameName);
        ScoreRecord scoreRecord = new ScoreRecord(playerId, gameName, score);
        if (prevRecord == null) {
            scoreTable.create(scoreRecord);
        } else if (prevRecord.getScore() != score) {
            scoreTable.update(scoreRecord);
        }
    }


    /**
     * Retrieves all the high scores for a given game.
     *
     * @param gameName Game name.
     * @return List of top scores.
     */
    @Override
    public List<ScoreRecord> getHighScores(String gameName) {
        return scoreTable.retrieveMany(gameName);
    }


    /**
     * Retrieves the best score for player in a game.
     *
     * @param playerId Player ID.
     * @param gameName Game name.
     * @return Best score.
     */
    @Override
    public ScoreRecord getScore(String playerId, String gameName) {
        return scoreTable.retrieveOne(new ScoreRecord(playerId, gameName, 0));
    }


    /**
     * Retrieves all scores for all games.
     *
     * @return All scores.
     */
    @Override
    public List<ScoreRecord> getAllScores() {
        return scoreTable.retrieveAll();
    }


    /**
     * Retrieves a game's metadata record.
     *
     * @param gameName Game name.
     * @return Game metadata record.
     */
    @Override
    public GameRecord getGame(String gameName) {
        return gameTable.retrieveOne(new GameRecord(gameName, false));
    }


    /**
     * Deletes a score record.
     *
     * @param playerId Player ID.
     * @param gameName Game name.
     */
    @Override
    public void deleteScore(String playerId, String gameName) {
        scoreTable.delete(new ScoreRecord(playerId, gameName, 0));
    }


    /**
     * Deletes a game and its scores.
     *
     * @param gameName Game name.
     */
    @Override
    public void deleteGame(String gameName) {
        gameTable.delete(new GameRecord(gameName, false));
    }


    /** Backup game metadata. */
    @Override
    public void backupGame() {
        gameTable.backup();
    }


    /** Backup scores. */
    @Override
    public void backupScores() {
        scoreTable.backup();
    }
}
