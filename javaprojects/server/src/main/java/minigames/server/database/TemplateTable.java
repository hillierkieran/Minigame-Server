package minigames.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;

import minigames.server.database.Database;
import minigames.server.database.DatabaseAccessException;
import minigames.server.database.DerbyDatabaseTable;
// import your object class here

/**
 * This class provides a fill-in-the-blanks template implementations of CRUD
 * (Create, Retrieve, Update, Delete) operations for the specified table.
 * 
 * Please replace placeholders and blank areas with your information before using.
 */
public class TemplateTable extends DerbyDatabaseTable<YourObjectClass> {

    // Constants representing table name and column names
    private static final String TABLE_NAME = "your_table_name_here";
    private static final String COLUMN_EXAMPLE1 = "example_column1";
    private static final String COLUMN_EXAMPLE2 = "example_column2";
    // ... Add more columns as needed


    /**
     * Constructor to initialise the table structure.
     *
     * @param database The database connection object.
     */
    public TemplateTable(Database database) {
        super(database, TABLE_NAME);
    }


    // Simple getters for the column names, to be used elsewhere if needed
    public String getColumnExample1() { return COLUMN_EXAMPLE1; }
    public String getColumnExample2() { return COLUMN_EXAMPLE2; }
    // ... Add more getters for other columns


    // Return a list containing the value(s) of the primary key or compensate keys
    @Override
    protected List<Object> getPrimaryKeyValues(YourObjectClass record) {
        return Arrays.asList(
            record.getExample1()
            // ... Add more values if using a compensate key structure
        );
    }


    // Define table creation SQL with columns and primary key(s)
    @Override
    protected String getTableCreationSQL() {
        return (
            "CREATE TABLE " + 
                TABLE_NAME +
            " (" +
                COLUMN_EXAMPLE1 + " YOUR_DATA_TYPE_HERE, " +
                COLUMN_EXAMPLE2 + " INT, " +
                // ... Add more columns and their data types
                "PRIMARY KEY " +
                    COLUMN_EXAMPLE1 // Your primary key column(s)
            ")"
        );
    }


    // Define SQL statement to insert a new record into the table
    @Override
    protected String getInsertSQL() {
        return (
            "INSERT INTO " + 
                TABLE_NAME + 
            " (" +
                COLUMN_EXAMPLE1 + ", " +
                COLUMN_EXAMPLE2 +
                // ... Add more columns as needed
            ") VALUES (?, ?, ...)"
            // Ensure the number of '?' matches the number of columns
        );
    }


    // Return a list containing the values to be inserted
    @Override
    protected List<Object> getInsertValues(YourObjectClass record) {
        return Arrays.asList(
            record.getExample1(),
            record.getExample2(),
            // ... Add more values as needed
        );
    }


    // Define SQL statement to update an existing record based on primary key(s)
    @Override
    protected String getUpdateSQL() {
        return (
            "UPDATE " + 
                TABLE_NAME + 
            " SET " +
                COLUMN_EXAMPLE2 + " = ?, " + // Value(s) to be set
                // ... Add more columns as needed
            " WHERE " +
                COLUMN_EXAMPLE1 + " = ?" // Your primary key column(s)
                // ... If multiple primary keys, add more conditions
        );
    }


    // Return a list containing the values to be updated (don't include keys)
    @Override
    protected List<Object> getUpdateSetValues(YourObjectClass record) {
        return Arrays.asList(
            record.getExample1(),
            // ... Add more values to update
        );
    }


    // Define SQL statement to retrieve a single record based on primary key(s)
    @Override
    protected String getRetrieveOneSQL() {
        return (
            "SELECT " +
                COLUMN_EXAMPLE1 + ", " +
                COLUMN_EXAMPLE2 +
                // ... Add more columns as needed
            " FROM " +
                TABLE_NAME + 
            " WHERE " +
                COLUMN_EXAMPLE1 + " = ?" // Your primary key column(s)
                // ... If multiple primary keys, add more conditions
        );
    }


    // Define SQL statement to retrieve multiple records based on some criteria (e.g., a foreign key)
    @Override
    protected String getRetrieveManySQL() {
        return (
            "SELECT " +
                COLUMN_EXAMPLE1 + ", " +
                COLUMN_EXAMPLE2 +
                // ... Add more columns as needed
            " FROM " +
                TABLE_NAME + 
            " WHERE " +
                COLUMN_EXAMPLE2 + " = ?" // Your criteria column(s)
        );
    }


    // Return a list containing the criteria values to match for retrieval
    @Override
    protected List<Object> getRetrieveManyKeyValues(YourObjectClass record) {
        return Arrays.asList(
            record.getCriteriaValue()
            // ... Add more criteria values as needed
        );
    }


    // Define SQL statement to retrieve all records in the table
    @Override
    protected String getRetrieveAllSQL() {
        return (
            "SELECT " +
                COLUMN_EXAMPLE1 + ", " +
                COLUMN_EXAMPLE2 +
                // ... Add more columns as needed
            " FROM " +
                TABLE_NAME
        );
    }


    // Define SQL statement to delete a record based on primary key(s)
    @Override
    protected String getDeleteSQL() {
        return (
            "DELETE FROM " +
                TABLE_NAME +
            " WHERE " +
                COLUMN_EXAMPLE1 + " = ?" // Your primary key column(s)
                // ... If multiple primary keys, add more conditions
        );
    }


    // Convert a ResultSet row (SQL query output) into a YourObjectClass object
    @Override
    protected YourObjectClass mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new YourObjectClass(
            rs.getYourType(COLUMN_EXAMPLE1),
            rs.getInt(COLUMN_EXAMPLE2)
            // ... Convert more columns from the ResultSet
        );
    }
}
