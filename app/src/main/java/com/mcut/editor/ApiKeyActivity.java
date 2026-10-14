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

    private static final String PREF_NAME = "McutPrefs";

    // ใช้ชื่อเดียวกันทั้งแอป
    private static final String KEY_API = "openai_api_key";

    private static final String EXTRA_EDIT_KEY = "edit_key";

    private EditText editApiKey;
    private Button btnSaveKey;

    private SharedPreferences prefs;

    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_api_key);

            editApiKey = findViewById(R.id.editApiKey);
            btnSaveKey = findViewById(R.id.btnSaveKey);

            prefs = getSharedPreferences(
                    PREF_NAME,
                    MODE_PRIVATE
            );

            editMode = getIntent().getBooleanExtra(
                    EXTRA_EDIT_KEY,
                    false
            );

            String savedKey = prefs.getString(KEY_API, "");

            /*
             * ถ้าเข้ามาจากปุ่มตั้งค่า
             * ให้แสดง API Key ที่เคยบันทึกไว้
             *
             * ถ้าเปิดแอปครั้งแรกและยังไม่มี API Key
             * ให้ผู้ใช้กรอก API Key ก่อน
             */

            if (editMode) {

                if (savedKey != null && !savedKey.isEmpty()) {
                    editApiKey.setText(savedKey);
                    editApiKey.setSelection(savedKey.length());
                }

            } else {

                if (savedKey != null && !savedKey.isEmpty()) {
                    openMainActivity();
                    return;
                }
            }

            btnSaveKey.setOnClickListener(v -> {

                String key = editApiKey
                        .getText()
                        .toString()
                        .trim();

                if (key.isEmpty()) {

                    Toast.makeText(
                            ApiKeyActivity.this,
                            "กรุณากรอก OpenAI API Key",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                /*
                 * บันทึก API Key ลง SharedPreferences
                 *
                 * หมายเหตุ:
                 * การบันทึกคีย์ในแอปมือถือโดยตรง
                 * ไม่เหมาะสำหรับแอปที่เผยแพร่ให้ผู้อื่นใช้
                 * เพราะอาจถูกดึงคีย์ออกจากแอปได้
                 */

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

                openMainActivity();
            });

        } catch (Exception e) {

            Log.e(TAG, "เกิดข้อผิดพลาดใน ApiKeyActivity", e);

            Toast.makeText(
                    this,
                    "เกิดข้อผิดพลาด: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openMainActivity() {

        try {

            Intent intent = new Intent(
                    ApiKeyActivity.this,
                    MainActivity.class
            );

            startActivity(intent);

            finish();

        } catch (Exception e) {

            Log.e(TAG, "ไม่สามารถเปิด MainActivity ได้", e);

            Toast.makeText(
                    this,
                    "ไม่สามารถเปิดหน้าหลักได้",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
