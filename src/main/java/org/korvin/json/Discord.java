package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Discord {
    private Channel channel;
    private DateRange dateRange;
    private String exportedAt;
    private Guild guild;
    private long messageCount;
    private Message[] messages;

    @JsonProperty("channel")
    public Channel getChannel() { return channel; }
    @JsonProperty("channel")
    public void setChannel(Channel value) { this.channel = value; }

    @JsonProperty("dateRange")
    public DateRange getDateRange() { return dateRange; }
    @JsonProperty("dateRange")
    public void setDateRange(DateRange value) { this.dateRange = value; }

    @JsonProperty("exportedAt")
    public String getExportedAt() { return exportedAt; }
    @JsonProperty("exportedAt")
    public void setExportedAt(String value) { this.exportedAt = value; }

    @JsonProperty("guild")
    public Guild getGuild() { return guild; }
    @JsonProperty("guild")
    public void setGuild(Guild value) { this.guild = value; }

    @JsonProperty("messageCount")
    public long getMessageCount() { return messageCount; }
    @JsonProperty("messageCount")
    public void setMessageCount(long value) { this.messageCount = value; }

    @JsonProperty("messages")
    public Message[] getMessages() { return messages; }
    @JsonProperty("messages")
    public void setMessages(Message[] value) { this.messages = value; }
}
