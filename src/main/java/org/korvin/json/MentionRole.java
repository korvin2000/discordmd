package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class MentionRole {
    private String color;
    private String id;
    private String name;
    private long position;

    @JsonProperty("color")
    public String getColor() { return color; }
    @JsonProperty("color")
    public void setColor(String value) { this.color = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("position")
    public long getPosition() { return position; }
    @JsonProperty("position")
    public void setPosition(long value) { this.position = value; }
}
