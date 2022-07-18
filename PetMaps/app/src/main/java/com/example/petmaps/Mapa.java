package com.example.petmaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Mapa extends Fragment {

    View view;

    String endereco;
    double latitude, longitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mapa, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(true);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            FirebaseFirestore.getInstance().collection("/animal") // Criando uma coleção
                    .document("animal")
                    .collection("animal")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e("Teste", e.getMessage(), e);
                                return;
                            }

                            List<DocumentSnapshot> documentos = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documento : documentos) {

                                final Animal animal = documento.toObject(Animal.class);
                                endereco = animal.getRua() + ", " + animal.getBairro() + ", " + animal.getCidade() + " - " + animal.getEstado();
                                Log.e("Teste", endereco);

                                //buscaCoordenadas("https://maps.googleapis.com/maps/api/geocode/json?address=" + endereco + "&key=AIzaSyBoDavGlqUm9rLrj5Rb2TC9sTdzbTXU9TY");

                                //Chamada da classe Volley para requisições à API do Google
                                RequestQueue queue = Volley.newRequestQueue(view.getContext().getApplicationContext());

                                //Configura o método e envio e implementa o listener
                                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://maps.googleapis.com/maps/api/geocode/json?address=" + endereco + "&key=AIzaSyBoDavGlqUm9rLrj5Rb2TC9sTdzbTXU9TY", new Response.Listener<String>() {
                                    //Quando houver uma requisição e resposta válida...
                                    @Override
                                    public void onResponse(String response) {
                                        //Criação de vetores JSON para armazenar o conteúdo de cada seção do retorno
                                        JSONArray listaResults = null; //results

                                        try {
                                            //Converte a resposta da chamada da API para um objeto JSON
                                            JSONObject lista = new JSONObject(response);

                                            //Retira do objeto "lista" somente o trecho marcado como "routes"
                                            listaResults = lista.getJSONArray("results");

                                            //Laço for para percorrer todas as posições/trechos dentro das rotas
                                            for(int i=0 ; i<listaResults.length() ; i++) {

                                                latitude = ((JSONObject) listaResults.get(i)).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                                Log.d("Teste", "Lat"+latitude);

                                                longitude = ((JSONObject) listaResults.get(i)).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                                Log.d("Teste", "Lon"+longitude);

                                                LatLng ponto = new LatLng(latitude, longitude);
                                                Log.e("Teste", "Ponto Marker" + ponto);
                                                googleMap.addMarker(new MarkerOptions().position(ponto).title(animal.getSituacao()));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError erro) {
                                        Log.d("GPS_ROTAS", erro.toString());
                                    }
                                });

                                //Executa o comando
                                queue.add(stringRequest);
                            }
                        }
                    });
        }
    };

    /*public void buscaCoordenadas(String url){
        //Chamada da classe Volley para requisições à API do Google
        RequestQueue queue = Volley.newRequestQueue(view.getContext().getApplicationContext());

        //Configura o método e envio e implementa o listener
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            //Quando houver uma requisição e resposta válida...
            @Override
            public void onResponse(String response) {
                //Criação de vetores JSON para armazenar o conteúdo de cada seção do retorno
                JSONArray listaResults = null; //results

                try {
                    //Converte a resposta da chamada da API para um objeto JSON
                    JSONObject lista = new JSONObject(response);

                    //Retira do objeto "lista" somente o trecho marcado como "routes"
                    listaResults = lista.getJSONArray("results");

                    //Laço for para percorrer todas as posições/trechos dentro das rotas
                    for(int i=0 ; i<listaResults.length() ; i++) {

                        latitude = ((JSONObject) listaResults.get(i)).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        Log.d("Teste", "Lat"+latitude);

                        longitude = ((JSONObject) listaResults.get(i)).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        Log.d("Teste", "Lon"+longitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError erro) {
                Log.d("GPS_ROTAS", erro.toString());
            }
        });

        //Executa o comando
        queue.add(stringRequest);
    }*/
}
