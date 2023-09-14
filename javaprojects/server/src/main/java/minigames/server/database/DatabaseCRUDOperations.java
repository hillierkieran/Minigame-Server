package minigames.server.database;

import java.util.List;


/**
 * Represents a generic database table with basic CRUD
 * (Create, Retrieve, Update, Delete) operations.
 * Specific implementations for various databases and
 * table structures can extend this interface.
 *
 * @param <T> The type of records stored in the table.
 *
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public interface DatabaseCRUDOperations<T> {


    /**
     * Inserts a new record into the table.
     *
     * @param record The record to be created.
     */
    public void create(T record);


    /**
     * Updates an existing record in the table.
     *
     * @param record The record with updated details.
     */
    public void update(T record);


    /**
     * Retrieves a single record from the table based on specific criteria.
     *
     * @param record Used to get the criteria or key for the record retrieval.
     * @return The retrieved record, or null if not found.
     */
    public T retrieveOne(T record);


    /**
     * Retrieves multiple records from the table based on a given filter or criteria.
     *
     * @param filterCriteria The criteria to filter the retrieved records.
     * @return A list of retrieved records, empty list if none found.
     */
    public List<T> retrieveMany(Object filterCriteria);


    /**
     * Retrieves all records from the table.
     *
     * @return A list of all records in the table.
     */
    public List<T> retrieveAll();


    /**
     * Deletes a specific record from the table.
     *
     * @param record The record to be deleted.
     */
    public void delete(T record);
}
