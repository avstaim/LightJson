package com.staim.lightjson.test;

import com.staim.lightjson.JsonType;
import com.staim.lightjson.annotations.JsonGetter;
import com.staim.lightjson.annotations.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a_scherbinin on 17.04.14.
 */
@JsonObject
public class TestBean {
    private int number;
    private String string;
    private boolean bool;
    private List<Integer> integerList;
    private TestBean2 testBean2;

    public TestBean() {}

    @JsonGetter
    public int getNumber() {
        return number;
    }
    @JsonGetter(name="customInteger")
    public Integer getNumberAsInteger() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @JsonGetter(type=JsonType.STRING)
    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }

    @JsonGetter(type=JsonType.BOOLEAN)
    public boolean isBool() {
        return bool;
    }
    public void setBool(boolean bool) {
        this.bool = bool;
    }

    @JsonGetter
    public List<Integer> getIntegerList() {
        return integerList;
    }
    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }

    @JsonGetter(type = JsonType.OBJECT, name = "bean2")
    public TestBean2 getTestBean2() {
        return testBean2;
    }
    public void setTestBean2(TestBean2 testBean2) {
        this.testBean2 = testBean2;
    }

    @JsonGetter(type = JsonType.ARRAY, name = "testBean2List")
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
