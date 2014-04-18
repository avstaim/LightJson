package com.staim.lightjson.test;

import com.staim.lightjson.Json;
import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.LightJson;
import com.staim.lightjson.annotations.JsonGetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by a_scherbinin on 17.04.14.
 */
public class Test {
    public static void main(String[] args) {
        TestBean testBean = new TestBean();
        testBean.setBool(true);
        testBean.setNumber(10);
        testBean.setString("Hello world");

        //int[] arr = {0, 1, 2, 3, 4};
        //List<int[]> myList = Arrays.asList(arr);

        List<Integer> myList = new ArrayList<>();
        for (int i = 0; i < 10; i++) myList.add(i);
        testBean.setIntegerList(myList);

        System.out.println("Begin!");

/*        try {
            JsonElement jsonElement = processObject(testBean);
            System.out.println("result: " + jsonElement.serialize());
        } catch (JsonException e) {
            e.printStackTrace();
        }*/

        try {
            Json<TestBean> jsonTestBean = new LightJson<>(testBean);
            System.out.println("result: " + jsonTestBean.marshal());
        } catch (JsonException e) {
            e.printStackTrace();
        }

    }

    public static JsonElement processObject(Object object) throws JsonException {
        JsonElement jsonElement = new JsonElement(JsonElement.JSONType.OBJECT);

        Class aClass = object.getClass();

        boolean isClassAnnotated = aClass.isAnnotationPresent(JsonGetter.class);

        Method[] methods = aClass.getMethods();

        try {
            for (Method method : methods) {
                if (isClassAnnotated || method.isAnnotationPresent(JsonGetter.class)) {
                    JsonGetter annotation = isClassAnnotated ? (JsonGetter) aClass.getAnnotation(JsonGetter.class) : method.getAnnotation(JsonGetter.class);
                    JsonElement.JSONType type = annotation.type();
                    Class<?> returnType = method.getReturnType();

                    if (type == JsonElement.JSONType.ANY) {
                        if (Number.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType))
                            type = JsonElement.JSONType.NUMBER;
                        else if (String.class.isAssignableFrom(returnType)) type = JsonElement.JSONType.STRING;
                        else if (boolean.class.isAssignableFrom(returnType) || Boolean.class.isAssignableFrom(returnType))
                            type = JsonElement.JSONType.BOOLEAN;
                        else if (List.class.isAssignableFrom(returnType)) type = JsonElement.JSONType.ARRAY;
                        else if (Date.class.isAssignableFrom(returnType)) type = JsonElement.JSONType.DATE;
                        else if (Map.class.isAssignableFrom(returnType) || returnType.isAnnotationPresent(JsonGetter.class)) type = JsonElement.JSONType.OBJECT;
                        else {
                            //Unsupported type
                            continue;
                        }
                    }


                    String name = method.getName();
                    if (type != JsonElement.JSONType.NULL && !name.startsWith(type == JsonElement.JSONType.BOOLEAN ? "is" : "get")) {
                        if (!isClassAnnotated) throw new JsonException("Naming convention violated: " + name);
                        else continue;
                    }

                    if (name.equals("getClass")) continue;
                    switch (type) {
                        case ANY:
                            // never go here
                            break;
                        case ARRAY:
                            if (List.class.isAssignableFrom(returnType)) {

                            } else throw new JsonException("Wrong return type");
                            break;
                        case OBJECT:
                            if (Map.class.isAssignableFrom(returnType)) {
                                Map map = (Map)method.invoke(object);
                                jsonElement.add(map, name.substring(3).toLowerCase());
                            } else if (returnType.isAnnotationPresent(JsonGetter.class)) {

                            } else throw new JsonException("Wrong return type");
                            break;
                        case STRING:
                            String string;
                            if (String.class.isAssignableFrom(returnType)) {
                                string = (String)method.invoke(object);
                            } else if (Object.class.isAssignableFrom(returnType)) {
                                string = method.invoke(object).toString();
                            } else throw new JsonException("Wrong return type");
                            jsonElement.add(string, name.substring(3).toLowerCase());
                            break;
                        case NUMBER:
                            if (Number.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType)) {
                                Number number = (Number)method.invoke(object);
                                jsonElement.add(number, name.substring(3).toLowerCase());
                            } else throw new JsonException("Wrong return type");
                            break;
                        case BOOLEAN:
                            if (Boolean.class.isAssignableFrom(returnType) || boolean.class.isAssignableFrom(returnType)) {
                                Boolean bool = (Boolean)method.invoke(object);
                                jsonElement.add(bool, name.substring(2).toLowerCase());
                            } else throw new JsonException("Wrong return type");
                            break;
                        case DATE:
                            if (Date.class.isAssignableFrom(returnType)) {
                                Date date = (Date)method.invoke(object);
                                jsonElement.add(date, name.substring(3).toLowerCase());
                            } else throw new JsonException("Wrong return type");
                            break;
                        case NULL:
                            jsonElement.addElement(new JsonElement(JsonElement.JSONType.NULL), name);
                    }
                }
                System.out.println(method.getName() + " - " + method.getReturnType().getName());
                if (method.getReturnType() == int.class) System.out.println("int");


            }
            return jsonElement;
        } catch (InvocationTargetException|IllegalAccessException e) {
            throw new JsonException("Getter Invocation failed");
        }
    }



}
