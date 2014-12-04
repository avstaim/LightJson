package com.staim.lightjson.implementations;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonMarshaller;
import com.staim.lightjson.JsonType;
import com.staim.lightjson.annotations.JsonField;
import com.staim.lightjson.annotations.JsonObject;
import com.staim.lightjson.implementations.elements.JsonArray;
import com.staim.lightjson.implementations.elements.JsonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Json Marshaller Implementation
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
@SuppressWarnings("unchecked")
public class MarshallerImpl implements JsonMarshaller {
    private Object _object;

    public MarshallerImpl(Object object) { _object = object; }

    @Override
    public String marshal() throws JsonException {
        return marshal(_object).serialize();
    }

    private static JsonElement marshal(Object object) throws JsonException {
        if (object == null) return new JsonNull();

        Class aClass = object.getClass();
        if (!aClass.isAnnotationPresent(JsonObject.class)) throw new JsonException("Class is not annotated as JsonObject");

        JsonElement jsonElement = new com.staim.lightjson.implementations.elements.JsonObject();

        boolean isAutomaticBinding = ((JsonObject)aClass.getAnnotation(JsonObject.class)).AutomaticBinding();

        try {
            for (Field field : aClass.getDeclaredFields()) {
                JsonType type = null;
                String jsonName = "";
                if (field.isAnnotationPresent(JsonField.class)) {
                    JsonField annotation = field.getAnnotation(JsonField.class);
                    type = annotation.type();
                    jsonName = annotation.name();
                } else if (isAutomaticBinding) {
                    type = JsonType.ANY;
                }
                if (type != null) {
                    if (type == JsonType.ANY) {
                        try { type = getTypeForField(field); }
                        catch (UnsupportedOperationException e) { continue; }
                    }

                    String name = field.getName();

                    if (jsonName.isEmpty()) jsonName = name;

                    Class<?> fieldType = field.getType();
                    field.setAccessible(true);

                    if (field.get(object) == null) {
                        jsonElement.add(name, new JsonNull());
                        continue;
                    }

                    switch (type) {
                        case ANY:
                            // never go here
                            break;
                        case ARRAY:
                            if (fieldType.isArray()) {
                                Class arrayComponentType = fieldType.getComponentType();
                                Object array = field.get(object);
                                jsonElement.add(jsonName, processArray(array, arrayComponentType));
                            } else if (Collection.class.isAssignableFrom(fieldType)) {
                                Collection collection = (Collection)field.get(object);
                                jsonElement.add(jsonName, processCollection(collection));
                            }  else throw new JsonException("Wrong return type");
                            break;
                        case OBJECT:
                            if (Map.class.isAssignableFrom(fieldType)) {
                                Map map = (Map) field.get(object);
                                jsonElement.add(jsonName, processMap(map, field));
                            } else if (fieldType.isAnnotationPresent(JsonObject.class)) {
                                Object subObject = field.get(object);
                                JsonElement subElement = marshal(subObject);
                                jsonElement.add(jsonName, subElement);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case STRING:
                            String string;
                            if (String.class.isAssignableFrom(fieldType)) {
                                string = (String)field.get(object);
                            } else if (Object.class.isAssignableFrom(fieldType)) {
                                string = field.get(object).toString();
                            } else throw new JsonException("Wrong return type");
                            jsonElement.add(jsonName, string);
                            break;
                        case NUMBER:
                            if (Number.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)) {
                                Number number = (Number)field.get(object);
                                jsonElement.add(jsonName, number);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case BOOLEAN:
                            if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                                Boolean bool = (Boolean)field.get(object);
                                jsonElement.add(jsonName, bool);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case DATE:
                            if (Date.class.isAssignableFrom(fieldType)) {
                                Date date = (Date)field.get(object);
                                jsonElement.add(jsonName, date);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case NULL:
                            jsonElement.add(jsonName, new JsonNull());
                            break;
                        case RAW:
                            if (JsonElement.class.isAssignableFrom(fieldType)) {
                                jsonElement.add(jsonName, field.get(object));
                            } else throw new JsonException("Raw fields must be of JsonElement type");
                    }
                }
            }
            return jsonElement;
        } catch (IllegalAccessException e) {
            throw new JsonException("Getting field value failed");
        }
    }

    private static Object getSubObject(Object object) throws JsonException {
        JsonType type;
        try { type = getTypeForClass(object.getClass()); }
        catch (UnsupportedOperationException e) { throw new JsonException("Unsupported Value Class"); }

        switch (type) {
            case ANY:
                // never go here
                break;
            case ARRAY:
                if (object.getClass().isArray()) {
                    Class subComponentType = object.getClass().getComponentType();
                    return processArray(object, subComponentType);
                } else return processCollection((Collection) object);
            case OBJECT:
                if (object instanceof Map) {
                    Map map = (Map) object;
                    processMap(map, null);
                }
                else return marshal(object);
                break;
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case DATE:
                return object;
            case NULL:
        }
        return null;
    }

    private static JsonElement processCollection(Collection collection) throws JsonException {
        JsonElement jsonElement = new JsonArray();
        if (collection.isEmpty()) return jsonElement;
        Class genericClass;
        Iterator it = collection.iterator();
        if (it.hasNext()) genericClass = it.next().getClass();
        else return jsonElement;
        if (genericClass != null) {
            it = collection.iterator();
            while (it.hasNext()) jsonElement.add(getSubObject(it.next()));
        } else throw new JsonException("Unsupported Collection");
        return jsonElement;
    }

    private static JsonElement processArray(Object array, Class<?> componentType) throws JsonException {
        JsonElement jsonElement = new JsonArray();
        int length = Array.getLength(array);
        if (length == 0) return jsonElement;
        if (componentType != null)
            for (int i = 0; i < length; i++)
                jsonElement.add(getSubObject(Array.get(array, i)));
        else throw new JsonException("Unsupported Array");
        return jsonElement;
    }

    private static JsonElement processMap(Map map, Field field) throws JsonException {
        JsonElement jsonElement = new JsonArray();
        if (map.isEmpty()) return jsonElement;
        Class<?> keyClass;
        if (field != null) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            keyClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            if (!keyClass.equals(String.class)) throw new JsonException("Map fields must have string key types");
        }

        for (Map.Entry<String, Object> entry : ((Map<String, Object>)map).entrySet())
            jsonElement.add(entry.getKey(), getSubObject(entry.getValue()));

        return jsonElement;
    }

    private static JsonType getTypeForClass(Class<?> aClass) {
        JsonType type;
        if (Number.class.isAssignableFrom(aClass) || int.class.isAssignableFrom(aClass) || long.class.isAssignableFrom(aClass) || float.class.isAssignableFrom(aClass) || double.class.isAssignableFrom(aClass))
            type = JsonType.NUMBER;
        else if (String.class.isAssignableFrom(aClass)) type = JsonType.STRING;
        else if (boolean.class.isAssignableFrom(aClass) || Boolean.class.isAssignableFrom(aClass))
            type = JsonType.BOOLEAN;
        else if (aClass.isArray() || Collection.class.isAssignableFrom(aClass)) type = JsonType.ARRAY;
        else if (Date.class.isAssignableFrom(aClass)) type = JsonType.DATE;
        else if (Map.class.isAssignableFrom(aClass) || aClass.isAnnotationPresent(JsonObject.class)) type = JsonType.OBJECT;
        else {
            //Unsupported type
            throw new UnsupportedOperationException();
        }
        return type;
    }

    private static JsonType getTypeForField(Field field) throws UnsupportedOperationException {
        Class<?> returnType = field.getType();
        return getTypeForClass(returnType);
    }
}
