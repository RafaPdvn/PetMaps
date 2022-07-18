package com.example.petmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Principal extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // Atributos
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Referência
        navigationView = findViewById(R.id.nav_view);

        // Passa a Activity para notificar e escutar quando um item da Bottom Navigation for selecionado
        navigationView.setOnNavigationItemSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, Inicio.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void teste(boolean visivel){
        if(visivel)
            navigationView.setVisibility(View.VISIBLE);
        else
            navigationView.setVisibility(View.GONE);
    }

    // Transformar a Activity em uma ouvinte (Listener) dos itens do menu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.navigation_dicas:
                Fragment dicasFragment = Dicas.newInstance();
                openFragment(dicasFragment);
                break;

            case R.id.navigation_mapa:
                Fragment mapaFragment = new Mapa();
                openFragment(mapaFragment);
                break;

            case R.id.navigation_inicio:
                Fragment inicioFragment = Inicio.newInstance();
                openFragment(inicioFragment);
                break;

            case R.id.navigation_vaquinha:
                Fragment vaquinhaFragment = Vaquinha.newInstance();
                openFragment(vaquinhaFragment);
                break;

            case R.id.navigation_perfil:
                Fragment perfilFragment = Perfil.newInstance();
                openFragment(perfilFragment);
                break;
        }
        return true;
    }

    private void openFragment(Fragment fragment) {
        // Adicionar nosso Fragment à interface do usuário
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
