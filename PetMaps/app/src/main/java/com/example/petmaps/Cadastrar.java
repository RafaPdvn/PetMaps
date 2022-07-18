package com.example.petmaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class Cadastrar extends AppCompatActivity {

    // Objetos para os componentes da tela
    private EditText campoNome, campoEmail, campoSenha, campoConfirmarSenha, campoCidade;
    private Spinner spinnerEstado;
    private ImageView campoImagem;
    private Button botaoCadastrar, botaoFoto;
    private CheckBox checkOng;

    // Identificador de Recurso Uniforme (serve para identificação de documentos)
    private Uri mSelected;

    // Inicio do método onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        /* REFERÊNCIAS */

        // EditText
        campoNome = findViewById(R.id.campoNome);
        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        campoConfirmarSenha = findViewById(R.id.campoConfirmaSenha);
        campoCidade = findViewById(R.id.campoCidade);

        // ImagemView
        campoImagem = findViewById(R.id.imageViewButton);

        // Spinner
        spinnerEstado = findViewById(R.id.spinner);

        // CheckBox
        checkOng = findViewById(R.id.checkOng);

        // Button
        botaoCadastrar = findViewById(R.id.btCad);
        botaoFoto = findViewById(R.id.buttonImg);

        /* EVENTO DOS BOTÕES */

        // Botão para adicionar foto
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarFoto(); // Método para selecionar a foto
            }
        }); // Fim do botão para adicionar foto

        // Botão para cadastrar usuário
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criarUsuario(); // Método para criar usuário
            }
        }); // Fim do Botão para cadastrar usuário
    } // Fim do método onCreate

    // Início do método selecionar foto
    private void selecionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK); // Nova intenção de uma action do formado pick (obter)
        intent.setType("image/*"); // Filtra o tipo de dado
        startActivityForResult(intent, 0); // Abrir a intent(galeria) e vai retornar a informação com o código 0
    } // Fim do método selecionar foto

    // Método que passa o código e o dado que foi "retirado" de outra intenção
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificando se o código da informação é igual a zero, para não confundir com outras requisições vindas de outras intenções
        if(requestCode == 0){
            // Pegar o dado(imagem) selecionado na intenção
            mSelected = data.getData(); // Caminho aonde a imagem se encontra no celular

            /* Atribuindo a imagem em uma ImageView */
            Bitmap bitmap = null; // Instância de Bitmap

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mSelected); // Pega a referência
                campoImagem.setImageDrawable(new BitmapDrawable(bitmap)); // Passando a imagem para a ImageView
                botaoFoto.setAlpha(0); // Escondendo o botão de selecionar a foto
            } catch (IOException e) {
                // Se der algum erro
            }
        }
    }

    // Início do método criarUsuario
    private void criarUsuario() { // Usar os recursos do firebase quando todos os campos tiverem preenchidos

        // Dados dos campos EditText
        String nome = campoNome.getText().toString();
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();
        String confirmaSenha = campoConfirmarSenha.getText().toString();
        String estado = spinnerEstado.getSelectedItem().toString();
        String cidade = campoCidade.getText().toString();

        // Verificando as informações necessárias
        if (mSelected == null || nome == null || nome.isEmpty() || email == null || email.isEmpty() || senha == null || senha.isEmpty() || !senha.equals(confirmaSenha) || estado.equals("Estado") || cidade == null || cidade.isEmpty()){
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Adicionando informações no Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha) // Passar o email e a senha (que será usado para efetuar o login)
                // Eventos que vericam se as informações foram cadastradas
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) { // Evento de sucesso
                        if (task.isSuccessful()) {
                            salvarUsuario(); // Método para salvar o usuários no firebase
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { // Evento de falha
                        Log.i("teste", e.getMessage()); // Imprime no Logcat o erro
                    }
                });
    } // Fim do método criarUsuario


    // Início do método salvarUsuario
    private void salvarUsuario() {

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
                                String uid = FirebaseAuth.getInstance().getUid(); // Pegando a instância do uid atual
                                String nome = campoNome.getText().toString();
                                String foto = uri.toString();
                                String email = campoEmail.getText().toString();
                                String senha = campoSenha.getText().toString();
                                String estado = spinnerEstado.getSelectedItem().toString();
                                String cidade = campoCidade.getText().toString();
                                String ong = "Não";

                                // Verificando se é uma conta de Ong
                                if(checkOng.isChecked()){
                                    ong = "Sim";
                                }

                                // Instância do objeto usuario
                                Usuario usuario = new Usuario();

                                usuario.setUuid(uid);
                                usuario.setNome(nome);
                                usuario.setFoto(foto);
                                usuario.setEmail(email);
                                usuario.setSenha(senha);
                                usuario.setEstado(estado);
                                usuario.setCidade(cidade);
                                usuario.setOng(ong);

                                // Referência do Firestore
                                FirebaseFirestore.getInstance().collection("usuarios") // Criando uma coleção
                                        .document(uid)
                                        .set(usuario) // Adicionando o objeto usuario
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Intenção para acessar as funções do app
                                                Intent intent = new Intent(Cadastrar.this, Principal.class); // Instanciar uma intente
                                                // Atribuir flags (.setFlags()) para esconder a tela anterior
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Transforma a nova activity como principal
                                                startActivity(intent); // Abre a intent

                                                Toast.makeText(Cadastrar.this, "Cadastro efetuado com sucesso!", Toast.LENGTH_SHORT).show();
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

}