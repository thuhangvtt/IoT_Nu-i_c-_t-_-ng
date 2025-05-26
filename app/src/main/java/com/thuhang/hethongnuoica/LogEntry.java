package com.thuhang.hethongnuoica;

public class LogEntry {
    public String time;
    public String mode;
    public float gram;

    public LogEntry() {} // Bắt buộc cho Firebase

    public LogEntry(String time, String mode, float gram) {
        this.time = time;
        this.mode = mode;
        this.gram = gram;
    }
}
