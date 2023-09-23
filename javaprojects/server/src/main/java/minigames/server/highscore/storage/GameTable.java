package minigames.server.highscore;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;

import minigames.server.database.*;


/**
 * Handles database operations for game metadata records.
 */
public class GameTable extends DatabaseTable<GameRecord> {

    public static final String TABLE_NAME = "HIGH_SCORE_GAME_METADATA";
    public static final String COLUMN_GAME_NAME = "game_name";
    public static final String COLUMN_IS_LOWER_BETTER = "is_lower_better";


    /**
     * Constructor.
     * @param database The database instance.
     */
    public GameTable(Database database) {
        super(database, TABLE_NAME);
    }


    // Get column names
    public String getColumnGameName() { return COLUMN_GAME_NAME; }
    public String getColumnIsLowerBetter() { return COLUMN_IS_LOWER_BETTER; }


    @Override
    public List<String> getColumnNames() {
        return Arrays.asList(
            COLUMN_GAME_NAME,
            COLUMN_IS_LOWER_BETTER
        );
    }

    @Override
    public List<String> getKeyColumnNames() {
        return Arrays.asList(
            COLUMN_GAME_NAME
        );
    }

    @Override
    protected List<Object> getPrimaryKeyValues(Object record) {
        return Arrays.asList(
            ((GameRecord) record).getGameName()
        );
    }

    @Override
    protected String getTableCreationSQL() {
        return (
            "CREATE TABLE " +
                TABLE_NAME +
            " (" +
                COLUMN_GAME_NAME + " VARCHAR(255) PRIMARY KEY, " +
                COLUMN_IS_LOWER_BETTER + " BOOLEAN " +
            ")"
        );
    }

    @Override
    protected String getInsertSQL() {
        return (
            "INSERT INTO " +
                TABLE_NAME +
            " (" +
                COLUMN_GAME_NAME + ", " +
                COLUMN_IS_LOWER_BETTER +
            ") VALUES (?, ?)"
        );
    }
    @Override
    protected List<Object> getInsertValues(GameRecord record) {
        return Arrays.asList(
            record.getGameName(),
            record.isLowerBetter()
        );
    }

    @Override
    protected String getUpdateSQL() {
        return  "UPDATE " +
                    TABLE_NAME +
                " SET " +
                    COLUMN_IS_LOWER_BETTER + " = ? " +
                "WHERE " +
                    COLUMN_GAME_NAME + " = ?";
    }
    @Override
    protected List<Object> getUpdateSetValues(GameRecord record) {
        return Arrays.asList(
            record.isLowerBetter(),
            record.getGameName()
        );
    }

    @Override
    protected String getRetrieveOneSQL() {
        return (
            "SELECT " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_IS_LOWER_BETTER +
            " FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_GAME_NAME + " = ?"
        );
    }

    @Override
    protected String getRetrieveManySQL() {
        return getRetrieveAllSQL();
    }
    @Override
    protected List<Object> getRetrieveManyKeyValues(Object filterCriteria) {
        return null;
    }

    @Override
    protected String getRetrieveAllSQL() {
        return (
            "SELECT " +
                COLUMN_GAME_NAME + ", " +
                COLUMN_IS_LOWER_BETTER +
            "FROM " +
                TABLE_NAME
        );
    }

    @Override
    protected String getDeleteSQL() {
        return (
            "DELETE FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_GAME_NAME + " = ?"
        );
    }

    @Override
    protected GameRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new GameRecord(
            rs.getString(COLUMN_GAME_NAME),
            rs.getBoolean(COLUMN_IS_LOWER_BETTER)
        );
    }
}
