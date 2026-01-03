package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Channel {
    private String category;
    private String categoryID;
    private String id;
    private String name;
    private String topic;
    private String type;

    @JsonProperty("category")
    public String getCategory() { return category; }
    @JsonProperty("category")
    public void setCategory(String value) { this.category = value; }

    @JsonProperty("categoryId")
    public String getCategoryID() { return categoryID; }
    @JsonProperty("categoryId")
    public void setCategoryID(String value) { this.categoryID = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("topic")
    public String getTopic() { return topic; }
    @JsonProperty("topic")
    public void setTopic(String value) { this.topic = value; }

    @JsonProperty("type")
    public String getType() { return type; }
    @JsonProperty("type")
    public void setType(String value) { this.type = value; }
}
