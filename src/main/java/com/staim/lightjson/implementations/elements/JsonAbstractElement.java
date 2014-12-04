package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonType;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * Abstract Class for JsonElement Implementations
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
abstract class JsonAbstractElement implements JsonElement {
    //////////////////////////// INTERFACE /////////////////////////////////////////////////////////////////////////////

    //// Getters ////

    @Override public Number getNumberData() { return null; }
    @Override public String getStringData() { return null; }
    @Override public Boolean getBooleanData() { return null; }
    @Override public Date getDateData() {return null; }

    @Override
    public JsonElement get(int index) throws JsonException {
        throw new JsonException("Array Operation error: JSON Element is not Array");
    }

    @Override
    public JsonElement get(String name) throws JsonException {
        throw new JsonException("Object Operation error: JSON Element is not Object");
    }

    @Override
    public Iterator<JsonElement> iterator() throws JsonException {
        throw new JsonException("Iteration not supported for this JsonElement");
    }

    @Override
    public int size() throws JsonException {
        throw new JsonException("Iteration not supported for this JsonElement");
    }

    //// Adders ////

    @Override
    public void add(Object object) throws JsonException {
        throw new JsonException("Array Operation error: JSON Element is not Array");
    }

    @Override
    public void add(String name, Object object) throws JsonException {
        throw new JsonException("Object Operation error: JSON Element is not Object");
    }

    //////////////////////////// SERIALIZATION /////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override public String toString() { return this.serialize(); }

    @SuppressWarnings("unchecked")
    @Override
    public String serialize() {
        switch (getType()) {
            case OBJECT:
                String resObject = "";
                Map<String, JsonElement> objectData = (Map<String, JsonElement>) getData();
                for (Map.Entry<String, JsonElement> entry : objectData.entrySet()) {
                    if (!resObject.isEmpty()) resObject += ", ";
                    final String key = entry.getKey();
                    final JsonElement value = entry.getValue();
                    resObject += String.format("\"%1$s\" : %2$s", key, (value != null ? value.serialize() : "\"null\""));
                }
                return "{" + resObject + "}";
            case ARRAY:
                String resArray = "";
                Collection<JsonElement> arrayData = (Collection<JsonElement>) getData();
                for (JsonElement anArrayData : arrayData) {
                    if (!resArray.isEmpty()) resArray += ", ";
                    resArray += (anArrayData).serialize();
                }
                return "[" + resArray + "]";
            case BOOLEAN:
                return this.getBooleanData() ? "true" : "false";
            case NUMBER:
                String res = "";
                Number numberData = getNumberData();
                if (numberData instanceof Byte) res += numberData.byteValue();
                else if (numberData instanceof Short) res += numberData.shortValue();
                else if (numberData instanceof Integer) res += numberData.intValue();
                else if (numberData instanceof Long) res += numberData.longValue();
                else if (numberData instanceof Float) res += numberData.floatValue();
                else if (numberData instanceof Double) res += numberData.doubleValue();
                return res;
            case STRING:
                return "\"" + stringForJSON(this.getStringData()) + "\"";
            case DATE:
                @SuppressWarnings("SpellCheckingInspection")
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                return "\"" + dateFormat.format(this.getDateData()).replaceAll("\\\\", "\\\\\\\\") + "\"";
            case NULL:
            default:
                return "null";
        }
    }

    /**
     * Format String for JSON Serialization
     * @param input - string to format
     * @return result String
     */
    private static String stringForJSON(String input) {
        if (input == null || input.isEmpty()) return "";

        final int len = input.length();
        final StringBuilder result = new StringBuilder(len + len / 4);
        final StringCharacterIterator iterator = new StringCharacterIterator(input);
        char ch = iterator.current();
        while (ch != CharacterIterator.DONE) {
            if (ch == '\n') {
                result.append("\\n");
            } else if (ch == '\r') {
                result.append("\\r");
            } else if (ch == '\'') {
                result.append("\\\'");
            } else if (ch == '"') {
                result.append("\\\"");
            } else {
                result.append(ch);
            }
            ch = iterator.next();
        }
        return result.toString();
    }

    /**
     * Get Json Element from plain object
     *
     * @param object - Object to create from
     * @return JSON Element
     * @throws com.staim.lightjson.JsonException
     */
    @SuppressWarnings("unchecked")
    protected JsonElement getJsonElementFromObject(Object object) throws JsonException {
        if (object == null)                return new JsonNull();
        if (object instanceof JsonElement) return (JsonElement)object;
        if (object instanceof String)      return new JsonString((String)object);
        if (object instanceof Number)      return new JsonNumber((Number)object);
        if (object instanceof Boolean)     return new JsonBoolean((Boolean)object);
        if (object instanceof Map)         return new JsonObject((Map<String, JsonElement>) object);
        if (object instanceof Collection)  return new JsonArray((Collection<JsonElement>)object);
        if (object instanceof Date)        return new JsonDate((Date)object);
        return new JsonNull();
    }

    /**
     * Get JsonElement Type by name of the given Object Class
     *
     * @param object - object to get JSON Type from
     * @return JsonType:
     *         Map<String, JsonElement> 	- "OBJECT",
     *         List<JsonElement>			- "ARRAY",
     *         Boolean 						- "BOOLEAN",
     *         Number 						- "NUMBER",
     *         String 						- "STRING",
     *         other 						- "NULL"
     */
    protected JsonType getType(Object object) {
        if (object == null) return JsonType.NULL;
        if (object instanceof String) return JsonType.STRING;
        if (object instanceof Number) return JsonType.NUMBER;
        if (object instanceof Boolean) return JsonType.BOOLEAN;
        if (object instanceof List) return JsonType.ARRAY;
        if (object instanceof Map) return JsonType.OBJECT;
        if (object instanceof Date)  return JsonType.DATE;
        return JsonType.NULL;
    }
}
