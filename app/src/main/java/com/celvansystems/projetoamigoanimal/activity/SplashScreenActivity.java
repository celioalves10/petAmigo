package com.celvansystems.projetoamigoanimal.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.BuildConfig;
import com.celvansystems.projetoamigoanimal.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean crash = false;

        try {
            setContentView(R.layout.activity_splashscreen);
        } catch (Exception e) {
            crash = true;
            e.printStackTrace();
        } finally {
            if(crash) {
                mostrarLogin();
            }
        }

        inicializarComponentes();

        login();


    }

    private void login() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Handler handle = new Handler();
            handle.postDelayed(this::mostrarLogin, 600);
        } else {
            mostrarLogin();
        }
    }

    private void inicializarComponentes() {

        TextView txvVersaoSplash = findViewById(R.id.txvVersaoSplash);
        if (txvVersaoSplash != null && txvVersaoSplash.getText() != null) {
            txvVersaoSplash.setText(String.format("%s %s", txvVersaoSplash.getText(), BuildConfig.VERSION_NAME));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView imvPegada = findViewById(R.id.imv_pegada2);
            imvPegada.setElevation(35);
        }
    }

    private void mostrarLogin() {
        Intent intent = new Intent(SplashScreenActivity.this,
                LoginActivity.class);
        startActivity(intent);
        finish();
    }
}