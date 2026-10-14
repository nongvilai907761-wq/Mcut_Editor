package com.mcut.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ApiKeyActivity extends AppCompatActivity {

    private static final String TAG = "ApiKeyActivity";

    private EditText editApiKey;
    private Button btnSaveKey;

    // ชื่อไฟล์สำหรับเก็บการตั้งค่า
    private static final String PREF_NAME = "McutPrefs";

    // เปลี่ยนจาก Gemini API Key เป็น OpenAI API Key
    private static final String KEY_API = "openai_api_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_api_key);

        // เชื่อมต่อช่องกรอก API Key และปุ่มบันทึก
        editApiKey = findViewById(R.id.editApiKey);
        btnSaveKey = findViewById(R.id.btnSaveKey);

        // โหลดการตั้งค่าที่บันทึกไว้
        SharedPreferences prefs =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // ตรวจสอบว่าเปิดหน้านี้จากปุ่ม SETTINGS หรือไม่
        boolean editMode = getIntent().getBooleanExtra(
                "edit_key",
                false
        );

        // โหลด OpenAI API Key ที่เคยบันทึกไว้
        String savedKey = prefs.getString(KEY_API, "");

        if (savedKey == null) {
            savedKey = "";
        }

        // หากมีคีย์เดิม ให้แสดงในช่องกรอก
        if (!savedKey.isEmpty()) {
            editApiKey.setText(savedKey);

            // หากไม่ได้เข้ามาเพื่อแก้ไข ให้เปิดหน้าหลัก
            if (!editMode) {
                navigateToMain();
                return;
            }
        }

        // ปุ่มบันทึก OpenAI API Key
        btnSaveKey.setOnClickListener(v -> {

            String key = editApiKey
                    .getText()
                    .toString()
                    .trim();

            // ตรวจสอบช่องว่าง
            if (key.isEmpty()) {
                Toast.makeText(
                        ApiKeyActivity.this,
                        "กรุณากรอก OpenAI API Key",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // บันทึก OpenAI API Key
            boolean saved = prefs.edit()
                    .putString(KEY_API, key)
                    .commit();

            if (!saved) {
                Toast.makeText(
                        ApiKeyActivity.this,
                        "บันทึก OpenAI API Key ไม่สำเร็จ",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Toast.makeText(
                    ApiKeyActivity.this,
                    "บันทึก OpenAI API Key สำเร็จ",
                    Toast.LENGTH_SHORT
            ).show();

            // กลับไปยังหน้าหลัก
            navigateToMain();
        });
    }

    // เปิดหน้าหลักของแอป
    private void navigateToMain() {
        try {
            Intent intent = new Intent(
                    ApiKeyActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);
            finish();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to start MainActivity",
                    e
            );

            Toast.makeText(
                    ApiKeyActivity.this,
                    "ไม่สามารถเปิดหน้าหลักได้",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
