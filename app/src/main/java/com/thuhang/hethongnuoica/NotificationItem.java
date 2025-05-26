package com.thuhang.hethongnuoica;

public class NotificationItem {
    public String message;
    public long timestamp;
    public boolean seen;

    public NotificationItem() {}

    public NotificationItem(String message, long timestamp, boolean seen) {
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
    }
}
