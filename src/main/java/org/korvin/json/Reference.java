package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Reference {
    private String channelID;
    private String guildID;
    private String messageID;

    @JsonProperty("channelId")
    public String getChannelID() { return channelID; }
    @JsonProperty("channelId")
    public void setChannelID(String value) { this.channelID = value; }

    @JsonProperty("guildId")
    public String getGuildID() { return guildID; }
    @JsonProperty("guildId")
    public void setGuildID(String value) { this.guildID = value; }

    @JsonProperty("messageId")
    public String getMessageID() { return messageID; }
    @JsonProperty("messageId")
    public void setMessageID(String value) { this.messageID = value; }
}
