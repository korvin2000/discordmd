package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class MessageInlineEmoji {
    private String code;
    private String id;
    private String imageURL;
    private boolean isAnimated;
    private String name;

    @JsonProperty("code")
    public String getCode() { return code; }
    @JsonProperty("code")
    public void setCode(String value) { this.code = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("imageUrl")
    public String getImageURL() { return imageURL; }
    @JsonProperty("imageUrl")
    public void setImageURL(String value) { this.imageURL = value; }

    @JsonProperty("isAnimated")
    public boolean getIsAnimated() { return isAnimated; }
    @JsonProperty("isAnimated")
    public void setIsAnimated(boolean value) { this.isAnimated = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }
}
