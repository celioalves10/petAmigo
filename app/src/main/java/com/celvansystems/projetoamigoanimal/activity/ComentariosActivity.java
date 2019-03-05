package com.celvansystems.projetoamigoanimal.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.adapter.AdapterComentarios;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.celvansystems.projetoamigoanimal.model.Comentario;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComentariosActivity extends AppCompatActivity {

    private Animal anuncioSelecionado;
    private AdapterComentarios adapterComentarios;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private EditText edtComentario;
    private ImageButton imbComentario;

    private View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        //configurar toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.comentarios_titulo);

        layout = findViewById(R.id.constraint_comentarios);

        RecyclerView recyclercomentarios = findViewById(R.id.recyclerComentarios);
        recyclercomentarios.setItemAnimator(null);
        anuncioSelecionado = (Animal) getIntent().getSerializableExtra("anuncioSelecionado");

        if(anuncioSelecionado != null){

            //comentarios
            listaComentarios = anuncioSelecionado.getListaComentarios();
            edtComentario = findViewById(R.id.editTextComentarAnuncio);
            imbComentario = findViewById(R.id.imageButton_comentarAnuncio);
            imbComentario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comentarAnuncio(anuncioSelecionado);
                }
            });

            //configurar recyclerview
            try {
                RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
                recyclercomentarios.setLayoutManager(lm);
                recyclercomentarios.setHasFixedSize(true);
                adapterComentarios = new AdapterComentarios(listaComentarios);
                recyclercomentarios.setAdapter(adapterComentarios);
            } catch (Exception e){e.printStackTrace();}
        }
    }

    /**
     * metodo que insere comentarios no firebase
     * @param anuncio animal
     */
    private void comentarAnuncio(Animal anuncio) {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {

            final DatabaseReference comentarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais")
                    .child(anuncio.getIdAnimal())
                    .child("comentarios");

            String texto = edtComentario.getText().toString();
            String nomeUsuario = Objects.requireNonNull(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser()).getDisplayName();
            Usuario usuario = new Usuario();

            if(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getPhotoUrl() != null) {
                usuario.setFoto(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser().getPhotoUrl().toString());
            }
            if(nomeUsuario!=null) {
                usuario.setNome(nomeUsuario);
            }

            final Comentario coment = new Comentario(usuario, texto, Util.getDataAtualBrasil());

            if(Util.validaTexto(texto)){
                Task<Void> inserirComentarioRef = comentarioRef
                        .push().setValue(coment);

                inserirComentarioRef.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Util.setSnackBar(layout, "Comentário inserido!");
                        edtComentario.setText(null);

                    }
                });
            } else {
                Util.setSnackBar(layout, "Comentário inválido!");
            }

        } else {
            Util.setSnackBar(layout, "Usuário não logado!");
        }
    }
}
