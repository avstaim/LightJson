package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;

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
}