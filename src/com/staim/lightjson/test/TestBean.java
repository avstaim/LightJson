package com.staim.lightjson.test;

import com.staim.lightjson.annotations.JsonObject;

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

    public TestBean() {}

    //@JsonGetter
    public int getNumber() {
        return number;
    }
    //@JsonGetter
    public int getNumberAsInteger() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    //@JsonGetter(type=JsonElement.JSONType.STRING)
    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }

    //@JsonGetter(type=JsonElement.JSONType.BOOLEAN)
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
}
