package com.atiqur.pidtuner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.atiqur.pidtuner.databinding.ActivitySettingsBinding;
import com.atiqur.pidtuner.utils.HelperUtils;
import com.atiqur.pidtuner.utils.ToolbarHelper;

import java.math.BigInteger;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    ActivitySettingsBinding binding;
    public static final String KEY_KP = "keyKp";
    public static final String KEY_KD = "keyKd";
    public static final String KEY_KI = "keyKi";
    public static final String SHARED_PREFS = "sharedPref";


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

        loadData();
        errorControl();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (binding.kpEditText.getEditText() != null) {
            binding.kpEditText.getEditText().setText(String.valueOf((int) sharedPreferences.getFloat(KEY_KP, 50f)));
        }
        if (binding.kdEditText.getEditText() != null) {
            binding.kdEditText.getEditText().setText(String.valueOf((int) sharedPreferences.getFloat(KEY_KD, 50f)));
        }
        if (binding.kiEditText.getEditText() != null) {
            binding.kiEditText.getEditText().setText(String.valueOf((int) sharedPreferences.getFloat(KEY_KI, 50f)));
        }
    }

    private void errorControl() {
        Objects.requireNonNull(binding.kpEditText.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!HelperUtils.convertString(s).isEmpty() && (new BigInteger(HelperUtils.convertString(s)).compareTo(new BigInteger("900"))) <= 0) {
                    binding.kpEditText.setError("");
                } else {
                    binding.kpEditText.setError("Please set value between 1 to 900");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Objects.requireNonNull(binding.kdEditText.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!HelperUtils.convertString(s).isEmpty() && (new BigInteger(HelperUtils.convertString(s)).compareTo(new BigInteger("900"))) <= 0) {
                    binding.kdEditText.setError("");
                } else {
                    binding.kdEditText.setError("Please set value between 1 to 900");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Objects.requireNonNull(binding.kiEditText.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!HelperUtils.convertString(s).isEmpty() && (new BigInteger(HelperUtils.convertString(s)).compareTo(new BigInteger("900"))) <= 0) {
                    binding.kiEditText.setError("");
                } else {
                    binding.kiEditText.setError("Please set value between 1 to 900");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void done(View view) {

        if (binding.kpEditText.getEditText() != null && binding.kdEditText.getEditText() != null && binding.kiEditText.getEditText() != null) {
            if (!binding.kpEditText.getEditText().getText().toString().isEmpty() && !binding.kdEditText.getEditText().getText().toString().isEmpty() &&
                    !binding.kiEditText.getEditText().getText().toString().isEmpty()) {
                float kp = Float.parseFloat(binding.kpEditText.getEditText().getText().toString());
                float kd = Float.parseFloat(binding.kdEditText.getEditText().getText().toString());
                float ki = Float.parseFloat(binding.kiEditText.getEditText().getText().toString());
                if (kp > 0 && kd > 0 && ki > 0) {
                    saveData(kp, kd, ki);
                    Intent intent = new Intent();
                    intent.putExtra(KEY_KP, kp);
                    intent.putExtra(KEY_KD, kd);
                    intent.putExtra(KEY_KI, ki);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    private void saveData(float kp, float kd, float ki) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat(KEY_KP, kp);
        editor.putFloat(KEY_KD, kd);
        editor.putFloat(KEY_KI, ki);

        editor.apply();
    }
}