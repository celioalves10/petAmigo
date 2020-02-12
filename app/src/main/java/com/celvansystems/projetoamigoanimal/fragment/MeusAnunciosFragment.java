package com.celvansystems.projetoamigoanimal.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.adapter.AdapterMeusAnuncios;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.LinearLayoutManagerWrapper;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeusAnunciosFragment extends Fragment {

    private List<Animal> anuncios = new ArrayList<>();
    private AdapterMeusAnuncios adapterMeusAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private View viewFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View layout;
    private AlertDialog dialog;

    public MeusAnunciosFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewFragment = inflater.inflate(R.layout.fragment_meusanuncios, container, false);
        inicializarComponentes();

        return viewFragment;
    }

    private void recuperarAnuncios() {

        try {
            dialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setMessage(R.string.procurando_anuncios)
                    .setCancelable(false)
                    .build();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    anuncios.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Animal animal = ds.getValue(Animal.class);
                        if (animal != null && animal.getDonoAnuncio() != null) {
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
                    Util.setSnackBar(layout, getString(R.string.falha_carregar_anuncios));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * inicializa os componentes da view
     */
    @SuppressLint({"RestrictedApi", "CutPasteId"})
    private void inicializarComponentes() {

        try {
            layout = viewFragment.findViewById(R.id.frame_layout_meus_anuncios_fragment);

            //fab
            FloatingActionButton fabCadastrar = viewFragment.findViewById(R.id.fabcadastrar_meus_anuncios);
            fabCadastrar.setVisibility(View.VISIBLE);
            fabCadastrar.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.view_pager, new CadastrarAnuncioFragment()).addToBackStack("tag").commit();
                }
            });

            RecyclerView recyclerMeusAnuncios = viewFragment.findViewById(R.id.recyclerMeusAnuncios);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWrapper(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerMeusAnuncios.setLayoutManager(mLayoutManager);
            // configuracoes iniciais

            anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais");

            //configurar recyclerview
            recyclerMeusAnuncios.setHasFixedSize(true);

            adapterMeusAnuncios = new AdapterMeusAnuncios(anuncios);
            recyclerMeusAnuncios.setAdapter(adapterMeusAnuncios);


            //recupera anuncios para o usuario
            recuperarAnuncios();

            //refresh
            swipeRefreshLayout = viewFragment.findViewById(R.id.swipeRefreshLayout_meus_anuncios);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshRecyclerAnuncios();
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fabCadastrar.setElevation(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Refresh itens
     */
    private void refreshRecyclerAnuncios() {
        try {
            anuncios.clear();
            recuperarAnuncios();
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    