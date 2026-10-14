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

    private final ArrayList<MediaItem> mediaItems =
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

            /*
             * คงการทำงานเดิมไว้
             * ส่วนนี้ยังต้องเชื่อมกับระบบ AI
             * และระบบประมวลผลวิดีโอจริง
             */
        });

        // ปุ่มบันทึกวิดีโอ
        btnSaveVideo.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "ระบบส่งออกวิดีโอยังต้องเชื่อมกับตัวประมวลผลวิดีโอ",
                    Toast.LENGTH_LONG
            ).show();
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

        // ตัวเลือกหลายรูปภาพ
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "image")
        );

        // ตัวเลือกหลายวิดีโอ
        videoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "video")
        );

        // ตัวเลือกหลายไฟล์เสียง
        audioPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "audio")
        );

        // ตัวเลือกสื่อทั่วไป
        allMediaPicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> addSelectedFiles(uris, "media")
        );
    }

    /*
     * แก้ไขจุดสำคัญ:
     * เปลี่ยน ArrayList<Uri> เป็น List<Uri>
     * เพื่อให้ตรงกับชนิดข้อมูลที่ตัวเลือกไฟล์ส่งกลับมา
     */
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

            // เก็บสิทธิ์อ่านไฟล์ไว้เมื่อ Android อนุญาต
            try {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
                // บาง URI อาจไม่รองรับการเก็บสิทธิ์ถาวร
            }

            String name = getFileName(uri);

            MediaItem item = new MediaItem(
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

    private String getTypeLabel(String type) {

        switch (type) {

            case "image":
                return "รูปภาพ";

            case "video":
                return "วิดีโอ";

            case "audio":
                return "เสียง";

            default:
                return "สื่อ";
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
