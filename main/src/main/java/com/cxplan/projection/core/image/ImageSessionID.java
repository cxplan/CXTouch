package com.cxplan.projection.core.image;

import java.util.Objects;

/**
 * @author Kenny
 * created on 2018/11/22
 */
public class ImageSessionID {

    public static final int TYPE_NODE = 1;
    public static final int TYPE_HUB = 2;

    private int type;//The node type
    private String id;

    public ImageSessionID(int type, String id) {
        this.type = type;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageSessionID)) return false;
        ImageSessionID imageSessionID = (ImageSessionID) o;
        return type == imageSessionID.type &&
                Objects.equals(id, imageSessionID.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return "ImageSessionID{" +
                "type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}
