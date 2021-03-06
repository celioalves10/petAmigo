package com.celvansystems.projetoamigoanimal.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.adapter.AdapterComentarios;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.GerenciadorPRO;
import com.celvansystems.projetoamigoanimal.helper.LinearLayoutManagerWrapper;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.celvansystems.projetoamigoanimal.model.Comentario;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
//import com.ironsource.mediationsdk.IronSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("Registered")
public class ComentariosActivity extends AppCompatActivity implements MoPubInterstitial.InterstitialAdListener {

    private Animal anuncioSelecionado;
    private AdapterComentarios adapterComentarios;
    private EditText edtComentario;
    private RecyclerView recyclercomentarios;
    private MoPubInterstitial mInterstitial;
    private View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        configurarIntersticialMOPUB();

        configuracoesIniciais();
    }

    private void configuracoesIniciais() {

        try {
            //configurar toolbar
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.comentarios_titulo);

            layout = findViewById(R.id.constraint_comentarios);

            recyclercomentarios = findViewById(R.id.recyclerComentarios);

            ImageView imvInfoComentarios = findViewById(R.id.imv_info_comentarios);
            imvInfoComentarios.setOnClickListener(view -> {
                Intent detalhesIntent = new Intent(ComentariosActivity.this, DetalhesAnimalActivity.class);
                detalhesIntent.putExtra("anuncioSelecionado", anuncioSelecionado);
                startActivity(detalhesIntent);
            });

            ImageButton imbComentario = findViewById(R.id.imageButton_comentarAnuncio);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imbComentario.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                imvInfoComentarios.setImageTintList(getResources().getColorStateList(R.color.colorAccent));
            }

            imbComentario.setOnClickListener(v -> comentarAnuncio(anuncioSelecionado));

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false);
            recyclercomentarios.setLayoutManager(mLayoutManager);

            recyclercomentarios.setItemAnimator(null);
            anuncioSelecionado = (Animal) getIntent().getSerializableExtra("anuncioSelecionado");

            if (anuncioSelecionado != null) {
                String idAnimal = anuncioSelecionado.getIdAnimal();
                final DatabaseReference anunciosRef = ConfiguracaoFirebase.getFirebase()
                        .child("meus_animais");
                final DatabaseReference fotosRef = anunciosRef.child(idAnimal).child("fotos");

                ///////////////////////////////////

                anunciosRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot anuncios : dataSnapshot.getChildren()) {

                            if (anuncios != null) {
                                if (Objects.requireNonNull(anuncios.child("idAnimal").getValue()).toString().equalsIgnoreCase(idAnimal)) {

                                    if (anuncios.child("especie").getValue() != null) {
                                        anuncioSelecionado.setEspecie(Objects.requireNonNull(anuncios.child("especie").getValue()).toString());
                                    }
                                    if (anuncios.child("raca").getValue() != null) {
                                        anuncioSelecionado.setRaca(Objects.requireNonNull(anuncios.child("raca").getValue()).toString());
                                    }
                                    if (anuncios.child("porte").getValue() != null) {
                                        anuncioSelecionado.setPorte(Objects.requireNonNull(anuncios.child("porte").getValue()).toString());
                                    }
                                    if (anuncios.child("sexo").getValue() != null) {
                                        anuncioSelecionado.setSexo(Objects.requireNonNull(anuncios.child("sexo").getValue()).toString());
                                    }
                                    if (anuncios.child("cidade").getValue() != null) {
                                        anuncioSelecionado.setCidade(Objects.requireNonNull(anuncios.child("cidade").getValue()).toString());
                                    }
                                    if (anuncios.child("uf").getValue() != null) {
                                        anuncioSelecionado.setUf(Objects.requireNonNull(anuncios.child("uf").getValue()).toString());
                                    }
                                    if (anuncios.child("idade").getValue() != null) {
                                        anuncioSelecionado.setIdade(Objects.requireNonNull(anuncios.child("idade").getValue()).toString());
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                /////////////////////////////////
                //Log.d("INFO13", "id animal: " + idAnimal);

                fotosRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<String> listaFotos = new ArrayList<>();

                        for (final DataSnapshot fotos : dataSnapshot.getChildren()) {

                            if (fotos.getValue() != null) {
                                String foto = Objects.requireNonNull(fotos.getValue()).toString();
                                listaFotos.add(foto);
                            }
                        }
                        anuncioSelecionado.setFotos(listaFotos);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //comentarios
                List<Comentario> listaComentarios = anuncioSelecionado.getListaComentarios();
                edtComentario = findViewById(R.id.editTextComentarAnuncio);

                //configurar recyclerview
                recyclercomentarios.setHasFixedSize(true);
                adapterComentarios = new AdapterComentarios(listaComentarios);
                recyclercomentarios.setAdapter(adapterComentarios);

                updateRecycler(anuncioSelecionado);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarIntersticialMOPUB() {

        try {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Constantes.INTERSTICIAL2)

                    .withLogLevel(MoPubLog.LogLevel.DEBUG)
                    .withLegitimateInterestAllowed(false)
                    .build();

            MoPub.initializeSdk(ComentariosActivity.this, sdkConfiguration, () -> {
                Log.d("INFO77", "SDK initialized coment");

                mInterstitial = new MoPubInterstitial(ComentariosActivity.this, Constantes.INTERSTICIAL2);
                // Remember that "this" refers to your current activity.
                mInterstitial.setInterstitialAdListener(ComentariosActivity.this);
                mInterstitial.load();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////MOPUB/////////////////

    // Defined by your application, indicating that you're ready to show an interstitial ad.
    void showInterstitialMethod() {

        if (!GerenciadorPRO.isPRO) {
            if (mInterstitial.isReady()) {
                mInterstitial.show();
                mInterstitial.load();
                Log.d("INFO77", "show chamado");
            } else {
                Log.d("INFO77", "not ready coment");
                mInterstitial.load();
            }
        }
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.d("INFO77", "loaded coment");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Log.d("INFO77", "failed coment");
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.d("INFO77", "shown coment");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.d("INFO77", "clicked coment");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.d("INFO77", "dismissed coment");
    }

    /**
     * metodo que insere comentarios no firebase
     *
     * @param anuncio animal
     */
    private void comentarAnuncio(final Animal anuncio) {

        try {
            if (ConfiguracaoFirebase.isUsuarioLogado()) {

                final Context ctx = this.getApplicationContext();

                if (edtComentario.getText() != null && !edtComentario.getText().toString().equalsIgnoreCase("")) {

                    final DatabaseReference comentarioRef = ConfiguracaoFirebase.getFirebase()
                            .child("meus_animais")
                            .child(anuncio.getIdAnimal())
                            .child("comentarios");

                    //Dados do Usuário
                    DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                            .child("usuarios");

                    usuariosRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot usuarios : dataSnapshot.getChildren()) {

                                if (usuarios != null) {

                                    UserInfo user = ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser();

                                    if (usuarios.child("id").getValue() != null && user != null) {

                                        if (Objects.requireNonNull(usuarios.child("id").getValue()).toString().equalsIgnoreCase(Objects.requireNonNull(user).getUid())) {

                                            Usuario usuario = new Usuario();
                                            usuario.setId(ConfiguracaoFirebase.getIdUsuario());

                                            //Dados fora do cadastro
                                            String texto = edtComentario.getText().toString();

                                            if (usuarios.child("nome").getValue() != null) {
                                                usuario.setNome(Objects.requireNonNull(usuarios.child("nome").getValue()).toString());
                                            } else {
                                                String nomeUsuario = Objects.requireNonNull(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser()).getDisplayName();
                                                if (nomeUsuario != null) {
                                                    usuario.setNome(nomeUsuario);
                                                }
                                            }
                                            if (usuarios.child("foto").getValue() != null) {
                                                usuario.setFoto(Objects.requireNonNull(usuarios.child("foto").getValue()).toString());
                                            }

                                            //Inserindo o comentário
                                            if (Util.validaTexto(texto)) {
                                                usuario.setId(ConfiguracaoFirebase.getIdUsuario());
                                                Comentario coment = new Comentario(usuario, texto, Util.getDataAtualBrasil());

                                                comentarioRef.push().setValue(coment)
                                                        .addOnCompleteListener(task -> {

                                                            Util.setSnackBar(layout, getString(R.string.comentario_inserido));
                                                            edtComentario.setText(null);
                                                        });
                                            } else {
                                                Util.setSnackBar(layout, ctx.getString(R.string.insira_comentario_valido));
                                            }

                                            updateRecycler(anuncio);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                } else {
                    Util.setSnackBar(layout, ctx.getString(R.string.insira_comentario_valido));
                }

            } else {
                Util.setSnackBar(layout, getString(R.string.usuario_nao_logado));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateRecycler(final Animal anuncio) {

        try {
            //Update do RecyclerView
            final DatabaseReference comentarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais")
                    .child(anuncio.getIdAnimal())
                    .child("comentarios");

            comentarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Comentario> comentsList = new ArrayList<>();
                    for (DataSnapshot comentarios : dataSnapshot.getChildren()) {

                        Comentario coment = new Comentario();

                        if (comentarios != null) {

                            Usuario usuario = new Usuario();
                            if (comentarios.child("usuario").child("id").getValue() != null) {
                                usuario.setId(Objects.requireNonNull(comentarios.child("usuario").child("id").getValue()).toString());
                            }
                            if (comentarios.child("usuario").child("nome").getValue() != null) {
                                usuario.setNome(Objects.requireNonNull(comentarios.child("usuario").child("nome").getValue()).toString());
                            }
                            coment.setUsuario(usuario);
                            if (comentarios.child("datahora").getValue() != null) {
                                coment.setDatahora(Objects.requireNonNull(comentarios.child("datahora").getValue()).toString());
                            }
                            if (comentarios.child("texto").getValue() != null) {
                                coment.setTexto(Objects.requireNonNull(comentarios.child("texto").getValue()).toString());
                            }
                            comentsList.add(coment);
                            anuncio.setListaComentarios(comentsList);
                        }
                    }
                    adapterComentarios = new AdapterComentarios(comentsList);
                    recyclercomentarios.setAdapter(adapterComentarios);
                    adapterComentarios.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            //Fim do update do Recycler
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showInterstitialMethod();
        Log.d("INFO77", "back pressed coment");
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showInterstitialMethod();
            Log.d("INFO77", "home pressed coment");
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
        mInterstitial.destroy();
        super.onDestroy();
    }
}
