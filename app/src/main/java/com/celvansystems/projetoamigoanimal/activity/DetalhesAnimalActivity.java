package com.celvansystems.projetoamigoanimal.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Objects;

public class DetalhesAnimalActivity extends AppCompatActivity {

    private View layout;
    private Animal anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_animal);

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

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };
            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
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

                                    btnVerTelefone.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (usuarios.child("telefone").getValue() != null) {

                                                final String telefone = Objects.requireNonNull(usuarios.child("telefone").getValue()).toString();
                                                Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                                                        telefone, null));
                                                startActivity(i);

                                            } else {
                                                Util.setSnackBar(layout, getString(R.string.telefone_nao_cadastrado));
                                            }
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

        //Sempre modo Retrato
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        configuraAdMob();
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

    /**
     * método que configura as propagandas via AdMob
     */
    private void configuraAdMob() {

        //admob
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        //AdView
        try {
            //banner teste
            AdRequest adRequest = new AdRequest.Builder().build();
            AdView adView = findViewById(R.id.banner_detalhes_animal);
            adView.loadAd(adRequest);

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.d("INFO22", "det loaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.d("INFO22", "det failed: " + errorCode);
                }

                @Override
                public void onAdClosed() {
                    Log.d("INFO22", "det closed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("INFO22", "det exception " + e.getMessage());

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
