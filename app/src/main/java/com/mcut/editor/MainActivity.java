package com.mcut.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// นำเข้าไลบรารี AndroidX Media3 สำหรับประมวลผลวิดีโอ
import androidx.media3.common.MediaItem;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText promptEditText;

    private Button btnImage;
    private Button btnVideo;
    private Button btnAudio;
    private Button btnAiGenerate;
    private Button btnSaveVideo;
    private Button btnSettings;

    private ListView mediaListView;

    private final ArrayList<com.mcut.editor.MediaItem> mediaItems =
            new ArrayList<>();

    private final ArrayList<String> mediaNames =
            new ArrayList<>();

    private ArrayAdapter<String> mediaAdapter;

    private ActivityResultLauncher<String[]> imagePicker;
    private ActivityResultLauncher<String[]> videoPicker;
    private ActivityResultLauncher<String[]> audioPicker;
    private ActivityResultLauncher<String[]> allMediaPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        promptEditText = findViewById(R.id.promptEditText);

        btnImage = findViewById(R.id.btnImage);
        btnVideo = findViewById(R.id.btnVideo);
        btnAudio = findViewById(R.id.btnAudio);
        btnAiGenerate = findViewById(R.id.btnAiGenerate);
        btnSaveVideo = findViewById(R.id.btnSaveVideo);
        btnSettings = findViewById(R.id.btnSettings);

        mediaListView = findViewById(R.id.mediaListView);

        mediaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mediaNames
        );

        mediaListView.setAdapter(mediaAdapter);

        registerFilePickers();

        // ปุ่มเลือกรูปภาพ
        btnImage.setOnClickListener(v -> {
            imagePicker.launch(new String[]{"image/*"});
        });

        // ปุ่มเลือกวิดีโอ
        btnVideo.setOnClickListener(v -> {
            videoPicker.launch(new String[]{"video/*"});
        });

        // ปุ่มเลือกเสียง
        btnAudio.setOnClickListener(v -> {
            audioPicker.launch(new String[]{"audio/*"});
        });

        // ปุ่มส่งคำสั่ง AI
        btnAiGenerate.setOnClickListener(v -> {

            String prompt = promptEditText
                    .getText()
                    .toString()
                    .trim();

            if (prompt.isEmpty()) {
                Toast.makeText(
                        this,
                        "กรุณาพิมพ์คำสั่ง AI ก่อน",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (mediaItems.isEmpty()) {
                Toast.makeText(
                        this,
                        "กรุณาเลือกรูปภาพ วิดีโอ หรือเสียงก่อน",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "รับคำสั่งแล้ว จำนวนไฟล์: "
                            + mediaItems.size()
                            + " รายการ",
                    Toast.LENGTH_LONG
            ).show();
        });

        // ปุ่มบันทึกและประมวลผลวิดีโอด้วย Media3 Transformer
        btnSaveVideo.setOnClickListener(v -> {
            if (mediaItems.isEmpty()) {
                Toast.makeText(
                        this,
                        "กรุณาเลือกไฟล์สื่ออย่างน้อย 1 รายการก่อนส่งออกวิดีโอ",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "กำลังเริ่มต้นเรนเดอร์วิดีโอด้วย Media3...",
                    Toast.LENGTH_SHORT
            ).show();

            processVideoWithMedia3();
        });

        // ปุ่มตั้งค่า
        btnSettings.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ApiKeyActivity.class
            );

            intent.putExtra("edit_key", true);

            startActivity(intent);
        });

        // แตะรายการค้างไว้เพื่อลบไฟล์ออก
        mediaListView.setOnItemLongClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0
                            && position < mediaItems.size()) {

                        mediaItems.remove(position);
                        mediaNames.remove(position);

                        mediaAdapter.notifyDataSetChanged();

                        Toast.makeText(
                                this,
                                "ลบรายการแล้ว",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    return true;
                }
        );
    }

    private void registerFilePickers() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "image")
        );

        videoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "video")
        );

        audioPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "audio")
        );

        allMediaPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "media")
        );
    }

    private void addSelectedFiles(
            List<Uri> uris,
            String type
    ) {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }

            try {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (Exception ignored) {}

            String name = getFileName(uri);

            com.mcut.editor.MediaItem item = new com.mcut.editor.MediaItem(
                    uri.toString(),
                    type,
                    0
            );

            mediaItems.add(item);

            mediaNames.add(
                    getTypeLabel(type)
                            + " : "
                            + name
            );
        }

        mediaAdapter.notifyDataSetChanged();

        Toast.makeText(
                this,
                "เพิ่มไฟล์แล้ว "
                        + uris.size()
                        + " รายการ",
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * ฟังก์ชันประมวลผลวิดีโอโดยใช้ AndroidX Media3 Transformer
     */
    private void processVideoWithMedia3() {
        try {
            // ค้นหาไฟล์วิดีโอแรกจากรายการที่ผู้ใช้เลือกมาเป็นตัวอย่างในการประมวลผล
            String inputUriString = null;
            for (com.mcut.editor.MediaItem item : mediaItems) {
                if ("video".equals(item.getType())) {
                    inputUriString = item.getUri();
                    break;
                }
            }

            if (inputUriString == null) {
                Toast.makeText(this, "ไม่พบไฟล์วิดีโอสำหรับประมวลผล กรุณาเลือกไฟล์วิดีโอ", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri inputUri = Uri.parse(inputUriString);
            File outputFile = new File(getExternalFilesDir(null), "output_edited.mp4");
            if (outputFile.exists()) {
                outputFile.delete();
            }

            // สร้าง Transformer สำหรับแปลงหรือเรนเดอร์วิดีโอ
            Transformer transformer = new Transformer.Builder(this)
                    .addListener(new Transformer.Listener() {
                        @Override
                        public void onCompleted(Transformer transformer, Composition composition, ExportResult exportResult) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "บันทึกวิดีโอสำเร็จที่: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
                        }

                        @Override
                        public void onError(Transformer transformer, Composition composition, ExportResult exportResult, ExportException exportException) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "เกิดข้อผิดพลาด: " + exportException.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    })
                    .build();

            MediaItem mediaItem = MediaItem.fromUri(inputUri);
            EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(mediaItem).build();

            // เริ่มกระบวนการแปลงไฟล์วิดีโอ
            transformer.start(editedMediaItem, outputFile.getAbsolutePath());

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getTypeLabel(String type) {
        switch (type) {
            case "image": return "รูปภาพ";
            case "video": return "วิดีโอ";
            case "audio": return "เสียง";
            default: return "สื่อ";
        }
    }

    private String getFileName(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null || name.isEmpty()) {
            return uri.toString();
        }
        int slash = name.lastIndexOf('/');
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        return name;
    }
}
