package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;

import java.util.List;

/**
 * Test Bean
 *
 * Created by a_scherbinin on 17.04.14.
 */
@JsonObject(AutomaticBinding = true)
public class TestBean {
    private int number;
    private String string;
    private boolean bool;
    private List<Integer> integerList;
    private TestBean2 testBean2;

    private double doubleValue;

    public TestBean() {}

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }

    public boolean isBool() {
        return bool;
    }
    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public List<Integer> getIntegerList() {
        return integerList;
    }
    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }

    public TestBean2 getTestBean2() {
        return testBean2;
    }
    public void setTestBean2(TestBean2 testBean2) {
        this.testBean2 = testBean2;
    }

    public double getDoubleValue() {
        return doubleValue;
    }
    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }
}
