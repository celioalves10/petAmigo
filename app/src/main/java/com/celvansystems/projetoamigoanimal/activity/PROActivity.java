package com.celvansystems.projetoamigoanimal.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.GerenciadorPRO;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class PROActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;
    private View layout;
    private Button btnPRO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.usuario_pro));

        if (GerenciadorPRO.isPRO) {
            startActivity(new Intent(PROActivity.this, UsuarioPROActivity.class));
            finish();
        }
        inializaComponentes();
        configuraNavBar();
    }

    private void configuraNavBar() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.lightgray));
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inializaComponentes() {

        try {
            bp = new BillingProcessor(this, Constantes.LICENSE_KEY_GOOGLE_PLAY, this);
            bp.initialize();

            layout = findViewById(R.id.linear_layout_pro);
            btnPRO = findViewById(R.id.btn_pro);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnPRO.setElevation(10);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        // ação dos botões de doação
        configuraAcoesBotoes();
    }

    private void configuraAcoesBotoes() {

        btnPRO.setOnClickListener(view -> {

            if (ConfiguracaoFirebase.isUsuarioLogado()) {
                bp.consumePurchase(Constantes.PRODUCT_ID_5_REAIS);
                bp.purchase(PROActivity.this, Constantes.PRODUCT_ID_5_REAIS);
            } else {
                Util.setSnackBar(layout, getString(R.string.usuario_nao_logado));
            }
        });
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Log.d("INFO89", productId + details);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        //Util.setSnackBar(layout, "History restored!");
        Log.d("INFO89", "history");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Util.setSnackBar(layout, getString(R.string.error) + errorCode);
        Log.d("INFO89", "erro");
    }

    @Override
    public void onBillingInitialized() {
        //Util.setSnackBar(layout, getString(R.string.escolha_opcao_ajude_manter_app));
        Log.d("INFO89", "billing initialized");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //salva como versao PRO
        if (resultCode != 0) {
            salvaUsuarioPROnoBD();
        }
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void salvaUsuarioPROnoBD() {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {

            try {
                DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                        .child("usuarios");
                String id = ConfiguracaoFirebase.getIdUsuario();

                usuariosRef.child(id).child("pro").setValue("true").addOnCompleteListener(task -> {
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("pro", true); // Storing boolean - true/false
                    editor.apply(); // commit changes
                    GerenciadorPRO.isPRO = true;
                    startActivity(new Intent(PROActivity.this, UsuarioPROActivity.class));
                    Log.d("INFO89", "PRO true!");
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}
