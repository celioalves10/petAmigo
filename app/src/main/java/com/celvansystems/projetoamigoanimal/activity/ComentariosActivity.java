package com.celvansystems.projetoamigoanimal.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.adapter.AdapterComentarios;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.LinearLayoutManagerWrapper;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.celvansystems.projetoamigoanimal.model.Comentario;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComentariosActivity extends AppCompatActivity {

    private Animal anuncioSelecionado;
    private AdapterComentarios adapterComentarios;
    private EditText edtComentario;
    private RecyclerView recyclercomentarios;

    private View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);
        try {
            //configurar toolbar
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.comentarios_titulo);

            layout = findViewById(R.id.constraint_comentarios);

            recyclercomentarios = findViewById(R.id.recyclerComentarios);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false);
            recyclercomentarios.setLayoutManager(mLayoutManager);

            recyclercomentarios.setItemAnimator(null);
            anuncioSelecionado = (Animal) getIntent().getSerializableExtra("anuncioSelecionado");

            if (anuncioSelecionado != null) {

                //comentarios
                List<Comentario> listaComentarios = anuncioSelecionado.getListaComentarios();
                edtComentario = findViewById(R.id.editTextComentarAnuncio);
                ImageButton imbComentario = findViewById(R.id.imageButton_comentarAnuncio);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imbComentario.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                }

                imbComentario.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comentarAnuncio(anuncioSelecionado);
                        hideKeyboard(getApplicationContext(), edtComentario);
                    }
                });

                //configurar recyclerview
                recyclercomentarios.setHasFixedSize(true);
                adapterComentarios = new AdapterComentarios(listaComentarios);
                recyclercomentarios.setAdapter(adapterComentarios);

                updateRecycler(anuncioSelecionado);
            }
            configuraAdMob();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            Util.setSnackBar(layout, getString(R.string.comentario_inserido));
                                                            edtComentario.setText(null);
                                                        }
                                                    });
                                        } else {
                                            Util.setSnackBar(layout, ctx.getString(R.string.insira_comentario_valido));
                                        }

                                        updateRecycler(anuncio);
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
                            usuario.setId(Objects.requireNonNull(comentarios.child("usuario").child("id").getValue()).toString());
                            usuario.setNome(Objects.requireNonNull(comentarios.child("usuario").child("nome").getValue()).toString());

                            coment.setUsuario(usuario);
                            coment.setDatahora(Objects.requireNonNull(comentarios.child("datahora").getValue()).toString());
                            coment.setTexto(Objects.requireNonNull(comentarios.child("texto").getValue()).toString());
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

    /**
     * método que configura as propagandas via AdMob
     */
    private void configuraAdMob() {

        //admob
        //MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("INFO22", "MobileAds inicializado em comentarios");
            }
        });
        //AdView
        try {
            //banner teste
            final AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(getString(R.string.testeDeviceId))
                    .build();

            AdView adView = findViewById(R.id.banner_comentarios);
            //final AdRequest adRequest = new AdRequest.Builder().build();
            //adView.setAdUnitId(getString(R.string.admob_banner2_id));
            adView.loadAd(adRequest);

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.d("INFO22", "com loaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.d("INFO22", "com failed: " + errorCode);
                }

                @Override
                public void onAdClosed() {
                    Log.d("INFO22", "com closed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Context context, View editText) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
