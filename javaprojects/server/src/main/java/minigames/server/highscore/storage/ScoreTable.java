package minigames.server.highscore;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;

import minigames.server.database.*;


/**
 * Handles database operations for game metadata records.
 */
public class ScoreTable extends DatabaseTable<ScoreRecord> {

    private static final String TABLE_NAME = "high_score_records";
    private static final String COLUMN_PLAYER_ID = "player_id";
    private static final String COLUMN_GAME_NAME = "game_name";
    private static final String COLUMN_SCORE = "score";

    private GameTable gameTable;

    /**
     * Constructor.
     * @param database The database instance.
     * @param referencedTable reference to the game table.
     */
    public ScoreTable(Database database, GameTable referencedTable) {
        super(database, TABLE_NAME);
        this.gameTable = referencedTable;
    }


    // Get column names
    public String getColumnPlayerId() { return COLUMN_PLAYER_ID; }
    public String getColumnGameName() { return COLUMN_GAME_NAME; }
    public String getColumnScore() { return COLUMN_SCORE; }


    @Override
    public List<String> getColumnNames() {
        return Arrays.asList(
            COLUMN_PLAYER_ID,
            COLUMN_GAME_NAME,
            COLUMN_SCORE
        );
    }

    @Override
    public List<String> getKeyColumnNames() {
        return Arrays.asList(
            COLUMN_PLAYER_ID,
            COLUMN_GAME_NAME
        );
    }

    @Override
    protected List<Object> getPrimaryKeyValues(Object record) {
        return Arrays.asList(
            ((ScoreRecord) record).getPlayerId(),
            ((ScoreRecord) record).getGameName()
        );
    }

    @Override
    protected String getTableCreationSQL() {
        return  (
            "CREATE TABLE " +
                TABLE_NAME +
            " (" +
                COLUMN_PLAYER_ID + " VARCHAR(255), " +
                COLUMN_GAME_NAME + " VARCHAR(255) " +
                    // game must exist to record a score for it
                    "REFERENCES " + gameTable.getTableName() + " (" +
                        gameTable.getColumnGameName() +
                    ") " +
                    // If a game is deleted, so will it's scores
                    "ON DELETE CASCADE, " +
                COLUMN_SCORE + " INT, " +
                "PRIMARY KEY (" +
                    COLUMN_PLAYER_ID + ", " +
                    COLUMN_GAME_NAME +
                ")" +
            ")"
        );
    }

    @Override
    protected String getInsertSQL() {
        return (
            "INSERT INTO " +
                TABLE_NAME +
            " (" +
                COLUMN_PLAYER_ID + ", " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_SCORE +
            ") VALUES (?, ?, ?)"
        );
    }
    @Override
    protected List<Object> getInsertValues(ScoreRecord record) {
        return Arrays.asList(
            record.getPlayerId(),
            record.getGameName(),
            record.getScore()
        );
    }

    @Override
    protected String getUpdateSQL() {
        return (
            "UPDATE " +
                TABLE_NAME +
            " SET " +
                COLUMN_SCORE + " = ? " +
            "WHERE " +
                COLUMN_PLAYER_ID + " = ? AND " +
                COLUMN_GAME_NAME + " = ?"
        );
    }
    @Override
    protected List<Object> getUpdateSetValues(ScoreRecord record) {
        return Arrays.asList(
            record.getScore()
        );
    }

    @Override
    protected String getRetrieveOneSQL() {
        return (
            "SELECT " +
                COLUMN_PLAYER_ID + ", " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_SCORE +
            " FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_PLAYER_ID + " = ? AND " +
                COLUMN_GAME_NAME + " = ?"
        );
    }

    @Override
    protected String getRetrieveManySQL() {
        return (
            "SELECT " +
                COLUMN_PLAYER_ID + ", " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_SCORE +
            " FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_GAME_NAME + " = ?"
        );
    }
    @Override
    protected List<Object> getRetrieveManyKeyValues(Object gameName) {
        return Arrays.asList(
            gameName
        );
    }

    @Override
    protected String getRetrieveAllSQL() {
        return (
            "SELECT " +
                COLUMN_PLAYER_ID + ", " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_SCORE +
            " FROM " +
                TABLE_NAME
        );
    }

    @Override
    protected String getDeleteSQL() {
        return (
            "DELETE FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_PLAYER_ID + " = ? AND " +
                COLUMN_GAME_NAME + " = ?"
        );
    }

    @Override
    protected ScoreRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new ScoreRecord(
            rs.getString(COLUMN_PLAYER_ID),
            rs.getString(COLUMN_GAME_NAME),
            rs.getInt(COLUMN_SCORE)
        );
    }
}
