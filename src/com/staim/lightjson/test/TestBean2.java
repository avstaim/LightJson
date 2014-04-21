package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;

/**
 * Created by a_scherbinin on 21.04.14.
 */
@JsonObject(AutomaticMethodBinding = true)
public class TestBean2 {

    private int integer2;
    private String string2;

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
}