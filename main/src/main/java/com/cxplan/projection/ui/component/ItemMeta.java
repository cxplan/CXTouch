package com.cxplan.projection.ui.component;

/**
 * @author KennyLiu
 * @created on 2018/12/14
 */
public class ItemMeta<T> {

    private String name;
    private T value;

    public ItemMeta(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }
}
