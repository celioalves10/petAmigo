package com.celvansystems.projetoamigoanimal.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Util;

import java.util.Random;

public class DoacaoActivity extends AppCompatActivity {

    //private View view;
    private BillingProcessor bp;
    private View layout;
    private ImageView imvDoacao;
    private Button btnDoar3, btnDoar5, btnDoar10, btnDoar50, btnDoar100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doacao);

        inializaComponentes();
        carregarFotoMarketing();
    }

    /**
     * escolhe aleatoriamente a foto usada em DoacaoFragment
     */
    private void carregarFotoMarketing() {

        try {
            Random random = new Random();
            int minimo = 1;
            int maximo = 3;
            int aleatorio = random.nextInt((maximo - minimo) + 1) + minimo;

            if (aleatorio == 1) {
                imvDoacao.setImageResource(R.drawable.marketing1);
            } else if (aleatorio == 2) {
                imvDoacao.setImageResource(R.drawable.marketing2);
            } else {
                imvDoacao.setImageResource(R.drawable.marketing3);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void inializaComponentes() {

        iniciarBillingProcessor();

        layout = findViewById(R.id.linear_layout_doacao2);

        imvDoacao = findViewById(R.id.imv_doacao_act);

        btnDoar3 = findViewById(R.id.btn_doar3_reais_act);
        btnDoar5 = findViewById(R.id.btn_doar5_reais_act);
        btnDoar10 = findViewById(R.id.btn_doar10_reais_act);
        btnDoar50 = findViewById(R.id.btn_doar50_reais_act);
        btnDoar100 = findViewById(R.id.btn_doar100_reais_act);
        //btnDoar500 = findViewById(R.id.btn_doar500_reais);

        //Context ctx = view.getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnDoar3.setElevation(10);
            btnDoar5.setElevation(10);
            btnDoar10.setElevation(10);
            btnDoar50.setElevation(10);
            btnDoar100.setElevation(10);
            CardView card = findViewById(R.id.cardView_doacao_act);
            card.setBackgroundTintList(getResources().getColorStateList(R.color.backgroundcolor));
        }

        // ação dos botões de doação
        configuraAcoesBotoes();
    }

    private void iniciarBillingProcessor() {

        bp = new BillingProcessor(DoacaoActivity.this, Constantes.LICENSE_KEY_GOOGLE_PLAY, new BillingProcessor.IBillingHandler(){

            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                Log.d("INFO79",productId);
            }

            @Override
            public void onPurchaseHistoryRestored() {
                Log.d("INFO79","history");
            }

            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
                Util.setSnackBar(layout, getString(R.string.error) + errorCode);
                Log.d("INFO79","erro");
            }

            @Override
            public void onBillingInitialized() {
                Log.d("INFO79","billing initialized");
            }
        });

        bp.initialize();
    }

    /**
     * Ação dos botões de doação
     */
    private void configuraAcoesBotoes() {

        btnDoar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                bp.consumePurchase(Constantes.PRODUCT_ID_3_REAIS);
                bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_3_REAIS);
            }
        });

        btnDoar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                Log.d("INFO79","5");
                bp.consumePurchase(Constantes.PRODUCT_ID_5_REAIS);
                bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_5_REAIS);
            }
        });

        btnDoar10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                bp.consumePurchase(Constantes.PRODUCT_ID_10_REAIS);
                bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_10_REAIS);
            }
        });

        btnDoar50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                bp.consumePurchase(Constantes.PRODUCT_ID_50_REAIS);
                bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_50_REAIS);
            }
        });

        btnDoar100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                bp.consumePurchase(Constantes.PRODUCT_ID_100_REAIS);
                bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_100_REAIS);
            }
        });

        /*btnDoar500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvaPurchasedPref();
                bp.consumePurchase(Constantes.PRODUCT_ID_500_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_500_REAIS);
                salvaClique();
}
        });*/
    }

    /*@Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Log.d("INFO89",productId + details);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d("INFO89","history");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Util.setSnackBar(layout, getString(R.string.error) + errorCode);
        Log.d("INFO89","erro");
    }

    @Override
    public void onBillingInitialized() {
        Log.d("INFO89","billing initialized");
    }*/

    private void salvaPurchasedPref(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("purchased", true);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
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
