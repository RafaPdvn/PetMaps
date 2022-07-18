package com.example.petmaps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

public class EditarPerfil extends Fragment {

    View view;

    // Objetos para os componentes da tela
    private EditText campoNome, campoEmail, campoSenha, campoCidade;
    private Spinner spinnerEstado;
    private ImageView campoImagem;
    private Button botaoEditar, botaoFoto;
    private CheckBox checkOng;
    private ImageButton btnVoltar;

    // Identificador de Recurso Uniforme (serve para identificação de documentos)
    private Uri mSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        /* REFERÊNCIAS */

        // EditText
        campoNome = view.findViewById(R.id.campoNome);
        campoEmail = view.findViewById(R.id.campoEmail);
        campoSenha = view.findViewById(R.id.campoSenha);
        campoCidade = view.findViewById(R.id.campoCidade);

        campoEmail.setEnabled(false);
        campoSenha.setEnabled(false);

        // ImagemView
        campoImagem = view.findViewById(R.id.imageViewButton);

        // Spinner
        spinnerEstado = view.findViewById(R.id.spinner);

        // Button
        botaoEditar = view.findViewById(R.id.btnEditar);
        botaoFoto = view.findViewById(R.id.buttonImg);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // CheckBox
        checkOng = view.findViewById(R.id.checkOng);

        /* EVENTO DOS BOTÕES */

        botaoFoto.setAlpha(0); // Escondendo o botão de selecionar a foto

        // Botão para adicionar foto
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // Nova intenção de uma action do formado pick (obter)
                intent.setType("image/*"); // Filtra o tipo de dado
                startActivityForResult(intent, 4); // Abrir a intent(galeria) e vai retornar a informação com o código 0
            }
        }); // Fim do botão para adicionar foto

        // Botão para cadastrar usuário
        botaoEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarUsuario(); // Método para criar usuário
            }
        }); // Fim do Botão para cadastrar usuário


        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Perfil());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buscaUsuario();
    }

    // Método que passa o código e o dado que foi "retirado" de outra intenção
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificando se o código da informação é igual a zero, para não confundir com outras requisições vindas de outras intenções
        if(requestCode == 4){
            // Pegar o dado(imagem) selecionado na intenção
            mSelected = data.getData(); // Caminho aonde a imagem se encontra no celular

            /* Atribuindo a imagem em uma ImageView */
            Bitmap bitmap = null; // Instância de Bitmap

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), mSelected); // Pega a referência
                campoImagem.setImageDrawable(new BitmapDrawable(bitmap)); // Passando a imagem para a ImageView
                botaoFoto.setAlpha(0); // Escondendo o botão de selecionar a foto
            } catch (IOException e) {
                // Se der algum erro
            }
        }
    }

    // Início do método salvarUsuario
    private void editarUsuario() {
        String foto = UUID.randomUUID().toString(); // Gera uma referência do arquivo de uma forma única
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + foto); // Referência do Storage
        ref.putFile(mSelected) // Subindo a imagem para o Storage
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // Verifica se subiu a imagem
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { // Trazer a refência da url (aonde está hospedado - URI) para conseguir trabalhar com a imagem
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.i("teste", uri.toString()); // Imprimir no Logcat a referência pública dessa foto

                                // Pegando as informações necessárias do usuário
                                String uid = FirebaseAuth.getInstance().getUid();
                                String nome = campoNome.getText().toString();
                                String foto = uri.toString();
                                String email = campoEmail.getText().toString();
                                String senha = campoSenha.getText().toString();
                                String estado = spinnerEstado.getSelectedItem().toString();
                                String cidade = campoCidade.getText().toString();

                                Usuario usuario = new Usuario();

                                usuario.setUuid(FirebaseAuth.getInstance().getUid());
                                usuario.setNome(nome);
                                usuario.setFoto(foto);
                                usuario.setEmail(email);
                                usuario.setSenha(senha);
                                usuario.setEstado(estado);
                                usuario.setCidade(cidade);

                                // Referência do Firestore
                                FirebaseFirestore.getInstance().collection("usuarios") // Criando uma coleção
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .set(usuario) // Adicionando o objeto usuario
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                transaction.replace(R.id.container, new Perfil());
                                                transaction.addToBackStack(null);
                                                transaction.commit();

                                                Toast.makeText(view.getContext(), "Atualização efetuada com sucesso!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) { // Evento de falha
                                                Log.i("Teste", e.getMessage()); // Mostrar a falha no Logcat
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { // Verificar se deu algum problema
                        Log.e("teste", e.getMessage(), e); // Problema vai aparecer no Logcat
                    }
                });
    } // Fim do método salvarUsuario

    private void buscaUsuario(){
        FirebaseFirestore.getInstance().collection("/usuarios")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);

                        campoNome.setText(usuario.getNome());
                        campoEmail.setText(usuario.getEmail());
                        campoSenha.setText(usuario.getSenha());
                        campoCidade.setText(usuario.getCidade());

                        //spinnerEstado.setText(usuario.getEstado());

                        Picasso.get()
                                .load(usuario.getfoto())
                                .into(campoImagem);

                        if (usuario.getOng().equals("Sim")){
                            checkOng.setChecked(true);
                        } else {
                            checkOng.setChecked(false);
                        }

                        //Dentro desta variável conteria o valor do banco, abaixo um exemplo
                        String estado = usuario.getEstado();

                        //Criando o adapter
                        ArrayAdapter<CharSequence> adp = ArrayAdapter.createFromResource(view.getContext(), R.array.Estado, android.R.layout.simple_spinner_item);

                        //Setando o valor
                        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEstado.setAdapter(adp);

                        if (!estado.equals(null)) {
                            int position = adp.getPosition(estado);
                            spinnerEstado.setSelection(position);
                            position = 0;
                        }
                    }
                });
    }

    public static EditarPerfil newInstance() {
        return new EditarPerfil();
    }
}