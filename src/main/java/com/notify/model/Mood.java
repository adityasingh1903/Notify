package com.notify.model;

public enum Mood {
    HAPPY("😊 Happy"),
    SAD("😢 Sad"),
    EXCITED("🤩 Excited"),
    CALM("😌 Calm"),
    ANXIOUS("😰 Anxious"),
    GRATEFUL("🙏 Grateful");

    private final String displayName;

    Mood(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}