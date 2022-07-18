package com.example.petmaps;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class Telachat extends Fragment {

    View view;
    private Button botaoChat;
    private GroupAdapter adapter;
    private Usuario usuarioLogado;
    private EditText editMensagem;
    private Bundle recuperaExtras;
    private RecyclerView rv;
    private ImageButton btnVoltar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_telachat, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        recuperaExtras = this.getArguments();

        //Referência
        botaoChat = view.findViewById(R.id.btchat);
        editMensagem = view.findViewById(R.id.btedit);
        rv = view.findViewById(R.id.recyclerchat);
        btnVoltar = view.findViewById(R.id.btnVoltar);
        TextView tvNome = view.findViewById(R.id.tvNome);
        tvNome.setText(recuperaExtras.getString("usuario"));

        botaoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensagem();
            }
        });

        adapter = new GroupAdapter(); //Criando o adapter
        rv.setLayoutManager(new LinearLayoutManager(view.getContext())); //Atribuindo um estilo para a recycler
        rv.setAdapter(adapter); //Atribuindo o adapter

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new  NovaConversa());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        FirebaseFirestore.getInstance().collection("/usuarios")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        usuarioLogado = documentSnapshot.toObject(Usuario.class);
                        buscaMensagem();
                    }
                });
    }

    private void buscaMensagem() {
        if (usuarioLogado != null){
            String enviandoId = usuarioLogado.getUuid();
            String recebendoId = recuperaExtras.getString("uuid");

            FirebaseFirestore.getInstance().collection("/conversas")
                    .document(enviandoId)
                    .collection(recebendoId)
                    .orderBy("tempoEnvio", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null){
                                for (DocumentChange doc: documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Mensagem mensagem = doc.getDocument().toObject(Mensagem.class);
                                        adapter.add(new ItemMensagem(mensagem));
                                    }
                                }
                            }
                        }
                    });
        }
    }

    // Método que vai montar as estruturas de quando abertar o botão enviar
    private void enviarMensagem() {

        //Referências
        String texto = editMensagem.getText().toString();
        editMensagem.setText(null);
        final String enviandoID = FirebaseAuth.getInstance().getUid();
        final String recebendoID = recuperaExtras.getString("uuid");
        long tempoEnvio = System.currentTimeMillis();

        // Criando um objeto
        final Mensagem mensagem = new Mensagem();

        // Atribuindo as informações nos campos
        mensagem.setIdEnvio(enviandoID);
        mensagem.setIdRecebido(recebendoID);
        mensagem.setTempoEnvio(tempoEnvio);
        mensagem.setTexto(texto);

        // Verificando se tem algo para ser enviado
        if (!mensagem.getTexto().isEmpty()){

            // Cadastrando no banco de dados as mensagens
            FirebaseFirestore.getInstance().collection("/conversas")//Criando uma nova coleção no Firestore
                    .document(enviandoID) // Criar um documento de quem está enviando
                    .collection(recebendoID) // Criar uma coleção para quem o usuário está enviando, já que o usuário pode enviar várias mensagens
                    .add(mensagem) //Adicionando o objeto
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() { //Evento de sucesso
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contatos contatos = new Contatos();
                            contatos.setUid(recebendoID);
                            contatos.setNome(recuperaExtras.getString("usuario"));
                            contatos.setFoto(recuperaExtras.getString("foto"));
                            contatos.setHoraEnviada(mensagem.getTempoEnvio());
                            contatos.setUltimaMensagem(mensagem.getTexto());

                            FirebaseFirestore.getInstance().collection("/ultimas-mensagens")
                                    .document(enviandoID)
                                    .collection("/contatos")
                                    .document(recebendoID)
                                    .set(contatos);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() { //Evento de erro
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });

            // Cadastrando as informações "ao contrário" para o usuário que está recebendo
            FirebaseFirestore.getInstance().collection("/conversas")
                    .document(recebendoID)
                    .collection(enviandoID)
                    .add(mensagem)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contatos contatos = new Contatos();
                            contatos.setUid(recebendoID);
                            contatos.setNome(recuperaExtras.getString("usuario"));
                            contatos.setFoto(recuperaExtras.getString("foto"));
                            contatos.setHoraEnviada(mensagem.getTempoEnvio());
                            contatos.setUltimaMensagem(mensagem.getTexto());

                            FirebaseFirestore.getInstance().collection("/ultimas-mensagens")
                                    .document(recebendoID)
                                    .collection("/contatos")
                                    .document(enviandoID)
                                    .set(contatos);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });
        }
    }

    //Construindo o ViewHolder da tela
    private class ItemMensagem extends Item<ViewHolder> {

        //Criando um objeto da classe Mensagem
        private final Mensagem mensagem;

        //Contrutor da mensagem
        private ItemMensagem(Mensagem mensagem) {
            this.mensagem = mensagem;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {

            // Referência da mensagem enviada e da foto do usuário
            TextView txtMensagem = viewHolder.itemView.findViewById(R.id.txtMensagem);
            ImageView imgMensagem = viewHolder.itemView.findViewById(R.id.imgMensagem);

            // Atribuindo a foto e o texto
            txtMensagem.setText(mensagem.getTexto()); //Texto

            if (mensagem.getIdEnvio().equals(FirebaseAuth.getInstance().getUid())){
                Picasso.get()
                        .load(usuarioLogado.getfoto())
                        .into(imgMensagem); //Inserindo a imagem
            } else {
                Picasso.get()
                        .load(recuperaExtras.getString("foto"))
                        .into(imgMensagem); //Inserindo a imagem
            }
        }

        @Override
        public int getLayout() { //Responsável pelo retorno do layout
            return mensagem.getIdEnvio().equals(FirebaseAuth.getInstance().getUid()) // Se for usuário logado
                    ? R.layout.item_to_user // Devolve esse layout
                    : R.layout.item_from_user; // Se não, devolve esse
        }
    }

    public static Telachat newInstance() {
        return new Telachat();
    }
}
