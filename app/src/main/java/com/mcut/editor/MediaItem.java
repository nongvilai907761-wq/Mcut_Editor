package com.mcut.editor;

public class MediaItem {
    private String uri;
    private String type;
    private int duration;

    // Constructor สำหรับรับค่าตอนสร้าง Object
    public MediaItem(String uri, String type, int duration) {
        this.uri = uri;
        this.type = type;
        this.duration = duration;
    }

    // เมธอดดึง Uri ของไฟล์ (ที่ MainActivity ต้องใช้)
    public String getUri() {
        return uri;
    }

    // เมธอดดึงประเภทของไฟล์ เช่น "video", "image", "audio" (ที่ MainActivity ต้องใช้)
    public String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
