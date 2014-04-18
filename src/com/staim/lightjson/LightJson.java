package com.staim.lightjson;

import com.staim.lightjson.annotations.JsonGetter;
import com.staim.lightjson.annotations.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * LightJson initial implementation of Json interface
 * Created by a_scherbinin on 18.04.14.
 */
public class LightJson<T> implements Json<T> {
    private JsonElement element;

    public LightJson(T t) throws JsonException {
        element = processObject(t);
    }

    public LightJson(String jsonString) throws JsonException {
        element = new JsonElement(jsonString);
    }

    @Override
    public T unmarshal() {
        return null;
    }

    @Override
    public String marshal() {
        return element.serialize();
    }

    @Override
    public JsonElement getElement() {
        return element;
    }

    private static JsonElement.JSONType getTypeForClass(Class<?> aClass) {
        JsonElement.JSONType type;
        if (Number.class.isAssignableFrom(aClass) || int.class.isAssignableFrom(aClass) || long.class.isAssignableFrom(aClass) || float.class.isAssignableFrom(aClass) || double.class.isAssignableFrom(aClass))
            type = JsonElement.JSONType.NUMBER;
        else if (String.class.isAssignableFrom(aClass)) type = JsonElement.JSONType.STRING;
        else if (boolean.class.isAssignableFrom(aClass) || Boolean.class.isAssignableFrom(aClass))
            type = JsonElement.JSONType.BOOLEAN;
        else if (List.class.isAssignableFrom(aClass)) type = JsonElement.JSONType.ARRAY;
        else if (Date.class.isAssignableFrom(aClass)) type = JsonElement.JSONType.DATE;
        else if (Map.class.isAssignableFrom(aClass) || aClass.isAnnotationPresent(JsonGetter.class)) type = JsonElement.JSONType.OBJECT;
        else {
            //Unsupported type
            throw new UnsupportedOperationException();
        }
        return type;
    }

    private static JsonElement.JSONType getTypeForMethod(Method method) throws UnsupportedOperationException {
        Class<?> returnType = method.getReturnType();
        return getTypeForClass(returnType);
    }

    private static JsonElement processObject(Object object) throws JsonException {
        JsonElement jsonElement = new JsonElement(JsonElement.JSONType.OBJECT);
        Class aClass = object.getClass();
        boolean isClassAnnotated = aClass.isAnnotationPresent(JsonObject.class);
        Method[] methods = aClass.getMethods();

        try {
            for (Method method : methods) {
                JsonElement.JSONType type = null;
                String jsonName = "";
                if (method.isAnnotationPresent(JsonGetter.class)) {
                    JsonGetter annotation = method.getAnnotation(JsonGetter.class);
                    type = annotation.type();
                    jsonName = annotation.name();
                } else if (isClassAnnotated) {
                    type = JsonElement.JSONType.ANY;
                }
                if (type != null) {
                    if (type == JsonElement.JSONType.ANY) {
                        try { type = getTypeForMethod(method); }
                        catch (UnsupportedOperationException e) { continue; }
                    }

                    String name = method.getName();
                    if (name.equals("getClass")) continue;

                    if (jsonName.isEmpty()) {
                        if (type != JsonElement.JSONType.NULL && !name.startsWith(type == JsonElement.JSONType.BOOLEAN ? "is" : "get")) {
                            if (!isClassAnnotated) throw new JsonException("Naming convention violated: " + name);
                            else continue;
                        }
                        jsonName = name.substring(type == JsonElement.JSONType.BOOLEAN ? 2 : 3).toLowerCase();
                    }

                    Class<?> returnType = method.getReturnType();

                    switch (type) {
                        case ANY:
                            // never go here
                            break;
                        case ARRAY:
                            if (List.class.isAssignableFrom(returnType)) {
                                List list = (List)method.invoke(object);
                                jsonElement.addElement(processCollection(list), jsonName);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case OBJECT:
                            if (Map.class.isAssignableFrom(returnType)) {
                                Map map = (Map)method.invoke(object);
                                //TODO: verify map type
                                jsonElement.add(map, name.substring(3).toLowerCase());
                            } else if (returnType.isAnnotationPresent(JsonGetter.class)) {
                                Object subObject = method.invoke(object);
                                JsonElement subElement = processObject(subObject);
                                jsonElement.addElement(subElement, jsonName);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case STRING:
                            String string;
                            if (String.class.isAssignableFrom(returnType)) {
                                string = (String)method.invoke(object);
                            } else if (Object.class.isAssignableFrom(returnType)) {
                                string = method.invoke(object).toString();
                            } else throw new JsonException("Wrong return type");
                            jsonElement.add(string, jsonName);
                            break;
                        case NUMBER:
                            if (Number.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType)) {
                                Number number = (Number)method.invoke(object);
                                jsonElement.add(number, jsonName);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case BOOLEAN:
                            if (Boolean.class.isAssignableFrom(returnType) || boolean.class.isAssignableFrom(returnType)) {
                                Boolean bool = (Boolean)method.invoke(object);
                                jsonElement.add(bool, jsonName);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case DATE:
                            if (Date.class.isAssignableFrom(returnType)) {
                                Date date = (Date)method.invoke(object);
                                jsonElement.add(date, jsonName);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case NULL:
                            jsonElement.addElement(new JsonElement(JsonElement.JSONType.NULL), name);
                    }
                }
            }
            return jsonElement;
        } catch (InvocationTargetException |IllegalAccessException e) {
            throw new JsonException("Getter Invocation failed");
        }
    }

    private static JsonElement processCollection(Collection collection) throws JsonException {
        JsonElement jsonElement = new JsonElement(JsonElement.JSONType.ARRAY);
        if (collection.isEmpty()) return jsonElement;
        Class genericClass;
        Iterator it = collection.iterator();
        if (it.hasNext()) genericClass = it.next().getClass();
        else return jsonElement;
        if (genericClass != null) {

            JsonElement.JSONType type;
            try { type = getTypeForClass(genericClass); }
            catch (UnsupportedOperationException e) { throw new JsonException("Unsupported Collection"); }

            it = collection.iterator();
            switch (type) {
                case ANY:
                    // never go here
                    break;
                case ARRAY:
                    while (it.hasNext()) jsonElement.addElement(processCollection((Collection) it.next()));
                    break;
                case OBJECT:
                    while (it.hasNext()) jsonElement.addElement(processObject(it.next()));
                    break;
                case STRING:
                    while (it.hasNext()) jsonElement.add((String)it.next());
                    break;
                case NUMBER:
                    while (it.hasNext()) jsonElement.add((Number)it.next());
                    break;
                case BOOLEAN:
                    while (it.hasNext()) jsonElement.add((Boolean)it.next());
                    break;
                case DATE:
                    while (it.hasNext()) jsonElement.add((Date)it.next());
                    break;
                case NULL:
                    while (it.hasNext()) jsonElement.addElement(new JsonElement(JsonElement.JSONType.NULL));
            }
        } else throw new JsonException("Unsupported Collection");
        return jsonElement;
    }
}
