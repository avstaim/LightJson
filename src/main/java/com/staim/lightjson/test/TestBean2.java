package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Test Bean 2
 * Created by a_scherbinin on 21.04.14.
 */
@JsonObject(AutomaticBinding = true)
public class TestBean2 {
    private int integer2;
    private String string2;

    private int[] intArray;

    private Map<String, String> stringMap;
    private Map<String, Integer> numberMap;
    private Map<String, Map<String, TestBean2>> bean2Map;
    private Map<String, List<Map<String, String>>> complexMap;

    public TestBean2() {}

    public int getInteger2() {
        return integer2;
    }
    public void setInteger2(int integer2) {
        this.integer2 = integer2;
    }

    public String getString2() {
        return string2;
    }
    public void setString2(String string2) {
        this.string2 = string2;
    }

    public int[] getIntArray() {
        return intArray;
    }
    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public Map<String, String> getStringMap() {
        return stringMap;
    }
    public void setStringMap(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    public Map<String, Integer> getNumberMap() {
        return numberMap;
    }
    public void setNumberMap(Map<String, Integer> numberMap) {
        this.numberMap = numberMap;
    }

    public Map<String, Map<String, TestBean2>> getBean2Map() {
        return bean2Map;
    }
    public void setBean2Map(Map<String, Map<String, TestBean2>> bean2Map) {
        this.bean2Map = bean2Map;
    }

    public Map<String, List<Map<String, String>>> getComplexMap() {
        return complexMap;
    }
    public void setComplexMap(Map<String, List<Map<String, String>>> complexMap) {
        this.complexMap = complexMap;
    }
}