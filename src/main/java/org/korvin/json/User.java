package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class User {
    private String avatarURL;
    private String color;
    private String discriminator;
    private String id;
    private boolean isBot;
    private String name;
    private String nickname;

    @JsonProperty("avatarUrl")
    public String getAvatarURL() { return avatarURL; }
    @JsonProperty("avatarUrl")
    public void setAvatarURL(String value) { this.avatarURL = value; }

    @JsonProperty("color")
    public String getColor() { return color; }
    @JsonProperty("color")
    public void setColor(String value) { this.color = value; }

    @JsonProperty("discriminator")
    public String getDiscriminator() { return discriminator; }
    @JsonProperty("discriminator")
    public void setDiscriminator(String value) { this.discriminator = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("isBot")
    public boolean getIsBot() { return isBot; }
    @JsonProperty("isBot")
    public void setIsBot(boolean value) { this.isBot = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("nickname")
    public String getNickname() { return nickname; }
    @JsonProperty("nickname")
    public void setNickname(String value) { this.nickname = value; }
}
