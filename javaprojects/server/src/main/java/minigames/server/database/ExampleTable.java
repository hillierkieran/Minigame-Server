package minigames.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;


/**
 * Template for CRUD operations on a table.
 * Copy class and replace placeholders before use.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 * See `server.highscore.DerbyHighScoreStorage` for how I use these tables
 */
public class ExampleTable extends DatabaseTable<ExampleRecord> {

    // Constants representing table name and column names
    private static final String TABLE_NAME      = "EXAMPLE_TABLE";
    private static final String COLUMN_EXAMPLE1 = "example_column1";
    private static final String COLUMN_EXAMPLE2 = "example_column2";
    // ... Add more columns as needed

    // Optional reference to another table(s) if using foreign keys
    private DatabaseTable referencedTable;


// Constructors

    /**
     * Constructor to initialise the table structure using default database.
     */
    public ExampleTable() {
        super(TABLE_NAME);
    }

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
     * FOR TESTS
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


// Getters

    public String getColumnExample1() { return COLUMN_EXAMPLE1; }
    public String getColumnExample2() { return COLUMN_EXAMPLE2; }
    // ... Add more columns as required

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList(
            COLUMN_EXAMPLE1,
            COLUMN_EXAMPLE2
            // ... Add more columns as required
        );
    }

    @Override
    public List<String> getKeyColumnNames() {
        return Arrays.asList(
            COLUMN_EXAMPLE1
            // ... Add more columns if using a compensate key structure
        );
    }


// SQL statement building methods

    /** 
     * Returns the primary key values for the provided object.
     * 
     * @param obj Object to retrieve primary key values from.
     * @return List of primary key values.
     * */
    @Override
    protected List<Object> getPrimaryKeyValues(Object obj) {
        return Arrays.asList(
            ((ExampleRecord) obj).getKey() // I'm using a record object but you could directly pass a string to int
            // ... Add more values if using a compensate key structure
        );
    }

    /**
     * Returns the table creation SQL statement with columns and primary key(s)
     * 
     * @return SQL string to create the table.
     */
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
                    COLUMN_EXAMPLE1 +
                    // ... Add more key columns if using a compensate key structure
                ")" +
            ")"
        );
    }

    /**
     * Returns the insert record SQL statement
     * 
     * @return SQL string for insert operations.
     */
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

    /**
     * Returns the values to be inserted into the table for a record.
     * 
     * @param record Record to get values from.
     * @return List of values for insert operations.
     */
    @Override
    protected List<Object> getInsertValues(ExampleRecord record) {
        return Arrays.asList(
            record.getKey(),
            record.getValue()
            // ... Add more values as needed
        );
    }

    /**
     * Returns the update record SQL statement
     * 
     * @return SQL string for update operations.
     */
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

    /**
     * Returns the values to be modified during an update operation (don't include keys).
     * 
     * @param record Record to get values from.
     * @return List of values for update operations.
     */
    @Override
    protected List<Object> getUpdateSetValues(ExampleRecord record) {
        return Arrays.asList(
            record.getValue()
            // ... Add more values to update
        );
    }

    /**
     * Returns the SQL query to retrieve one record 
     * 
     * @return SQL string to retrieve a single record.
     */
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

    /**
     * Returns the SQL query to retrieve multiple records
     * 
     * @return SQL string to retrieve multiple records based on a criterion.
     */
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

    /** 
     * Returns the key values used for filtering in retrieveMany operations.
     * 
     * @param filterCriteria Criteria to filter records.
     * @return List of key values for filtering. 
     */
    @Override
    protected List<Object> getRetrieveManyKeyValues(Object filterCriteria) {
        return Arrays.asList(
            ((ExampleRecord) filterCriteria).getValue() // Whatever criteria you want
            // ... Add more criteria values as needed
        );
    }

    /**
     * Returns the SQL query to retrieve all records from the table
     * 
     * @return SQL string to retrieve all records.
     */
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

    /**
     * Returns the delete record SQL statement
     * 
     * @return SQL string for delete operations.
     */
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


// Result mapper

    /** 
     * Maps a row returned from a query to an entity of type T (your record object class).
     * 
     * @param rs ResultSet row to map.
     * @return Mapped entity of type T.
     * @throws SQLException If mapping fails.
     */
    @Override
    protected ExampleRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new ExampleRecord(
            rs.getString(COLUMN_EXAMPLE1),
            rs.getInt(COLUMN_EXAMPLE2)
            // ... Convert more columns from the ResultSet as required
        );
    }
}
