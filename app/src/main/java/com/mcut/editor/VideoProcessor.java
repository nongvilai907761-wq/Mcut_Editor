package com.mcut.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessor {

    private static final String TAG = "VideoProcessor";

    // OpenAI API
    private static final String OPENAI_API_URL =
            "https://api.openai.com/v1/chat/completions";

    private static final String OPENAI_MODEL =
            "gpt-4.1-mini";

    private final Context context;
    private final Handler handler;

    public interface AiCallback {
        void onSuccess(String result);

        void onError(String error);
    }

    public interface ProcessCallback {
        void onProgress(String message);

        void onSuccess(List<String> processedVideoPaths);

        void onError(String error);
    }

    public VideoProcessor(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
    }

    // =====================================================
    // 1. ระบบประมวลผลรูปภาพและวิดีโอเดิม
    // =====================================================

    public void processMediaItems(
            List<MediaItem> items,
            List<Bitmap> bitmaps,
            ProcessCallback callback
    ) {

        new Thread(() -> {

            try {

                List<String> finalVideoPaths = new ArrayList<>();

                File cacheDir = context.getCacheDir();

                int bitmapIndex = 0;

                for (int i = 0; i < items.size(); i++) {

                    final int currentIndex = i;

                    MediaItem item = items.get(i);

                    if (item == null) {
                        continue;
                    }

                    String mediaType = item.getMediaType();

                    if (mediaType == null) {
                        continue;
                    }

                    if ("image".equalsIgnoreCase(mediaType)) {

                        handler.post(() ->
                                callback.onProgress(
                                        "กำลังแปลงรูปภาพเป็นวิดีโอ "
                                                + "(รายการที่ "
                                                + (currentIndex + 1)
                                                + ")..."
                                )
                        );

                        if (bitmaps != null
                                && bitmapIndex < bitmaps.size()) {

                            Bitmap bmp = bitmaps.get(bitmapIndex++);

                            if (bmp != null) {

                                String convertedVideoPath =
                                        ImageConverter.convertImageToVideo(
                                                bmp,
                                                cacheDir
                                        );

                                if (convertedVideoPath != null) {

                                    finalVideoPaths.add(
                                            convertedVideoPath
                                    );
                                }
                            }
                        }

                    } else if ("video".equalsIgnoreCase(mediaType)) {

                        handler.post(() ->
                                callback.onProgress(
                                        "กำลังเตรียมไฟล์วิดีโอ "
                                                + "(รายการที่ "
                                                + (currentIndex + 1)
                                                + ")..."
                                )
                        );

                        String videoPath = item.getFilePath();

                        if (videoPath != null
                                && !videoPath.trim().isEmpty()) {

                            finalVideoPaths.add(videoPath);
                        }
                    }

                    // หมายเหตุ:
                    // ระบบเดิมนี้ยังไม่ได้รวมเสียงลงในวิดีโอ
                    // และยังไม่ได้เรนเดอร์ Timeline ทั้งหมด
                }

                handler.post(() ->
                        callback.onSuccess(finalVideoPaths)
                );

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Error processing media items",
                        e
                );

                String message = e.getMessage();

                if (message == null) {
                    message = "ไม่ทราบสาเหตุ";
                }

                final String errorMessage =
                        "ProcessMedia Error: " + message;

                handler.post(() ->
                        callback.onError(errorMessage)
                );
            }

        }).start();
    }

    // =====================================================
    // 2. เชื่อมต่อ OpenAI API
    //
    // คงชื่อ callGeminiApi ไว้เพื่อให้ MainActivity.java
    // เดิมเรียกใช้งานได้โดยไม่ต้องแก้ชื่อฟังก์ชัน
    // =====================================================

    public void callGeminiApi(
            String apiKey,
            String prompt,
            AiCallback callback
    ) {

        new Thread(() -> {

            HttpURLConnection conn = null;

            try {

                // ตรวจสอบ API Key

                if (apiKey == null
                        || apiKey.trim().isEmpty()) {

                    postError(
                            callback,
                            "ยังไม่ได้กรอก OpenAI API Key "
                                    + "กรุณาเปิดหน้าตั้งค่า "
                                    + "แล้วกรอก API Key"
                    );

                    return;
                }

                final String cleanApiKey = apiKey.trim();

                // ตรวจสอบ Prompt

                if (prompt == null
                        || prompt.trim().isEmpty()) {

                    postError(
                            callback,
                            "กรุณากรอกคำสั่งสำหรับ AI"
                    );

                    return;
                }

                // สร้าง URL สำหรับ OpenAI API

                URL url = new URL(OPENAI_API_URL);

                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");

                conn.setConnectTimeout(20000);

                conn.setReadTimeout(60000);

                conn.setDoOutput(true);

                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                conn.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                conn.setRequestProperty(
                        "Authorization",
                        "Bearer " + cleanApiKey
                );

                // =================================================
                // สร้าง JSON Request
                // =================================================

                JSONObject requestJson = new JSONObject();

                requestJson.put(
                        "model",
                        OPENAI_MODEL
                );

                requestJson.put(
                        "temperature",
                        0.4
                );

                JSONArray messages = new JSONArray();

                // System message

                JSONObject systemMessage = new JSONObject();

                systemMessage.put(
                        "role",
                        "system"
                );

                systemMessage.put(
                        "content",
                        "คุณเป็น AI ผู้ช่วยวางแผนการตัดต่อวิดีโอ "
                                + "ตอบเป็นภาษาไทยให้เข้าใจง่าย "
                                + "เมื่อผู้ใช้ขอแผนการตัดต่อ "
                                + "ให้ระบุลำดับฉาก ระยะเวลา "
                                + "แนวทางเลือกภาพ เพลง "
                                + "และคำแนะนำสำหรับการตัดต่อ "
                                + "อย่าอ้างว่าได้สร้างหรือเรนเดอร์ "
                                + "วิดีโอแล้ว หากยังไม่ได้ทำจริง"
                );

                messages.put(systemMessage);

                // User message

                JSONObject userMessage = new JSONObject();

                userMessage.put(
                        "role",
                        "user"
                );

                userMessage.put(
                        "content",
                        prompt
                );

                messages.put(userMessage);

                requestJson.put(
                        "messages",
                        messages
                );

                // แปลง JSON เป็น UTF-8

                byte[] requestBody =
                        requestJson.toString().getBytes(
                                StandardCharsets.UTF_8
                        );

                // ส่ง Request ไปยัง OpenAI

                try (OutputStream os = conn.getOutputStream()) {

                    os.write(requestBody);

                    os.flush();
                }

                // อ่าน HTTP Status Code

                int responseCode = conn.getResponseCode();

                InputStream responseStream;

                if (responseCode >= 200
                        && responseCode < 300) {

                    responseStream = conn.getInputStream();

                } else {

                    responseStream = conn.getErrorStream();
                }

                String responseText =
                        readStream(responseStream);

                // ตรวจสอบผลลัพธ์

                if (responseCode >= 200
                        && responseCode < 300) {

                    String aiMessage =
                            parseOpenAIResponse(responseText);

                    if (aiMessage == null
                            || aiMessage.trim().isEmpty()) {

                        postError(
                                callback,
                                "OpenAI API ตอบกลับมา "
                                        + "แต่ไม่พบข้อความคำตอบ"
                        );

                        return;
                    }

                    Log.d(
                            TAG,
                            "OpenAI API request successful"
                    );

                    postSuccess(
                            callback,
                            aiMessage
                    );

                } else {

                    String errorMessage =
                            createApiErrorMessage(
                                    responseCode,
                                    responseText
                            );

                    Log.e(
                            TAG,
                            errorMessage
                    );

                    postError(
                            callback,
                            errorMessage
                    );
                }

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "OpenAI API connection error",
                        e
                );

                String message = e.getMessage();

                if (message == null) {
                    message = "ไม่ทราบสาเหตุ";
                }

                final String errorMessage =
                        "เชื่อมต่อ OpenAI API ไม่สำเร็จ: "
                                + message;

                postError(
                        callback,
                        errorMessage
                );

            } finally {

                if (conn != null) {
                    conn.disconnect();
                }
            }

        }).start();
    }

    // =====================================================
    // 3. อ่านข้อมูลจาก Response Stream
    // =====================================================

    private String readStream(InputStream inputStream)
            throws Exception {

        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                response.append(line);
            }
        }

        return response.toString();
    }

    // =====================================================
    // 4. แยกข้อความคำตอบจาก OpenAI API
    // =====================================================

    private String parseOpenAIResponse(
            String jsonResponse
    ) throws Exception {

        JSONObject root = new JSONObject(jsonResponse);

        JSONArray choices = root.optJSONArray("choices");

        if (choices == null || choices.length() == 0) {

            throw new Exception(
                    "ไม่พบ choices ในคำตอบจาก OpenAI"
            );
        }

        JSONObject firstChoice =
                choices.getJSONObject(0);

        JSONObject message =
                firstChoice.optJSONObject("message");

        if (message == null) {

            throw new Exception(
                    "ไม่พบ message ในคำตอบจาก OpenAI"
            );
        }

        String content = message.optString(
                "content",
                ""
        );

        if (content == null || content.trim().isEmpty()) {

            throw new Exception(
                    "OpenAI ส่งข้อความคำตอบกลับมาเป็นค่าว่าง"
            );
        }

        return content.trim();
    }

    // =====================================================
    // 5. สร้างข้อความ Error ที่อ่านเข้าใจง่าย
    // =====================================================

    private String createApiErrorMessage(
            int responseCode,
            String responseText
    ) {

        StringBuilder message = new StringBuilder();

        message.append(
                "OpenAI API Error Code: "
        );

        message.append(responseCode);

        message.append("\n");

        String apiMessage = responseText;

        try {

            JSONObject root = new JSONObject(responseText);

            JSONObject errorObject =
                    root.optJSONObject("error");

            if (errorObject != null) {

                String errorText = errorObject.optString(
                        "message",
                        ""
                );

                String errorType = errorObject.optString(
                        "type",
                        ""
                );

                if (!errorText.isEmpty()) {

                    apiMessage = errorText;
                }

                if (!errorType.isEmpty()) {

                    message.append("ประเภทข้อผิดพลาด: ");

                    message.append(errorType);

                    message.append("\n");
                }
            }

        } catch (Exception ignored) {

            // ถ้าอ่าน JSON ไม่ได้ จะใช้ข้อความดิบ
            // จากเซิร์ฟเวอร์แทน
        }

        message.append("รายละเอียด: ");

        message.append(apiMessage);

        // คำแนะนำเบื้องต้นตาม HTTP Status

        if (responseCode == 401) {

            message.append(
                    "\nกรุณาตรวจสอบ OpenAI API Key"
            );

        } else if (responseCode == 403) {

            message.append(
                    "\nกรุณาตรวจสอบสิทธิ์การใช้งาน API"
            );

        } else if (responseCode == 429) {

            message.append(
                    "\nกรุณาตรวจสอบโควตาและยอดเงิน "
                            + "สำหรับการใช้งาน OpenAI API"
            );

        } else if (responseCode >= 500) {

            message.append(
                    "\nเซิร์ฟเวอร์อาจมีปัญหาชั่วคราว "
                            + "กรุณาลองใหม่ภายหลัง"
            );
        }

        return message.toString();
    }

    // =====================================================
    // 6. ส่งผลลัพธ์กลับไปยัง Main Thread
    // =====================================================

    private void postSuccess(
            AiCallback callback,
            String result
    ) {

        handler.post(() -> {

            if (callback != null) {

                callback.onSuccess(result);
            }
        });
    }

    private void postError(
            AiCallback callback,
            String error
    ) {

        handler.post(() -> {

            if (callback != null) {

                callback.onError(error);
            }
        });
    }
}
