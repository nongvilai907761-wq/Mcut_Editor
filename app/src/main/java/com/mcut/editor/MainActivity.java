package com.mcut.editor;

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
    private EditText promptEditText;
    private Button btnImage, btnVideo, btnAudio, btnAiGenerate, btnSaveVideo, btnSettings;
    private VideoProcessor videoProcessor;
    private String apiKey;
    private ArrayList<MediaItem> mediaTimeline = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);

            videoProcessor = new VideoProcessor(this);

            SharedPreferences prefs = getSharedPreferences("McutPrefs", MODE_PRIVATE);
            apiKey = prefs.getString("gemini_api_key", "");

            promptEditText = findViewById(R.id.promptEditText);
            btnImage = findViewById(R.id.btnImage);
            btnVideo = findViewById(R.id.btnVideo);
            btnAudio = findViewById(R.id.btnAudio);
            btnAiGenerate = findViewById(R.id.btnAiGenerate);
            btnSaveVideo = findViewById(R.id.btnSaveVideo);
            btnSettings = findViewById(R.id.btnSettings);

            btnImage.setOnClickListener(v -> {
                mediaTimeline.add(new MediaItem("sample_image.png", "image", 3000));
                Toast.makeText(this, "เพิ่มรูปภาพลงใน Timeline แล้ว", Toast.LENGTH_SHORT).show();
            });

            btnVideo.setOnClickListener(v -> {
                mediaTimeline.add(new MediaItem("sample_video.mp4", "video", 5000));
                Toast.makeText(this, "เพิ่มวิดีโอลงใน Timeline แล้ว", Toast.LENGTH_SHORT).show();
            });

            btnAudio.setOnClickListener(v -> {
                mediaTimeline.add(new MediaItem("sample_audio.mp3", "audio", 5000));
                Toast.makeText(this, "เพิ่มเสียงลงใน Timeline แล้ว", Toast.LENGTH_SHORT).show();
            });

            btnAiGenerate.setOnClickListener(v -> {
                String prompt = promptEditText.getText().toString().trim();
                if (prompt.isEmpty()) {
                    Toast.makeText(this, "กรุณาพิมพ์คำสั่ง Prompt ก่อน", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "กำลังส่งคำสั่งไปยัง Gemini AI...", Toast.LENGTH_SHORT).show();
                
                videoProcessor.callGeminiApi(apiKey, prompt, new VideoProcessor.AiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(MainActivity.this, "AI ตอบกลับสำเร็จ!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "เกิดข้อผิดพลาด: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            });

            btnSaveVideo.setOnClickListener(v -> Toast.makeText(this, "กำลังเรนเดอร์วิดีโอ...", Toast.LENGTH_SHORT).show());
            btnSettings.setOnClickListener(v -> Toast.makeText(this, "ระบบการตั้งค่าพร้อมใช้งาน", Toast.LENGTH_SHORT).show());

        } catch (Throwable e) {
            // แปลง StackTrace ทั้งหมดให้ออกมาเป็นข้อความตัวหนังสือแบบละเอียด
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTraceString = sw.toString();

            // พิมพ์ลง Logcat แบบยาวเพื่อให้อ่านได้
            Log.e(TAG, "CRASH DETAILS: \n" + stackTraceString);

            // แสดง Error ออกมาเป็น Toast ยาวๆ บนหน้าจอทันที เพื่อให้คุณเห็นตัวหนังสือโค้ดที่พัง
            showErrorDialog(stackTraceString);
        }
    }

    private void showErrorDialog(String errorText) {
        // ตัดข้อความให้แสดงผลใน Toast แบบยาวพิเศษ หรือเด้งฟอร์มเตือน
        runOnUiThread(() -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("❌ แอปเกิดข้อผิดพลาด (Crash Details)");
            builder.setMessage(errorText);
            builder.setPositiveButton("ปิด", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }
}
