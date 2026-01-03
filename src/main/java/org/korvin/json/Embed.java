package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Embed {
    private EmbedAuthor author;
    private String color;
    private String description;
    private Field[] fields;
    private Footer footer;
    private String[] images;
    private EmbedInlineEmoji[] inlineEmojis;
    private Thumbnail thumbnail;
    private String timestamp;
    private String title;
    private String url;
    private Video video;

    @JsonProperty("author")
    public EmbedAuthor getAuthor() { return author; }
    @JsonProperty("author")
    public void setAuthor(EmbedAuthor value) { this.author = value; }

    @JsonProperty("color")
    public String getColor() { return color; }
    @JsonProperty("color")
    public void setColor(String value) { this.color = value; }

    @JsonProperty("description")
    public String getDescription() { return description; }
    @JsonProperty("description")
    public void setDescription(String value) { this.description = value; }

    @JsonProperty("fields")
    public Field[] getFields() { return fields; }
    @JsonProperty("fields")
    public void setFields(Field[] value) { this.fields = value; }

    @JsonProperty("footer")
    public Footer getFooter() { return footer; }
    @JsonProperty("footer")
    public void setFooter(Footer value) { this.footer = value; }

    @JsonProperty("images")
    public String[] getImages() { return images; }
    @JsonProperty("images")
    public void setImages(String[] value) { this.images = value; }

    @JsonProperty("inlineEmojis")
    public EmbedInlineEmoji[] getInlineEmojis() { return inlineEmojis; }
    @JsonProperty("inlineEmojis")
    public void setInlineEmojis(EmbedInlineEmoji[] value) { this.inlineEmojis = value; }

    @JsonProperty("thumbnail")
    public Thumbnail getThumbnail() { return thumbnail; }
    @JsonProperty("thumbnail")
    public void setThumbnail(Thumbnail value) { this.thumbnail = value; }

    @JsonProperty("timestamp")
    public String getTimestamp() { return timestamp; }
    @JsonProperty("timestamp")
    public void setTimestamp(String value) { this.timestamp = value; }

    @JsonProperty("title")
    public String getTitle() { return title; }
    @JsonProperty("title")
    public void setTitle(String value) { this.title = value; }

    @JsonProperty("url")
    public String getURL() { return url; }
    @JsonProperty("url")
    public void setURL(String value) { this.url = value; }

    @JsonProperty("video")
    public Video getVideo() { return video; }
    @JsonProperty("video")
    public void setVideo(Video value) { this.video = value; }
}
