package com.mcut.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ApiKeyActivity extends AppCompatActivity {

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

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedKey = prefs.getString(KEY_API, "");
        if (!savedKey.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnSaveKey.setOnClickListener(v -> {
            String key = editApiKey.getText().toString().trim();
            if (key.isEmpty()) {
                Toast.makeText(this, "กรุณากรอก API Key จาก Google AI Studio", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_API, key).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
