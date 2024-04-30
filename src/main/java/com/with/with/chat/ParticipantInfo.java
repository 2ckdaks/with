package com.with.with.chat;

import java.util.Set;

public class ParticipantInfo {
    private int count;
    private Set<String> names;

    public ParticipantInfo(int count, Set<String> names) {
        this.count = count;
        this.names = names;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }
}
