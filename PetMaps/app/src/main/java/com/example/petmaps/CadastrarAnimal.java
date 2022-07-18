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
import java.util.Date;
import java.util.UUID;

public class CadastrarAnimal extends Fragment {

    View view;

    // Objetos para os componentes da tela
    private EditText campoNome, campoIdade, campoCidade, campoBairro, campoRua, campoDescricao;
    private Spinner spinnerRaca, spinnerSituacao, spinnerEstado;
    private Button btnFoto, btnPublicar;
    private ImageView imgFoto;
    private ImageButton btnVoltar;

    private Usuario usuarioLogado;

    // Identificador de Recurso Uniforme (serve para identificação de documentos)
    private Uri mSelected;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cadastrar_animal, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        /* REFERÊNCIAS */

        // EditText
        campoNome = view.findViewById(R.id.inicio_nome_pet);
        campoIdade = view.findViewById(R.id.inicio_idade_pet);
        campoCidade = view.findViewById(R.id.inicio_cidade);
        campoBairro = view.findViewById(R.id.inicio_bairro);
        campoRua = view.findViewById(R.id.inicio_rua);
        campoDescricao = view.findViewById(R.id.inicio_descricao);

        // Spinner
        spinnerRaca = view.findViewById(R.id.inicio_spinner_raca);
        spinnerSituacao = view.findViewById(R.id.inicio_spinner_post);
        spinnerEstado = view.findViewById(R.id.inicio_spinner_estado);

        // ImagemView
        imgFoto = view.findViewById(R.id.imgFoto);

        // Button
        btnFoto = view.findViewById(R.id.btnFoto);
        btnPublicar = view.findViewById(R.id.btnPublicar);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        /* EVENTO DOS BOTÕES */
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // Nova intenção de uma action do formado pick (obter)
                intent.setType("image/*"); // Filtra o tipo de dado
                startActivityForResult(intent, 2); // Abrir a intent(galeria) e vai retornar a informação com o código 0
            }
        });

        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarAnimal(); // Método para cadastrar animais
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificando se o código da informação é igual a zero, para não confundir com outras requisições vindas de outras intenções
        if(requestCode == 2){
            // Pegar o dado(imagem) selecionado na intenção
            mSelected = data.getData(); // Caminho aonde a imagem se encontra no celular

            /* Atribuindo a imagem em uma ImageView */
            Bitmap bitmap = null; // Instância de Bitmap

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), mSelected); // Pega a referência
                imgFoto.setImageDrawable(new BitmapDrawable(bitmap)); // Passando a imagem para a ImageView
            } catch (IOException e) {
                // Se der algum erro
            }
        }
    }

    // Início do método salvarAnimal
    private void salvarAnimal(){

        if (mSelected == null){
            Toast.makeText(view.getContext(), "Adicione uma foto", Toast.LENGTH_SHORT).show();
            return;
        }

        String foto = UUID.randomUUID().toString(); // Gera uma referência do arquivo de uma forma única
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/animais/" + foto); // Referência do Storage
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
                                String idade = campoIdade.getText().toString();
                                String cidade = campoCidade.getText().toString();
                                String bairro = campoBairro.getText().toString();
                                String rua = campoRua.getText().toString();
                                String descricao = campoDescricao.getText().toString();

                                String raca = spinnerRaca.getSelectedItem().toString();
                                String situacao = spinnerSituacao.getSelectedItem().toString();
                                String estado = spinnerEstado.getSelectedItem().toString();

                                // Verificando as informações necessárias
                                if (nome == null || nome.isEmpty() || idade == null || idade.isEmpty() || cidade == null || cidade.isEmpty() ||
                                        bairro == null || bairro.isEmpty() || rua == null || rua.isEmpty() || descricao == null || descricao.isEmpty() ||
                                        estado.equals("Estado")){
                                    Toast.makeText(view.getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //Pegando a data do dispositivo
                                Date date = new Date();
                                //Convertendo a data para o tipo long
                                long d = date.getTime();

                                final Animal animal = new Animal();

                                animal.setUuid(uid);
                                animal.setFoto(foto);
                                animal.setNome(nome);
                                animal.setIdade(idade);
                                animal.setCidade(cidade);
                                animal.setBairro(bairro);
                                animal.setRua(rua);
                                animal.setDescrição(descricao);
                                animal.setRaca(raca);
                                animal.setSituacao(situacao);
                                animal.setEstado(estado);
                                animal.setTempo(d);

                                // Instância do objeto animal
                                //Animal animal = new Animal(uid, foto, nome, idade, cidade, bairro, rua, descrição, raca, situacao, estado);

                                // Referência do Firestore
                                FirebaseFirestore.getInstance().collection("animal") // Criando uma coleção
                                        .document("animal")
                                        .collection("animal")
                                        .add(animal) // Adicionando o objeto do animal do usuario
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                transaction.replace(R.id.container, new Inicio());
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
    } // Fim do método salvarAnimal

    public static CadastrarAnimal newInstance() {
        return new CadastrarAnimal();
    }

}
