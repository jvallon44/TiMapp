package com.timappweb.timapp.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable{

    public Integer id;
    public String name;
    public int count_ref;

    public Tag(String name, int count_ref) {
        this.count_ref = count_ref;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCountRef() {
        return count_ref;
    }

    public void setCountRef(int count_ref) {
        this.count_ref = count_ref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.equals(tag.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
