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

public class CriarVaquinha extends Fragment {

    View view;

    // Objetos para os componentes da tela
    private EditText campoTitulo, campoDescricao, campoValor;
    private Button btnFoto, btnPublicar;
    private ImageView imgFoto;
    private ImageButton btnVoltar;

    // Identificador de Recurso Uniforme (serve para identificação de documentos)
    private Uri mSelected;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_criar_vaquinha, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        /* REFERÊNCIAS */

        // EditText
        campoTitulo = view.findViewById(R.id.titulo_vaquinha);
        campoDescricao = view.findViewById(R.id.descricao_vaquinha);
        campoValor = view.findViewById(R.id.valor_vaquinha);

        // ImagemView
        imgFoto = view.findViewById(R.id.imgFotoVaquinha);

        // Button
        btnFoto = view.findViewById(R.id.btnFotoVaquinha);
        btnPublicar = view.findViewById(R.id.btnPublicarVaquinha);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // Nova intenção de uma action do formado pick (obter)
                intent.setType("image/*"); // Filtra o tipo de dado
                startActivityForResult(intent, 3); // Abrir a intent(galeria) e vai retornar a informação com o código 0
            }
        });

        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarVaquinha(); // Método para cadastrar vaquinhas
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Vaquinha());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificando se o código da informação é igual a zero, para não confundir com outras requisições vindas de outras intenções
        if(requestCode == 3){
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

    // Início do método salvarVaquinha
    private void salvarVaquinha() {

        if (mSelected == null){
            Toast.makeText(view.getContext(), "Adicione uma foto", Toast.LENGTH_SHORT).show();
            return;
        }

        String foto = UUID.randomUUID().toString(); // Gera uma referência do arquivo de uma forma única
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/vaquinha/" + foto); // Referência do Storage
        ref.putFile(mSelected)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                // Pegando as informações necessárias do animal
                                String uid = FirebaseAuth.getInstance().getUid(); // Pegando a instância do uid atual
                                String foto = uri.toString();
                                String titulo = campoTitulo.getText().toString();
                                String descricao = campoDescricao.getText().toString();
                                float valor = Float.parseFloat(campoValor.getText().toString());

                                // Verificando as informações necessárias
                                if (titulo == null || titulo.isEmpty() || descricao == null || descricao.isEmpty() || valor == 0){
                                    Toast.makeText(view.getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //Pegando a data do dispositivo
                                Date date = new Date();
                                //Convertendo a data para o tipo long
                                long d = date.getTime();

                                final GSVaquinha vaquinha = new GSVaquinha();

                                vaquinha.setUuid(uid);
                                vaquinha.setFoto(foto);
                                vaquinha.setTitulo(titulo);
                                vaquinha.setDescricao(descricao);
                                vaquinha.setValor(valor);
                                vaquinha.setTempo(d);
                                vaquinha.setValorRecebido(0);

                                FirebaseFirestore.getInstance().collection("vaquinha")
                                        .document(vaquinha.getUuid())
                                        .set(vaquinha)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                transaction.replace(R.id.container, new Vaquinha());
                                                transaction.addToBackStack(null);
                                                transaction.commit();

                                                Toast.makeText(getContext(), "Cadastro efetuado com sucesso!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("Teste", e.getMessage()); // Mostrar a falha no Logcat
                                            }
                                        });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("teste", e.getMessage(), e); // Problema vai aparecer no Logcat
            }
        });
    }// Fim do método salvarVaquinha

    public static CriarVaquinha newInstance() {
        return new CriarVaquinha();
    }

}
