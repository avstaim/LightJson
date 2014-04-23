package com.staim.lightjson.test;

import com.staim.lightjson.Json;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.LightJson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a_scherbinin on 17.04.14.
 */
public class Test {
    public static void main(String[] args) {
        TestBean testBean = new TestBean();
        testBean.setBool(true);
        testBean.setNumber(10);
        testBean.setString("Hello world");

        TestBean2 testBean2 = new TestBean2();
        testBean2.setInteger2(25);
        testBean2.setString2("Whatszzupppp!!!");

        testBean.setTestBean2(testBean2);

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

        String testJson;

        try {
            Json<TestBean> jsonTestBean = new LightJson<>(testBean);
            testJson = jsonTestBean.marshal();
            System.out.println("marshal result: " + testJson);
        } catch (JsonException e) {
            e.printStackTrace();
            return;
        }

        try {
            Json<TestBean> jsonTestBean = new LightJson<>(testJson);
            TestBean uTestBean = jsonTestBean.unmarshal(TestBean.class);
            System.out.println("unmarshal result: " + uTestBean.getString() + " - " + uTestBean.getNumber() + " - " + uTestBean.isBool() + " - " + uTestBean.getIntegerList().size());

            for (Integer i : uTestBean.getIntegerList())
                System.out.println("list: " + i);

            System.out.println("unmarshal bean2: " + uTestBean.getTestBean2().getInteger2() + " - " + uTestBean.getTestBean2().getString2());
        } catch (JsonException e) {
            e.printStackTrace();
        }


    }

   /* public static JsonElement processObject(Object object) throws JsonException {
        JsonElement jsonElement = new JsonElement(JsonType.OBJECT);

        Class aClass = object.getClass();

        boolean isClassAnnotated = aClass.isAnnotationPresent(JsonGetter.class);

        Method[] methods = aClass.getMethods();

        try {
            for (Method method : methods) {
                if (isClassAnnotated || method.isAnnotationPresent(JsonGetter.class)) {
                    JsonGetter annotation = isClassAnnotated ? (JsonGetter) aClass.getAnnotation(JsonGetter.class) : method.getAnnotation(JsonGetter.class);
                    JsonType type = annotation.type();
                    Class<?> returnType = method.getReturnType();

                    if (type == JsonType.ANY) {
                        if (Number.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType))
                            type = JsonType.NUMBER;
                        else if (String.class.isAssignableFrom(returnType)) type = JsonType.STRING;
                        else if (boolean.class.isAssignableFrom(returnType) || Boolean.class.isAssignableFrom(returnType))
                            type = JsonType.BOOLEAN;
                        else if (List.class.isAssignableFrom(returnType)) type = JsonType.ARRAY;
                        else if (Date.class.isAssignableFrom(returnType)) type = JsonType.DATE;
                        else if (Map.class.isAssignableFrom(returnType) || returnType.isAnnotationPresent(JsonGetter.class)) type = JsonType.OBJECT;
                        else {
                            //Unsupported type
                            continue;
                        }
                    }


                    String name = method.getName();
                    if (type != JsonType.NULL && !name.startsWith(type == JsonType.BOOLEAN ? "is" : "get")) {
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
                            jsonElement.addElement(new JsonElement(JsonType.NULL), name);
                    }
                }
                System.out.println(method.getName() + " - " + method.getReturnType().getName());
                if (method.getReturnType() == int.class) System.out.println("int");


            }
            return jsonElement;
        } catch (InvocationTargetException|IllegalAccessException e) {
            throw new JsonException("Getter Invocation failed");
        }
    }*/



}
