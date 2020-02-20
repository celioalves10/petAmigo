package com.celvansystems.projetoamigoanimal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
//import com.ironsource.mediationsdk.IronSource;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Objects;

public class DetalhesAnimalActivity extends AppCompatActivity {

    private View layout;
    private Animal anuncioSelecionado;
    private AppLovinAd loadedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhesanimal);

        //configurar toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.detalhes);

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
            if(anuncioSelecionado.getFotos()!= null) {
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

                                        //mostraAppLovinIntersticial();

                                        if (usuarios.child("telefone").getValue() != null) {

                                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                                            boolean purchased = pref.getBoolean("purchased", false); // getting boolean

                                            if(purchased || Constantes.isPRO) {
                                                final String telefone = Objects.requireNonNull(usuarios.child("telefone").getValue()).toString();
                                                Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                                                        telefone, null));
                                                startActivity(i);
                                            } else {
                                                //abre a main activity que abre o DoacaoFragment
                                                Intent i = new Intent(DetalhesAnimalActivity.this, MainActivity.class);
                                                i.putExtra("telefone", true);
                                                startActivity(i);
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
        if(!Constantes.isPRO) {
            configuraAppLovinIntersticial();
        }
    }

    public void configuraAppLovinIntersticial() {

        if(!Constantes.isPRO) {

            AppLovinSdk.initializeSdk(this);

            // Load an Interstitial Ad
            AppLovinSdk.getInstance(this).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    loadedAd = ad;
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    // Look at AppLovinErrorCodes.java for list of error codes.
                }
            });
        }
    }

    public void mostraAppLovinIntersticial() {

        if(!Constantes.isPRO) {
            AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(this), this);
            // Optional: Assign listeners
            //interstitialAd.setAdDisplayListener( ... );
            //interstitialAd.setAdClickListener( ... );
            //interstitialAd.setAdVideoPlaybackListener( ... );
            interstitialAd.showAndRender(loadedAd);
        }
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
            mostraAppLovinIntersticial();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                mostraAppLovinIntersticial();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        //IronSource.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //IronSource.onPause(this);
    }
}
