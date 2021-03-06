package com.celvansystems.projetoamigoanimal.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.activity.MainActivity;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Permissoes;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.R.layout.simple_spinner_item;

/**
 * A simple {@link Fragment} subclass.
 */
public class CadastrarAnuncioFragment extends Fragment
        implements View.OnClickListener {

    private Spinner spnEspecie, spnSexo, spnIdade, spnPorte;
    private Spinner spnEstado, spnCidade;
    private EditText edtRaca, edtNome;
    private AutoCompleteTextView edtDescricao;
    private HashMap<Integer, String> listaFotosRecuperadas;
    private List<String> listaURLFotos;
    private ImageView imagem1, imagem2, imagem3;
    private android.app.AlertDialog dialog;
    private StorageReference storage;
    private int requisicao;
    private View layout, viewFragment;
    private Button btnCadastrarAnuncio;
    private ArrayAdapter<String> adapterEspecies, adapterIdades;
    private ArrayAdapter<String> adapterCidades, adapterSexos;
    private ArrayAdapter<String> adapterEstados, adapterPortes;
    private boolean editando = false;
    private String idEditando;

    //Permissoes
    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public CadastrarAnuncioFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewFragment = inflater.inflate(R.layout.fragment_cadastraranuncio, container, false);
        Bundle bundle = this.getArguments();

        inicializarComponentes();

        if (bundle != null) {
            Animal anuncio = (Animal) bundle.getSerializable("anuncioSelecionado");
            btnCadastrarAnuncio.setText(getString(R.string.atualizar_cadastro));
            if (anuncio != null) {
                preencheCampos(anuncio);
            }
        }
        return viewFragment;
    }

    /**
     * Configuracoes iniciais
     */
    @SuppressLint("UseSparseArrays")
    private void inicializarComponentes() {

        storage = ConfiguracaoFirebase.getFirebaseStorage();

        layout = viewFragment.findViewById(R.id.frame_layout_cad_anuncio);

        spnEspecie = viewFragment.findViewById(R.id.spinner_cad_Especie);
        spnSexo = viewFragment.findViewById(R.id.spinner_cad_Sexo);
        spnIdade = viewFragment.findViewById(R.id.spinner_cad_idade);
        spnPorte = viewFragment.findViewById(R.id.spinner_cad_porte);
        spnEstado = viewFragment.findViewById(R.id.spinner_cad_estado_complemento);
        spnCidade = viewFragment.findViewById(R.id.spinner_cad_cidade_complemento);
        edtDescricao = viewFragment.findViewById(R.id.editText_cad_descrição);

        edtDescricao.setVerticalScrollBarEnabled(true);
        edtDescricao.setMovementMethod(new ScrollingMovementMethod());
        edtRaca = viewFragment.findViewById(R.id.edtRaca);
        edtNome = viewFragment.findViewById(R.id.editText_cad_NomeAnimal);

        btnCadastrarAnuncio = viewFragment.findViewById(R.id.btnCadAnuncio);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnCadastrarAnuncio.setElevation(25);
            edtDescricao.setNestedScrollingEnabled(true);
        }

        //imageviews
        imagem1 = viewFragment.findViewById(R.id.imageCad1);
        imagem2 = viewFragment.findViewById(R.id.imageCad2);
        imagem3 = viewFragment.findViewById(R.id.imageCad3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        imagem3.setOnClickListener(this);

        //listaFotosRecuperadas = new ArrayList<>();
        listaFotosRecuperadas = new HashMap<>();
        listaURLFotos = new ArrayList<>();

        spnEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setAdapterSpinnerCidades();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, (AppCompatActivity) getActivity(), 1);

        carregarDadosSpinner();

        btnCadastrarAnuncio.setOnClickListener(v -> validarDadosAnuncio());
    }

    private void preencheCampos(Animal anuncio) {

        edtNome.setText(anuncio.getNome());
        edtRaca.setText(anuncio.getRaca());
        edtDescricao.setText(anuncio.getDescricao());

        int spinnerPosition = adapterEspecies.getPosition(anuncio.getEspecie());
        spnEspecie.setSelection(spinnerPosition);

        spinnerPosition = adapterSexos.getPosition(anuncio.getSexo());
        spnSexo.setSelection(spinnerPosition);

        spinnerPosition = adapterIdades.getPosition(anuncio.getIdade());
        spnIdade.setSelection(spinnerPosition);

        spinnerPosition = adapterPortes.getPosition(anuncio.getPorte());
        spnPorte.setSelection(spinnerPosition);

        spinnerPosition = adapterEstados.getPosition(anuncio.getUf());
        spnEstado.setSelection(spinnerPosition);

        setAdapterSpinnerCidades();
        spinnerPosition = adapterCidades.getPosition(anuncio.getCidade());
        spnCidade.setSelection(spinnerPosition);

        editando = true;
        idEditando = anuncio.getIdAnimal();

        configuraFotos(anuncio);
    }

    /**
     * Popula imaageviews das imagens
     *
     * @param anuncio animal
     */
    private void configuraFotos(Animal anuncio) {

        try {
            dialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setMessage(R.string.carregando_fotos)
                    .setCancelable(false)
                    .build();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> listaFotos = anuncio.getFotos();

        String[] fotosAnuncioSelecionado = new String[listaFotos.size()];
        fotosAnuncioSelecionado = listaFotos.toArray(fotosAnuncioSelecionado);

        final Context ctx = viewFragment.getContext();

        final String[] finalFotosAnuncioSelecionado = fotosAnuncioSelecionado;

        for (int i = 0; i < fotosAnuncioSelecionado.length; i++) {

            switch (i) {

                //imagem mark1
                case 0:
                    try {
                        imagem1.post(() -> Picasso.get().load(finalFotosAnuncioSelecionado[0])
                                .resize(imagem1.getWidth(), imagem1.getHeight())
                                .centerCrop()
                                .into(imagem1, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Drawable drawable = imagem1.getDrawable();

                                        Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();

                                        String path = MediaStore.Images.Media.insertImage(ctx
                                                .getContentResolver(), mBitmap, null, null);
                                        Uri uri = Uri.parse(path);

                                        listaFotosRecuperadas.put(1, uri.toString());
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                    }
                                }));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                //imagem mark2
                case 1:
                    try {
                        imagem2.post(() -> Picasso.get().load(finalFotosAnuncioSelecionado[1])
                                .resize(imagem2.getWidth(), imagem2.getHeight())
                                .centerCrop()
                                .into(imagem2, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Drawable drawable = imagem2.getDrawable();

                                        Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();

                                        String path = MediaStore.Images.Media.insertImage(ctx
                                                .getContentResolver(), mBitmap, null, null);
                                        Uri uri = Uri.parse(path);


                                        listaFotosRecuperadas.put(2, uri.toString());
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                    }
                                }));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                //imagem mark3
                case 2:

                    try {
                        imagem3.post(() -> Picasso.get().load(finalFotosAnuncioSelecionado[2])
                                .resize(imagem3.getWidth(), imagem3.getHeight())
                                .centerCrop()
                                .into(imagem3, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Drawable drawable = imagem3.getDrawable();
                                        // ...
                                        Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();

                                        String path = MediaStore.Images.Media.insertImage(ctx
                                                .getContentResolver(), mBitmap, null, null);
                                        Uri uri = Uri.parse(path);

                                        listaFotosRecuperadas.put(3, uri.toString());
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                    }
                                }));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }


    /**
     * Configura o adapter do spinner Cidades
     */
    private void setAdapterSpinnerCidades() {

        try {
            adapterCidades = new ArrayAdapter<>(Objects.requireNonNull(getContext()), simple_spinner_item,
                    Util.getCidadesJSON(spnEstado.getSelectedItem().toString(), getContext()));
            adapterCidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spnCidade.setAdapter(adapterCidades);
            adapterCidades.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param animal anuncio
     */
    private void salvarAnuncio(Animal animal) {

        hideKeyboard(getContext(), edtDescricao);

        try {
            dialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setMessage(R.string.salvando_anuncio)
                    .setCancelable(false)
                    .build();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //salvar imagem no storage
        try {
            int tamanhoLista = animal.getFotos().size();
            for (int i = 0; i < tamanhoLista; i++) {
                String urlImagem = animal.getFotos().get(i);
                salvarFotosStorage(animal, urlImagem, tamanhoLista, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param animal     animal
     * @param url        url
     * @param totalFotos número de fotos
     * @param contador   contador
     */
    private void salvarFotosStorage(final Animal animal, String url, final int totalFotos, int contador) {


        try {
            //cria nó do storage
            final StorageReference imagemAnimal = storage
                    .child("imagens")
                    .child("animais")
                    .child(animal.getIdAnimal())
                    .child("imagem" + contador);

            Uri selectedImage = Uri.parse(url);

            //imagem comprimida
            byte[] byteArray = comprimirImagem(selectedImage);

            if(byteArray != null) {

                UploadTask uploadTask = imagemAnimal.putBytes(byteArray);

                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return imagemAnimal.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Uri firebaseUrl = task.getResult();
                        String urlConvertida = Objects.requireNonNull(firebaseUrl).toString();
                        listaURLFotos.add(urlConvertida);

                        if (totalFotos == listaURLFotos.size()) {
                            animal.setFotos(listaURLFotos);
                            animal.salvar();
                            Util.setSnackBar(layout, getString(R.string.sucesso_ao_fazer_upload));

                            MainActivity.reconfiguraNotificacoes(getActivity());

                            dialog.dismiss();


                            //redireciona para MeusAnunciosFragment
                            FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.view_pager, new MeusAnunciosFragment()).addToBackStack(null).commit();
                        }
                    } else {
                        Util.setSnackBar(layout, getString(R.string.falha_upload));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * metodo auxiliar para comprimir imagens que serao salvas do firebase storage
     *
     * @param selectedImage imagem selecionada
     * @return byte[]
     */
    private byte[] comprimirImagem(Uri selectedImage) {
        byte[] byteArray = null;
        try {
            InputStream imageStream;

            imageStream = Objects.requireNonNull(getContext()).getApplicationContext().getContentResolver().openInputStream(
                    selectedImage);

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, Constantes.QUALIDADE_IMAGEM, stream);
            byteArray = stream.toByteArray();

            stream.close();

        } catch (OutOfMemoryError error) {
            exibirDialogMemoria();
            error.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void exibirDialogMemoria() {

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle(getString(R.string.erro_memoria));
            builder.setMessage(getString(R.string.pouca_memoria));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * cria objeto que representa os campos preenchidos da activity
     *
     * @return animal
     */
    private Animal getAnimalDaActivity() {

        Animal retorno = new Animal();
        if (editando) {
            retorno.setIdAnimal(idEditando);
        }

        try {
            String nome = edtNome.getText().toString();
            String especie = spnEspecie.getSelectedItem().toString();
            String sexo = spnSexo.getSelectedItem().toString();
            String idade = spnIdade.getSelectedItem().toString();
            String porte = spnPorte.getSelectedItem().toString();
            String raca = edtRaca.getText().toString();
            String descricao = edtDescricao.getText().toString();
            String estado = spnEstado.getSelectedItem().toString();
            String cidade = spnCidade.getSelectedItem().toString();

            retorno.setDonoAnuncio(ConfiguracaoFirebase.getIdUsuario());
            retorno.setNome(nome);
            retorno.setEspecie(especie);
            retorno.setSexo(sexo);
            retorno.setIdade(idade);
            retorno.setPorte(porte);
            retorno.setRaca(raca);
            retorno.setDescricao(descricao);
            retorno.setUf(estado);
            retorno.setCidade(cidade);
            retorno.setFotos(new ArrayList<>(listaFotosRecuperadas.values()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * valida o preenchimento dos dados pelo usuário
     */
    private void validarDadosAnuncio() {

        Animal animal = this.getAnimalDaActivity();

        if (listaFotosRecuperadas.size() != 0) {
            if (!animal.getNome().isEmpty()) {
                if (!animal.getRaca().isEmpty()) {
                    if (!animal.getDescricao().isEmpty()) {

                        salvarAnuncio(animal);

                    } else {
                        Util.setSnackBar(layout, getString(R.string.preencha_descricao));
                    }
                } else {
                    Util.setSnackBar(layout, getString(R.string.preencha_raca));
                }
            } else {
                Util.setSnackBar(layout, getString(R.string.preencha_nome));
            }
        } else {
            Util.setSnackBar(layout, getString(R.string.selecione_foto));
        }
    }

    /**
     * clique das fotos
     *
     * @param v view
     */
    @Override
    public void onClick(View v) {

        requisicao = 1;

        switch (v.getId()) {
            case R.id.imageCad1:
                requisicao = 1;
                break;
            case R.id.imageCad2:
                requisicao = 2;
                break;
            case R.id.imageCad3:
                requisicao = 3;
                break;
        }
        escolherImagem();
    }

    /**
     * ImagePick
     */
    private void escolherImagem() {
        try {


            PickSetup setup = new PickSetup()
                    .setTitle(getString(R.string.escolha))
                    .setFlip(true)
                    .setMaxSize(Constantes.PICK_MAX_SIZE)
                    .setCameraButtonText(getString(R.string.cameraa))
                    .setCancelText(getString(R.string.cancelar))
                    .setGalleryButtonText(getString(R.string.galeria));
            Log.d("INFO91", "escolher imagem");

            //.setTitleColor(yourColor)
            //.setBackgroundColor(yourColor)
            //.setProgressText(yourText)
            //.setProgressTextColor(yourColor)
            //.setCancelTextColor(yourColor)
            //.setButtonTextColor(R.color.colorAccent)
            //.setDimAmount(yourFloat)
            //.setIconGravity(Gravity.LEFT)
            //.setButtonOrientation(LinearLayoutCompat.VERTICAL)
            //.setSystemDialog(false)
            //.setGalleryIcon(yourIcon)
            //.setCameraIcon(yourIcon);
            //PickImageDialog.build(setup).show(this);

            PickImageDialog.build(setup)
                    .setOnPickResult(r -> {

                        Log.d("INFO91", "pick 1");
                        try {
                            if (r != null) {
                                Log.d("INFO91", "pick 2");

                                if (r.getError() == null) {
                                    Log.d("INFO91", "pick 3");

                                    Uri imagemSelecionada = r.getUri();
                                    String caminhoImagem = Objects.requireNonNull(imagemSelecionada).toString();

                                    if (requisicao == 1) {
                                        Log.d("INFO91", "req 1");

                                        imagem1.setImageURI(r.getUri());

                                    } else if (requisicao == 2) {
                                        Log.d("INFO91", "req 2");

                                        imagem2.setImageURI(r.getUri());

                                    } else {
                                        Log.d("INFO91", "req 3");

                                        imagem3.setImageURI(r.getUri());
                                    }

                                    listaFotosRecuperadas.put(requisicao, caminhoImagem);
                                } else {
                                    Log.d("INFO91", "get error: " + r.getError().getMessage());
                                }
                            }
                        } catch (OutOfMemoryError e) {
                            Log.d("INFO91", "erro: memory " + e.getMessage());
                            e.printStackTrace();
                        }
                    })
                    .setOnPickCancel(() -> Log.d("INFO91", "pick cancel")).show(Objects.requireNonNull(getActivity()).getSupportFragmentManager());
        } catch (Exception e) {
            Log.d("INFO91", "erro final: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * preenche os spinners
     */
    private void carregarDadosSpinner() {

        String[] especies = Util.getEspecies(Objects.requireNonNull(getContext()));
        String[] sexos = getResources().getStringArray(R.array.sexos);
        String[] idades = getResources().getStringArray(R.array.idade);
        String[] portes = getResources().getStringArray(R.array.portes);
        String[] estados = Util.getEstadosJSON(getContext());

        //especies
        try {
            adapterEspecies = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, especies);
            adapterEspecies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnEspecie.setAdapter(adapterEspecies);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //sexos
        try {
            adapterSexos = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sexos);

            adapterSexos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnSexo.setAdapter(adapterSexos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* idades */
        try {
            adapterIdades = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, idades);
            adapterIdades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnIdade.setAdapter(adapterIdades);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* portes */
        try {
            adapterPortes = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, portes);
            adapterPortes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnPorte.setAdapter(adapterPortes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*estados*/
        try {
            adapterEstados = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, estados);
            adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnEstado.setAdapter(adapterEstados);
            spnEstado.setSelection(25);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            for (int permissaoResultado : grantResults) {
                if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    alertaValidacaoPermissao();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alertaValidacaoPermissao() {

        try {
            Context ctx = Objects.requireNonNull(getActivity()).getApplicationContext();

            if (ctx != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle(getString(R.string.permissoes_negadas));
                builder.setMessage(getString(R.string.necessario_aceitar_permissoes));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {

                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * esconde o teclado virtual
     *
     * @param context  contexto
     * @param editText view
     */
    private static void hideKeyboard(Context context, View editText) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}