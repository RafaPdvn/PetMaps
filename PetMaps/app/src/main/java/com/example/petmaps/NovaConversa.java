package com.example.petmaps;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.List;

public class NovaConversa extends Fragment {

    private GroupAdapter adapter;
    private ImageButton btnVoltar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nova_conversa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        RecyclerView rv = view.findViewById(R.id.listadeusuarios);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        adapter = new GroupAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                ItemUsuario itemUsuario = (ItemUsuario) item;
                Bundle extras = new Bundle();
                extras.putString("usuario", itemUsuario.usuario.getNome());
                extras.putString("foto", itemUsuario.usuario.getfoto());
                extras.putString("uuid", itemUsuario.usuario.getUuid());

                Telachat telachat = new Telachat();
                telachat.setArguments(extras);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, telachat);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Chat());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buscaUsuarios();
    }

    private void buscaUsuarios() {

        FirebaseFirestore.getInstance().collection("/usuarios")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Log.e("Teste", e.getMessage(), e);
                            return;
                        }

                        List<DocumentSnapshot> documentos = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documento: documentos) {
                            Usuario usuario = documento.toObject(Usuario.class);
                            Log.d("Teste", usuario.getNome());
                            adapter.add(new ItemUsuario(usuario));
                        }
                    }
                });
    }

    private class ItemUsuario extends Item<ViewHolder> {

        private final Usuario usuario;

        private ItemUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtNomeusuario = viewHolder.itemView.findViewById(R.id.tvNContato);
            ImageView imgUsuario = viewHolder.itemView.findViewById(R.id.imgNContato);

            txtNomeusuario.setText(usuario.getNome());
            Picasso.get()
                    .load(usuario.getfoto())
                    .into(imgUsuario);
        }

        @Override
        public int getLayout() {
            return R.layout.item_usuario;
        }
    }

    public static NovaConversa newInstance() {
        return new NovaConversa();
    }
}
