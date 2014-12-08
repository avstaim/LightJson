package com.staim.lightjson.test;

import com.staim.lightjson.*;
import com.staim.lightjson.implementations.parsers.ParserScalable;
import com.staim.lightjson.implementations.parsers.ParserSimple;
import com.staim.lightjson.implementations.serializers.SerializerForkJoin;
import com.staim.lightjson.implementations.serializers.SerializerRecursive;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary Testing
 *
 * Created by a_shcherbinin on 18.06.14.
 */
public class JsonTest {

    @Test
    public void testJson() {
        TestBean testBean = new TestBean();
        testBean.setBool(true);
        testBean.setNumber(10);
        testBean.setString("Hello world");
        testBean.setDoubleValue(2);

        int[] intArray = {2, 4, 6, 8, 10};

        TestBean2 testBean2 = new TestBean2();
        testBean2.setInteger2(25);
        testBean2.setString2("AaBbCcDdEeFf!!!");
        testBean2.setIntArray(intArray);

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("key1", "value1");
        stringMap.put("key2", "value2");
        testBean2.setStringMap(stringMap);

        testBean.setTestBean2(testBean2);

        List<Integer> integerList = new ArrayList<>();
        for (int i = 0; i < 10; i++) integerList.add(i);
        testBean.setIntegerList(integerList);

        System.out.println("Begin!");

        String jsonString;

        LightJson.setSerializerType(LightJson.SerializerType.SimpleRecursive);
        try {
            jsonString = LightJson.json().marshaller(testBean).marshal();
            System.out.println("marshal result: " + jsonString);
            Assert.assertEquals(228, jsonString.length());
        } catch (JsonException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
        }

        LightJson.setSerializerType(LightJson.SerializerType.ForkJoin);
        try {
            jsonString = LightJson.json().marshaller(testBean).marshal();
            System.out.println("marshal result: " + jsonString);
            Assert.assertEquals(228, jsonString.length());
        } catch (JsonException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
        }

        LightJson.setParserType(LightJson.ParserType.Simple);
        try {
            TestBean uTestBean = LightJson.json().unmarshaller(jsonString).unmarshal(TestBean.class);
            Assert.assertNotNull(uTestBean);
            Assert.assertEquals(true, uTestBean.isBool());
            Assert.assertEquals(10, uTestBean.getNumber());
            Assert.assertEquals("Hello world", uTestBean.getString());
            Assert.assertEquals(2d, uTestBean.getDoubleValue());
            List<Integer> uIntegerList = uTestBean.getIntegerList();
            Assert.assertNotNull(uIntegerList);
            Assert.assertEquals(10, uIntegerList.size());
            Assert.assertEquals(0, (int)uIntegerList.get(0));
            Assert.assertEquals(3, (int)uIntegerList.get(3));
            Assert.assertEquals(5, (int)uIntegerList.get(5));
            Assert.assertEquals(9, (int)uIntegerList.get(9));
            TestBean2 uTestBean2 = uTestBean.getTestBean2();
            Assert.assertNotNull(uTestBean2);
            Assert.assertEquals(25, uTestBean2.getInteger2());
            Assert.assertEquals("AaBbCcDdEeFf!!!", uTestBean2.getString2());
            int[] uIntArray = uTestBean2.getIntArray();
            Assert.assertNotNull(uIntArray);
            Assert.assertEquals(5, uIntArray.length);
            Assert.assertEquals(2, uIntArray[0]);
            Assert.assertEquals(6, uIntArray[2]);
            Assert.assertEquals(10, uIntArray[4]);
            Map<String, String> uStringMap = uTestBean2.getStringMap();
            Assert.assertNotNull(uStringMap);
            Assert.assertEquals(2, uStringMap.size());
            Assert.assertEquals("value1", uStringMap.get("key1"));
            Assert.assertEquals("value2", uStringMap.get("key2"));
        } catch (JsonException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
        }

        LightJson.setParserType(LightJson.ParserType.Scalable);
        try {
            TestBean uTestBean = LightJson.json().unmarshaller(jsonString).unmarshal(TestBean.class);
            Assert.assertNotNull(uTestBean);
            Assert.assertEquals(true, uTestBean.isBool());
            Assert.assertEquals(10, uTestBean.getNumber());
            Assert.assertEquals("Hello world", uTestBean.getString());
            Assert.assertEquals(2d, uTestBean.getDoubleValue());
            List<Integer> uIntegerList = uTestBean.getIntegerList();
            Assert.assertNotNull(uIntegerList);
            Assert.assertEquals(10, uIntegerList.size());
            Assert.assertEquals(0, (int)uIntegerList.get(0));
            Assert.assertEquals(3, (int)uIntegerList.get(3));
            Assert.assertEquals(5, (int)uIntegerList.get(5));
            Assert.assertEquals(9, (int) uIntegerList.get(9));
            TestBean2 uTestBean2 = uTestBean.getTestBean2();
            Assert.assertNotNull(uTestBean2);
            Assert.assertEquals(25, uTestBean2.getInteger2());
            Assert.assertEquals("AaBbCcDdEeFf!!!", uTestBean2.getString2());
            int[] uIntArray = uTestBean2.getIntArray();
            Assert.assertNotNull(uIntArray);
            Assert.assertEquals(5, uIntArray.length);
            Assert.assertEquals(2, uIntArray[0]);
            Assert.assertEquals(6, uIntArray[2]);
            Assert.assertEquals(10, uIntArray[4]);
            Map<String, String> uStringMap = uTestBean2.getStringMap();
            Assert.assertNotNull(uStringMap);
            Assert.assertEquals(2, uStringMap.size());
            Assert.assertEquals("value1", uStringMap.get("key1"));
            Assert.assertEquals("value2", uStringMap.get("key2"));
        } catch (JsonException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return;
        }

        try {
            JsonParser parserSimple = new ParserSimple();
            JsonParser parserScalable = new ParserScalable();

            JsonElement element1 = parserSimple.parse(TestJson1.jsonString);
            JsonElement element2 = parserScalable.parse(TestJson1.jsonString);

            Assert.assertEquals(JsonType.OBJECT, element1.getType());
            Assert.assertEquals(JsonType.OBJECT, element2.getType());

            JsonSerializer serializerRecursive = new SerializerRecursive();
            JsonSerializer serializerForkJoin = new SerializerForkJoin();

            String restoredString1 = serializerForkJoin.serialize(element1);
            String restoredString2 = serializerForkJoin.serialize(element2);
            String restoredString3 = serializerRecursive.serialize(element1);
            String restoredString4 = serializerRecursive.serialize(element2);

            int length = 57054;
            Assert.assertEquals(length, restoredString1.length());
            Assert.assertEquals(length, restoredString2.length());
            Assert.assertEquals(length, restoredString3.length());
            Assert.assertEquals(length, restoredString4.length());
        } catch (JsonException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }
}
