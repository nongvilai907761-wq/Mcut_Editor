package com.mcut.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessor {
    private static final String TAG = "VideoProcessor";
    private final Context context;
    private final Handler handler;

    public interface AiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    // Callback สำหรับการประมวลผลคิววิดีโอและรูปภาพ
    public interface ProcessCallback {
        void onProgress(String message);
        void onSuccess(List<String> processedVideoPaths);
        void onError(String error);
    }

    public VideoProcessor(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * ฟังก์ชันสำหรับจัดการรายการมีเดียทั้งหมด (รองรับทั้ง Video และ Image)
     * โดยจะแปลงรูปภาพเป็นวิดีโอ 3 วินาทีอัตโนมัติผ่าน ImageConverter
     */
    public void processMediaItems(List<MediaItem> items, List<Bitmap> bitmaps, ProcessCallback callback) {
        new Thread(() -> {
            try {
                List<String> finalVideoPaths = new ArrayList<>();
                File cacheDir = context.getCacheDir();
                int bitmapIndex = 0;

                for (int i = 0; i < items.size(); i++) {
                    final int currentIndex = i;
                    MediaItem item = items.get(i);
                    String mediaType = item.getMediaType();

                    if ("image".equalsIgnoreCase(mediaType)) {
                        handler.post(() -> callback.onProgress("กำลังแปลงรูปภาพเป็นวิดีโอ (รายการที่ " + (currentIndex + 1) + ")..."));
                        
                        if (bitmaps != null && bitmapIndex < bitmaps.size()) {
                            Bitmap bmp = bitmaps.get(bitmapIndex++);
                            if (bmp != null) {
                                String convertedVideoPath = ImageConverter.convertImageToVideo(bmp, cacheDir);
                                finalVideoPaths.add(convertedVideoPath);
                            }
                        }
                    } else if ("video".equalsIgnoreCase(mediaType)) {
                        handler.post(() -> callback.onProgress("กำลังเตรียมไฟล์วิดีโอ (รายการที่ " + (currentIndex + 1) + ")..."));
                        finalVideoPaths.add(item.getFilePath());
                    }
                }

                handler.post(() -> callback.onSuccess(finalVideoPaths));

            } catch (Exception e) {
                Log.e(TAG, "Error processing media items", e);
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    // ฟังก์ชันเชื่อมต่อ Gemini AI API ภายนอก (อัปเดตเป็น v1 และแก้ Error 404 เรียบร้อย)
    public void callGeminiApi(String apiKey, String prompt, AiCallback callback) {
        new Thread(() -> {
            try {
                // เปลี่ยนเป็น Endpoint v1 เพื่อให้เชื่อมต่อได้สำเร็จและไม่เกิด Error 404
                URL url = new URL("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
                String jsonInputString = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        
                        String aiMessage = parseGeminiResponse(response.toString());
                        handler.post(() -> callback.onSuccess(aiMessage));
                    }
                } else {
                    handler.post(() -> callback.onError("API Error Code: " + code));
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    // ฟังก์ชันเสริมสำหรับแกะข้อความคำตอบจากโครงสร้าง JSON ของ Gemini
    private String parseGeminiResponse(String jsonResponse) {
        try {
            if (jsonResponse.contains("\"text\":")) {
                int start = jsonResponse.indexOf("\"text\":") + 8;
                int end = jsonResponse.indexOf("\"", start);
                if (start > 7 && end > start) {
                    return jsonResponse.substring(start, end).replace("\\n", "\n");
                }
            }
        } catch (Exception ignored) {}
        return jsonResponse;
    }
}
