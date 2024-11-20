package com.identicum.schemas;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the schema for a Group object in DSpace.
 * Handles serialization and deserialization from/to JSON.
 */
public class GroupSchema {

    private String id;
    private String name;
    private List<String> members;

    // =====================================
    // Constructors
    // =====================================
    public GroupSchema() {
        this.members = new ArrayList<>();
    }

    public GroupSchema(String id, String name, List<String> members) {
        this.id = id;
        this.name = name;
        this.members = members != null ? members : new ArrayList<>();
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    // =====================================
    // Serialization to JSON
    // =====================================
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("members", new JSONArray(members));
        return json;
    }

    // =====================================
    // Deserialization from JSON
    // =====================================
    public static GroupSchema fromJson(JSONObject json) {
        List<String> membersList = new ArrayList<>();
        JSONArray membersArray = json.optJSONArray("members");
        if (membersArray != null) {
            for (int i = 0; i < membersArray.length(); i++) {
                membersList.add(membersArray.getString(i));
            }
        }
        return new GroupSchema(
                json.optString("id"),
                json.optString("name"),
                membersList
        );
    }

    public void setGroupName(String asStringValue) {
    }

    public void setDescription(String asStringValue) {

    }
}
