package com.staim.lightjson;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON Parser\Serializer
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class JsonElement {
    private static final Logger log = Logger.getLogger(JsonElement.class.toString());

    /**
     * Type of JSON Element
     */
    public enum JSONType {
        OBJECT,
        ARRAY,
        STRING,
        NUMBER,
        BOOLEAN,
        DATE, // serialization only
        NULL,
        ANY  // serialization only
    }

    private JSONType type = JSONType.NULL;

    // Storage
    private Map<String, JsonElement> objectData;
    private List<JsonElement> arrayData;
    private Number numberData;
    private String stringData;
    private Boolean booleanData;
    private Date dateData;

    /**
     * Parse JSON Element from JSON String
     *
     * @param json - JSON String
     * @throws JsonException
     */
    public JsonElement(String json) throws JsonException {
        final String res = json.trim();

        // Try trivial variants
        if ("{}".equals(res) || "{ }".equals(res)) { // Empty Object
            this.type = JSONType.OBJECT;
            objectData = new HashMap<>();
            return;
        }
        if ("[]".equals(res) || "[ ]".equals(res)) { // Empty Array
            this.type = JSONType.ARRAY;
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
            log.log(Level.SEVERE, "JSON Parse error", ex);
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
        this.type = JSONType.OBJECT;
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
        this.type = JSONType.ARRAY;
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
        this.type = JSONType.STRING;
        this.stringData = json;
    }

    private void parseFloat(String json) {
        this.type = JSONType.NUMBER;
        this.numberData = Float.parseFloat(json);
    }

    /**
     * Parse Boolean, Number, Null or Undefined Elements
     *
     * @param json - JSON String
     */
    private void parseOther(String json) {
        if (json.equals("true")) {
            this.type = JSONType.BOOLEAN;
            this.booleanData = true;
            return;
        }
        if (json.equals("false")) {
            this.type = JSONType.BOOLEAN;
            this.booleanData = false;
            return;
        }
        if (json.equals("null") || json.equals("NULL")) {
            this.type = JSONType.NULL;
            return;
        }
        this.type = JSONType.NUMBER;
        this.numberData = Long.parseLong(json);
    }

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
     * Create empty JSON Element of given type
     *
     * @param type - Type of new JSON Element
     */
    public JsonElement(JSONType type) {
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
     *
     * @param type  - Type of new JSON Element
     * @param value - Initializer for JsonElement (HashMap<String, JsonElement> for "OBJECT", List<JsonElement> for "ARRAY", Boolean for "BOOLEAN", Long for "NUMBER", String for "STRING", Ignored for "NULL")
     * @throws JsonException
     */
    @SuppressWarnings("unchecked")
    public JsonElement(JSONType type, Object value) throws JsonException {
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
        for (Object obj : objList) {
            if (obj instanceof JsonElement) {
                final JsonElement jsonElement = (JsonElement) obj;
                addElement(jsonElement);
            } else if (obj instanceof JsonSerializable) {
                final JsonElement jsonElement = ((JsonSerializable) obj).toJSON();
                addElement(jsonElement);
            } else {
                add(obj);
            }
        }
    }

    // Getters and Setters

    /**
     * @return the type
     */
    public JSONType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(JSONType type) {
        this.type = type;
    }

    /**
     * @return the objectData
     */
    public Map<String, JsonElement> getObjectData() {
        return objectData;
    }

    /**
     * @param objectData the objectData to set
     */
    public void setObjectData(Map<String, JsonElement> objectData) {
        this.objectData = objectData;
    }

    /**
     * @return the arrayData
     */
    public List<JsonElement> getArrayData() {
        return arrayData;
    }

    /**
     * @param arrayData the arrayData to set
     */
    public void setArrayData(List<JsonElement> arrayData) {
        this.arrayData = arrayData;
    }

    /**
     * @return the numberData
     */
    public Number getNumberData() {
        return numberData;
    }

    /**
     * @param numberData the numberData to set
     */
    public void setNumberData(Number numberData) {
        this.numberData = numberData;
    }

    /**
     * @return the stringData
     */
    public String getStringData() {
        return stringData;
    }

    /**
     * @param stringData the stringData to set
     */
    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    /**
     * @return the booleanData
     */
    public Boolean getBooleanData() {
        return booleanData;
    }

    /**
     * @param booleanData the booleanData to set
     */
    public void setBooleanData(Boolean booleanData) {
        this.booleanData = booleanData;
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
     * Get Sub Element of parsed JSON Object
     *
     * @param name - name of element
     * @return SubElement
     */
    public JsonElement getSub(String name) {
        if (this.type != JSONType.OBJECT) return null;
        return this.objectData.get(name);
    }

    /**
     * Get Sub Element of parsed JSON Array
     *
     * @param index - Index of Array Element
     * @return SubElement
     */
    public JsonElement getSub(int index) {
        if (this.type != JSONType.ARRAY) return null;
        return this.arrayData.get(index);
    }


    /**
     * Get String Parameter of JSON Object Element
     *
     * @param name - name of Sub Element
     * @return String value if JSON Element is OBJECT and has String Sub Element with given name, returns null otherwise
     */
    private String getParameter(String name) {
        if (this.type != JSONType.OBJECT) return null;
        final JsonElement element = this.objectData.get(name);
        if (element == null) return null;
        return element.getStringData();
    }

    /**
     * Try to get JSON Object Sub Element of type String
     *
     * @param name - Parameter Name
     * @return String value
     * @throws JsonException
     */
    public String getStringElement(String name) throws JsonException {
        final Logger log = Logger.getLogger("getStringElement");
        String parameter = null;
        if (this.type == JSONType.OBJECT) {
            final JsonElement element = this.objectData.get(name);
            if (element != null)
                parameter = element.getStringData();
        }
        if (parameter == null) {
            final String error_message = "Required parameter is missing: " + name;
            log.severe(error_message);
            throw new JsonException(error_message);
        }
        return parameter;
    }

    /**
     * Try to get JSON Object Sub Element of type Number
     *
     * @param name - Parameter Name
     * @return Number value
     * @throws JsonException
     */
    public int getNumberElement(String name) throws JsonException {
        final Logger log = Logger.getLogger("getNumberElement");
        Integer parameter = null;
        if (this.type == JSONType.OBJECT) {
            final JsonElement element = this.objectData.get(name);
            if (element != null)
                parameter = element.getNumberData().intValue();
        }
        if (parameter == null) {
            final String error_message = "Required parameter is missing: " + name;
            log.severe(error_message);
            throw new JsonException(error_message);
        }
        return parameter;
    }

    /**
     * Try to get JSON Object Sub Element of type Boolean
     *
     * @param name - Parameter Name
     * @return Boolean value
     * @throws JsonException
     */
    public boolean getBooleanElement(String name) throws JsonException {
        final Logger log = Logger.getLogger("getBooleanElement");
        Boolean parameter = null;
        if (this.type == JSONType.OBJECT) {
            final JsonElement element = this.objectData.get(name);
            if (element != null)
                parameter = element.getBooleanData();
        }
        if (parameter == null) {
            final String error_message = "Required parameter is missing: " + name;
            log.severe(error_message);
            throw new JsonException(error_message);
        }
        return parameter;
    }


    /**
     * Try to get OBJECT from JSON Object Parameter
     *
     * @param name - Parameter Name
     * @return JsonElement value
     * @throws JsonException
     */
    public JsonElement getParameterObject(String name) throws JsonException {
        final Logger log = Logger.getLogger("getParameterObject");
        final JsonElement parameter = getSub(name);
        if (parameter == null) {
            final String error_message = "Required parameter is missing: " + name;
            log.severe(error_message);
            throw new JsonException(error_message);
        }
        return parameter;
    }

    /**
     * Add SubElement to JSON Array
     *
     * @param element - JsonElement to add
     * @throws JsonException
     */
    public void addElement(JsonElement element) throws JsonException {
        if (type != JSONType.ARRAY) throw new JsonException("addElement error: JSON Element is not ARRAY");
        if (arrayData == null) throw new JsonException("addElement error: JSON Array data is null");
        arrayData.add(element);
    }

    /**
     * Add SubElement to JSON Object
     *
     * @param element - JsonElement to add
     * @param name    - name of element in OBJECT
     * @throws JsonException
     */
    public void addElement(JsonElement element, String name) throws JsonException {
        if (type != JSONType.OBJECT) throw new JsonException("addElement error: JSON Element is not OBJECT");
        if (objectData == null) throw new JsonException("addElement error: JSON Object data is null");
        if (element == null) objectData.put(name, new JsonElement(JSONType.NULL));
        else objectData.put(name, element);
    }

    /**
     * Add SubElement to JSON Array
     *
     * @param element - JsonElement to add
     * @throws JsonException
     */
    public void addElement(JsonSerializable element) throws JsonException {
        if (element != null) addElement(element.toJSON());
        else addElement(new JsonElement(JSONType.NULL));
    }

    /**
     * Add SubElement to JSON Object
     *
     * @param element - JsonElement to add
     * @param name    - name of element in OBJECT
     * @throws JsonException
     */
    public void addElement(JsonSerializable element, String name) throws JsonException {
        if (element != null) addElement(element.toJSON(), name);
        else addElement(new JsonElement(JSONType.NULL), name);
    }

    /**
     * Add new SubElement to JSON Array with given data
     *
     * @param elementData - data to add to ARRAY. Types:
     *                    Map<String, JsonElement> 		- "OBJECT",
     *                    List<JsonElement>				- "ARRAY",
     *                    Boolean 						- "BOOLEAN",
     *                    Integer 						- "NUMBER"
     *                    Long 							- "NUMBER",
     *                    String 							- "STRING",
     *                    other 							- "NULL"
     * @throws JsonException
     */
    public void add(Object elementData) throws JsonException {
        if (type != JSONType.ARRAY) throw new JsonException("addElement error: JSON Element is not ARRAY");
        if (arrayData == null) throw new JsonException("addElement error: JSON Array data is null");
        if (elementData == null) {
            arrayData.add(new JsonElement(JSONType.NULL));
            return;
        }
        final JsonElement element = new JsonElement(getType(elementData), elementData);
        arrayData.add(element);
    }

    /**
     * Add new SubElement to JSON Object with given data
     *
     * @param elementData - data to add to ARRAY. Types:
     *                    Map<String, JsonElement> 		- "OBJECT",
     *                    List<JsonElement>				- "ARRAY",
     *                    Boolean 						- "BOOLEAN",
     *                    Integer 						- "NUMBER"
     *                    Long 							- "NUMBER",
     *                    String 							- "STRING",
     *                    other 							- "NULL"
     * @param name        - name of element in OBJECT
     * @throws JsonException
     */
    public void add(Object elementData, String name) throws JsonException {
        if (type != JSONType.OBJECT) throw new JsonException("addElement error: JSON Element is not OBJECT");
        if (objectData == null) throw new JsonException("addElement error: JSON Object data is null");
        if (elementData == null) {
            objectData.put(name, new JsonElement(JSONType.NULL));
            return;
        }
        final JsonElement element = new JsonElement(getType(elementData), elementData);
        objectData.put(name, element);
    }

    /**
     * Get JsonElement Type by name of the given Object Class
     *
     * @param className - name of the class returned by Object.getClass().getSimpleName()
     * @return JSONType:
     *         Map<String, JsonElement> 		- "OBJECT",
     *         List<JsonElement>				- "ARRAY",
     *         Boolean 						- "BOOLEAN",
     *         Integer 						- "NUMBER"
     *         Long 							- "NUMBER",
     *         String 							- "STRING",
     *         other 							- "NULL"
     */
/*    private JSONType getType(String className) {
        if (className.equals("String")) return JSONType.STRING;
        if (className.equals("Integer") || className.equals("Long")) return JSONType.NUMBER;
        if (className.equals("Float")) return JSONType.numberFloat;
        if (className.equals("Boolean")) return JSONType.BOOLEAN;
        if (className.equals("List") || className.equals("ArrayList")) return JSONType.ARRAY;
        if (className.equals("Map") || className.equals("HashMap")) return JSONType.OBJECT;
        if (className.equals("Date")) return JSONType.DATE;
        return JSONType.NULL;

    }*/

    /**
     * Get JsonElement Type by name of the given Object Class
     *
     * @param object - object to get JSON Type from
     * @return JSONType:
     *         Map<String, JsonElement> 		- "OBJECT",
     *         List<JsonElement>				- "ARRAY",
     *         Boolean 						- "BOOLEAN",
     *         Integer 						- "NUMBER"
     *         Long 							- "NUMBER",
     *         String 							- "STRING",
     *         other 							- "NULL"
     */
    private JSONType getType(Object object) {
        if (object instanceof String) return JSONType.STRING;
        if (object instanceof Number) return JSONType.NUMBER;
        if (object instanceof Boolean) return JSONType.BOOLEAN;
        if (object instanceof List) return JSONType.ARRAY;
        if (object instanceof Map) return JSONType.OBJECT;
        if (object instanceof Date)  return JSONType.DATE;
        return JSONType.NULL;
    }

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

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        return this.serialize();
    }

    public static String stringForJSON(String input) {
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
}
