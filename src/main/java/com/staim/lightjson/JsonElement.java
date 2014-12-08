package com.staim.lightjson;

import java.util.Date;
import java.util.Iterator;

/**
 * Json Element for Parsing and Serialization
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonElement {
    /**
     * Get Type of Json Element
     *
     * @return the type
     */
    JsonType getType();

    /**
     * @deprecated Use getData() instead
     * @return the Number Data
     */
    @Deprecated
    Number getNumberData();

    /**
     * @deprecated Use getData() instead
     * @return the String Data
     */
    @Deprecated
    String getStringData();

    /**
     * @deprecated Use getData() instead
     * @return the BooleanData
     */
    @Deprecated
    Boolean getBooleanData();

    /**
     * @deprecated Use getData() instead
     * @return the Date Data
     */
    @Deprecated
    Date getDateData();

    /**
     * Get JsonElement contents
     *
     * @return Generified Object
     */
    <T> T getData();

    /**
     * Get sub element of Array-type Json Element
     * @param index - index of sub element
     * @return Json Element
     * @throws JsonException
     */
    JsonElement get(int index) throws JsonException;

    /**
     * Get sub element of Object-type Json Element
     * @param name - name of sub element
     * @return Json Element
     * @throws JsonException
     */
    JsonElement get(String name) throws JsonException;

    /**
     * Get iterator for Objects and Arrays
     * @return iterator
     * @throws JsonException when JsonElement is not JSON Object or Array
     */
    Iterator<JsonElement> iterator() throws JsonException;

    /**
     * Get Object or Array Size
     * @return size of Object or Array
     * @throws JsonException when JsonElement is not JSON Object or Array
     */
    int size() throws JsonException;

    /**
     * Add Java Object to JSON Array
     * @param object - object to add
     * @throws JsonException when JsonElement is not JSON Object
     */
    void add(Object object) throws JsonException;

    /**
     * Add Java Object to JSON Object (associative array)
     * @param name - name to associate object with
     * @param object - object to add
     * @throws JsonException when JsonElement is not JSON Array
     */
    void add(String name, Object object) throws JsonException;

    /**
     * Serialize JSON Element to JSONString
     *
     * @deprecated Use JsonSerializer instead
     * @return JSON String
     */
    @Deprecated
    String serialize();
}
