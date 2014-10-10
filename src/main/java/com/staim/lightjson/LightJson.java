package com.staim.lightjson;

import com.staim.lightjson.annotations.JsonField;
import com.staim.lightjson.annotations.JsonObject;

import java.lang.reflect.*;
import java.util.*;

/**
 * LightJson initial implementation of Json interface
 * Created by a_scherbinin on 18.04.14.
 */
@SuppressWarnings("unchecked")
public class LightJson<T> implements Json<T> {
    private JsonElement element;

    public LightJson(T t) throws JsonException {
        element = Marshaller.marshal(t);
    }

    public LightJson(String jsonString) throws JsonException {
        element = new JsonElement(jsonString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T unmarshal(Class<T> unmarshalClass) throws JsonException {
        try {
            Object object = Unmarshaller.unmarshal(element, unmarshalClass);
            if (unmarshalClass.isInstance(object)) return (T)object;
            else throw new JsonException("Class cast impossible");
        } catch (ClassCastException e) {
            throw new JsonException("Class cast impossible");
        }
    }

    @Override
    public String marshal() {
        return element.serialize();
    }

    @Override
    public JsonElement getElement() {
        return element;
    }

    private static class Util {
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

        private static Class<?> getGenericClass(Field field) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }

        @SuppressWarnings("UnusedDeclaration")
        private static Class<?> getGenericClass(Class<?> aClass) {
            ParameterizedType genericSuperclass;
            if (aClass.getGenericSuperclass() instanceof ParameterizedType) genericSuperclass = (ParameterizedType) aClass.getGenericSuperclass();
            else genericSuperclass = (ParameterizedType) aClass.getSuperclass().getGenericSuperclass();
            return (Class<?>) genericSuperclass.getActualTypeArguments()[1];
        }

        private static Class<?> getSecondGenericClass(Field field) throws JsonException {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            if (!String.class.equals(parameterizedType.getActualTypeArguments()[0])) throw new JsonException("Map fields must have string key types");
            return (Class<?>) parameterizedType.getActualTypeArguments()[1];
        }

        private static Class<?> getSecondGenericClass(Class<?> aClass) throws JsonException {
            ParameterizedType genericSuperclass;
            if (aClass.getGenericSuperclass() instanceof ParameterizedType) genericSuperclass = (ParameterizedType) aClass.getGenericSuperclass();
            else genericSuperclass = (ParameterizedType) aClass.getSuperclass().getGenericSuperclass();
            if (!String.class.equals(genericSuperclass.getActualTypeArguments()[0])) throw new JsonException("Map fields must have string key types");
            return (Class<?>) genericSuperclass.getActualTypeArguments()[1];
        }

        private static Object convertNumber(Number number, Class<?> numberType) throws JsonException {
            if (number == null) return null;
            if (Number.class.isAssignableFrom(numberType)) {
                if (Long.class.isAssignableFrom(numberType)) return number.longValue();
                else if (Integer.class.isAssignableFrom(numberType)) return number.intValue();
                else if (Float.class.isAssignableFrom(numberType)) return number.floatValue();
                else if (Double.class.isAssignableFrom(numberType)) return number.doubleValue();
                else return number;
            } else if (long.class.isAssignableFrom(numberType)) return number.longValue();
            else if (int.class.isAssignableFrom(numberType)) return number.intValue();
            else if (double.class.isAssignableFrom(numberType)) return number.doubleValue();
            else if (float.class.isAssignableFrom(numberType)) return number.floatValue();
            else throw new JsonException("Wrong parameter type");
        }
    }

    private static class Marshaller {
        private static JsonElement marshal(Object object) throws JsonException {
            if (object == null) return new JsonElement(JsonType.NULL);

            Class aClass = object.getClass();
            if (!aClass.isAnnotationPresent(JsonObject.class)) throw new JsonException("Class is not annotated as JsonObject");

            JsonElement jsonElement = new JsonElement(JsonType.OBJECT);

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
                            try { type = Util.getTypeForField(field); }
                            catch (UnsupportedOperationException e) { continue; }
                        }

                        String name = field.getName();

                        if (jsonName.isEmpty()) jsonName = name;

                        Class<?> fieldType = field.getType();
                        field.setAccessible(true);

                        if (field.get(object) == null) {
                            jsonElement.add(name, new JsonElement(JsonType.NULL));
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
                                jsonElement.add(name, new JsonElement(JsonType.NULL));
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
            try { type = Util.getTypeForClass(object.getClass()); }
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
            JsonElement jsonElement = new JsonElement(JsonType.ARRAY);
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
            JsonElement jsonElement = new JsonElement(JsonType.ARRAY);
            int length = Array.getLength(array);
            if (length == 0) return jsonElement;
            if (componentType != null)
                for (int i = 0; i < length; i++)
                    jsonElement.add(getSubObject(Array.get(array, i)));
            else throw new JsonException("Unsupported Array");
            return jsonElement;
        }

        private static JsonElement processMap(Map map, Field field) throws JsonException {
            JsonElement jsonElement = new JsonElement(JsonType.OBJECT);
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
    }

    private static class Unmarshaller {
        private static Object unmarshal(JsonElement jsonElement, Class<?> aClass) throws JsonException {
            if (jsonElement == null) return null;
            if (jsonElement.getType() != JsonType.OBJECT)
                throw new JsonException("Json Element is not Object");
            if (!aClass.isAnnotationPresent(JsonObject.class))
                throw new JsonException("Class is not annotated as JsonObject");

            boolean isAutomaticBinding = aClass.getAnnotation(JsonObject.class).AutomaticBinding();

            try {
                Constructor<?> constructor = aClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object object = constructor.newInstance();

                for (Field field : aClass.getDeclaredFields()) {
                    JsonType type;
                    String jsonName = "";
                    if (field.isAnnotationPresent(JsonField.class)) {
                        JsonField annotation = field.getAnnotation(JsonField.class);
                        type = annotation.type();
                        jsonName = annotation.name();
                    } else if (isAutomaticBinding) {
                        type = JsonType.ANY;
                    } else continue;

                    if (type != null) {
                        String name = field.getName();

                        if (jsonName.isEmpty()) jsonName = name;
                        Class<?> fieldType = field.getType();

                        field.setAccessible(true);

                        if (type == JsonType.ANY) {
                            try { type = Util.getTypeForClass(fieldType); }
                            catch (UnsupportedOperationException e) { continue; }
                        }
                        JsonElement jsonElementCurrent = jsonElement.get(jsonName);
                        if (jsonElementCurrent == null || jsonElementCurrent.getType() == JsonType.NULL) {
                            field.set(object, null);
                            continue;
                        }

                        switch (type) {
                            case ANY:
                                // never go here
                                break;
                            case ARRAY:
                                if (fieldType.isArray()) {
                                    Class<?> arrayComponentType = fieldType.getComponentType();
                                    Object subObject = processJsonArray(jsonElement.get(jsonName), arrayComponentType);
                                    field.set(object, subObject);
                                } else if (Collection.class.isAssignableFrom(fieldType)) {
                                    Class<?> genericClass = Util.getGenericClass(field);
                                    Object subObject = processJsonArrayAsCollection(jsonElement.get(jsonName), fieldType, genericClass);
                                    field.set(object, subObject);
                                } else throw new JsonException("Wrong return type in field: " + jsonName);
                                break;
                            case OBJECT:
                                if (Map.class.isAssignableFrom(fieldType)) {
                                    Object subObject = processJsonAsMap(jsonElement.get(jsonName), fieldType, Util.getSecondGenericClass(field));
                                    field.set(object, subObject);
                                } else if (fieldType.isAnnotationPresent(JsonObject.class)) {
                                    Object subObject = unmarshal(jsonElement.get(jsonName), fieldType);
                                    field.set(object, subObject);
                                } else throw new JsonException("Wrong return type in field: " + jsonName);
                                break;
                            case STRING:
                                if (String.class.isAssignableFrom(fieldType)) {
                                    String stringData = jsonElement.get(jsonName).getStringData();
                                    if (stringData != null) field.set(object, stringData);
                                    else {
                                        Number numberData = jsonElement.get(jsonName).getNumberData();
                                        if (numberData != null) field.set(object, numberData.toString());
                                    }
                                } else throw new JsonException("Wrong parameter type in field: " + jsonName);
                                break;
                            case NUMBER:
                                Number numberData = jsonElement.get(jsonName).getNumberData();
                                if (numberData == null) {
                                    String stringData = jsonElement.get(jsonName).getStringData();
                                    try {
                                        if (stringData != null && !stringData.isEmpty()) {
                                            if (stringData.contains(".") || stringData.contains(","))
                                                numberData = Double.parseDouble(stringData);
                                            else
                                                numberData = Long.parseLong(stringData);
                                        }
                                    } catch (NumberFormatException ignored) {}
                                }
                                if (Number.class.isAssignableFrom(fieldType)) {
                                    if (numberData == null) field.set(object, null);
                                    else if (Long.class.isAssignableFrom(fieldType)) {
                                        field.set(object, numberData.longValue());
                                    } else if (Integer.class.isAssignableFrom(fieldType)) {
                                        field.set(object, numberData.intValue());
                                    } else if (Double.class.isAssignableFrom(fieldType)) {
                                        field.set(object, numberData.doubleValue());
                                    } else if (Float.class.isAssignableFrom(fieldType)) {
                                        field.set(object, numberData.floatValue());
                                    } else throw new JsonException("Wrong number format in field: " + jsonName);
                                } else if (numberData == null) {
                                    throw new JsonException("No number data in field: " + jsonName);
                                } else if (long.class.isAssignableFrom(fieldType)) {
                                    field.set(object, numberData.longValue());
                                } else if (int.class.isAssignableFrom(fieldType)) {
                                    field.set(object, numberData.intValue());
                                } else if (double.class.isAssignableFrom(fieldType)) {
                                    field.set(object, numberData.doubleValue());
                                } else if (float.class.isAssignableFrom(fieldType)) {
                                    field.set(object, numberData.floatValue());
                                } else throw new JsonException("Wrong parameter type in field: " + jsonName);
                                break;
                            case BOOLEAN:
                                if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                                    field.set(object, jsonElement.get(jsonName).getBooleanData());
                                } else throw new JsonException("Wrong parameter type in field: " + jsonName);
                                break;
                            case DATE:
                                if (Date.class.isAssignableFrom(fieldType)) {
                                    field.set(object, jsonElement.get(jsonName).getDateData());
                                } else throw new JsonException("Wrong parameter type in field: " + jsonName);
                                break;
                            case NULL:
                                field.set(object, null);
                        }
                    }
                }
                return object;
            } catch (InstantiationException e) {
                throw new JsonException("Unable to create instance of class: " + aClass.getName());
            } catch (IllegalAccessException e) {
                throw new JsonException("Illegal Access problem: " + e.getMessage());
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new JsonException("JSON structure failure");
            } catch (NoSuchMethodException e) {
                throw new JsonException("No such method problem: " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw new JsonException("Invocation Target Problem: " + e.getMessage());
            }
        }

        private static Object getSubObject(JsonElement jsonElement, Class<?> valueClass) throws JsonException, InstantiationException, IllegalAccessException {
            JsonType type;
            try { type = Util.getTypeForClass(valueClass); }
            catch (UnsupportedOperationException e) { throw new JsonException("Unsupported Value Class"); }

            switch (type) {
                case ANY:
                    // never go here
                    break;
                case ARRAY:
                    throw new JsonException("Lists of Lists are not supported at the moment");
                    //break;

                case OBJECT:
                    if (Map.class.isAssignableFrom(valueClass)) return processJsonAsMap(jsonElement, valueClass, Util.getSecondGenericClass(valueClass));
                    else return unmarshal(jsonElement, valueClass);

                case STRING:  return jsonElement.getStringData();
                case NUMBER:  return Util.convertNumber(jsonElement.getNumberData(), valueClass);
                case BOOLEAN: return jsonElement.getBooleanData();
                case DATE:    return jsonElement.getDateData();
                case NULL:    return null;
            }
            return null;
        }

        private static Object processJsonArrayAsCollection(JsonElement jsonElement, Class<?> parameterClass, Class<?> genericClass) throws JsonException, IllegalAccessException, InstantiationException {
            if (jsonElement.getType() != JsonType.ARRAY) throw new JsonException("Json Element is not Array");

            if (!Collection.class.isAssignableFrom(parameterClass)) throw new JsonException("Incompatible types");

            Collection newCollection;
            if (parameterClass.isInterface()) {
                if (parameterClass.isAssignableFrom(ArrayList.class)) newCollection = new ArrayList();
                else throw new JsonException("Unsupported collection type");
            } else newCollection = (Collection)parameterClass.newInstance();

            Iterator<JsonElement> it = jsonElement.iterator();
            while (it.hasNext()) newCollection.add(getSubObject(it.next(), genericClass));

            return newCollection;
        }

        private static Object processJsonArray(JsonElement jsonElement, Class<?> componentType) throws JsonException, IllegalAccessException, InstantiationException {
            if (jsonElement.getType() != JsonType.ARRAY) throw new JsonException("Json Element is not Array");
            Object array = Array.newInstance(componentType, jsonElement.size());

            Iterator<JsonElement> it = jsonElement.iterator();
            int i = 0;
            while (it.hasNext()) Array.set(array, i++, getSubObject(it.next(), componentType));
            return array;
        }

        private static Map processJsonAsMap(JsonElement jsonElement, Class<?> mapClass, Class<?> valueClass) throws JsonException, IllegalAccessException, InstantiationException {
            Map map;
            if (mapClass.isInterface()) {
                if (mapClass.isAssignableFrom(HashMap.class)) map = new HashMap();
                else throw new JsonException("Unsupported collection type");
            } else map = (Map)mapClass.newInstance();

            Map<String, JsonElement> jsonMap = (Map<String, JsonElement>)jsonElement.getData();
            for (Map.Entry<String, JsonElement> entry : jsonMap.entrySet())
                map.put(entry.getKey(), getSubObject(jsonElement.get(entry.getKey()), valueClass));

            return map;
        }
    }
}
