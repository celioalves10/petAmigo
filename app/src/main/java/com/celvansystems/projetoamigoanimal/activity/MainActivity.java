package com.celvansystems.projetoamigoanimal.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.celvansystems.projetoamigoanimal.helper.GerenciadorNotificacoes;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.fragment.AnunciosFragment;
import com.celvansystems.projetoamigoanimal.fragment.CadastrarAnuncioFragment;
import com.celvansystems.projetoamigoanimal.fragment.DoacaoFragment;
import com.celvansystems.projetoamigoanimal.fragment.MeusAnunciosFragment;
import com.celvansystems.projetoamigoanimal.fragment.PerfilUsuarioFragment;
import com.celvansystems.projetoamigoanimal.fragment.SobreAppFragment;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth autenticacao;
    private NavigationView navigationView;
    private ImageView imageViewPerfil, imvAds;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        verificaUsuarioPRO();

        inicializarComponentes();

        configuraNavBar();

        habilitaOpcoesNav();

        //Notificações
        reconfiguraNotificacoes(this);
    }

    private void verificaUsuarioPRO() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Constantes.isPRO = prefs.getBoolean("pro", false);
    }

    public static void reconfiguraNotificacoes(Context ctx) {
        try {
            GerenciadorNotificacoes gn = new GerenciadorNotificacoes(ctx);
            gn.configuraNotificacoesMain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configuraNavBar() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.lightgray));
                imageViewPerfil.setElevation(25);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_doacao);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
            if (resultCode != 0) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("purchased", true);
                editor.apply();
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }*/

    private void inicializarComponentes() {

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

            View layout = findViewById(R.id.view_pager);

            navigationView = findViewById(R.id.nav_view);
            headerView = navigationView.getHeaderView(0);

            imageViewPerfil = headerView.findViewById(R.id.imageView_perfil);
            imvAds = findViewById(R.id.imv_ads);
            imvAds.setOnClickListener(view ->
                    startActivity(new Intent(MainActivity.this, PROActivity.class)));

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            Intent myIntent = getIntent(); // gets the previously created intent
            String usuario_excluido = myIntent.getStringExtra("usuario_excluido");

            if (usuario_excluido != null) {

                if (usuario_excluido.equalsIgnoreCase(getString(R.string.sim))) {
                    Util.setSnackBar(layout, getString(R.string.usuario_excluido));
                } else if (usuario_excluido.equalsIgnoreCase(getString(R.string.nao))) {
                    Util.setSnackBar(layout, getString(R.string.falha_excluir_usuario));
                }
            }

            boolean telefone = getIntent().getBooleanExtra("telefone", false);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (telefone) {
                fragmentTransaction.replace(R.id.view_pager, new DoacaoFragment()).commit();
            } else {
                fragmentTransaction.replace(R.id.view_pager, new AnunciosFragment()).commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregaDadosUsuario() {

        try {
            //Dados do Usuário
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                    .child("usuarios");

            usuariosRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot usuarios : dataSnapshot.getChildren()) {

                        if (usuarios != null) {

                            FirebaseUser user = autenticacao.getCurrentUser();

                            if (usuarios.child("id").getValue() != null && ConfiguracaoFirebase.isUsuarioLogado()) {
                                if (Objects.requireNonNull(usuarios.child("id").getValue()).toString().equalsIgnoreCase(Objects.requireNonNull(user).getUid())) {

                                    TextView navUsername = headerView.findViewById(R.id.textview_nome_humano);
                                    TextView navEmail = headerView.findViewById(R.id.textView_email_cadastrado);

                                    Usuario usuario = new Usuario();
                                    usuario.setId(ConfiguracaoFirebase.getIdUsuario());

                                    //nome
                                    if (usuarios.child("nome").getValue() != null) {
                                        usuario.setNome(Objects.requireNonNull(usuarios.child("nome").getValue()).toString());
                                        navUsername.setText(usuario.getNome());
                                    } else {
                                        navUsername.setText(user.getDisplayName());
                                    }

                                    //email
                                    if (usuarios.child("email").getValue() != null) {

                                        usuario.setEmail(Objects.requireNonNull(usuarios.child("email").getValue()).toString());
                                        navEmail.setText(usuario.getEmail());
                                    } else {
                                        navEmail.setText(user.getEmail());
                                    }
                                    //foto
                                    if (usuarios.child("foto").getValue() != null) {
                                        usuario.setFoto(Objects.requireNonNull(usuarios.child("foto").getValue()).toString());
                                        Picasso.get().load(usuario.getFoto()).into(imageViewPerfil);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void habilitaOpcoesNav() {

        try {
            Menu menuNav = navigationView.getMenu();

            MenuItem nav_minha_conta = menuNav.findItem(R.id.nav_minha_conta);
            //MenuItem nav_config_notificacoes = menuNav.findItem(R.id.nav_config_notificacoes);
            MenuItem nav_meus_anuncios = menuNav.findItem(R.id.nav_meus_anuncios);
            MenuItem nav_pet_cad = menuNav.findItem(R.id.pet_cad);
            MenuItem nav_pet_adote = menuNav.findItem(R.id.pet_adote);
            //MenuItem nav_pro = menuNav.findItem(R.id.nav_pro);
            //MenuItem nav_doacao = menuNav.findItem(R.id.doacao);
            //MenuItem nav_compartilhar_app = menuNav.findItem(R.id.nav_share_app);
            //MenuItem nav_conversar = menuNav.findItem(R.id.nav_conversar);
            //MenuItem nav_help = menuNav.findItem(R.id.nav_help);
            MenuItem nav_sair = menuNav.findItem(R.id.nav_sair);
            //MenuItem nav_pet_procurado = menuNav.findItem(R.id.pet_procurado);


            if (ConfiguracaoFirebase.isUsuarioLogado()) {

                //Log.d("INFO100", "email verificado");
                nav_minha_conta.setEnabled(true);
                nav_minha_conta.setTitle(R.string.txt_minha_conta);

                //nav_config_notificacoes.setEnabled(true);
                nav_meus_anuncios.setEnabled(true);
                nav_pet_cad.setEnabled(true);
                nav_pet_adote.setEnabled(true);
                //nav_conversar.setEnabled(true);
                nav_sair.setEnabled(true);
                //nav_pet_procurado.setEnabled(true);

            } else {
                nav_minha_conta.setTitle(R.string.txt_entrar);
                nav_minha_conta.setOnMenuItemClickListener(item -> {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                });
                //nav_config_notificacoes.setEnabled(false);
                nav_meus_anuncios.setEnabled(false);
                nav_pet_cad.setEnabled(false);
                nav_pet_adote.setEnabled(false);
                //nav_conversar.setEnabled(false);
                nav_sair.setEnabled(false);
                //nav_pet_procurado.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            carregaDadosUsuario();

            if (Constantes.isPRO) {
                imvAds.setImageResource(R.drawable.coroa_branca);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        try {
            //implementa fragmento
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_minha_conta) {

                fragmentTransaction.replace(R.id.view_pager, new PerfilUsuarioFragment()).addToBackStack("tag").commit();
            } else if (id == R.id.nav_meus_anuncios) {

                //reuso da activit meus anuncios
                fragmentTransaction.replace(R.id.view_pager, new MeusAnunciosFragment()).addToBackStack("tag").commit();

            } else if (id == R.id.pet_cad) {
                // fragment pet_cad cadastrar anuncio
                fragmentTransaction.replace(R.id.view_pager, new CadastrarAnuncioFragment()).addToBackStack("tag").commit();
            } else if (id == R.id.pet_adote) {
                //reuso da activity cadastrar anuncio
                fragmentTransaction.replace(R.id.view_pager, new AnunciosFragment()).addToBackStack("tag").commit();
            } else if (id == R.id.doacao) {
                // implementar funções na activit doação.
                fragmentTransaction.replace(R.id.view_pager, new DoacaoFragment()).addToBackStack("tag").commit();

            } else if (id == R.id.nav_pro) {
                startActivity(new Intent(this, PROActivity.class));

            } else if (id == R.id.nav_share_app) {

                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, Constantes.APPLICATION_NAME);
                    String shareMessage = "\n" + Constantes.APPLICATION_MESSAGE + "\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + Constantes.APPLICATION_ID + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, getText(R.string.escolha_uma_opcao)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else if (id == R.id.nav_help) {

                fragmentTransaction.replace(R.id.view_pager, new SobreAppFragment()).addToBackStack("tag").commit();

            } else if (id == R.id.nav_sair) {

                startActivity(new Intent(this, LoginActivity.class));

                if (ConfiguracaoFirebase.isUsuarioLogado()) {
                    autenticacao.signOut();
                    LoginManager.getInstance().logOut();
                }

                invalidateOptionsMenu(); //invalida o menu e chama o onPrepare de novo

                //Limpa todas as notificaçoes
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
                finish();
            }

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
