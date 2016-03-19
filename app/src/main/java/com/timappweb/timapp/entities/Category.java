package com.timappweb.timapp.entities;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable{

    public int id;
    public String name;
    public int resourceBlack;
    public int resourceWhite;

    public Category(int id, String name, int resourceBlack, int resourceWhite) {
        this.id = id;
        this.name = name;
        this.resourceBlack = resourceBlack;
        this.resourceWhite = resourceWhite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public static String idsToString(List<Category> categories) {
        if (categories == null || categories.size() == 0){
            return "";
        }
        String res = String.valueOf(categories.get(0).id);

        for (int i = 1; i < categories.size(); i++){
            res += "," + String.valueOf(categories.get(i).id);
        }
        return res;
    }
}
