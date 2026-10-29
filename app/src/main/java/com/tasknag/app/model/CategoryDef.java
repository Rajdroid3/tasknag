package com.tasknag.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryDef implements Serializable {
    private String id;
    private String name;
    private String colorHex;
    private String iconName;
    private boolean isBuiltIn;

    public CategoryDef() {}

    public CategoryDef(String id, String name, String colorHex, String iconName, boolean isBuiltIn) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
        this.iconName = iconName;
        this.isBuiltIn = isBuiltIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "#1976D2";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName != null ? iconName : "ic_tag";
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    public void setBuiltIn(boolean builtIn) {
        isBuiltIn = builtIn;
    }

    public static List<CategoryDef> getDefaultCategories() {
        List<CategoryDef> list = new ArrayList<>();
        list.add(new CategoryDef("HEALTH", "Health", "#E53935", "ic_health", true));
        list.add(new CategoryDef("STUDY", "Study", "#3949AB", "ic_study", true));
        list.add(new CategoryDef("WORK", "Work", "#FB8C00", "ic_work", true));
        list.add(new CategoryDef("PERSONAL", "Personal", "#8E24AA", "ic_personal", true));
        list.add(new CategoryDef("EXERCISE", "Exercise", "#43A047", "ic_exercise", true));
        list.add(new CategoryDef("HOUSEHOLD", "Household", "#00897B", "ic_household", true));
        list.add(new CategoryDef("CUSTOM", "Custom", "#546E7A", "ic_custom", true));
        return list;
    }

    public static CategoryDef findCategory(List<CategoryDef> categories, String nameOrId) {
        if (nameOrId == null) return getDefaultCategories().get(3); // Personal
        for (CategoryDef cat : categories) {
            if (cat.getId().equalsIgnoreCase(nameOrId) || cat.getName().equalsIgnoreCase(nameOrId)) {
                return cat;
            }
        }
        for (CategoryDef cat : getDefaultCategories()) {
            if (cat.getId().equalsIgnoreCase(nameOrId) || cat.getName().equalsIgnoreCase(nameOrId)) {
                return cat;
            }
        }
        return new CategoryDef(nameOrId.toUpperCase(), nameOrId, "#546E7A", "ic_custom", false);
    }
}
