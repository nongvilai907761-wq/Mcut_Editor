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

        // อ่านข้อมูลที่บันทึกไว้
        SharedPreferences prefs =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // ตรวจสอบว่าเข้าหน้านี้เพื่อแก้ไข API Key หรือไม่
        boolean editMode =
                getIntent().getBooleanExtra("edit_key", false);

        // โหลด OpenAI API Key ที่เคยบันทึกไว้
        String savedKey = prefs.getString(KEY_API, "");

        if (savedKey == null) {
            savedKey = "";
        }

        // ถ้ามีคีย์อยู่แล้ว ให้แสดงคีย์เดิมในช่องกรอก
        if (!savedKey.isEmpty()) {
            editApiKey.setText(savedKey);

            // ถ้าไม่ได้เข้ามาเพื่อแก้ไข ให้กลับหน้าหลัก
            if (!editMode) {
                navigateToMain();
                return;
            }
        }

        // ปุ่มบันทึก API Key
        btnSaveKey.setOnClickListener(v -> {

            String key = editApiKey
                    .getText()
                    .toString()
                    .trim();

            // ตรวจสอบว่าผู้ใช้กรอกคีย์หรือยัง
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
                        "บันทึก API Key ไม่สำเร็จ",
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

            // ล้างหน้า API Key ออกจากลำดับหน้าที่เปิดอยู่
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
