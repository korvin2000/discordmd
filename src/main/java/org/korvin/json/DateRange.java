package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class DateRange {
    private String after;
    private String before;

    @JsonProperty("after")
    public String getAfter() { return after; }
    @JsonProperty("after")
    public void setAfter(String value) { this.after = value; }

    @JsonProperty("before")
    public String getBefore() { return before; }
    @JsonProperty("before")
    public void setBefore(String value) { this.before = value; }
}
