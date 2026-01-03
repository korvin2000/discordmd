package org.korvin.json;

import com.fasterxml.jackson.annotation.*;

public class Reaction {
    private long count;
    private Emoji emoji;
    private User[] users;

    @JsonProperty("count")
    public long getCount() { return count; }
    @JsonProperty("count")
    public void setCount(long value) { this.count = value; }

    @JsonProperty("emoji")
    public Emoji getEmoji() { return emoji; }
    @JsonProperty("emoji")
    public void setEmoji(Emoji value) { this.emoji = value; }

    @JsonProperty("users")
    public User[] getUsers() { return users; }
    @JsonProperty("users")
    public void setUsers(User[] value) { this.users = value; }
}
