package com.example.petmaps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class Chat extends Fragment {

    View view;
    private FloatingActionButton faNovaConversa;
    private RecyclerView rv;
    private GroupAdapter adapter;
    private ImageButton btnVoltar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        // Referências
        faNovaConversa = view.findViewById(R.id.faNovaMensagem);
        rv = view.findViewById(R.id.recycleViewUltimaMensagem);
        btnVoltar = view.findViewById(R.id.btnVoltar);
        adapter = new GroupAdapter();
        
        rv.setLayoutManager(new LinearLayoutManager(view.getContext())); //Atribuindo um estilo para a recycler
        rv.setAdapter(adapter); //Atribuindo o adapter


        faNovaConversa.setOnClickListener(new View.OnClickListener() {
            @Override
          public void onClick(View v) {
              FragmentTransaction transaction = getFragmentManager().beginTransaction();
              transaction.replace(R.id.container, new NovaConversa());
              transaction.addToBackStack(null);
              transaction.commit();
          }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Inicio());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buscarUltimaMensagem();
    }

    private void buscarUltimaMensagem() {
        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("/ultimas-mensagens")
                .document(uid)
                .collection("contatos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null){
                            for (DocumentChange doc: documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED){
                                    Contatos contatos = doc.getDocument().toObject(Contatos.class);

                                    adapter.add(new ContatosItem(contatos));
                                }

                            }
                        }
                    }
                });
    }

    private class ContatosItem extends Item<ViewHolder>{

        private final Contatos contatos;

        private ContatosItem(Contatos contatos) {
            this.contatos = contatos;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView nome = viewHolder.itemView.findViewById(R.id.tvNomeUsuario);
            TextView mensagem = viewHolder.itemView.findViewById(R.id.tvMensagem);
            ImageView foto = viewHolder.itemView.findViewById(R.id.imgUsuario);

            nome.setText(contatos.getNome());
            mensagem.setText(contatos.getUltimaMensagem());
            Picasso.get()
                    .load(contatos.getFoto())
                    .into(foto);
        }

        @Override
        public int getLayout() {
            return R.layout.item_lastmsg;
        }
    }

    public static Chat newInstance() {
        return new Chat();
    }
}
