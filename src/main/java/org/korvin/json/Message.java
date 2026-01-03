package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Message {
    private Attachment[] attachments;
    private MessageAuthor author;
    private String callEndedTimestamp;
    private String content;
    private Embed[] embeds;
    private String id;
    private MessageInlineEmoji[] inlineEmojis;
    private boolean isPinned;
    private Mention[] mentions;
    private Reaction[] reactions;
    private Reference reference;
    private Sticker[] stickers;
    private String timestamp;
    private String timestampEdited;
    private String type;

    @JsonProperty("attachments")
    public Attachment[] getAttachments() { return attachments; }
    @JsonProperty("attachments")
    public void setAttachments(Attachment[] value) { this.attachments = value; }

    @JsonProperty("author")
    public MessageAuthor getAuthor() { return author; }
    @JsonProperty("author")
    public void setAuthor(MessageAuthor value) { this.author = value; }

    @JsonProperty("callEndedTimestamp")
    public String getCallEndedTimestamp() { return callEndedTimestamp; }
    @JsonProperty("callEndedTimestamp")
    public void setCallEndedTimestamp(String value) { this.callEndedTimestamp = value; }

    @JsonProperty("content")
    public String getContent() { return content; }
    @JsonProperty("content")
    public void setContent(String value) { this.content = value; }

    @JsonProperty("embeds")
    public Embed[] getEmbeds() { return embeds; }
    @JsonProperty("embeds")
    public void setEmbeds(Embed[] value) { this.embeds = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("inlineEmojis")
    public MessageInlineEmoji[] getInlineEmojis() { return inlineEmojis; }
    @JsonProperty("inlineEmojis")
    public void setInlineEmojis(MessageInlineEmoji[] value) { this.inlineEmojis = value; }

    @JsonProperty("isPinned")
    public boolean getIsPinned() { return isPinned; }
    @JsonProperty("isPinned")
    public void setIsPinned(boolean value) { this.isPinned = value; }

    @JsonProperty("mentions")
    public Mention[] getMentions() { return mentions; }
    @JsonProperty("mentions")
    public void setMentions(Mention[] value) { this.mentions = value; }

    @JsonProperty("reactions")
    public Reaction[] getReactions() { return reactions; }
    @JsonProperty("reactions")
    public void setReactions(Reaction[] value) { this.reactions = value; }

    @JsonProperty("reference")
    public Reference getReference() { return reference; }
    @JsonProperty("reference")
    public void setReference(Reference value) { this.reference = value; }

    @JsonProperty("stickers")
    public Sticker[] getStickers() { return stickers; }
    @JsonProperty("stickers")
    public void setStickers(Sticker[] value) { this.stickers = value; }

    @JsonProperty("timestamp")
    public String getTimestamp() { return timestamp; }
    @JsonProperty("timestamp")
    public void setTimestamp(String value) { this.timestamp = value; }

    @JsonProperty("timestampEdited")
    public String getTimestampEdited() { return timestampEdited; }
    @JsonProperty("timestampEdited")
    public void setTimestampEdited(String value) { this.timestampEdited = value; }

    @JsonProperty("type")
    public String getType() { return type; }
    @JsonProperty("type")
    public void setType(String value) { this.type = value; }
}
