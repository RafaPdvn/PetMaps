package com.example.petmaps;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Vaquinha extends Fragment {

    // Objeto da view
    View view;

    // Atributos
    private RecyclerView recyclerView;
    private FloatingActionButton faBotao;
    private GroupAdapter adapter;
    private Usuario usuario;

    private int statusProgress=0;
    private float porcentagem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vaquinha, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(true);

        // Referências
        recyclerView = view.findViewById(R.id.recycleView);
        faBotao = view.findViewById(R.id.floating_post_vaquinha);

        // Evento do botão
        faBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new CriarVaquinha());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        adapter = new GroupAdapter(); //Criando o adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext())); //Atribuindo um estilo para a recycler
        recyclerView.setAdapter(adapter); //Atribuindo o adapter

        FirebaseFirestore.getInstance().collection("/vaquinha")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        buscaVaquinhas();
                    }
                });
    }

    private void buscaVaquinhas() {


        FirebaseFirestore.getInstance().collection("/vaquinha")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null){
                            for (DocumentChange doc: documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    GSVaquinha vaquinha = doc.getDocument().toObject(GSVaquinha.class);
                                    //Log.e("Teste", doc.getDocument().getId());
                                    adapter.add(new ItemVaquinha(vaquinha));
                                }
                            }
                        }
                    }
                });
    }

    private class ItemVaquinha extends Item<ViewHolder> {

        private final GSVaquinha vaquinha;

        private ItemVaquinha(GSVaquinha vaquinha) {
            this.vaquinha = vaquinha;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            // Referências das propriedades dos componentes da tela
            final ImageView imgUsuario = viewHolder.itemView.findViewById(R.id.userAvatar);
            ImageView imgPublicacao = viewHolder.itemView.findViewById(R.id.imagemVaquinha);
            final TextView tvNome = viewHolder.itemView.findViewById(R.id.userName);
            TextView tvTempo = viewHolder.itemView.findViewById(R.id.textViewTime);
            TextView tvDescricao = viewHolder.itemView.findViewById(R.id.tvDescricao);
            TextView tvTitulo = viewHolder.itemView.findViewById(R.id.tvTitulo);
            final TextView tvValorRecebido = viewHolder.itemView.findViewById(R.id.tvValorRecebido);
            TextView tvValorPedido = viewHolder.itemView.findViewById(R.id.tvValorPedido);
            Button btnDoar = viewHolder.itemView.findViewById(R.id.btnDoar);
            ProgressBar progressBar = viewHolder.itemView.findViewById(R.id.progressBar);

            FirebaseFirestore.getInstance().collection("/usuarios")
                    .document(vaquinha.getUuid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            usuario = documentSnapshot.toObject(Usuario.class);

                            tvNome.setText(usuario.getNome());
                            Picasso.get()
                                    .load(usuario.getfoto())
                                    .into(imgUsuario);
                        }
                    });

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dataHora = new Date(vaquinha.getTempo());
            tvTempo.setText(sdf.format(dataHora));

            tvTitulo.setText(vaquinha.getTitulo());
            tvDescricao.setText(vaquinha.getDescricao());
            tvValorRecebido.setText(String.valueOf(vaquinha.getValorRecebido()));
            tvValorPedido.setText(String.valueOf(vaquinha.getValor()));

            Picasso.get()
                    .load(vaquinha.getFoto())
                    .into(imgPublicacao);

            porcentagem = (vaquinha.getValorRecebido() * 100)/vaquinha.getValor();
            statusProgress = Math.round(porcentagem);
            progressBar.setProgress(statusProgress);

            Log.d("Teste", String.valueOf(statusProgress));

            btnDoar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle extrasVaquinha = new Bundle();
                    extrasVaquinha.putString("uid", vaquinha.getUuid());
                    extrasVaquinha.putString("foto", vaquinha.getFoto());
                    extrasVaquinha.putString("titulo", vaquinha.getTitulo());
                    extrasVaquinha.putString("descricao", vaquinha.getDescricao());
                    extrasVaquinha.putFloat("valor", vaquinha.getValor());
                    extrasVaquinha.putLong("d", vaquinha.getTempo());
                    extrasVaquinha.putFloat("valorRecebido", vaquinha.getValorRecebido());

                    Doar doar = new Doar();
                    doar.setArguments(extrasVaquinha);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, doar);
                    transaction.addToBackStack(null);
                    transaction.commit();

                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.vaquinha_post;
        }
    }

    public static Vaquinha newInstance(){
        return  new Vaquinha();
    }
}
