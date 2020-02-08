package com.calbitica.app.Models.Habitica;

public class Progress {
    private Float up, down, collectedItems;

    public Float getUp() {
        return up;
    }

    public void setUp(Float up) {
        this.up = up;
    }

    public Float getDown() {
        return down;
    }

    public void setDown(Float down) {
        this.down = down;
    }

    public Float getCollectedItems() {
        return collectedItems;
    }

    public void setCollectedItems(Float collectedItems) {
        this.collectedItems = collectedItems;
    }
}
