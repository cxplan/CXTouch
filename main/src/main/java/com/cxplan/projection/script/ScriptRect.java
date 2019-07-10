package com.cxplan.projection.script;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Kenny
 * created on 2019/3/28
 */
public class ScriptRect implements Serializable {
    private int left;
    private int top;
    private int right;
    private int bottom;

    public ScriptRect() {}
    public ScriptRect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @JsonProperty("l")
    public int getLeft() {
        return left;
    }
    @JsonProperty("l")
    public void setLeft(int left) {
        this.left = left;
    }

    @JsonProperty("t")
    public int getTop() {
        return top;
    }

    @JsonProperty("t")
    public void setTop(int top) {
        this.top = top;
    }

    @JsonProperty("r")
    public int getRight() {
        return right;
    }

    @JsonProperty("r")
    public void setRight(int right) {
        this.right = right;
    }

    @JsonProperty("b")
    public int getBottom() {
        return bottom;
    }

    @JsonProperty("b")
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    /**
     * Returns true if the rectangle is empty (left >= right or top >= bottom)
     */
    @JsonIgnore
    public final boolean isEmpty() {
        return left >= right || top >= bottom;
    }

    /**
     * @return the rectangle's width. This does not check for a valid rectangle
     * (i.e. left <= right) so the result may be negative.
     */
    public final int width() {
        return right - left;
    }

    /**
     * @return the rectangle's height. This does not check for a valid rectangle
     * (i.e. top <= bottom) so the result may be negative.
     */
    public final int height() {
        return bottom - top;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptRect r = (ScriptRect) o;
        return left == r.left && top == r.top && right == r.right && bottom == r.bottom;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect("); sb.append(left); sb.append(", ");
        sb.append(top); sb.append(" - "); sb.append(right);
        sb.append(", "); sb.append(bottom); sb.append(")");
        return sb.toString();
    }
}
