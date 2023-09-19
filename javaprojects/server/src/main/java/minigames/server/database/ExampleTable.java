package minigames.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;


/**
 * This class provides a fill-in-the-blanks template implementations of the CRUD
 * (Create, Retrieve, Update, Delete) operations for the specified table.
 * 
 * Please copy this file, replace placeholders and fill blank areas with your information before using.
 * 
 * Only call the methods listed in `DatabaseCRUDOperations`.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class ExampleTable extends DatabaseTable<ExampleRecord> {

    // Constants representing table name and column names
    private static final String TABLE_NAME = "your_table_name_here";
    private static final String COLUMN_EXAMPLE1 = "example_column1";
    private static final String COLUMN_EXAMPLE2 = "example_column2";
    // ... Add more columns as needed

    // Optional reference to another table(s) if using foreign keys
    private DatabaseTable referencedTable;


    /**
     * Optional constructor to initialise the table structure with reference
     * to another table so you can use foreign keys.
     *
     * @param referencedTable The reference table object.
     */
    public ExampleTable(DatabaseTable referencedTable) {
        this();
        this.referencedTable = referencedTable;
    }


    /**
     * Optional constructor to initialise the table structure using a defined database.
     *
     * @param database The defined database object.
     */
    public ExampleTable(Database database) {
        super(database, TABLE_NAME);
    }


    /**
     * FOR TESTS
     * Optional constructor to initialise the table structure using a defined database and name.
     *
     * @param database The defined database object.
     * @param tableName The defined table name.
     */
    public ExampleTable(Database database, String tableName) {
        super(database, tableName);
    }


    /**
     * Constructor to initialise the table structure using default database.
     */
    public ExampleTable() {
        super(TABLE_NAME);
    }


    // Simple getters for the column names, to be used elsewhere if needed
    public String getColumnExample1() { return COLUMN_EXAMPLE1; }
    public String getColumnExample2() { return COLUMN_EXAMPLE2; }
    // ... Add more getters for other columns


    // Return a list containing the value(s) of the primary key or compensate keys
    @Override
    protected List<Object> getPrimaryKeyValues(Object record) {
        return Arrays.asList(
            ((ExampleRecord) record).getKey()
            // ... Add more values if using a compensate key structure
        );
    }


    // Define table creation SQL with columns and primary key(s)
    @Override
    protected String getTableCreationSQL() {
        return (
            "CREATE TABLE " + 
                getTableName() +
            " (" +
                COLUMN_EXAMPLE1 + " VARCHAR(255), " +
                COLUMN_EXAMPLE2 + " INT, " +
                // ... Add more columns and their data types
                "PRIMARY KEY (" +
                    COLUMN_EXAMPLE1 +  // Your primary key column(s)
                ")" +
            ")"
        );
    }


    // Define SQL statement to insert a new record into the table
    @Override
    protected String getInsertSQL() {
        return (
            "INSERT INTO " + 
                getTableName() + 
            " (" +
                COLUMN_EXAMPLE1 + ", " +
                COLUMN_EXAMPLE2 +
                // ... Add more columns as needed
            ") VALUES (?, ?)"
            // Ensure the number of '?' matches the number of columns
        );
    }


    // Return a list containing the values to be inserted
    @Override
    protected List<Object> getInsertValues(ExampleRecord record) {
        return Arrays.asList(
            record.getKey(),
            record.getValue()
            // ... Add more values as needed
        );
    }


    // Define SQL statement to update an existing record based on primary key(s)
    @Override
    protected String getUpdateSQL() {
        return (
            "UPDATE " + 
                getTableName() + 
            " SET " +
                COLUMN_EXAMPLE2 + " = ? " + // Value(s) to be set
                // ... Add more columns as needed
            " WHERE " +
                COLUMN_EXAMPLE1 + " = ?" // Your primary key column(s)
                // ... If multiple primary keys, add more conditions
        );
    }


    // Return a list containing the values to be updated (don't include keys)
    @Override
    protected List<Object> getUpdateSetValues(ExampleRecord record) {
        return Arrays.asList(
            record.getValue()
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
                getTableName() + 
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
                getTableName() + 
            " WHERE " +
                COLUMN_EXAMPLE2 + " = ?" // Your criteria column(s)
        );
    }


    // Return a list containing the criteria values to match for retrieval
    @Override
    protected List<Object> getRetrieveManyKeyValues(Object filterCriteria) {
        return Arrays.asList(
            ((ExampleRecord) filterCriteria).getValue() // Whatever criteria you want
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
                getTableName()
        );
    }


    // Define SQL statement to delete a record based on primary key(s)
    @Override
    protected String getDeleteSQL() {
        return (
            "DELETE FROM " +
                getTableName() +
            " WHERE " +
                COLUMN_EXAMPLE1 + " = ?" // Your primary key column(s)
                // ... If multiple primary keys, add more conditions
        );
    }


    // Convert a ResultSet row (SQL query output) into a ExampleRecord object
    @Override
    protected ExampleRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new ExampleRecord(
            rs.getString(COLUMN_EXAMPLE1),
            rs.getInt(COLUMN_EXAMPLE2)
            // ... Convert more columns from the ResultSet
        );
    }
}
