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

    private static JsonType getTypeForClass(Class<?> aClass) {
        JsonType type;
        if (Number.class.isAssignableFrom(aClass) || int.class.isAssignableFrom(aClass) || long.class.isAssignableFrom(aClass) || float.class.isAssignableFrom(aClass) || double.class.isAssignableFrom(aClass))
            type = JsonType.NUMBER;
        else if (String.class.isAssignableFrom(aClass)) type = JsonType.STRING;
        else if (boolean.class.isAssignableFrom(aClass) || Boolean.class.isAssignableFrom(aClass))
            type = JsonType.BOOLEAN;
        else if (Collection.class.isAssignableFrom(aClass)) type = JsonType.ARRAY;
        else if (Date.class.isAssignableFrom(aClass)) type = JsonType.DATE;
        else if (Map.class.isAssignableFrom(aClass) || aClass.isAnnotationPresent(JsonObject.class)) type = JsonType.OBJECT;
        else {
            //Unsupported type
            throw new UnsupportedOperationException();
        }
        return type;
    }

    private static JsonType getTypeForMethod(Method method) throws UnsupportedOperationException {
        Class<?> returnType = method.getReturnType();
        return getTypeForClass(returnType);
    }

    private static JsonElement processObject(Object object) throws JsonException {
        if (object == null) return new JsonElement(JsonType.NULL);

        Class aClass = object.getClass();
        if (!aClass.isAnnotationPresent(JsonObject.class)) throw new JsonException("Class is not annotated as JsonObject");

        JsonElement jsonElement = new JsonElement(JsonType.OBJECT);

        boolean isAutomaticMethodBinding = ((JsonObject)aClass.getAnnotation(JsonObject.class)).AutomaticMethodBinding();
        Method[] methods = aClass.getMethods();

        try {
            for (Method method : methods) {
                JsonType type = null;
                String jsonName = "";
                if (method.isAnnotationPresent(JsonGetter.class)) {
                    JsonGetter annotation = method.getAnnotation(JsonGetter.class);
                    type = annotation.type();
                    jsonName = annotation.name();
                } else if (isAutomaticMethodBinding) {
                    type = JsonType.ANY;
                }
                if (type != null) {
                    if (type == JsonType.ANY) {
                        try { type = getTypeForMethod(method); }
                        catch (UnsupportedOperationException e) { continue; }
                    }

                    String name = method.getName();
                    if (name.equals("getClass")) continue;

                    if (jsonName.isEmpty()) {
                        if (type != JsonType.NULL && !name.startsWith(type == JsonType.BOOLEAN ? "is" : "get")) {
                            if (!isAutomaticMethodBinding) throw new JsonException("Naming convention violated: " + name);
                            else continue;
                        }
                        jsonName = name.substring(type == JsonType.BOOLEAN ? 2 : 3).toLowerCase();
                    }

                    Class<?> returnType = method.getReturnType();

                    switch (type) {
                        case ANY:
                            // never go here
                            break;
                        case ARRAY:
                            if (List.class.isAssignableFrom(returnType)) {
                                List list = (List)method.invoke(object);
                                jsonElement.add(jsonName, processCollection(list));
                            } else throw new JsonException("Wrong return type");
                            break;
                        case OBJECT:
                            if (Map.class.isAssignableFrom(returnType)) {
                                Map map = (Map)method.invoke(object);
                                //TODO: verify map type
                                jsonElement.add(name.substring(3).toLowerCase(), map);
                            } else if (returnType.isAnnotationPresent(JsonObject.class)) {
                                Object subObject = method.invoke(object);
                                JsonElement subElement = processObject(subObject);
                                jsonElement.add(jsonName, subElement);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case STRING:
                            String string;
                            if (String.class.isAssignableFrom(returnType)) {
                                string = (String)method.invoke(object);
                            } else if (Object.class.isAssignableFrom(returnType)) {
                                string = method.invoke(object).toString();
                            } else throw new JsonException("Wrong return type");
                            jsonElement.add(jsonName, string);
                            break;
                        case NUMBER:
                            if (Number.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType)) {
                                Number number = (Number)method.invoke(object);
                                jsonElement.add(jsonName, number);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case BOOLEAN:
                            if (Boolean.class.isAssignableFrom(returnType) || boolean.class.isAssignableFrom(returnType)) {
                                Boolean bool = (Boolean)method.invoke(object);
                                jsonElement.add(jsonName, bool);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case DATE:
                            if (Date.class.isAssignableFrom(returnType)) {
                                Date date = (Date)method.invoke(object);
                                jsonElement.add(jsonName, date);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case NULL:
                            jsonElement.add(name, new JsonElement(JsonType.NULL));
                    }
                }
            }
            return jsonElement;
        } catch (InvocationTargetException |IllegalAccessException e) {
            throw new JsonException("Getter Invocation failed");
        }
    }

    @SuppressWarnings("RedundantCast")
    private static JsonElement processCollection(Collection collection) throws JsonException {
        JsonElement jsonElement = new JsonElement(JsonType.ARRAY);
        if (collection.isEmpty()) return jsonElement;
        Class genericClass;
        Iterator it = collection.iterator();
        if (it.hasNext()) genericClass = it.next().getClass();
        else return jsonElement;
        if (genericClass != null) {

            JsonType type;
            try { type = getTypeForClass(genericClass); }
            catch (UnsupportedOperationException e) { throw new JsonException("Unsupported Collection"); }

            it = collection.iterator();
            switch (type) {
                case ANY:
                    // never go here
                    break;
                case ARRAY:
                    while (it.hasNext()) jsonElement.add(processCollection((Collection) it.next()));
                    break;
                case OBJECT:
                    while (it.hasNext()) jsonElement.add(processObject(it.next()));
                    break;
                case STRING:
                    while (it.hasNext()) jsonElement.add((String) it.next());
                    break;
                case NUMBER:
                    while (it.hasNext()) jsonElement.add((Number) it.next());
                    break;
                case BOOLEAN:
                    while (it.hasNext()) jsonElement.add((Boolean) it.next());
                    break;
                case DATE:
                    while (it.hasNext()) jsonElement.add((Date)it.next());
                    break;
                case NULL:
                    while (it.hasNext()) jsonElement.add(new JsonElement(JsonType.NULL));
            }
        } else throw new JsonException("Unsupported Collection");
        return jsonElement;
    }
}
