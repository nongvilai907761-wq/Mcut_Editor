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
    private static final String KEY_API = "gemini_api_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_key);

        editApiKey = findViewById(R.id.editApiKey);
        btnSaveKey = findViewById(R.id.btnSaveKey);

        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String savedKey = prefs.getString(KEY_API, "");
            
            // ถ้ามีคีย์อยู่แล้ว ให้ลองเปิด MainActivity ด้วยความปลอดภัย
            if (!savedKey.isEmpty()) {
                navigateToMain();
                return;
            }

            btnSaveKey.setOnClickListener(v -> {
                String key = editApiKey.getText().toString().trim();
                if (key.isEmpty()) {
                    Toast.makeText(this, "กรุณากรอก API Key จาก Google AI Studio", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // บันทึกคีย์ลง SharedPreferences
                prefs.edit().putString(KEY_API, key).apply();
                Toast.makeText(this, "บันทึก API Key สำเร็จ", Toast.LENGTH_SHORT).show();
                
                navigateToMain();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in ApiKeyActivity", e);
            Toast.makeText(this, "เกิดข้อผิดพลาด: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToMain() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MainActivity", e);
            Toast.makeText(this, "ไม่สามารถเปิดหน้าหลักได้: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
