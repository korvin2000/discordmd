package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class EmbedAuthor {
    private String iconURL;
    private String name;
    private String url;

    @JsonProperty("iconUrl")
    public String getIconURL() { return iconURL; }
    @JsonProperty("iconUrl")
    public void setIconURL(String value) { this.iconURL = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("url")
    public String getURL() { return url; }
    @JsonProperty("url")
    public void setURL(String value) { this.url = value; }
}
