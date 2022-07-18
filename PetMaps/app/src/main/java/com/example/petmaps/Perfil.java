package com.example.petmaps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class Perfil extends Fragment {

    private Usuario usuarioLogado;
    private ImageView imgUsuario;
    private TextView txtNomeUsuario, txtEmailUsuario, txtEstadoUsuario, txtCidadeUsuario, tvEditar;
    private ImageButton btnPet, btnEditar;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;

    RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(true);

        recyclerView = view.findViewById(R.id.recycleView);
        imgUsuario = view.findViewById(R.id.foto_perfil);
        txtNomeUsuario = view.findViewById(R.id.nome_perfil);
        txtEmailUsuario = view.findViewById(R.id.email_perfil);
        txtEstadoUsuario = view.findViewById(R.id.estado_perfil);
        txtCidadeUsuario = view.findViewById(R.id.cidade_perfil);
        btnPet = view.findViewById(R.id.btnPet);
        btnEditar = view.findViewById(R.id.btnEditar);
        tvEditar = view.findViewById(R.id.tvEditar);

        layoutManager = new GridLayoutManager(view.getContext(), 3);

        adapter = new GroupAdapter(); //Criando o adapter
        recyclerView.setLayoutManager(layoutManager); //Atribuindo um estilo para a recycler
        recyclerView.setAdapter(adapter); //Atribuindo o adapter

        btnPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new CadastrarAnimalUsuario());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        FirebaseFirestore.getInstance().collection("/animal")
                .document("animal_usuario")
                .collection(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        buscarAnimal();
                    }
                });
        buscaUsuario();

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new EditarPerfil());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        tvEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new EditarPerfil());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void buscaUsuario(){
        FirebaseFirestore.getInstance().collection("/usuarios")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        usuarioLogado = documentSnapshot.toObject(Usuario.class);
                        Log.d("Teste", usuarioLogado.getNome());

                        txtNomeUsuario.setText(usuarioLogado.getNome());
                        txtEmailUsuario.setText(usuarioLogado.getEmail());
                        txtEstadoUsuario.setText(usuarioLogado.getEstado());
                        txtCidadeUsuario.setText(usuarioLogado.getCidade());
                        Picasso.get()
                                .load(usuarioLogado.getfoto())
                                .into(imgUsuario);
                    }
                });
    }

    private void buscarAnimal() {
        FirebaseFirestore.getInstance().collection("/animal")
                .document("animal_usuario")
                .collection(FirebaseAuth.getInstance().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null){
                            for (DocumentChange doc: documentChanges){
                                if (doc.getType() == DocumentChange.Type.ADDED){
                                    AnimalUsuario animalUsuario = doc.getDocument().toObject(AnimalUsuario.class);
                                    adapter.add(new ItemAnimal(animalUsuario));
                                }
                            }
                        }
                    }
                });
    }

    private class ItemAnimal extends Item<ViewHolder> {

        private final AnimalUsuario animalUsuario;

        private ItemAnimal(AnimalUsuario animalUsuario) {
            this.animalUsuario = animalUsuario;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView imgAnimal = viewHolder.itemView.findViewById(R.id.imgAnimal);
            TextView tvNomeAnimal = viewHolder.itemView.findViewById(R.id.tvNomeAnimal);

            tvNomeAnimal.setText(animalUsuario.getNome());
            Picasso.get()
                    .load(animalUsuario.getFoto())
                    .into(imgAnimal);
        }

        @Override
        public int getLayout() {
            return R.layout.item_pet_perfil;
        }
    }

    public static Perfil newInstance() {
        return new Perfil();
    }

}
