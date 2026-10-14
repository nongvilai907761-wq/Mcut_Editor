package com.mcut.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "McutPrefs";
    private static final String KEY_API = "openai_api_key";

    private EditText promptEditText;
    private Button btnImage, btnVideo, btnAudio;
    private Button btnAiGenerate, btnSaveVideo, btnSettings;

    private VideoProcessor videoProcessor;
    private String apiKey = "";

    private ArrayList<MediaItem> mediaTimeline = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            videoProcessor = new VideoProcessor(this);

            loadApiKey();

            promptEditText = findViewById(R.id.promptEditText);
            btnImage = findViewById(R.id.btnImage);
            btnVideo = findViewById(R.id.btnVideo);
            btnAudio = findViewById(R.id.btnAudio);
            btnAiGenerate = findViewById(R.id.btnAiGenerate);
            btnSaveVideo = findViewById(R.id.btnSaveVideo);
            btnSettings = findViewById(R.id.btnSettings);

            btnImage.setOnClickListener(v -> {
                mediaTimeline.add(
                    new MediaItem("sample_image.png", "image", 3000)
                );

                Toast.makeText(
                    this,
                    "เพิ่มรูปภาพลงใน Timeline แล้ว",
                    Toast.LENGTH_SHORT
                ).show();
            });

            btnVideo.setOnClickListener(v -> {
                mediaTimeline.add(
                    new MediaItem("sample_video.mp4", "video", 5000)
                );

                Toast.makeText(
                    this,
                    "เพิ่มวิดีโอลงใน Timeline แล้ว",
                    Toast.LENGTH_SHORT
                ).show();
            });

            btnAudio.setOnClickListener(v -> {
                mediaTimeline.add(
                    new MediaItem("sample_audio.mp3", "audio", 5000)
                );

                Toast.makeText(
                    this,
                    "เพิ่มเสียงลงใน Timeline แล้ว",
                    Toast.LENGTH_SHORT
                ).show();
            });

            btnAiGenerate.setOnClickListener(v -> {

                String prompt = promptEditText
                    .getText()
                    .toString()
                    .trim();

                if (prompt.isEmpty()) {
                    Toast.makeText(
                        this,
                        "กรุณาพิมพ์คำสั่ง Prompt ก่อน",
                        Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                loadApiKey();

                if (apiKey.isEmpty()) {
                    Toast.makeText(
                        this,
                        "กรุณาใส่ OpenAI API Key ใน SETTINGS ก่อน",
                        Toast.LENGTH_LONG
                    ).show();

                    openApiKeySettings();
                    return;
                }

                Toast.makeText(
                    this,
                    "กำลังส่งคำสั่งไปยัง OpenAI...",
                    Toast.LENGTH_SHORT
                ).show();

                // คงชื่อเมธอดเดิมไว้ก่อน เพื่อให้เข้ากับ VideoProcessor
                // ที่มีอยู่ในโปรเจกต์
                videoProcessor.callGeminiApi(
                    apiKey,
                    prompt,
                    new VideoProcessor.AiCallback() {

                        @Override
                        public void onSuccess(String result) {
                            runOnUiThread(() ->
                                Toast.makeText(
                                    MainActivity.this,
                                    "AI ตอบกลับสำเร็จ!",
                                    Toast.LENGTH_LONG
                                ).show()
                            );
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() ->
                                Toast.makeText(
                                    MainActivity.this,
                                    "เกิดข้อผิดพลาด: " + error,
                                    Toast.LENGTH_LONG
                                ).show()
                            );
                        }
                    }
                );
            });

            btnSaveVideo.setOnClickListener(v ->
                Toast.makeText(
                    this,
                    "กำลังเรนเดอร์วิดีโอ...",
                    Toast.LENGTH_SHORT
                ).show()
            );

            btnSettings.setOnClickListener(v -> openApiKeySettings());

        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            String stackTraceString = sw.toString();

            Log.e(TAG, "CRASH DETAILS:\n" + stackTraceString);

            showErrorDialog(stackTraceString);
        }
    }

    private void loadApiKey() {
        SharedPreferences prefs =
            getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        apiKey = prefs.getString(KEY_API, "");
    }

    private void openApiKeySettings() {
        Intent intent = new Intent(
            MainActivity.this,
            ApiKeyActivity.class
        );

        // บอกหน้าตั้งค่าว่าให้เปิดโหมดแก้ไขคีย์
        intent.putExtra("edit_key", true);

        startActivity(intent);
    }

    private void showErrorDialog(String errorText) {
        runOnUiThread(() -> {
            android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);

            builder.setTitle("แอปเกิดข้อผิดพลาด");
            builder.setMessage(errorText);
            builder.setPositiveButton(
                "ปิด",
                (dialog, which) -> dialog.dismiss()
            );

            builder.show();
        });
    }
}
