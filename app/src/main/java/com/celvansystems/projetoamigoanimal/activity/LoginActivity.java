package com.celvansystems.projetoamigoanimal.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Util;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.NameNotFoundException;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Permissao para ler contatos.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private FirebaseAuth authentication;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView mImageBg_color;
    private Switch swtLoginCadastrar;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private View layout;
    private LoginButton btnLoginFacebook;
    private SignInButton btnLoginGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        setupActionBar();

        authentication = ConfiguracaoFirebase.getFirebaseAutenticacao();

        inicializarComponentes();

        configuraLoginFacebook();

        configuraLoginGoogle();

        configuraNavBar();

        try {

            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.celvansystems.projetoamigoanimal",
                    GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void inicializarComponentes() {

        try {

            mEmailView = findViewById(R.id.txiEmail);
            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);
            mImageBg_color = findViewById(R.id.imageViewbg_color);
            mPasswordView = findViewById(R.id.txiPassword);

            Button btnLogin = findViewById(R.id.btnLogin);
            btnLoginFacebook = findViewById(R.id.login_button_facebook);
            btnLoginGoogle = findViewById(R.id.login_button_google);

            swtLoginCadastrar = findViewById(R.id.swtLoginCadastrar);
            TextView txvEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

            layout = findViewById(R.id.login_layout);

            TextView txtAnuncios = findViewById(R.id.txtAnuncios);
            txtAnuncios.setOnClickListener(v -> {
                if(ConfiguracaoFirebase.isUsuarioLogado()) {
                    ConfiguracaoFirebase.getFirebaseAutenticacao().signOut();
                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            });

            mEmailView.setText("");
            mPasswordView.setText("");

            mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    tentarLogin();
                    return true;
                }
                return false;
            });

            txvEsqueciSenha.setOnClickListener(v -> {
                hideKeyboard(LoginActivity.this, mEmailView);
                if (mEmailView.getText() != null &&
                        !mEmailView.getText().toString().equalsIgnoreCase("") &&
                        mEmailView.getText().toString().contains("@")) {
                    enviarEmailRecuperacao(mEmailView.getText().toString());
                } else {
                    Util.setSnackBar(layout, getString(R.string.insira_seu_email));
                }
            });

            btnLogin.setOnClickListener(view -> tentarLogin());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.setElevation(50);
                btnLoginFacebook.setElevation(50);
                btnLoginGoogle.setElevation(50);
                mEmailView.setElevation(20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * método que configura o login por meio do facebook
     */
    private void configuraLoginFacebook() {

        try {
            callbackManager = CallbackManager.Factory.create();
            btnLoginFacebook.setLoginText(getString(R.string.fazer_login_facebook));
            btnLoginFacebook.setPermissions(Arrays.asList(
                    "email", "public_profile"));

            //Callback registration
            btnLoginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Util.setSnackBar(layout, "Login pelo facebook cancelado");
                }

                @Override
                public void onError(FacebookException exception) {
                    Util.setSnackBar(layout, "Erro: " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Util.setSnackBar(layout, e.getMessage());
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {

        try {
            showProgress(true);

            final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

            authentication.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        try {
                            if (task.isSuccessful()) {

                                FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();

                                concluiCadastroUsuario(user);

                                Util.setSnackBar(layout, getString(R.string.login_sucesso));
                            } else {
                                Util.setSnackBar(layout, getString(R.string.falha_login));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showProgress(false);
                        mImageBg_color.setVisibility(View.INVISIBLE);
                    }).addOnFailureListener(e -> Util.setSnackBar(layout, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            Util.setSnackBar(layout, e.getMessage());
        }
    }

    /**
     * método que configura o login por meio do google
     */
    private void configuraLoginGoogle() {

        try {
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.google_web_client))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

            //Google Login
            btnLoginGoogle.setSize(SignInButton.SIZE_WIDE);
            btnLoginGoogle.setOnClickListener(v -> signInGoogle());
        } catch (Exception e) {
            e.printStackTrace();
            Util.setSnackBar(layout, "1-" + e.getMessage());
        }
    }

    /**
     * ação do botao de login do google
     */
    private void signInGoogle() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, Constantes.GOOGLE_REQUEST_CODE);
        } catch (Exception e) {
            Util.setSnackBar(layout, "2-" + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (ConfiguracaoFirebase.isUsuarioLogado()) {

                // Verifica se há conta do google logada.
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                // Verifica se há conta do facebook logada.
                AccessToken token = AccessToken.getCurrentAccessToken();

                //se o usuário estiver logado, vai direto pra main
                if (checkIfEmailVerified() || token != null || account != null) {

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        } catch (Exception e) {
            Util.setSnackBar(layout, "Error 3 - " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == Constantes.GOOGLE_REQUEST_CODE) {

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                task.addOnCompleteListener(task1 -> {

                    // Google Sign In was successful, authenticate with Firebase
                    try {

                        GoogleSignInAccount account = task1.getResult(ApiException.class);

                        if (account != null) {
                            firebaseAuthWithGoogle(account);
                        } else {
                            Util.setSnackBar(layout, "account = null");
                        }
                        //handleSignInResult(task);
                    } catch (ApiException e) {
                        e.printStackTrace();
                        Util.setSnackBar(layout, "4-" + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Util.setSnackBar(layout, "14-" + e.getMessage());
                    }
                }).addOnFailureListener(e -> Util.setSnackBar(layout, "5-" + e.getMessage()));

            } else {
                //Facebook
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            Util.setSnackBar(layout, "6-" + e.getMessage());
        }
    }

    /**
     * método usado para fazer login no google com uma credential
     *
     * @param acct conta
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        showProgress(true);
        mImageBg_color.setVisibility(View.GONE);

        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            authentication.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {

                        try {
                            if (task.isSuccessful()) {

                                FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();

                                concluiCadastroUsuario(user);

                                Util.setSnackBar(layout, getString(R.string.login_sucesso));
                            } else {
                                Util.setSnackBar(layout, getString(R.string.falha_login));
                            }
                            showProgress(false);
                            mImageBg_color.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Util.setSnackBar(layout, "7-" + e.getMessage());
                        }
                    }).addOnFailureListener(e -> Util.setSnackBar(layout, "8-" + e.getMessage()));
        } catch (Exception e) {
            Util.setSnackBar(layout, "9-" + e.getMessage());
        }
    }

    private void concluiCadastroUsuario(final FirebaseUser user) {

        if (user != null) {
            try {
                final String uidTask = Objects.requireNonNull(user.getUid());
                final String nomeTask = Objects.requireNonNull(user.getDisplayName());
                final String fotoTask = Objects.requireNonNull(user.getPhotoUrl()).toString();
                final String emailTask = Objects.requireNonNull(user.getEmail());

                final DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                        .child("usuarios");

                usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        boolean usuarioExists = false;

                        //criando usuario...
                        Usuario usuario = new Usuario();
                        usuario.setId(Objects.requireNonNull(uidTask));
                        usuario.setNome(Objects.requireNonNull(nomeTask));
                        usuario.setFoto(Objects.requireNonNull(fotoTask));
                        usuario.setEmail(Objects.requireNonNull(emailTask));

                        for (DataSnapshot usuarios : dataSnapshot.getChildren()) {
                            if (usuarios != null) {
                                if (usuarios.child("id").getValue() != null) {

                                    if (Objects.requireNonNull(usuarios.child("id").getValue()).toString().equalsIgnoreCase(uidTask)) {
                                        usuarioExists = true;

                                        if (Objects.requireNonNull(usuarios.child("loginCompleto").getValue()).toString()
                                                .equalsIgnoreCase("true")) {

                                            //direciona para a tela principal
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                        } else {
                                            startActivity(new Intent(LoginActivity.this, ComplementoLoginActivity.class));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if (!usuarioExists) {
                            usuario.salvar();
                            startActivity(new Intent(LoginActivity.this, ComplementoLoginActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Util.setSnackBar(layout, "12-" + databaseError.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Util.setSnackBar(layout, "13-" + e.getMessage());
            }
        }
    }

    private void configuraNavBar() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.black_overlay));

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.black_overlay));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tentarLogin() {

        try {
            //esconde o teclado
            hideKeyboard(getApplicationContext(), mPasswordView);

            if (mEmailView != null && mPasswordView != null) {
                // Store values at the time of the login attempt.
                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();

                if (!email.equalsIgnoreCase("") && !password.equalsIgnoreCase("")) {
                    //cadastrando...
                    if (swtLoginCadastrar.isChecked()) {

                        authentication.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {

                                        swtLoginCadastrar.setChecked(false);
                                        Util.setSnackBar(layout, getString(R.string.cadastro_realizado));
                                        sendVerificationEmail();

                                        //criando usuario...
                                        Usuario usuario = new Usuario();
                                        usuario.setId(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid());
                                        usuario.setEmail(email);
                                        usuario.salvar();

                                    } else {
                                        String erroExcecao;
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            erroExcecao = getString(R.string.digite_senha_forte);
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            erroExcecao = getString(R.string.digite_email_valido);
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            erroExcecao = getString(R.string.conta_cadastrada);
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            erroExcecao = getString(R.string.usuario_inexistente);
                                        } catch (Exception e) {
                                            erroExcecao = getString(R.string.falha_cadastro_usuario) + e.getMessage();
                                            e.printStackTrace();
                                        }
                                        Util.setSnackBar(layout, getString(R.string.erro) + erroExcecao);
                                    }
                                });

                        //logando...
                    } else {

                        //direciona para a activity principal
                        authentication.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        if (checkIfEmailVerified()) {

                                            //redirecionamento de acordo com o cadastro do usuario (completo ou incompleto)
                                            String usuarioId = Objects.requireNonNull(ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser()).getUid();
                                            redireciona(usuarioId);

                                        } else {
                                            Util.setSnackBar(layout, getString(R.string.email_nao_verificado));

                                            //envia e-mail de verificacao
                                            sendVerificationEmail();
                                        }
                                    } else {
                                        Util.setSnackBar(layout, getString(R.string.email_senha_invalido));
                                    }
                                    showProgress(false);
                                    mImageBg_color.setVisibility(View.INVISIBLE);
                                });
                        showProgress(true);
                        mImageBg_color.setVisibility(View.VISIBLE);

                    }
                } else {
                    Util.setSnackBar(layout, getString(R.string.email_senha_invalido));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redireciona(final String usuarioId) {

        try {
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebase()
                    .child("usuarios");

            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot usuarios : dataSnapshot.getChildren()) {

                        try {
                            if (usuarios != null) {

                                if (Objects.requireNonNull(usuarios.child("id").getValue()).toString().equalsIgnoreCase(usuarioId)) {

                                    if (Objects.requireNonNull(usuarios.child("loginCompleto").getValue()).toString().equalsIgnoreCase("true")) {

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {

                                        Intent intent = new Intent(LoginActivity.this, ComplementoLoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

    /**
     * firebase envia e-mail de verificação
     */
    private void sendVerificationEmail() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            assert user != null;
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // email enviado
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarEmailRecuperacao(String email) {

        try {
            authentication.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Util.setSnackBar(layout, getString(R.string.email_enviado));

                        } else {
                            Util.setSnackBar(layout, getString(R.string.nao_foi_possivel_email));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * verifica se o e-mail do usuário foi validado pelo firebase
     *
     * @return boolean e-mail verified
     */
    private boolean checkIfEmailVerified() {

        boolean retorno = false;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            retorno = user.isEmailVerified();
        }
        return retorno;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        try {
            List<String> emails = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                emails.add(cursor.getString(ProfileQuery.ADDRESS));
                cursor.moveToNext();
            }
            addEmailsToAutoComplete(emails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {

        try {
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(LoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

            mEmailView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface ProfileQuery {

        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    private void populateAutoComplete() {
        try {
            if (!mayRequestContacts()) {
                return;
            }
            getLoaderManager().initLoader(0, null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permissoes_requeridas, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        try {
            if (requestCode == REQUEST_READ_CONTACTS) {
                if (grantResults.length == 1 && grantResults[0] == PERMISSION_GRANTED) {
                    populateAutoComplete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the {@link ActionBar}, if the API is available.
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        // Show the Up button in the action bar.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * esconde teclado
     * */
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
}

