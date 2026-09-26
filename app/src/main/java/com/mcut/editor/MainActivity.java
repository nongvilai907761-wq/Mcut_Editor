
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_MEDIA_REQUEST = 2002;

    private VideoProcessor videoProcessor;
    private final List<MediaItem> timelineList = new ArrayList<>();
    private LinearLayout timelineContainer;
    private EditText promptEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoProcessor = new VideoProcessor(this);

        // ออกแบบ Layout หน้าจอด้วยโค้ดระดับสูง ลื่นไหล รองรับทุกหน้าจอ Android
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);

        // หัวข้อแอป
        TextView tvTitle = new TextView(this);
        tvTitle.setText("🎬 Mcut Pro - High Performance Editor");
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setPadding(0, 0, 0, 24);
        mainLayout.addView(tvTitle);

        // 1. ปุ่มทดสอบระบบ FFmpeg Engine
        Button btnTest = new Button(this);
        btnTest.setText("⚡ ทดสอบระบบ FFmpeg Engine (6GB/8GB)");
        btnTest.setOnClickListener(v -> videoProcessor.testFFmpegConnection(new VideoProcessor.RenderCallback() {
            @Override
            public void onSuccess(String msg) {
                Toast.makeText(MainActivity.this, "✅ " + msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String err) {
                Toast.makeText(MainActivity.this, "❌ " + err, Toast.LENGTH_LONG).show();
            }
        }));
        mainLayout.addView(btnTest);

        // 2. ช่องใส่ AI Prompt
        promptEditText = new EditText(this);
        promptEditText.setHint("พิมพ์คำสั่ง AI Prompt ที่นี่...");
        promptEditText.setPadding(16, 16, 16, 16);
        mainLayout.addView(promptEditText);

        // 3. ปุ่มเลือกไฟล์สื่อ (รูปภาพ/วิดีโอ/เสียง)
        Button btnPick = new Button(this);
        btnPick.setText("➕ เลือกไฟล์รูปภาพ / วิดีโอ / เสียง");
        btnPick.setOnClickListener(v -> openFilePicker());
        mainLayout.addView(btnPick);

        // 4. แสดงรายการในไทม์ไลน์
        TextView tvHeader = new TextView(this);
        tvHeader.setText("\n📋 ไทม์ไลน์ปัจจุบัน:");
        tvHeader.setTextSize(14f);
        mainLayout.addView(tvHeader);

        timelineContainer = new LinearLayout(this);
        timelineContainer.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(timelineContainer);

        // 5. ปุ่มสั่งเรนเดอร์ภาพเป็นวิดีโอระดับสูง
        Button btnRender = new Button(this);
        btnRender.setText("🚀 เริ่มเรนเดอร์ภาพเป็นวิดีโอ (Render Engine)");
        btnRender.setBackgroundColor(0xFF007AFF);
        btnRender.setTextColor(0xFFFFFFFF);
        btnRender.setOnClickListener(v -> {
            Toast.makeText(this, "🔄 กำลังประมวลผลเรนเดอร์ผ่าน Engine...", Toast.LENGTH_SHORT).show();
            videoProcessor.renderTimelineToVideo(timelineList, new VideoProcessor.RenderCallback() {
                @Override
                public void onSuccess(String outputPath) {
                    Toast.makeText(MainActivity.this, "🎉 เรนเดอร์สำเร็จ! บันทึกที่: " + outputPath, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(MainActivity.this, "⚠️ เกิดข้อผิดพลาด: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
        mainLayout.addView(btnRender);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimes = {"image/*", "video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "เลือกไฟล์มีเดีย"), PICK_MEDIA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    addUriToTimeline(uri);
                }
            } else if (data.getData() != null) {
                addUriToTimeline(data.getData());
            }
            updateTimelineUI();
        }
    }

    private void addUriToTimeline(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type == null) type = "application/octet-stream";
        timelineList.add(new MediaItem(uri.toString(), type));
    }

    private void updateTimelineUI() {
        timelineContainer.removeAllViews();
        for (int i = 0; i < timelineList.size(); i++) {
            MediaItem item = timelineList.get(i);
            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + item.getFilePath());
            tv.setPadding(12, 12, 12, 12);
            timelineContainer.addView(tv);
        }
        Toast.makeText(this, "เพิ่มเข้าไทม์ไลน์แล้ว " + timelineList.size() + " รายการ", Toast.LENGTH_SHORT).show();
    }
}
