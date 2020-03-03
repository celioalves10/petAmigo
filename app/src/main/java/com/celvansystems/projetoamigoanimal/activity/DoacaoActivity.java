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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.GerenciadorPRO;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Objects;
import java.util.Random;

public class DoacaoActivity extends AppCompatActivity implements MoPubView.BannerAdListener {

    //private View view;
    private BillingProcessor bp;
    private View layout;
    private ImageView imvDoacao;
    private Button btnDoar3, btnDoar5, btnDoar10, btnDoar50, btnDoar100;
    private MoPubView moPubView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doacao);

        //configurar toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.doacoes);

        iniciarBillingProcessor();
        configurarBannerMOPUB();
        inializaComponentes();
        carregarFotoMarketing();
    }

    private void configurarBannerMOPUB() {

        try {
            if (!GerenciadorPRO.isPRO) {

                SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Constantes.BANNER_DOACAO)

                        .withLogLevel(MoPubLog.LogLevel.DEBUG)
                        .withLegitimateInterestAllowed(false)
                        .build();

                MoPub.initializeSdk(this, sdkConfiguration, () -> {
                    Log.d("Mopub", "SDK initialized doacao");

                    moPubView = findViewById(R.id.banner_doacao);
                    moPubView.setAdUnitId(Constantes.BANNER_DOACAO); // Enter your Ad Unit ID from www.mopub.com
                    moPubView.setAdSize(MoPubView.MoPubAdSize.HEIGHT_50); // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
                    moPubView.setBannerAdListener(this);
                    moPubView.setAutorefreshEnabled(true);
                    //moPubView.loadAd(MoPubView.MoPubAdSize.HEIGHT_50); // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
                    moPubView.loadAd();

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        Log.d("INFO77", "loaded banner doacao");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d("INFO77", "failed banner doacao");
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        Log.d("INFO77", "clicked banner doacao");
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
        Log.d("INFO77", "expanded banner doacao");
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
        Log.d("INFO77", "collpased banner doacao");
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

        layout = findViewById(R.id.linear_layout_doacao2);

        imvDoacao = findViewById(R.id.imv_doacao_act);

        btnDoar3 = findViewById(R.id.btn_doar3_reais_act);
        btnDoar5 = findViewById(R.id.btn_doar5_reais_act);
        btnDoar10 = findViewById(R.id.btn_doar10_reais_act);
        btnDoar50 = findViewById(R.id.btn_doar50_reais_act);
        btnDoar100 = findViewById(R.id.btn_doar100_reais_act);
        //btnDoar500 = findViewById(R.id.btn_doar500_reais);

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

        bp = new BillingProcessor(DoacaoActivity.this, Constantes.LICENSE_KEY_GOOGLE_PLAY, new BillingProcessor.IBillingHandler() {

            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                salvaPurchasedPref();
                Log.d("INFO79", productId + " purchased");
            }

            @Override
            public void onPurchaseHistoryRestored() {
                Log.d("INFO79", "history");
            }

            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
                Util.setSnackBar(layout, getString(R.string.operacao_nao_realizada));
                Log.d("INFO79", "erro");
            }

            @Override
            public void onBillingInitialized() {
                Log.d("INFO79", "billing initialized");
            }
        });

        bp.initialize();
    }

    /**
     * Ação dos botões de doação
     */
    private void configuraAcoesBotoes() {

        btnDoar3.setOnClickListener(view -> {
            bp.consumePurchase(Constantes.PRODUCT_ID_3_REAIS);
            bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_3_REAIS);
        });

        btnDoar5.setOnClickListener(view -> {
            bp.consumePurchase(Constantes.PRODUCT_ID_5_REAIS);
            bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_5_REAIS);
        });

        btnDoar10.setOnClickListener(view -> {
            bp.consumePurchase(Constantes.PRODUCT_ID_10_REAIS);
            bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_10_REAIS);
        });

        btnDoar50.setOnClickListener(view -> {
            bp.consumePurchase(Constantes.PRODUCT_ID_50_REAIS);
            bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_50_REAIS);
        });

        btnDoar100.setOnClickListener(view -> {
            bp.consumePurchase(Constantes.PRODUCT_ID_100_REAIS);
            bp.purchase(DoacaoActivity.this, Constantes.PRODUCT_ID_100_REAIS);
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

    private void salvaPurchasedPref() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("purchased", true);
        editor.apply();
        Log.d("INFO79", "pref salvo como true");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MoPub.onResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MoPub.onStop(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MoPub.onPause(this);
    }

    @Override
    public void onDestroy() {

        try {
            if (bp != null) {
                bp.release();
            }
            if (moPubView != null) {
                moPubView.destroy();
            }
            MoPub.onDestroy(this);

            super.onDestroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //showInterstitialMethod();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //showInterstitialMethod();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
