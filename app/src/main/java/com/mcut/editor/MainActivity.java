package com.mcut.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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

        /*
         * ถ้า activity_main.xml มี ListView
         * ที่ใช้ ID mediaListView จะสามารถแสดงรายการไฟล์ได้
         *
         * หาก XML ของคุณยังไม่มี ListView นี้
         * ต้องเพิ่มส่วนหน้าจอก่อนใช้โค้ดนี้
         */

        mediaListView = findViewById(R.id.mediaListView);

        mediaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mediaNames
        );

        mediaListView.setAdapter(mediaAdapter);

        registerFilePickers();

        btnImage.setOnClickListener(v -> {
            imagePicker.launch(new String[]{"image/*"});
        });

        btnVideo.setOnClickListener(v -> {
            videoPicker.launch(new String[]{"video/*"});
        });

        btnAudio.setOnClickListener(v -> {
            audioPicker.launch(new String[]{"audio/*"});
        });

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
             * ขั้นต่อไปต้องเชื่อม prompt และ mediaItems
             * เข้ากับระบบ AI และระบบสร้างวิดีโอจริง
             */
        });

        btnSaveVideo.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "ระบบส่งออกวิดีโอยังต้องเชื่อมกับตัวประมวลผลวิดีโอ",
                    Toast.LENGTH_LONG
            ).show();
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ApiKeyActivity.class
            );

            intent.putExtra("edit_key", true);

            startActivity(intent);
        });

        /*
         * แตะรายการค้างไว้เพื่อลบออกจากรายการ
         */
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
            ArrayList<Uri> uris,
            String type
    ) {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        for (Uri uri : uris) {

            if (uri == null) {
                continue;
            }

            /*
             * เก็บสิทธิ์อ่านไฟล์ไว้เท่าที่ Android อนุญาต
             * เพื่อให้สามารถนำ URI ไปอ่านต่อภายหลังได้
             */

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
                "เพิ่มไฟล์แล้ว " + uris.size() + " รายการ",
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
