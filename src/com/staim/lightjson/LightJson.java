package com.staim.lightjson;

import com.staim.lightjson.annotations.JsonGetter;
import com.staim.lightjson.annotations.JsonObject;
import com.staim.lightjson.annotations.JsonSetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * LightJson initial implementation of Json interface
 * Created by a_scherbinin on 18.04.14.
 */
@SuppressWarnings("unchecked")
public class LightJson<T> implements Json<T> {
    private JsonElement element;

    public LightJson(T t) throws JsonException {
        element = processObject(t);
    }

    public LightJson(String jsonString) throws JsonException {
        element = new JsonElement(jsonString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T unmarshal(Class<T> unmarshalClass) throws JsonException {
        try {
            Object object = processJsonElement(element, unmarshalClass);
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

        try {
            for (Method method : aClass.getMethods()) {
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
                            if (Collection.class.isAssignableFrom(returnType)) {
                                Collection collection = (Collection)method.invoke(object);
                                jsonElement.add(jsonName, processCollection(collection));
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
                    while (it.hasNext()) jsonElement.add((Date) it.next());
                    break;
                case NULL:
                    while (it.hasNext()) jsonElement.add(new JsonElement(JsonType.NULL));
            }
        } else throw new JsonException("Unsupported Collection");
        return jsonElement;
    }

    private static Object processJsonElement(JsonElement jsonElement, Class<?> aClass) throws JsonException {
        if (jsonElement == null) return null;
        if (jsonElement.getType() != JsonType.OBJECT) throw new JsonException("Json Element is not Object");
        if (!aClass.isAnnotationPresent(JsonObject.class)) throw new JsonException("Class is not annotated as JsonObject");

        boolean isAutomaticMethodBinding = aClass.getAnnotation(JsonObject.class).AutomaticMethodBinding();

        try {
            Object object = aClass.newInstance();

            for (Method method : aClass.getMethods()) {
                JsonType type;
                String jsonName = "";
                if (method.isAnnotationPresent(JsonSetter.class)) {
                    JsonSetter annotation = method.getAnnotation(JsonSetter.class);
                    type = annotation.type();
                    jsonName = annotation.name();
                } else if (isAutomaticMethodBinding) {
                    type = JsonType.ANY;
                } else continue;

                if (type != null) {
                    String name = method.getName();
                    //if (name.equals("getClass")) continue;

                    if (jsonName.isEmpty()) {
                        if (type != JsonType.NULL && !name.startsWith("set")) {
                            if (!isAutomaticMethodBinding) throw new JsonException("Naming convention violated: " + name);
                            else continue;
                        }
                        jsonName = name.substring(3).toLowerCase();
                    }

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) throw new JsonException("Wrong setter parameter count: " + parameterTypes.length);
                    Class<?> parameterClass = parameterTypes[0];

                    if (type == JsonType.ANY) {
                        try { type = getTypeForClass(parameterClass); }
                        catch (UnsupportedOperationException e) { continue; }
                    }

                    switch (type) {
                        case ANY:
                            // never go here
                            break;
                        case ARRAY:
                            if (Collection.class.isAssignableFrom(parameterClass)) {
                                if (!method.isAnnotationPresent(JsonSetter.class)) throw new JsonException("Array setters must always be annotated");
                                Class<?> genericClass = method.getAnnotation(JsonSetter.class).genericClass();
                                if (genericClass.equals(Object.class)) throw new JsonException("Array fields require genericClass parameter be annotated");
                                Object subObject = processJsonArray(jsonElement.get(jsonName), parameterClass, genericClass);
                                method.invoke(object, subObject);

                            } else throw new JsonException("Wrong return type");
                            break;
                        case OBJECT:
                            if (Map.class.isAssignableFrom(parameterClass)) {
                                //method.invoke(object, jsonElement.get(jsonName).getObjectData());
                                //TODO: !!!
                                //jsonElement.add(name.substring(3).toLowerCase(), map);
                            } else if (parameterClass.isAnnotationPresent(JsonObject.class)) {
                                Object subObject = processJsonElement(jsonElement.get(jsonName), parameterClass);
                                method.invoke(object, subObject);
                            } else throw new JsonException("Wrong return type");
                            break;
                        case STRING:
                            if (String.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getStringData());
                            } else throw new JsonException("Wrong parameter type");
                            break;
                        case NUMBER:
                            if (Number.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getNumberData());
                            } else if (long.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getNumberData().longValue());
                            } else if (int.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getNumberData().intValue());
                            } else if (double.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getNumberData().doubleValue());
                            } else if (float.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getNumberData().floatValue());
                            } else throw new JsonException("Wrong parameter type");
                            break;
                        case BOOLEAN:
                            if (Boolean.class.isAssignableFrom(parameterClass) || boolean.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getBooleanData());
                            } else throw new JsonException("Wrong parameter type");
                            break;
                        case DATE:
                            if (Date.class.isAssignableFrom(parameterClass)) {
                                method.invoke(object, jsonElement.get(jsonName).getDateData());
                            } else throw new JsonException("Wrong parameter type");
                            break;
                        case NULL:
                            jsonElement.add(name, new JsonElement(JsonType.NULL));
                    }
                }
            }

            return object;
        } catch (InstantiationException e) {
            throw new JsonException("Unable to create instance of class: " + aClass.getName());
        } catch (IllegalAccessException e) {
            throw new JsonException("Illegal Access problem: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new JsonException("Setter Invocation failed");
        } /*catch (NullPointerException e) {
            throw new JsonException("JSON structure failure");
        }*/
    }

    @SuppressWarnings("unchecked")
    private static Object processJsonArray(JsonElement jsonElement, Class<?> parameterClass, Class<?> genericClass) throws JsonException {
        if (jsonElement.getType() != JsonType.ARRAY) throw new JsonException("Json Element is not Array");

        try {
            if (!Collection.class.isAssignableFrom(parameterClass)) throw new JsonException("Incompatible types");

            Collection newCollection;
            if (parameterClass.isInterface()) {
                if (parameterClass.isAssignableFrom(ArrayList.class)) newCollection = new ArrayList();
                else throw new JsonException("Unsupported collection type");
            } else newCollection = (Collection)parameterClass.newInstance();


            JsonType type;
            try { type = getTypeForClass(genericClass); }
            catch (UnsupportedOperationException e) { throw new JsonException("Unsupported Collection"); }

            Iterator<JsonElement> it = jsonElement.getIterator();
            switch (type) {
                case ANY:
                    // never go here
                    break;
                case ARRAY:
                    //while (it.hasNext()) jsonElement.add(processCollection((Collection) it.next()));
                    throw new JsonException("Lists of Lists are not supported at the moment");
                    //break;
                case OBJECT:
                    while (it.hasNext()) newCollection.add(processJsonElement(it.next(), genericClass));
                    break;
                case STRING:
                    while (it.hasNext()) newCollection.add(it.next().getStringData());
                    break;
                case NUMBER:
                    while (it.hasNext()) {
                        if (Number.class.isAssignableFrom(genericClass)) {
                            if (Long.class.isAssignableFrom(genericClass))
                                newCollection.add(it.next().getNumberData().longValue());
                            else if (Integer.class.isAssignableFrom(genericClass))
                                newCollection.add(it.next().getNumberData().intValue());
                            else if (Float.class.isAssignableFrom(genericClass))
                                newCollection.add(it.next().getNumberData().floatValue());
                            else if (Double.class.isAssignableFrom(genericClass))
                                newCollection.add(it.next().getNumberData().doubleValue());
                            else newCollection.add(it.next().getNumberData());
                        } else if (long.class.isAssignableFrom(genericClass)) {
                            newCollection.add(it.next().getNumberData().longValue());
                        } else if (int.class.isAssignableFrom(genericClass)) {
                            newCollection.add(it.next().getNumberData().intValue());
                        } else if (double.class.isAssignableFrom(genericClass)) {
                            newCollection.add(it.next().getNumberData().doubleValue());
                        } else if (float.class.isAssignableFrom(genericClass)) {
                            newCollection.add(it.next().getNumberData().floatValue());
                        } else throw new JsonException("Wrong parameter type");
                    }
                    break;
                case BOOLEAN:
                    while (it.hasNext()) newCollection.add(it.next().getBooleanData());
                    break;
                case DATE:
                    while (it.hasNext()) newCollection.add(it.next().getDateData());
                    break;
                case NULL:
                    while (it.hasNext()) newCollection.add(null);
            }




            //Iterator <JsonElement> jsonElementIterator = jsonElement.getIterator();

/*            while (jsonElementIterator.hasNext()) {
                Object subObject;
                if (Collection.class.isAssignableFrom(genericClass)) throw new JsonException("Lists of Lists are not supported at the moment");
                else subObject = processJsonElement(jsonElementIterator.next(), genericClass);
                newCollection.add(subObject);
            }*/

            return newCollection;
        } catch (InstantiationException e) {
            throw new JsonException("Unable to create instance of class: " + parameterClass.getName());
        } catch (IllegalAccessException e) {
            throw new JsonException("Illegal Access problem: " + e.getMessage());
        }
    }
}
