package com.staim.lightjson.test;


import com.staim.lightjson.Json;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.LightJson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary Testing
 *
 * Created by a_scherbinin on 18.06.14.
 */
public class JsonTest {

    @Test
    public void testJson() {
        TestBean testBean = new TestBean();
        testBean.setBool(true);
        testBean.setNumber(10);
        testBean.setString("Hello world");

        int[] intArray = {2, 4, 6, 8, 10};

        TestBean2 testBean2 = new TestBean2();
        testBean2.setInteger2(25);
        testBean2.setString2("Whatszzupppp!!!");
        testBean2.setIntArray(intArray);

        TestBean2 testBean21 = new TestBean2();
        testBean21.setInteger2(50);
        testBean21.setString2("aaa");
        TestBean2 testBean22 = new TestBean2();
        testBean22.setInteger2(51);
        testBean22.setString2("bbb");
        TestBean2 testBean23 = new TestBean2();
        testBean23.setInteger2(52);
        testBean23.setString2("ccc");

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("key1", "value1");
        stringMap.put("key2", "value2");
        testBean2.setStringMap(stringMap);

        testBean.setTestBean2(testBean2);

        List<Integer> myList = new ArrayList<>();
        for (int i = 0; i < 10; i++) myList.add(i);
        testBean.setIntegerList(myList);

        System.out.println("Begin!");

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

            System.out.println("unmarshal bean2: " + uTestBean.getTestBean2().getInteger2() + " - " + uTestBean.getTestBean2().getString2() + " - " + uTestBean.getTestBean2().getIntArray().length);

            for (int i : uTestBean.getTestBean2().getIntArray())
                System.out.println("intArray: " + i);

            for (Map.Entry<String, String> entry : uTestBean.getTestBean2().getStringMap().entrySet())
                System.out.println("key: " + entry.getKey() +"; value: " + entry.getValue());
        } catch (JsonException e) {
            e.printStackTrace();
        }
    }
}
