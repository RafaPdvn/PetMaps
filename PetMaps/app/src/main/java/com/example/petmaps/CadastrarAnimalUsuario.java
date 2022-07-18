package com.example.petmaps;

import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class CadastrarAnimalUsuario extends Fragment {

    View view;

    // Objetos para os componentes da tela
    private EditText campoNome, campoIdade;
    private ImageView campoImagem;
    private Button botaoFoto, botaoCadastrar;
    private Spinner spnRaca;
    private ImageButton btnVoltar;

    // Identificador de Recurso Uniforme (serve para identificação de documentos)
    private Uri mSelected;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cadastrar_animal_usuario, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        /* REFERÊNCIAS */

        // EditText
        campoNome = view.findViewById(R.id.nomeAnimalU);
        spnRaca = view.findViewById(R.id.spnRaca);
        campoIdade = view.findViewById(R.id.idadeAnimalU);

        // ImagemView
        campoImagem = view.findViewById(R.id.imgAnimalU);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // Button
        botaoCadastrar = view.findViewById(R.id.btnCadastarAU);
        botaoFoto = view.findViewById(R.id.btnAnimalU);

        /* EVENTO DOS BOTÕES */

        // Botão para adicionar foto
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // Nova intenção de uma action do formado pick (obter)
                intent.setType("image/*"); // Filtra o tipo de dado
                startActivityForResult(intent, 1); // Abrir a intent(galeria) e vai retornar a informação com o código 0
            }
        }); // Fim do botão para adicionar foto

        // Botão para cadastrar animais do usuário
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarAnimalUsuario(); // Método para cadastrar animais do usuário
            }
        }); // Fim do Botão para cadastrar animais do usuário

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Perfil());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    } // Fim do método onCreate

    // Método que passa o código e o dado que foi "retirado" de outra intenção


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificando se o código da informação é igual a zero, para não confundir com outras requisições vindas de outras intenções
        if(requestCode == 1){
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

    // Início do método salvarAnimalUsuario
    private void salvarAnimalUsuario(){

        if (mSelected == null){
            Toast.makeText(view.getContext(), "Adicione uma foto", Toast.LENGTH_SHORT).show();
            return;
        }

        String foto = UUID.randomUUID().toString(); // Gera uma referência do arquivo de uma forma única
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/animais_usuario/" + foto); // Referência do Storage
        ref.putFile(mSelected) // Subindo a imagem para o Storage
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // Verifica se subiu a imagem
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) { // Trazer a refência da url (aonde está hospedado - URI) para conseguir trabalhar com a imagem
                        Log.i("teste", uri.toString()); // Imprimir no Logcat a referência pública dessa foto

                        // Pegando as informações necessárias do animal
                        String uid = FirebaseAuth.getInstance().getUid(); // Pegando a instância do uid atual
                        String foto = uri.toString();
                        String nome = campoNome.getText().toString();
                        String raca = spnRaca.getSelectedItem().toString();
                        String idade = campoIdade.getText().toString();


                        if (nome == null || nome.isEmpty() || idade == null || idade.isEmpty()){
                            Toast.makeText(view.getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AnimalUsuario animalUsuario = new AnimalUsuario();

                        animalUsuario.setUuid(uid);
                        animalUsuario.setFoto(foto);
                        animalUsuario.setNome(nome);
                        animalUsuario.setRaca(raca);
                        animalUsuario.setIdade(idade);

                        // Referência do Firestore
                        FirebaseFirestore.getInstance().collection("animal") // Criando uma coleção
                                .document("animal_usuario")
                                .collection(uid)
                                .add(animalUsuario) // Adicionando o objeto do animal do usuario
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                        transaction.replace(R.id.container, new Perfil());
                                        transaction.addToBackStack(null);
                                        transaction.commit();

                                        Toast.makeText(getContext(), "Cadastro efetuado com sucesso!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() { // Evento de falha
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Teste", e.getMessage()); // Mostrar a falha no Logcat
                                    }
                                });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {// Verificar se deu algum problema
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("teste", e.getMessage(), e); // Problema vai aparecer no Logcat
            }
        });
    } // Fim do método salvarAnimalUsuario

    public static CadastrarAnimalUsuario newInstance() {
        return new CadastrarAnimalUsuario();
    }

}
