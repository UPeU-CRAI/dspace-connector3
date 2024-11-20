package com.identicum.connectors.schemas;

import org.json.JSONObject;

/**
 * Schema representation for Group in DSpace.
 * Helps to construct and validate Group data payloads.
 */
public class GroupSchema {

    private String id;
    private String name;
    private String description;

    // Default Constructor
    public GroupSchema() {
    }

    // Constructor with all fields
    public GroupSchema(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
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

    /**
     * Convert this object to a JSON representation.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", this.id);
        json.put("name", this.name);
        json.put("description", this.description);
        return json;
    }

    /**
     * Construct a GroupSchema object from a JSON representation.
     */
    public static GroupSchema fromJson(JSONObject json) {
        GroupSchema schema = new GroupSchema();
        schema.setId(json.optString("id"));
        schema.setName(json.optString("name"));
        schema.setDescription(json.optString("description"));
        return schema;
    }
}
