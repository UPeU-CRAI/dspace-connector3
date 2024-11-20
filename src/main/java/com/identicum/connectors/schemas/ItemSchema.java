package com.identicum.connectors.schemas;

import org.json.JSONObject;

/**
 * Schema representation for Item in DSpace.
 * Helps to construct and validate Item data payloads.
 */
public class ItemSchema {

    private String id;
    private String name;
    private String metadata;

    // Default Constructor
    public ItemSchema() {
    }

    // Constructor with all fields
    public ItemSchema(String id, String name, String metadata) {
        this.id = id;
        this.name = name;
        this.metadata = metadata;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Convert this object to a JSON representation.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", this.id);
        json.put("name", this.name);
        json.put("metadata", this.metadata);
        return json;
    }

    /**
     * Construct an ItemSchema object from a JSON representation.
     */
    public static ItemSchema fromJson(JSONObject json) {
        ItemSchema schema = new ItemSchema();
        schema.setId(json.optString("id"));
        schema.setName(json.optString("name"));
        schema.setMetadata(json.optString("metadata"));
        return schema;
    }
}
