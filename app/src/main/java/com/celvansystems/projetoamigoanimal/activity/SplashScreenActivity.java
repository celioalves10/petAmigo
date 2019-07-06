package com.celvansystems.projetoamigoanimal.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.BuildConfig;
import com.celvansystems.projetoamigoanimal.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        inicializarComponentes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Handler handle = new Handler();
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mostrarLogin();
                }
            }, 1000);
        } else {
            mostrarLogin();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView imvPegada = findViewById(R.id.imv_pegada);
            imvPegada.setElevation(35);
        }

        //Sempre modo Retrato
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private void inicializarComponentes() {

        TextView txvVersaoSplash = findViewById(R.id.txvVersaoSplash);
        txvVersaoSplash.setText(String.format("%s %s", txvVersaoSplash.getText(), BuildConfig.VERSION_NAME));
    }

    private void mostrarLogin() {
        Intent intent = new Intent(SplashScreenActivity.this,
                LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
