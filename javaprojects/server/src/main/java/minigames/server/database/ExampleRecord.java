package minigames.server.database;

import java.util.Objects;
import java.util.Random;


/**
 * Represents an example database record for demonstration purposes.
 * Contains a key-value pair but you could add whatever you like.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class ExampleRecord {

    private String key;
    private int value;


    /**
     * Constructs a example record with a random key and value.
     */
    public ExampleRecord() {
        this.key = getRandomString(4);
        this.value = getRandomInt(4);
    }


    /**
     * Constructs an example record with the given key and value.
     *
     * @param key The key of the record.
     * @param value The value associated with the key.
     */
    public ExampleRecord(String key, int value) {
        this.key = key;
        this.value = value;
    }


    // Getters
    public String getKey() { return key; }
    public int getValue() { return value; }


    // Setters
    public void setKey(String key) { this.key = key;}
    public void setValue(int value) { this.value = value; }


    // Get random string
    private String getRandomString() {
        return getRandomString(255);
    }

    private String getRandomString(int length) {
        String characters = 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }

        return result.toString();
    }

    // Get random int
    private int getRandomInt() {
        return getRandomInt(Integer.MAX_VALUE);
    }

    private int getRandomInt(int length) {
        Random random = new Random();
        int maxValue = (int) Math.pow(10, length) - 1;
        return random.nextInt(maxValue + 1);
    }

    @Override
    public String toString() {
        return (
            "ExampleRecord{" +
            "key='" + key +
            "', value=" + value +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleRecord that = (ExampleRecord) o;
        return value == that.getValue() && key.equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}