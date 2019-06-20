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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GerenciadorNotificacoes {

    private static Map<String, List<Comentario>> comentariosNotificacoes = new HashMap<>();
    private Context ctx;

    public GerenciadorNotificacoes(Context ctx) {
        this.ctx = ctx;
    }

    public void configuraNotificacoesMain() {

        if (ConfiguracaoFirebase.isUsuarioLogado()) {

            final DatabaseReference anunciosRef = ConfiguracaoFirebase.getFirebase()
                    .child("meus_animais");

            final String usuarioAtual = ConfiguracaoFirebase.getIdUsuario();

            anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot anuncios : dataSnapshot.getChildren()) {

                        if (anuncios != null) {

                            final String idAnimal = Objects.requireNonNull(anuncios.child("idAnimal").getValue()).toString();
                            final String nome = Objects.requireNonNull(anuncios.child("nome").getValue()).toString();
                            final String donoAnuncio = Objects.requireNonNull(anuncios.child("donoAnuncio").getValue()).toString();
                            final String descricao = Objects.requireNonNull(anuncios.child("descricao").getValue()).toString();

                            if (donoAnuncio.equalsIgnoreCase(usuarioAtual)) {

                                Animal anuncio = new Animal();
                                anuncio.setIdAnimal(idAnimal);
                                anuncio.setDonoAnuncio(donoAnuncio);
                                anuncio.setNome(nome);
                                anuncio.setDescricao(descricao);

                                final Animal anuncioUsuarioAtual = anuncio;
                                final DatabaseReference comentariosRef = anunciosRef.child(idAnimal).child("comentarios");

                                Log.d("INFO13", "id animal: " + idAnimal);

                                comentariosRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        List<Comentario> listaComentarios = new ArrayList<>();

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

                                        comentariosNotificacoes.put(idAnimal, anuncioUsuarioAtual.getListaComentarios());

                                        addChildEvent(comentariosRef, anuncioUsuarioAtual);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
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

            List<Comentario> listaComentarios = new ArrayList<>();

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                int size = anuncioUsuarioAtual.getListaComentarios().size();
                String texto = Objects.requireNonNull(dataSnapshot.child("texto").getValue()).toString();

                String ultimoComent = "";

                if (size > 0) {
                    ultimoComent = anuncioUsuarioAtual.getListaComentarios().get(size - 1).getTexto();
                }

                if (size == 0 || texto.equals(ultimoComent)) {

                    Log.d("INFO13", "!!!!!!!!!! CHILD ADDED !!!!!!!!!!!!");
                    Log.d("INFO13", "animal: " + anuncioUsuarioAtual.getNome());

                    Comentario coment = new Comentario();
                    coment.setTexto(texto);
                    coment.setDatahora(Objects.requireNonNull(dataSnapshot.child("datahora").getValue()).toString());

                    Usuario usuario = new Usuario();
                    usuario.setId(Objects.requireNonNull(dataSnapshot.child("usuario").child("id").getValue()).toString());
                    usuario.setNome(Objects.requireNonNull(dataSnapshot.child("usuario").child("nome").getValue()).toString());

                    coment.setUsuario(usuario);
                    Log.d("INFO13", "comentário: "+coment.getTexto());
                    listaComentarios.add(coment);
                    anuncioUsuarioAtual.setListaComentarios(listaComentarios);

                    comentariosNotificacoes.put(anuncioUsuarioAtual.getIdAnimal(), anuncioUsuarioAtual.getListaComentarios());

                    createNotificationMessage(ctx, ctx.getString(R.string.novo_comentario) + " " + anuncioUsuarioAtual.getNome() + "!",
                            coment.getTexto(), anuncioUsuarioAtual);
                }
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

    private void createNotificationMessage(final Context ctx, String Title, String Msg, final Animal anuncio) {

        try {
            if (anuncio != null) {
                int size = anuncio.getListaComentarios().size();

                String comentarista = anuncio.getListaComentarios().get(size - 1).getUsuario().getId();

                Log.d("INFO13", "animal create: " + anuncio.getNome());
                Log.d("INFO13", "comentarista: " + comentarista);
                Log.d("INFO13", "dono create: " + anuncio.getDonoAnuncio());

                if (!anuncio.getDonoAnuncio().equalsIgnoreCase(comentarista)) {

                    int id = anuncio.getIdAnimal().hashCode();

                    Intent intent = new Intent(ctx, ComentariosActivity.class);
                    intent.putExtra("anuncioSelecionado", anuncio);
                    Log.d("INFO13", "INTENT: " + anuncio.getNome());

                    PendingIntent contentIntent = PendingIntent.getActivity(ctx, id, intent, 0);

                    Notification.Builder b = new Notification.Builder(ctx);

                    NotificationChannel mChannel = null;

                    b.setAutoCancel(true)
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                            .setContentTitle(Title)
                            .setTicker(Title)
                            .setContentText(Msg)
                            //.setContentIntent(contentIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
