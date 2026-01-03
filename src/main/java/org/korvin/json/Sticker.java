package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Sticker {
    private String format;
    private String id;
    private String name;
    private String sourceURL;

    @JsonProperty("format")
    public String getFormat() { return format; }
    @JsonProperty("format")
    public void setFormat(String value) { this.format = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("sourceUrl")
    public String getSourceURL() { return sourceURL; }
    @JsonProperty("sourceUrl")
    public void setSourceURL(String value) { this.sourceURL = value; }
}
