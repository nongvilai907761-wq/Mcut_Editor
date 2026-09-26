package com.mcut.editor;

public class MediaItem {
    private String filePath;
    private String mediaType;
    private long durationMs;

    public MediaItem(String filePath, String mediaType, long durationMs) {
        this.filePath = filePath;
        this.mediaType = mediaType;
        this.durationMs = durationMs;
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
