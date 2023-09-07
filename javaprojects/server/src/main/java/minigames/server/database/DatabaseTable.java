package minigames.server.database;

import java.util.List;


public interface DatabaseTable<T> {


    // Ensure that the table exists in the database
    public void ensureTableExists();


    // Create a new record
    public void create(T record);


    // Update an existing record
    public void update(T record);


    // Retrieve a single record
    public T retrieveOne(Object key);


    // Retrieve multiple records based on a criteria
    public List<T> retrieveMany(Object criteria);


    // Retrieve all records
    public List<T> retrieveAll();


    // Delete a record
    public void delete(T record);
}
