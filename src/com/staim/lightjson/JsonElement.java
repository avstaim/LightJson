package com.staim.lightjson;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * JSON Parser\Serializer
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class JsonElement {
    private JsonType type = JsonType.NULL;

    // Storage
    private Map<String, JsonElement> objectData;
    private List<JsonElement> arrayData;
    private Number numberData;
    private String stringData;
    private Boolean booleanData;
    private Date dateData;

    //////////////////////////// CONSTRUCTORS //////////////////////////////////////////////////////////////////////////

    /**
     * Json Element constructor, parsing JSON String
     * @param jsonString - String to parse
     * @throws JsonException
     */
    public JsonElement(String jsonString) throws JsonException { parse(jsonString); }

    /**
     * Create empty JSON Element of given type
     * @param type - Type of new JSON Element
     */
    public JsonElement(JsonType type) {
        this.type = type;
        switch (type) {
            case OBJECT:
                objectData = new HashMap<>();
                break;
            case ARRAY:
                arrayData = new ArrayList<>();
                break;
            case BOOLEAN:
                booleanData = false;
                break;
            case NUMBER:
                numberData = 0L;
                break;
            case STRING:
                stringData = "";
                break;
            case DATE:
                dateData = new Date();
            case NULL:
            default:
                /* Do nothing */
        }
    }

    /**
     * Create JSON Element of given type with initializer
     * @param type  - Type of new JSON Element
     * @param value - Initializer for JsonElement (HashMap<String, JsonElement> for "OBJECT", List<JsonElement> for "ARRAY", Boolean for "BOOLEAN", Long for "NUMBER", String for "STRING", Ignored for "NULL")
     * @throws JsonException
     */
    @SuppressWarnings("unchecked")
    public JsonElement(JsonType type, Object value) throws JsonException {
        this.type = type;
        switch (type) {
            case OBJECT:
                objectData = (Map<String, JsonElement>) value;
                break;
            case ARRAY:
                createFromList(value);
                break;
            case BOOLEAN:
                if (!(value instanceof Boolean)) throw new JsonException("Value is not Boolean");
                booleanData = (Boolean) value;
                break;
            case NUMBER:
                if (value instanceof Number) numberData = (Number) value;
                else throw new JsonException("Value is not Number");
                break;
            case STRING:
                if (!(value instanceof String)) throw new JsonException("Value is not String");
                stringData = (String) value;
                break;
            case DATE:
                if (!(value instanceof Date)) throw new JsonException("Value is not Date");
                dateData = (Date) value;
                break;
            case NULL:
            default:
                /* Do nothing */
        }
    }

    //////////////////////////// INTERFACE /////////////////////////////////////////////////////////////////////////////

    //// Getters ////

    /**
     * @return the type
     */
    public JsonType getType() {
        return type;
    }

    /**
     * @return the numberData
     */
    public Number getNumberData() {
        return numberData;
    }

    /**
     * @return the stringData
     */
    public String getStringData() {
        return stringData;
    }

    /**
     * @return the booleanData
     */
    public Boolean getBooleanData() {
        return booleanData;
    }

    /**
     * Get JsonElement contents as Object
     *
     * @return Object
     */
    public Object getData() {
        switch (this.type) {
            case OBJECT:
                return this.objectData;
            case ARRAY:
                return this.arrayData;
            case BOOLEAN:
                return this.booleanData;
            case NUMBER:
                return this.numberData;
            case STRING:
                return this.stringData;
            case DATE:
                return this.dateData;
            case NULL:
            default:
                return null;
        }
    }

    /**
     * Get sub element of Array-type Json Element
     * @param index - index of sub element
     * @return Json Element
     * @throws JsonException
     */
    public JsonElement get(int index) throws JsonException {
        verifyArray();
        return arrayData.get(index);
    }

    /**
     * Get sub element of Object-type Json Element
     * @param name - name of sub element
     * @return Json Element
     * @throws JsonException
     */
    public JsonElement get(String name) throws JsonException {
        verifyObject();
        return objectData.get(name);
    }

    //// Adders ////

    /**
     * Add Java Object to JSON Array
     * @param object - object to add
     * @throws JsonException
     */
    public void add(Object object) throws JsonException {
        verifyArray();
        arrayData.add(getJsonElementFromObject(object));
    }

    /**
     * Add Java Object to JSON Object (associative array)
     * @param name - name to associate object with
     * @param object - object to add
     * @throws JsonException
     */
    public void add(String name, Object object) throws JsonException {
        verifyObject();
        objectData.put(name, getJsonElementFromObject(object));
    }

    //// Common ////

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        return this.serialize();
    }

    //////////////////////////// PARSING ///////////////////////////////////////////////////////////////////////////////

    /**
     * Parse JSON Element from JSON String
     *
     * @param json - JSON String
     * @throws JsonException
     */
    private void parse(String json) throws JsonException {
        final String res = json.trim();

        // Try trivial variants
        if ("{}".equals(res) || "{ }".equals(res)) { // Empty Object
            this.type = JsonType.OBJECT;
            objectData = new HashMap<>();
            return;
        }
        if ("[]".equals(res) || "[ ]".equals(res)) { // Empty Array
            this.type = JsonType.ARRAY;
            arrayData = new ArrayList<>();
            return;
        }

        try {
            final String firstChar = res.substring(0, 1);
            switch (firstChar) {
                case "{":
                    parseObject(res); // JSON Object { key1 : value1, key2 : value2, ... }
                    break;
                case "[":
                    parseArray(res); // JSON Array [value1, value2, ...]
                    break;
                case "\"":
                    parseString(res); // JSON String "some STRING"
                    break;
                default:
                    parseOther(res); // Any other
                    break;
            }

        } catch (JsonException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JsonException("JSON Parse error: " + ex.getMessage());
        }
    }

    /**
     * Parse JSON Object in {}
     *
     * @param json - JSON String
     * @throws JsonException
     */
    private void parseObject(String json) throws JsonException {
        json = removeChar(json, "{", "}");
        this.type = JsonType.OBJECT;
        this.objectData = new HashMap<>();

        final char[] chars = json.trim().toCharArray();

        int parsePosition = 0;
        final int len = chars.length;
        String buf;

        do {
            buf = "";
            while (chars[parsePosition] != ':') {
                buf += chars[parsePosition];
                parsePosition++;
            }

            String name = buf.trim();
            name = removeChar(name, "\"", "\"");

            parsePosition++;
            boolean onQuote = false;
            int bfLevel = 0;
            int brLevel = 0;

            buf = "";
            while (chars[parsePosition] != ',' || onQuote || bfLevel > 0 || brLevel > 0) {
                buf += chars[parsePosition];
                if (chars[parsePosition] == '"' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) onQuote = !onQuote;
                if (!onQuote && chars[parsePosition] == '{' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) bfLevel++;
                if (!onQuote && chars[parsePosition] == '[' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) brLevel++;
                if (!onQuote && chars[parsePosition] == '}' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) bfLevel--;
                if (!onQuote && chars[parsePosition] == ']' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) brLevel--;
                parsePosition++;
                if (parsePosition == len) break;
            }
            final String value = buf.trim();

            final JsonElement subElement = new JsonElement(value);
            this.objectData.put(name, subElement);

            parsePosition++;
        } while (parsePosition < len);
    }

    /**
     * Parse JSON Array in []
     *
     * @param json - JSON String
     * @throws JsonException
     */
    private void parseArray(String json) throws JsonException {
        json = removeChar(json, "[", "]");
        this.type = JsonType.ARRAY;
        this.arrayData = new ArrayList<>();
        final char[] chars = json.trim().toCharArray();

        int parsePosition = 0;
        final int len = chars.length;
        String buf;

        do {
            boolean onQuote = false;
            int bfLevel = 0;
            int brLevel = 0;

            buf = "";
            while (chars[parsePosition] != ',' || onQuote || bfLevel > 0 || brLevel > 0) {
                buf += chars[parsePosition];
                if ((chars[parsePosition] == '"') && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) onQuote = !onQuote;
                if (!onQuote && chars[parsePosition] == '{' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) bfLevel++;
                if (!onQuote && chars[parsePosition] == '[' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) brLevel++;
                if (!onQuote && chars[parsePosition] == '}' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) bfLevel--;
                if (!onQuote && chars[parsePosition] == ']' && (parsePosition == 0 || chars[parsePosition - 1] != '\\')) brLevel--;
                parsePosition++;
                if (parsePosition == len) break;
            }
            final String value = buf.trim();

            final JsonElement subElement = new JsonElement(value);
            this.arrayData.add(subElement);

            parsePosition++;
        } while (parsePosition < len);
    }

    /**
     * Parse JSON String Element
     *
     * @param json - JSON String
     */
    private void parseString(String json) {
        json = removeChar(json, "\"", "\"");
        this.type = JsonType.STRING;
        this.stringData = json;
    }

    private void parseFloat(String json) {
        this.type = JsonType.NUMBER;
        this.numberData = Float.parseFloat(json);
    }

    /**
     * Parse Boolean, Number, Null or Undefined Elements
     *
     * @param json - JSON String
     */
    private void parseOther(String json) {
        if (json.equals("true")) {
            this.type = JsonType.BOOLEAN;
            this.booleanData = true;
            return;
        }
        if (json.equals("false")) {
            this.type = JsonType.BOOLEAN;
            this.booleanData = false;
            return;
        }
        if (json.equals("null") || json.equals("NULL")) {
            this.type = JsonType.NULL;
            return;
        }
        this.type = JsonType.NUMBER;
        this.numberData = Long.parseLong(json);
    }

    //////////////////////////// SERIALIZATION /////////////////////////////////////////////////////////////////////////

    /**
     * Serialize JSON Element to JSONString
     *
     * @return JSON String
     */
    public String serialize() {
        switch (this.type) {
            case OBJECT:
                String resObject = "";
                for (Map.Entry<String, JsonElement> entry : this.objectData.entrySet()) {
                    if (!resObject.isEmpty()) resObject += ", ";
                    final String key = entry.getKey();
                    final JsonElement value = entry.getValue();
                    resObject += String.format("\"%1$s\" : %2$s", key, (value != null ? value.serialize() : "\"null\""));
                }
                return "{" + resObject + "}";
            case ARRAY:
                String resArray = "";
                for (JsonElement anArrayData : this.arrayData) {
                    if (!resArray.isEmpty()) resArray += ", ";
                    resArray += (anArrayData).serialize();
                }
                return "[" + resArray + "]";
            case BOOLEAN:
                return this.booleanData ? "true" : "false";
            case NUMBER:
                String res = "";
                if (this.numberData instanceof Byte) res += this.numberData.byteValue();
                else if (this.numberData instanceof Short) res += this.numberData.shortValue();
                else if (this.numberData instanceof Integer) res += this.numberData.intValue();
                else if (this.numberData instanceof Long) res += this.numberData.longValue();
                else if (this.numberData instanceof Float) res += this.numberData.floatValue();
                else if (this.numberData instanceof Double) res += this.numberData.doubleValue();
                return res;
            case STRING:
                //return "\"" + this.stringData.replaceAll("\\\\", "\\\\\\\\") + "\"";
                return "\"" + stringForJSON(this.stringData) + "\"";
            case DATE:
                @SuppressWarnings("SpellCheckingInspection") final
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                return "\"" + dateFormat.format(this.dateData).replaceAll("\\\\", "\\\\\\\\") + "\"";
            case NULL:
            default:
                return "\"null\"";
        }
    }

    //////////////////////////// SERVICE FUNCTIONS /////////////////////////////////////////////////////////////////////

    /**
     * Remove given characters from the beginning and end of STRING
     *
     * @param str - String
     * @param ch1 - Character to remove from beginning
     * @param ch2 - Character to remove from end
     * @return Trimmed String
     */
    private static String removeChar(String str, String ch1, String ch2) {
        String res = str;
        if (res.substring(0, 1).equals(ch1)) res = res.substring(1);
        if (res.substring(res.length() - 1).equals(ch2)) res = res.substring(0, res.length() - 1);
        return res;
    }

    /**
     * Create JsonElement as Array from java.util.List
     *
     * @param data - java.util.List instance as Object
     * @throws JsonException
     */
    @SuppressWarnings("unchecked")
    private void createFromList(Object data) throws JsonException {
        if (!(data instanceof List)) throw new JsonException("Data is not List");
        final List<Object> objList = (List<Object>) data;
        arrayData = new ArrayList<>();
        for (Object object : objList) add(object);
    }

    /**
     * Get Json Element from plain object
     *
     * @param object - Object to create from
     * @return JSON Element
     * @throws JsonException
     */
    private JsonElement getJsonElementFromObject(Object object) throws JsonException {
        JsonElement jsonElement;
        if (object == null) jsonElement = new JsonElement(JsonType.NULL);
        else if (object instanceof JsonElement) jsonElement = (JsonElement) object;
        else if (object instanceof JsonSerializable) jsonElement = ((JsonSerializable) object).toJSON();
        else jsonElement = new JsonElement(getType(object), object);
        return jsonElement;
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
    private JsonType getType(Object object) {
        if (object instanceof String) return JsonType.STRING;
        if (object instanceof Number) return JsonType.NUMBER;
        if (object instanceof Boolean) return JsonType.BOOLEAN;
        if (object instanceof List) return JsonType.ARRAY;
        if (object instanceof Map) return JsonType.OBJECT;
        if (object instanceof Date)  return JsonType.DATE;
        return JsonType.NULL;
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

    private void verifyArray() throws JsonException {
        if (type != JsonType.ARRAY) throw new JsonException("Array Operation error: JSON Element is not Array");
        if (arrayData == null) throw new JsonException("Array Operation error: JSON Array data is null");
    }

    private void verifyObject() throws JsonException {
        if (type != JsonType.OBJECT) throw new JsonException("Object Operation error: JSON Element is not Object");
        if (objectData == null) throw new JsonException("Object Operation error: JSON Object data is null");
    }
}
