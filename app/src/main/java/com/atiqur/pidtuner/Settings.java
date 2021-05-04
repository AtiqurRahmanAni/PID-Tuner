package com.atiqur.pidtuner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.atiqur.pidtuner.databinding.ActivitySettingsBinding;
import com.atiqur.pidtuner.utils.ToolbarHelper;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    ActivitySettingsBinding binding;
    public static String key_kp = "keyKp";
    public static String key_kd = "keyKd";
    public static String key_ki = "keyKi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        ToolbarHelper.create(binding.toolbar, null, this, "Settings");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        errorControl();
    }

    private void errorControl() {
        binding.kpEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //int temp = Integer.parseInt(binding.kpEditText.getEditText().getText().toString());
                //Log.d("Input",temp+"");
                if (binding.kpEditText.getEditText().getText().toString().isEmpty()) {
                    binding.kpEditText.setError("Please set value between 1 to 300");
                } else {
                    binding.kpEditText.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void done(View view) {
        Intent intent = new Intent();
        intent.putExtra(key_kp, Float.parseFloat(binding.kpEditText.getEditText().getText().toString()));
        intent.putExtra(key_kd, Float.parseFloat(binding.kdEditText.getEditText().getText().toString()));
        intent.putExtra(key_ki, Float.parseFloat(binding.kiEditText.getEditText().getText().toString()));
        setResult(-1, intent);
        finish();
    }
}