package com.identicum.schemas;

import org.json.JSONObject;

/**
 * Represents the schema for an Item object in DSpace.
 * Handles serialization and deserialization from/to JSON.
 */
public class ItemSchema {
    private String id;
    private String name;
    private String description;

    // Additional fields specific to the Item
    private String type;

    // =====================================
    // Constructors
    // =====================================
    public ItemSchema() {}

    public ItemSchema(String id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // =====================================
    // Getters and Setters
    // =====================================
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // =====================================
    // Serialization to JSON
    // =====================================
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("description", description);
        json.put("type", type);
        return json;
    }

    // =====================================
    // Deserialization from JSON
    // =====================================
    public static ItemSchema fromJson(JSONObject json) {
        return new ItemSchema(
                json.optString("id"),
                json.optString("name"),
                json.optString("description"),
                json.optString("type")
        );
    }

    public void setItemName(String asStringValue) {
    }

    public void setItemDescription(String asStringValue) {

    }
}
