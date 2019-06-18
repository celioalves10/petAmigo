package com.celvansystems.projetoamigoanimal.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.activity.ComentariosActivity;
import com.celvansystems.projetoamigoanimal.model.Animal;
import com.celvansystems.projetoamigoanimal.model.Comentario;
import com.celvansystems.projetoamigoanimal.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GerenciadorNotificacoes {

    private List<Animal> listaAnunciosGeral;
    private static List<String> comentariosNotificacoes = new ArrayList<>();
    private Context ctx;

    public GerenciadorNotificacoes(Context ctx) {
        this.ctx = ctx;
    }

    public void configuraNotificacoesMain() {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {

            final DatabaseReference anunciosRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais");

            final String usuarioAtual = ConfiguracaoFirebase.getIdUsuario();
            listaAnunciosGeral = new ArrayList<>();

            anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot anuncios : dataSnapshot.getChildren()) {

                        if (anuncios != null) {

                            final String idAnimal = Objects.requireNonNull(anuncios.child("idAnimal").getValue()).toString();
                            //final String nome = Objects.requireNonNull(anuncios.child("nome").getValue()).toString();
                            final String donoAnuncio = Objects.requireNonNull(anuncios.child("donoAnuncio").getValue()).toString();

                            if (donoAnuncio.equalsIgnoreCase(usuarioAtual)) {

                                Log.d("INFO13", "dono igual usuario atual");

                                final Animal anuncio = new Animal();
                                anuncio.setIdAnimal(idAnimal);
                                anuncio.setDonoAnuncio(donoAnuncio);
                                //anuncio.setNome(nome);

                                listaAnunciosGeral.add(anuncio);

                                final DatabaseReference comentariosRef = anunciosRef.child(idAnimal).child("comentarios");
                                Log.d("INFO13", idAnimal);

                                comentariosRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        List<Comentario> listaComentarios = new ArrayList<>();
                                        Animal anuncioUsuarioAtual = anuncio;
                                        //anuncioUsuarioAtual.setNome(nome);

                                        for (final DataSnapshot comentarios : dataSnapshot.getChildren()) {

                                            Comentario coment = new Comentario();
                                            coment.setTexto(Objects.requireNonNull(comentarios.child("texto").getValue()).toString());
                                            coment.setDatahora(Objects.requireNonNull(comentarios.child("datahora").getValue()).toString());

                                            Usuario usuario = new Usuario();
                                            usuario.setId(Objects.requireNonNull(comentarios.child("usuario").child("id").getValue()).toString());
                                            usuario.setNome(Objects.requireNonNull(comentarios.child("usuario").child("nome").getValue()).toString());

                                            coment.setUsuario(usuario);

                                            listaComentarios.add(coment);
                                        }

                                        anuncioUsuarioAtual.setListaComentarios(listaComentarios);

                                        addChildEvent(comentariosRef, anuncioUsuarioAtual);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        comentariosNotificacoes = new ArrayList<>();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void addChildEvent(DatabaseReference comentariosRef, final Animal anuncioUsuarioAtual) {

        comentariosRef.addChildEventListener(new ChildEventListener() {

            List<Comentario> listaComentarios = anuncioUsuarioAtual.getListaComentarios();
            final Context contexto = ctx;

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Log.d("INFO13", "child added");

                Comentario coment = new Comentario();
                coment.setTexto(Objects.requireNonNull(dataSnapshot.child("texto").getValue()).toString());
                coment.setDatahora(Objects.requireNonNull(dataSnapshot.child("datahora").getValue()).toString());

                Usuario usuario = new Usuario();
                usuario.setId(Objects.requireNonNull(dataSnapshot.child("usuario").child("id").getValue()).toString());
                usuario.setNome(Objects.requireNonNull(dataSnapshot.child("usuario").child("nome").getValue()).toString());

                coment.setUsuario(usuario);

                listaComentarios.add(coment);
                anuncioUsuarioAtual.setListaComentarios(listaComentarios);

                configuraNotificacoes(anuncioUsuarioAtual);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void configuraNotificacoes(Animal anuncio) {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {

            try {
                final String idUsuario = ConfiguracaoFirebase.getIdUsuario();
                final String donoAnuncio = anuncio.getDonoAnuncio();

                //if (donoAnuncio.equalsIgnoreCase(idUsuario)) {

                    if (anuncio.getListaComentarios() != null) {
                        int size = anuncio.getListaComentarios().size();

                        if (size > 0) {
                            String texto = anuncio.getListaComentarios().get(size - 1).getTexto();
                            String comentarista = anuncio.getListaComentarios().get(size - 1).getUsuario().getId();

                            if (comentarista != null) {
                                Comentario coment = new Comentario();
                                coment.setTexto(texto);

                                //int sizeComentsNotificacoes = comentariosNotificacoes.size();


                                createNotificationMessage(ctx, ctx.getString(R.string.novo_comentario),
                                coment.getTexto(), anuncio);

                                comentariosNotificacoes.add(texto);
                            }
                        }
                    }
                //}

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationMessage(final Context ctx, String Title, String Msg, final Animal anuncio) {

        try {
            int size = anuncio.getListaComentarios().size();

            String comentarista = anuncio.getListaComentarios().get(size - 1).getUsuario().getId();
            Log.d("INFO13", "animal: " + anuncio.getNome());
            Log.d("INFO13", "comentarista: " + comentarista);
            Log.d("INFO13", "dono: " + anuncio.getDonoAnuncio());

            String texto = anuncio.getListaComentarios().get(size - 1).getTexto();

            if (!anuncio.getDonoAnuncio().equalsIgnoreCase(comentarista)) {

                int sizeComentsNotificacoes = comentariosNotificacoes.size();
                Log.d("INFO13", "sizeComents: " + sizeComentsNotificacoes);

                if (size == 0 || !comentariosNotificacoes.get(size - 1).equalsIgnoreCase(texto)) {

                    //Log.d("INFO13", "notificacao criada - usuario diferente " + comentarista);
                    int id = 0;
                    Intent intent = new Intent(ctx, ComentariosActivity.class);
                    intent.putExtra("anuncioSelecionado", anuncio);
                    //Log.d("INFO13", "sizeComentsCreate: " + anuncio.getListaComentarios().size());

                    PendingIntent contentIntent = PendingIntent.getActivity(ctx, id, intent, 0);

                    Notification.Builder b = new Notification.Builder(ctx);

                    NotificationChannel mChannel = null;

                    b.setAutoCancel(true)
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                            .setContentTitle(Title)
                            .setTicker(Title)
                            .setContentText(Msg)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(contentIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        mChannel = new NotificationChannel("cid", "name", NotificationManager.IMPORTANCE_HIGH);
                        b.setChannelId("cid");
                        mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .build());
                    }

                    NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.createNotificationChannel(mChannel);
                    }

                    notificationManager.notify(id, b.build());
                }
            } else {
                Log.d("INFO13", "notificacao nao criada - usuario igual " + comentarista);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
