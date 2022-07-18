package com.example.petmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button botaoEntrar, botaoCadastrar;
    private boolean permitiuINT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoEntrar = findViewById(R.id.botaoEntrar);
        botaoCadastrar = findViewById(R.id.botaoCadastrar);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},120);
        }else{
            permitiuINT = true;
        }

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Logar.class);
                startActivity(intent); // Abre uma nova atividade
            }
        });

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Cadastrar.class);
                startActivity(intent);
            }
        });
    }
}
