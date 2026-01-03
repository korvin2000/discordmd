package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Footer {
    private String text;

    @JsonProperty("text")
    public String getText() { return text; }
    @JsonProperty("text")
    public void setText(String value) { this.text = value; }
}
