package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Field {
    private boolean isInline;
    private String name;
    private String value;

    @JsonProperty("isInline")
    public boolean getIsInline() { return isInline; }
    @JsonProperty("isInline")
    public void setIsInline(boolean value) { this.isInline = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("value")
    public String getValue() { return value; }
    @JsonProperty("value")
    public void setValue(String value) { this.value = value; }
}
