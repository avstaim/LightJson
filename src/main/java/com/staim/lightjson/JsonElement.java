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
     * @return the type
     */
    JsonType getType();

    /**
     * @return the Number Data
     */
    Number getNumberData();

    /**
     * @return the String Data
     */
    String getStringData();

    /**
     * @return the BooleanData
     */
    Boolean getBooleanData();

    /**
     * @return the Date Data
     */
    Date getDateData();

    /**
     * Get JsonElement contents as Object
     *
     * @return Object
     */
    Object getData();

    /**
     * Get sub element of Array-type Json Element
     * @param index - index of sub element
     * @return Json Element
     * @throws com.staim.lightjson.JsonException
     */
    JsonElement get(int index) throws JsonException;

    /**
     * Get sub element of Object-type Json Element
     * @param name - name of sub element
     * @return Json Element
     * @throws com.staim.lightjson.JsonException
     */
    JsonElement get(String name) throws JsonException;

    Iterator<JsonElement> iterator() throws JsonException;

    int size() throws JsonException;

    /**
     * Add Java Object to JSON Array
     * @param object - object to add
     * @throws com.staim.lightjson.JsonException
     */
    void add(Object object) throws JsonException;

    /**
     * Add Java Object to JSON Object (associative array)
     * @param name - name to associate object with
     * @param object - object to add
     * @throws com.staim.lightjson.JsonException
     */
    void add(String name, Object object) throws JsonException;

    /* (non-Javadoc)
        * @see java.lang.Object#toString()
        */
    @Override
    String toString();

    /**
     * Serialize JSON Element to JSONString
     *
     * @return JSON String
     */
    String serialize();
}
