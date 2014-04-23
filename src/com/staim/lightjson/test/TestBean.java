package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;
import com.staim.lightjson.annotations.JsonSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a_scherbinin on 17.04.14.
 */
@JsonObject(AutomaticMethodBinding = true)
public class TestBean {
    private int number;
    private String string;
    private boolean bool;
    private List<Integer> integerList;
    private TestBean2 testBean2;

    public TestBean() {}

    public int getNumber() {
        return number;
    }
    public Integer getNumberAsInteger() {
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
    @JsonSetter(genericClass = Integer.class)
    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }

    public TestBean2 getTestBean2() {
        return testBean2;
    }
    public void setTestBean2(TestBean2 testBean2) {
        this.testBean2 = testBean2;
    }

    public List<TestBean2> testBean2List() {
        List<TestBean2> testBean2s = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            TestBean2 cTestBean2 = new TestBean2();
            cTestBean2.setInteger2(i);
            cTestBean2.setString2("test" + i);
            testBean2s.add(cTestBean2);
        }
        return testBean2s;
    }
}
