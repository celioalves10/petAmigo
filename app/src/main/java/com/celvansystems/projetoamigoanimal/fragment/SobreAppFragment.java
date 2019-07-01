package com.celvansystems.projetoamigoanimal.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.ConfiguracaoFirebase;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.google.firebase.auth.FirebaseUser;

public class SobreAppFragment extends Fragment {

    private View view;

    public SobreAppFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sobre_app, container, false);

        inializaComponentes();

        return view;
    }

    private void inializaComponentes() {

        ConstraintLayout constAvaliarApp = view.findViewById(R.id.constraintAvaliarApp);
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

        ConstraintLayout constCompartilharApp = view.findViewById(R.id.constraint_compartilhar);
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

        ConstraintLayout constEnviarFeedBack = view.findViewById(R.id.constraintFeedback);
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

        ConstraintLayout constErro = view.findViewById(R.id.constraintErro);
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

    }
}