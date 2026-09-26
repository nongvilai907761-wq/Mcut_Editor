package com.mcut.editor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoProcessor {
    private static final String TAG = "VideoProcessor";
    private final Context context;
    private final Handler handler;

    // โหลด C++ Library ที่เชื่อมต่อกันอย่างถูกต้อง
    static {
        System.loadLibrary("video_engine_pro");
    }

    // ประกาศ Native Method ให้ตรงกับ C++ ด้านล่าง
    public native String stringFromJNI();

    public interface AiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public VideoProcessor(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    // ฟังก์ชันเชื่อมต่อ Gemini AI API ภายนอกแบบอะซิงโครนัส
    public void callGeminiApi(String apiKey, String prompt, AiCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String jsonInputString = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";

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
                        handler.post(() -> callback.onSuccess(response.toString()));
                    }
                } else {
                    handler.post(() -> callback.onError("API Error Code: " + code));
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}
