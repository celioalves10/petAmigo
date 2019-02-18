package com.celvansystems.projetoamigoanimal.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.adapter.AdapterMeusAnuncios;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.RecyclerItemClickListener;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class MeusAnunciosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private List<Animal> anuncios = new ArrayList<>();
    private AdapterMeusAnuncios adapterMeusAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private StorageReference storage;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        inicializarComponentes();

        // configuracoes iniciais
        try {
            anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais");
        } catch (Exception e){e.printStackTrace();}

        //configurar recyclerview
        try {
            recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
            recyclerAnuncios.setHasFixedSize(true);

            adapterMeusAnuncios = new AdapterMeusAnuncios(anuncios);
            recyclerAnuncios.setAdapter(adapterMeusAnuncios);
        } catch (Exception e){e.printStackTrace();}

        //recupera anuncios para o usuario
        recuperarAnuncios();

        //adiciona evento de clique
        recyclerAnuncios.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerAnuncios, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Animal anuncioSelecionado = anuncios.get(position);
                Intent detalhesIntent = new Intent(view.getContext(), DetalhesAnimalActivity.class);
                detalhesIntent.putExtra("anuncioSelecionado", anuncioSelecionado);
                view.getContext().startActivity(detalhesIntent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

                Animal anuncioSelecionado = anuncios.get(position);
                anuncioSelecionado.remover();

                apagarFotosStorage(anuncioSelecionado);

                adapterMeusAnuncios.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "Anúncio excluído!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));
    }

    /**
     * metodo auxilicar que apaga as fotos de um animal
     * @param anuncio animal
     */
    private void apagarFotosStorage (Animal anuncio){

        try {
            StorageReference imagemAnimal = storage
                    .child("imagens")
                    .child("animais")
                    .child(anuncio.getIdAnimal());

            int numFotos = anuncio.getFotos().size();

            for (int i = 0; i < numFotos; i++) {
                String textoFoto = "imagem" + i;

                imagemAnimal.child(textoFoto).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Fotos excluídas com sucesso", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Falha ao deletar fotos", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * recupera os anuncios do usuario que estiver logado
     */
    private void recuperarAnuncios(){

        try {
            dialog = new SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("Procurando anúncios")
                    .setCancelable(false)
                    .build();
            dialog.show();
        } catch (Exception e){e.printStackTrace();}

        try {
            anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    anuncios.clear();
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Animal animal = ds.getValue(Animal.class);
                        if(animal!= null && animal.getDonoAnuncio()!= null) {
                            if (animal.getDonoAnuncio().equalsIgnoreCase(ConfiguracaoFirebase.getIdUsuario())) {
                                anuncios.add(animal);
                            }
                        }
                    }
                    Collections.reverse(anuncios);
                    adapterMeusAnuncios.notifyDataSetChanged();

                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Falha ao carregar anúncios.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){e.printStackTrace();}
    }

    /**
     * inicializa os componentes da view
     */
    private void inicializarComponentes(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storage = ConfiguracaoFirebase.getFirebaseStorage();

        FloatingActionButton fabCadastrar = findViewById(R.id.fabcadastrar);

        fabCadastrar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerAnuncios = findViewById(R.id.recycle_meus_anuncios);
    }
}
