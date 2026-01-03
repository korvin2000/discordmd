package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Guild {
    private String iconURL;
    private String id;
    private String name;

    @JsonProperty("iconUrl")
    public String getIconURL() { return iconURL; }
    @JsonProperty("iconUrl")
    public void setIconURL(String value) { this.iconURL = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }
}
