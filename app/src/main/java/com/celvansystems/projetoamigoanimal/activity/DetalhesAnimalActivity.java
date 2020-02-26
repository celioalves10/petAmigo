package com.celvansystems.projetoamigoanimal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.GerenciadorPRO;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Objects;

public class DetalhesAnimalActivity extends AppCompatActivity implements MoPubInterstitial.InterstitialAdListener {

    private View layout;
    private Animal anuncioSelecionado;
    private MoPubInterstitial mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhesanimal);

        //configurar toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.detalhes);

        configurarIntersticialMOPUB();

        inicializarComponentes();
        configuraNavBar();
    }

    private void inicializarComponentes() {

        CarouselView carouselView = findViewById(R.id.carouselView);
        TextView textNome = findViewById(R.id.txv_nome_meus_anuncios);
        TextView textEspecie = findViewById(R.id.txv_especie);
        TextView textGenero = findViewById(R.id.txv_genero);
        TextView textIdade = findViewById(R.id.txv_idade);
        TextView textPorte = findViewById(R.id.txv_porte);
        TextView textEstado = findViewById(R.id.txv_estado);
        TextView textCidade = findViewById(R.id.txv_cidade);
        TextView textRaca = findViewById(R.id.txv_raca);
        TextView textDescricao = findViewById(R.id.txv_descricao);

        layout = findViewById(R.id.linear_layout_detalhes_animal);
        final Button btnVerTelefone = findViewById(R.id.btnVerTelefone);

        //recupera anuncio para exibicao
        anuncioSelecionado = (Animal) getIntent().getSerializableExtra("anuncioSelecionado");

        if (anuncioSelecionado != null) {

            ImageListener imageListener = (position, imageView) -> {
                String urlString = anuncioSelecionado.getFotos().get(position);
                Picasso.get().load(urlString).into(imageView);
            };
            if (anuncioSelecionado.getFotos() != null) {
                carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            }
            carouselView.setImageListener(imageListener);

            //características do animal
            textNome.setText(anuncioSelecionado.getNome());
            textEspecie.setText(anuncioSelecionado.getEspecie());
            textGenero.setText(anuncioSelecionado.getSexo());
            textIdade.setText(anuncioSelecionado.getIdade());
            textPorte.setText(anuncioSelecionado.getPorte());
            textEstado.setText(anuncioSelecionado.getUf());
            textCidade.setText(anuncioSelecionado.getCidade());
            textRaca.setText(anuncioSelecionado.getRaca());
            textDescricao.setText(anuncioSelecionado.getDescricao());

            //setElevation API >= 21
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textDescricao.setElevation(50);
                btnVerTelefone.setElevation(25);
                carouselView.setElevation(25);
            }

            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                    .child("usuarios");

            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (final DataSnapshot usuarios : dataSnapshot.getChildren()) {
                        if (usuarios != null) {
                            if (usuarios.child("id").getValue() != null) {

                                if (Objects.requireNonNull(usuarios.child("id").getValue()).toString()
                                        .equalsIgnoreCase(anuncioSelecionado.getDonoAnuncio())) {

                                    btnVerTelefone.setOnClickListener(v -> {

                                        if (usuarios.child("telefone").getValue() != null) {

                                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                                            boolean purchased = pref.getBoolean("purchased", false); // getting boolean

                                            if (purchased || GerenciadorPRO.isPRO) {

                                                final String telefone = Objects.requireNonNull(usuarios.child("telefone").getValue()).toString();
                                                Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                                                        telefone, null));
                                                startActivity(i);

                                            } else {

                                                Intent i = new Intent(DetalhesAnimalActivity.this, DoacaoActivity.class);
                                                startActivity(i);
                                                showInterstitialMethod();

                                            }
                                        } else {
                                            Util.setSnackBar(layout, getString(R.string.telefone_nao_cadastrado));
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void configurarIntersticialMOPUB() {

        try {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Constantes.INTERSTICIAL1)

                    .withLogLevel(MoPubLog.LogLevel.DEBUG)
                    .withLegitimateInterestAllowed(false)
                    .build();

            MoPub.initializeSdk(this, sdkConfiguration, () -> {
                Log.d("Mopub", "SDK initialized det");

                mInterstitial = new MoPubInterstitial(DetalhesAnimalActivity.this, Constantes.INTERSTICIAL1);
                // Remember that "this" refers to your current activity.
                mInterstitial.setInterstitialAdListener(DetalhesAnimalActivity.this);
                mInterstitial.load();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showInterstitialMethod() {

        if (!GerenciadorPRO.isPRO) {
            if (mInterstitial.isReady()) {
                mInterstitial.show();
                mInterstitial.load();
            } else {
                Log.d("INFO77", "not ready det");
                mInterstitial.load();
            }
        }
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.d("INFO77", "loaded det");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Log.d("INFO77", "failed det");
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.d("INFO77", "shown det");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.d("INFO77", "clicked det");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.d("INFO77", "dismissed det");
    }

    private void configuraNavBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.lightgray));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showInterstitialMethod();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showInterstitialMethod();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        if(mInterstitial!=null) {
            mInterstitial.destroy();
        }
        MoPub.onDestroy(this);
        super.onDestroy();
    }
}
