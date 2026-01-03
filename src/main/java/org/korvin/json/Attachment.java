package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Attachment {
    private String fileName;
    private long fileSizeBytes;
    private String id;
    private String url;

    @JsonProperty("fileName")
    public String getFileName() { return fileName; }
    @JsonProperty("fileName")
    public void setFileName(String value) { this.fileName = value; }

    @JsonProperty("fileSizeBytes")
    public long getFileSizeBytes() { return fileSizeBytes; }
    @JsonProperty("fileSizeBytes")
    public void setFileSizeBytes(long value) { this.fileSizeBytes = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("url")
    public String getURL() { return url; }
    @JsonProperty("url")
    public void setURL(String value) { this.url = value; }
}
