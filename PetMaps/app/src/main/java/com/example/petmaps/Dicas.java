package com.example.petmaps;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Dicas extends Fragment {

    // Objeto da View
    View view;

    // Atributos
    private RecyclerView recyclerView;
    private DicaAdapter dicaAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dicas, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(true);

        // Referências
        recyclerView = view.findViewById(R.id.recycleView);

        //Atribuição de um Layout para exibição
        RecyclerView.LayoutManager layout = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layout);

        //Passando adapter para o RecyclerView
        dicaAdapter = new DicaAdapter();
        recyclerView.setAdapter(dicaAdapter);

        // Lista vazia
        List<Dica> dicas = new ArrayList<>();

        // Instanciando a classe publicacao
        Dica tip = new Dica();
        Dica tip2 = new Dica();
        Dica tip3 = new Dica();

        // Inserindo as dicas
        tip.setImgDicas(R.drawable.comedouro);
        tip.setTvTitulo("Comedouro lento");
        tip.setTvDica("Se o cão não mastiga a comida, mas a engole diretamente, isso pode causar " +
                "problemas de digestão, provocar vômitos e até fazer ele sufocar. Nas lojas especializadas " +
                "são vendidos comedouros lentos, com áreas sobressalentes que restringem o acesso aos " +
                "alimentos e obrigam o cão a comer com mais cuidado. Mas você também pode fazer isso sozinho. " +
                "Coloque alguns objetos grandes (por exemplo, uma bola de tênis ou pedras) na tigela do animal.");

        tip2.setImgDicas(R.drawable.coleira_gps);
        tip2.setTvTitulo("Coleira com GPS");
        tip2.setTvDica("A coleira com GPS permite aos donos de animais domésticos saberem o local exato " +
                "onde seu pets estão. Excelente ajuda em casos de algum dia seu cão fugir ou mesmo se " +
                "perder. Os passos do animal podem ser monitorados por um aplicativo para dispositivo móvel.");

        tip3.setImgDicas(R.drawable.escovar_dente);
        tip3.setTvTitulo("Escovar os dentes");
        tip3.setTvDica("Muito raramente, os animais permitem escovar os dentes. Mas você pode fazer " +
                "este procedimento higiênico de forma mais simples. Basta aplicar creme dental no " +
                "brinquedo de borracha favorito do seu pet. Assim, o seu amigo de quatro patas escovará " +
                "os dentes enquanto brinca.");

        // Adicionando a lista
        dicas.add(tip);
        dicas.add(tip2);
        dicas.add(tip3);

        // Fala para o adapter colocar as novas informações
        dicaAdapter.setDicas(dicas);

        // Fala para o adapter notificar a Recycler que houve mudanças
        dicaAdapter.notifyDataSetChanged();
    }

    // Classe que contém as propriedades das publicações
    private class Dica{

        // Atributos dos componentes que vai ter na tela
        private int imgDicas;
        private String tvTitulo, tvDica;

        // Getters e Setters
        public int getImgDicas() {
            return imgDicas;
        }

        public void setImgDicas(int imgDicas) {
            this.imgDicas = imgDicas;
        }

        public String getTvTitulo() {
            return tvTitulo;
        }

        public void setTvTitulo(String tvTitulo) {
            this.tvTitulo = tvTitulo;
        }

        public String getTvDica() {
            return tvDica;
        }

        public void setTvDica(String tvDica) {
            this.tvDica = tvDica;
        }
    }

    // Classe que vai armazenar os componentes da tela
    private static class DicaViewHolder extends RecyclerView.ViewHolder{

        // Propriedades
        private final ImageView imgDicas;
        private final TextView tvTitulo, tvDica;

        // Construtor
        public DicaViewHolder(@NonNull View itemView) {
            super(itemView);

            // Referências das propriedades dos componentes da tela
            imgDicas = itemView.findViewById(R.id.imgDicas);
            tvTitulo = itemView.findViewById(R.id.titulo_dicas);
            tvDica= itemView.findViewById(R.id.dica_dicas);
        }

        // Recebe todos os componentes da tela
        void bind (Dica dica){

            // Atribuindo as informações nos componentes já estabelecidos
            imgDicas.setImageResource(dica.getImgDicas());
            tvTitulo.setText(dica.getTvTitulo());
            tvDica.setText(dica.getTvDica());
        }
    }

    // Estruturar todos os componentes passados anteriormente
    private class DicaAdapter extends RecyclerView.Adapter<DicaViewHolder> {

        // Lista para manipular os dados
        private List<Dica> dicas = new ArrayList<>();

        // Criação do ViewHolder
        @NonNull
        @Override
        public DicaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.dicas_post, viewGroup, false);
            return new DicaViewHolder(view); // Atribuindo a tela
        }

        // Construi uma nova linha
        @Override
        public void onBindViewHolder(@NonNull DicaViewHolder dicaViewHolder, int i) {
            Dica dica = dicas.get(i);
            dicaViewHolder.bind(dica);
        }

        @Override
        public int getItemCount() {
            return dicas.size(); // Define o tamanho da lista
        }

        // Construir a lista

        public void setDicas(List<Dica> dicas) {
            this.dicas = dicas;
        }
    }

    public static Dicas newInstance() {
        return new Dicas();
    }

}
