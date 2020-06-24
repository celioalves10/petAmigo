package com.celvansystems.projetoamigoanimal.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class UsuarioPROActivity extends AppCompatActivity {

    private DatabaseReference anunciosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_pro);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.usuario_pro));
        configuraNavBar();

        inicializarComponentes();
    }

    private void inicializarComponentes() {

        Button btnApagarAnunciosAntigos = findViewById(R.id.btn_ApagarAntigos);
        btnApagarAnunciosAntigos.setVisibility(View.INVISIBLE);

        FirebaseUser usuario = ConfiguracaoFirebase.getFirebaseAutenticacao().getCurrentUser();

        if (usuario != null) {
            if (Objects.requireNonNull(usuario.getEmail()).equalsIgnoreCase("celioalves10@gmail.com") ||
                    usuario.getEmail().equalsIgnoreCase("celioalves10@hotmail.com")) {
                btnApagarAnunciosAntigos.setVisibility(View.VISIBLE);
            }
        }
        anunciosRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_animais");

        btnApagarAnunciosAntigos.setOnClickListener(
                this::onClick);
    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClick(View v) {
        try {

            anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        Animal animal = ds.getValue(Animal.class);

                        if (animal != null) {

                            String data = animal.getDataCadastro();

                            try {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Date date = sdf.parse(data);
                                Calendar cadastro = Calendar.getInstance();

                                if (date != null) {
                                    cadastro.setTime(date);
                                    Calendar hoje = Calendar.getInstance();

                                    if (daysBetween(cadastro.getTime(), hoje.getTime()) > Constantes.MAX_DIAS_ANUNCIOS) {
                                        animal.remover();
                                        Log.d("INFO45", animal.getDataCadastro());
                                        try {
                                            Thread.sleep(200);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
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
}
