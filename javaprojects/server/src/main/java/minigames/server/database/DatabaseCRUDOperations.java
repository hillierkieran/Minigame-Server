package minigames.server.database;

import java.util.List;


/**
 * Interface for generic database table CRUD operations.
 *
 * @param <T> Type of records in the table.
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public interface DatabaseCRUDOperations<T> {


    /**
     * Inserts a new record.
     *
     * @param record Record to insert.
     */
    public void create(T record);


    /**
     * Modifies an existing record.
     *
     * @param record Updated record.
     */
    public void update(T record);


    /**
     * Fetches a single record based on criteria.
     *
     * @param filterCriteria Criteria to filter record.
     * @return Retrieved record or null.
     */
    public T retrieveOne(Object filterCriteria);


    /**
     * Fetches multiple records based on criteria.
     *
     * @param filterCriteria Criteria for retrieval.
     * @return List of retrieved records.
     */
    public List<T> retrieveMany(Object filterCriteria);


    /**
     * Fetches all records.
     *
     * @return All records.
     */
    public List<T> retrieveAll();


    /**
     * Removes a specific record.
     *
     * @param record Record to remove.
     */
    public void delete(T record);
}
