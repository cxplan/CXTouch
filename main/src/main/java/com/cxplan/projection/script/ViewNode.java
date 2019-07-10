package com.cxplan.projection.script;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Kenny
 * created on 2019/3/28
 */
public class ViewNode implements Serializable {

    private String text;
    private String resourceId;
    private String className;
    private String packageName;
    private String contentDesc;
    private boolean checkable;
    private boolean checked;
    private boolean clickable;
    private boolean enabled;
    private boolean focusable;
    private boolean focused;
    private boolean scrollable;
    private boolean longClickable;
    private boolean password;
    private boolean selected;
    private boolean editable;
    private boolean visible;
    private ScriptRect bound;

    @JsonProperty("t")
    public String getText() {
        return text;
    }
    @JsonProperty("t")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("ri")
    public String getResourceId() {
        return resourceId;
    }
    @JsonProperty("ri")
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @JsonProperty("cn")
    public String getClassName() {
        return className;
    }
    @JsonProperty("cn")
    public void setClassName(String className) {
        this.className = className;
    }

    @JsonProperty("pn")
    public String getPackageName() {
        return packageName;
    }
    @JsonProperty("pn")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    @JsonProperty("cd")
    public String getContentDesc() {
        return contentDesc;
    }
    @JsonProperty("cd")
    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }
    @JsonProperty("ic")
    public boolean isCheckable() {
        return checkable;
    }
    @JsonProperty("ic")
    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }
    @JsonProperty("icd")
    public boolean isChecked() {
        return checked;
    }
    @JsonProperty("icd")
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    @JsonProperty("ica")
    public boolean isClickable() {
        return clickable;
    }
    @JsonProperty("ica")
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    @JsonProperty("ie")
    public boolean isEnabled() {
        return enabled;
    }
    @JsonProperty("ie")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    @JsonProperty("if")
    public boolean isFocusable() {
        return focusable;
    }
    @JsonProperty("if")
    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }
    @JsonProperty("ifd")
    public boolean isFocused() {
        return focused;
    }
    @JsonProperty("ifd")
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    @JsonProperty("is")
    public boolean isScrollable() {
        return scrollable;
    }
    @JsonProperty("is")
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
    @JsonProperty("ilc")
    public boolean isLongClickable() {
        return longClickable;
    }
    @JsonProperty("ilc")
    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }
    @JsonProperty("ip")
    public boolean isPassword() {
        return password;
    }
    @JsonProperty("ip")
    public void setPassword(boolean password) {
        this.password = password;
    }
    @JsonProperty("isd")
    public boolean isSelected() {
        return selected;
    }
    @JsonProperty("isd")
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    @JsonProperty("iv")
    public boolean isVisible() {
        return visible;
    }
    @JsonProperty("iv")
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    @JsonProperty("iea")
    public boolean isEditable() {
        return editable;
    }
    @JsonProperty("iea")
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @JsonProperty("b")
    public ScriptRect getBound() {
        return bound;
    }
    @JsonProperty("b")
    public void setBound(ScriptRect bound) {
        this.bound = bound;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{text=[");
        sb.append(text).append("], resourceId=[").append(resourceId).append("], className=[").append(className).
                append("], packageName=[").append(packageName).append("], contentDesc=[").append(contentDesc).
                append("], checkable=").append(checkable).append(", checked=").append(checked).
                append(", clickable=").append(clickable).append(", enabled=").append(enabled).
                append(", focusable=").append(focusable).append(", focused=").append(focused).
                append(", scrollable=").append(scrollable).append(", longClickable=").append(longClickable).
                append(", password=").append(password).append(", selected=").append(selected).
                append(", editable=").append(editable).append(", visible=").append(visible).append(", bound=[").
                append(bound.toString()).append("]");
        return sb.toString();
    }
}
