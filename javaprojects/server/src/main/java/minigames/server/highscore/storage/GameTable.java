package minigames.server.highscore;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;

import minigames.server.database.*;


public class GameTable extends DatabaseTable<GameMetadata> {

    private static final String TABLE_NAME = "high_score_game_metadata";
    private static final String COLUMN_GAME_NAME = "game_name";
    private static final String COLUMN_IS_LOWER_BETTER = "is_lower_better";


    public GameTable(Database database) {
        super(database, TABLE_NAME);
    }


    // Get column names
    public String getColumnGameName() { return COLUMN_GAME_NAME; }
    public String getColumnIsLowerBetter() { return COLUMN_IS_LOWER_BETTER; }


    @Override
    protected List<Object> getPrimaryKeyValues(GameMetadata record) {
        return Arrays.asList(
            record.getGameName()
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
    protected List<Object> getInsertValues(GameMetadata record) {
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
    protected List<Object> getUpdateSetValues(GameMetadata record) {
        return Arrays.asList(
            record.isLowerBetter(),
            record.getGameName()
        );
    }


    // Retrieve One
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


    // Retrieve Many
    @Override
    protected String getRetrieveManySQL() {
        return getRetrieveAllSQL();
    }
    @Override
    protected List<Object> getRetrieveManyKeyValues(Object filterCriteria) {
        return null;
    }


    // Retrieve All
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


    // Delete
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
    protected GameMetadata mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new GameMetadata(
            rs.getString(COLUMN_GAME_NAME),
            rs.getBoolean(COLUMN_IS_LOWER_BETTER)
        );
    }
}
