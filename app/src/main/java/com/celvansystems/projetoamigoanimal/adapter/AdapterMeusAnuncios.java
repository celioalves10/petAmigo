package com.celvansystems.projetoamigoanimal.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.activity.DetalhesAnimalActivity;
import com.celvansystems.projetoamigoanimal.fragment.CadastrarAnuncioFragment;
import com.celvansystems.projetoamigoanimal.fragment.MeusAnunciosFragment;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Permissoes;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdapterMeusAnuncios extends RecyclerView.Adapter<AdapterMeusAnuncios.MyViewHolder>
        implements Serializable {

    private List<Animal> anuncios;
    //Permissoes
    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public AdapterMeusAnuncios(List<Animal> anuncios) {
        this.anuncios = anuncios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_meus_anuncios, viewGroup, false);
        iniciarComponentes(item);
        return new MyViewHolder(item);
    }

    private void iniciarComponentes(View item) {

        Permissoes.validarPermissoes(permissoes, (AppCompatActivity) item.getContext(), 1);
    }

    private void configuracoesMaisOpcoes(AdapterMeusAnuncios.MyViewHolder myViewHolder) {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {
            myViewHolder.imvMaisOpcoesMeusAnuncios.setVisibility(View.VISIBLE);
        } else {
            myViewHolder.imvMaisOpcoesMeusAnuncios.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {

        try {
            if (anuncios != null) {

                final Context ctx = myViewHolder.itemView.getContext();

                final Animal anuncio = anuncios.get(i);

                if (anuncio != null) {

                    configuracoesMaisOpcoes(myViewHolder);

                    myViewHolder.dataCadastro.setText(anuncio.getDataCadastro());
                    myViewHolder.nome.setText(anuncio.getNome());
                    myViewHolder.idade.setText(anuncio.getIdade());
                    myViewHolder.cidade.setText(anuncio.getCidade());

                    //pega a primeira imagem cadastrada
                    List<String> urlFotos = anuncio.getFotos();

                    if (urlFotos != null && urlFotos.size() > 0) {
                        String urlCapa = urlFotos.get(0);

                        Picasso.get().load(urlCapa).into(myViewHolder.foto);
                    }

                    // ação de clique na foto do anuncio
                    myViewHolder.foto.setOnClickListener(v -> {
                        Intent detalhesIntent = new Intent(v.getContext(), DetalhesAnimalActivity.class);
                        detalhesIntent.putExtra("anuncioSelecionado", anuncio);
                        v.getContext().startActivity(detalhesIntent);
                    });

                    myViewHolder.imvCompartilharMeusAnuncios.setOnClickListener(v ->
                            myViewHolder.imvCompartilharMeusAnuncios.setOnClickListener(v1 ->
                                    compartilharAnuncio(myViewHolder, anuncio)));

                    myViewHolder.imvMaisOpcoesMeusAnuncios.setOnClickListener(v -> {

                        List<String> opcoesLista = new ArrayList<>();

                        if (ConfiguracaoFirebase.getIdUsuario().equalsIgnoreCase(anuncio.getDonoAnuncio())) {
                            opcoesLista.add(ctx.getString(R.string.editar));
                            opcoesLista.add(ctx.getString(R.string.compartilhar));
                            opcoesLista.add(ctx.getString(R.string.remover));
                        }

                        final String[] opcoes = new String[opcoesLista.size()];

                        for (int i1 = 0; i1 < opcoesLista.size(); i1++) {
                            opcoes[i1] = opcoesLista.get(i1);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(myViewHolder.imvMaisOpcoesMeusAnuncios.getContext());

                        builder.setItems(opcoes, (dialog, which) -> {

                            if (ctx.getString(R.string.editar).equals(opcoes[which])) {

                                Bundle data = new Bundle();
                                data.putSerializable("anuncioSelecionado", anuncio);

                                CadastrarAnuncioFragment cadFragment = new CadastrarAnuncioFragment();
                                cadFragment.setArguments(data);

                                AppCompatActivity activity = (AppCompatActivity) myViewHolder.itemView.getContext();
                                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.view_pager, cadFragment).addToBackStack("tag").commit();

                            } else if (ctx.getString(R.string.compartilhar).equalsIgnoreCase(opcoes[which])) {

                                compartilharAnuncio(myViewHolder, anuncio);

                            } else if (ctx.getString(R.string.remover).equals(opcoes[which])) {

                                new AlertDialog.Builder(myViewHolder.itemView.getContext())
                                        .setMessage(ctx.getString(R.string.tem_certeza_excluir_anuncio))
                                        .setCancelable(false)
                                        .setPositiveButton(ctx.getString(R.string.sim), (dialog1, id) -> {
                                            anuncio.remover();
                                            AppCompatActivity activity = (AppCompatActivity) myViewHolder.itemView.getContext();
                                            FragmentManager fragmentManager = activity.getSupportFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.view_pager, new MeusAnunciosFragment()).addToBackStack(null).commit();
                                        })
                                        .setNegativeButton(ctx.getString(R.string.nao), null)
                                        .show();

                            } else if (ctx.getString(R.string.denunciar).equals(opcoes[which])) {
                                Util.setSnackBar(myViewHolder.layout, ctx.getString(R.string.denunciar));
                            }
                        });
                        builder.show();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compartilharAnuncio(MyViewHolder myViewHolder, Animal anuncio) {

        try {
            Context ctx = myViewHolder.itemView.getContext();

            Drawable mDrawable = myViewHolder.foto.getDrawable();
            Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();

            String path = MediaStore.Images.Media.insertImage(myViewHolder.itemView.getContext()
                    .getContentResolver(), mBitmap, "Imagem", "");
            Uri uri = Uri.parse(path);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.instale_e_conheca) +
                    anuncio.getNome() + ctx.getString(R.string.disponivel_em) +
                    "https://play.google.com/store/apps/details?id=" + Constantes.APPLICATION_ID + "\n\n");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            myViewHolder.itemView.getContext().startActivity(Intent.createChooser(intent, ctx.getString(R.string.compartilhando_imagem)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        int retorno = 0;
        if (anuncios != null) {
            retorno = anuncios.size();
        }
        return retorno;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dataCadastro;
        TextView nome;
        TextView idade;
        TextView cidade;
        ImageView foto;
        ImageView imvMaisOpcoesMeusAnuncios;
        ImageView imvBottomMeusAnuncios;
        ImageView imvCompartilharMeusAnuncios;
        View layout, divider;

        MyViewHolder(View itemView) {
            super(itemView);

            try {
                dataCadastro = itemView.findViewById(R.id.textDataCadastroMeusAnuncios);
                nome = itemView.findViewById(R.id.txv_nome_meus_anuncios);
                idade = itemView.findViewById(R.id.textIdade_meus_anuncios);
                foto = itemView.findViewById(R.id.img_meus_anuncios);
                cidade = itemView.findViewById(R.id.textCidadePrincipal_meus_anuncios);
                imvMaisOpcoesMeusAnuncios = itemView.findViewById(R.id.imv_mais_opcoes_meus_anuncios);
                imvBottomMeusAnuncios = itemView.findViewById(R.id.imageView_bottom_meus_anuncios);
                imvCompartilharMeusAnuncios = itemView.findViewById(R.id.imv_compartilhar_meus_anuncios);

                imvCompartilharMeusAnuncios.bringToFront();
                imvMaisOpcoesMeusAnuncios.bringToFront();

                //Para a snackBar
                layout = itemView.findViewById(R.id.view_pager);
                divider = itemView.findViewById(R.id.divider_meus_anuncios);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    foto.setElevation(2);
                    imvCompartilharMeusAnuncios.setElevation(40);
                    imvMaisOpcoesMeusAnuncios.setElevation(40);
                    imvBottomMeusAnuncios.setElevation(2);
                    nome.setElevation(5);
                    idade.setElevation(5);
                    cidade.setElevation(5);
                    dataCadastro.setElevation(5);
                    divider.setElevation(5);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
