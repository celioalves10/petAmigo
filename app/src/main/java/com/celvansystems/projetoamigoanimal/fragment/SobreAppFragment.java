package com.celvansystems.projetoamigoanimal.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.celvansystems.projetoamigoanimal.BuildConfig;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.Constantes;

import java.util.Objects;

public class SobreAppFragment extends Fragment {

    private View view;
    private ConstraintLayout constAvaliarApp;
    private ConstraintLayout constCompartilharApp;
    private ConstraintLayout constEnviarFeedBack;
    private ConstraintLayout constErro;

    private ImageView imvFacebook;
    private ImageView imvInstagram;

    public SobreAppFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sobre_app, container, false);

        inializaComponentes();

        configurarAcoes();

        return view;
    }

    private void inializaComponentes() {

        constAvaliarApp = view.findViewById(R.id.constraintAvaliarApp);
        constCompartilharApp = view.findViewById(R.id.constraint_compartilhar);
        constEnviarFeedBack = view.findViewById(R.id.constraintFeedback);
        constErro = view.findViewById(R.id.constraintErro);
        imvFacebook = view.findViewById(R.id.imageView_seguir_face);
        imvInstagram = view.findViewById(R.id.imageView_seguir_insta);

        TextView txvVersion = view.findViewById(R.id.txvVersao);
        txvVersion.setText(String.format("%s %s", txvVersion.getText(), BuildConfig.VERSION_NAME));
    }

    private void configurarAcoes() {

        constAvaliarApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "https://play.google.com/store/apps/details?id=" + Constantes.APPLICATION_ID;

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        constCompartilharApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        constEnviarFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", getString(R.string.email_celvansystems), null));
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_usuario) + " " + Constantes.APPLICATION_NAME);
                    intent.putExtra(Intent.EXTRA_TEXT, R.string.mensagem);
                    startActivity(Intent.createChooser(intent, getString(R.string.escolha_cliente_email)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        constErro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", getString(R.string.email_celvansystems), null));
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.reportagem_bug) + " - " + Constantes.APPLICATION_NAME);
                    intent.putExtra(Intent.EXTRA_TEXT, R.string.mensagem);
                    startActivity(Intent.createChooser(intent, getString(R.string.escolha_cliente_email)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        imvFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = Constantes.ENDERECO_FACEBOOK;

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        imvInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = Constantes.ENDERECO_INSTAGRAM;

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) Objects
                .requireNonNull(getActivity())).getSupportActionBar()).hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(((AppCompatActivity) Objects
                .requireNonNull(getActivity())).getSupportActionBar()).show();
    }
}