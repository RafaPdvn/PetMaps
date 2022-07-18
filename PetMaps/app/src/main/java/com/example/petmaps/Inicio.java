package com.example.petmaps;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Inicio extends Fragment {

    // Objeto da View
    View view;

    // Atributos
    private ImageButton botaoImagem, botaoChat, botaoSair;
    private RecyclerView recyclerView;
    private FloatingActionButton faBotao;
    private GroupAdapter adapter;
    private RadioGroup radioGroup;
    private RadioButton rbAdocao, rbPerdido, rbEncontrado, rbTodos;

    private Usuario usuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_inicio, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(true);

        // Referências
        recyclerView = view.findViewById(R.id.recycleView);
        botaoImagem = view.findViewById(R.id.btnCamera);
        botaoChat = view.findViewById(R.id.btnChat);
        botaoSair = view.findViewById(R.id.btnSair);
        faBotao = view.findViewById(R.id.floating_post_inicio);
        radioGroup = view.findViewById(R.id.rgInicio);
        rbAdocao = view.findViewById(R.id.rbAdocao);
        rbEncontrado = view.findViewById(R.id.rbEncontrado);
        rbPerdido = view.findViewById(R.id.rbPerdido);
        rbTodos = view.findViewById(R.id.rbTodos);

        adapter = new GroupAdapter(); //Criando o adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext())); //Atribuindo um estilo para a recycler
        recyclerView.setAdapter(adapter); //Atribuindo o adapter
        adapter.notifyDataSetChanged();

        FirebaseFirestore.getInstance().collection("/animal")
                .document("animal")
                .collection("animal")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        buscaAnimais();
                    }
                });

        // Evento do botão
        faBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new CadastrarAnimal());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Evento do botão
        botaoImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
            }
        });

        // Evento do botão
        botaoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Chat());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        botaoSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                recyclerView.setAdapter(null);
                adapter.clear();

                filtro();

                recyclerView.setAdapter(adapter); //Atribuindo o adapter
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void filtro(){
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.rbAdocao:
                FirebaseFirestore.getInstance().collection("/animal")
                        .document("animal")
                        .collection("animal")
                        .whereEqualTo("situacao", "Adoção")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                                if (documentChanges != null){
                                    for (DocumentChange doc: documentChanges) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Animal animal = doc.getDocument().toObject(Animal.class);
                                            adapter.add(new ItemPublicacao(animal));
                                        }
                                    }
                                }
                            }
                        });
                break;

            case R.id.rbEncontrado:
                FirebaseFirestore.getInstance().collection("/animal")
                        .document("animal")
                        .collection("animal")
                        .whereEqualTo("situacao", "Encontrado")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                                if (documentChanges != null){
                                    for (DocumentChange doc: documentChanges) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Animal animal = doc.getDocument().toObject(Animal.class);
                                            adapter.add(new ItemPublicacao(animal));
                                        }
                                    }
                                }
                            }
                        });
                break;

            case R.id.rbPerdido:
                FirebaseFirestore.getInstance().collection("/animal")
                        .document("animal")
                        .collection("animal")
                        .whereEqualTo("situacao", "Perdido")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                                if (documentChanges != null){
                                    for (DocumentChange doc: documentChanges) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Animal animal = doc.getDocument().toObject(Animal.class);
                                            adapter.add(new ItemPublicacao(animal));
                                        }
                                    }
                                }
                            }
                        });
                break;

            case R.id.rbTodos:
                FirebaseFirestore.getInstance().collection("/animal")
                        .document("animal")
                        .collection("animal")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                                if (documentChanges != null){
                                    for (DocumentChange doc: documentChanges) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Animal animal = doc.getDocument().toObject(Animal.class);
                                            adapter.add(new ItemPublicacao(animal));
                                        }
                                    }
                                }
                            }
                        });
                break;
        }
    }

    private void buscaAnimais(){
        FirebaseFirestore.getInstance().collection("/animal")
                .document("animal")
                .collection("animal")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null){
                                for (DocumentChange doc: documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                         Animal animal = doc.getDocument().toObject(Animal.class);
                                         adapter.add(new ItemPublicacao(animal));
                                    }
                                }
                            }
                        }
                    });
    }

    private class ItemPublicacao extends Item<ViewHolder> {

        private final Animal animal;

        private ItemPublicacao(Animal animal) {
            this.animal = animal;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            final ImageView imagemUsuario = viewHolder.itemView.findViewById(R.id.userAvatar);
            ImageView imagemPublicacao = viewHolder.itemView.findViewById(R.id.postImagem);
            final TextView tvNome = viewHolder.itemView.findViewById(R.id.userName);
            TextView tvTempo = viewHolder.itemView.findViewById(R.id.textViewTime);
            TextView tvConteudo = viewHolder.itemView.findViewById(R.id.textViewContent);
            TextView tvSituacao = viewHolder.itemView.findViewById(R.id.textViewSituacao);

            FirebaseFirestore.getInstance().collection("/usuarios")
                    .document(animal.getUuid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            usuario = documentSnapshot.toObject(Usuario.class);

                            tvNome.setText(usuario.getNome());
                            Picasso.get()
                                    .load(usuario.getfoto())
                                    .into(imagemUsuario);
                        }
                    });

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dataHora = new Date(animal.getTempo());
            tvTempo.setText(sdf.format(dataHora));

            tvSituacao.setText(animal.getSituacao());
            tvConteudo.setText("Nome do animal: " + animal.getNome()
                                + "\n" + "Raça: " + animal.getRaca()
                                + "\n" + "Idade: " + animal.getIdade()
                                + "\n" + "Localização: " + animal.getRua() + ", " + animal.getBairro() + ", " + animal.getCidade() + "-" + animal.getEstado()
                                + "\n" + "Descrição: " + animal.getDescrição());
            Picasso.get()
                    .load(animal.getFoto())
                    .into(imagemPublicacao);
        }

        @Override
        public int getLayout() {
            return R.layout.inicio_post;
        }
    }

    public static Inicio newInstance() {
        return new Inicio();
    }

}
