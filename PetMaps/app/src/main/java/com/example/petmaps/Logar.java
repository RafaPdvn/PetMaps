package com.example.petmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Logar extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoLogar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logar);

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        botaoLogar = findViewById(R.id.botaoLogar);

        botaoLogar.setOnClickListener(new View.OnClickListener() { //Evento do botão logar
            @Override
            public void onClick(View view) {

                // Dados dos campos EditText
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                // Verificando as informações necessárias
                if (email == null || email.isEmpty() || senha == null || senha.isEmpty()){
                    Toast.makeText(Logar.this, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Refência do Firebase Authentication
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                        // Eventos que vericam se as informações estão corretas
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) { // Evento de sucesso
                                if (task.isSuccessful()) {
                                    Log.i("Teste", task.getResult().getUser().getUid()); // Mostar no Logcat se o Login foi efetuado

                                    Intent intent = new Intent(Logar.this, Principal.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent); // Abre uma nova atividade
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) { // Evento de falha
                                Log.i("teste", e.getMessage()); // Imprime no Logcat o erro
                            }
                        });
            }
        });
    }
}
