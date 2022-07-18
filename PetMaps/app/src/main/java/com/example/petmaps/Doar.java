package com.example.petmaps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Doar extends Fragment {

    View view;
    private ImageButton btnVoltar;
    private Bundle recuperaExtrasVaquinha;
    private EditText campoValor, campoMensagem;
    private RadioGroup rgDoacao;
    private Button btnDoar;

    private String formaPagamento;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_doar, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((Principal)getActivity()).teste(false);

        campoValor = view.findViewById(R.id.valor_doacao);
        campoMensagem = view.findViewById(R.id.edtMensagem);
        rgDoacao = view.findViewById(R.id.radiogroup_doacao);
        btnDoar  = view.findViewById(R.id.btnDoar);

        recuperaExtrasVaquinha = this.getArguments();

        btnVoltar = view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new Vaquinha());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnDoar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doarVaquinha();
            }
        });
    }

    public void filtro(){
        switch (rgDoacao.getCheckedRadioButtonId()){
            case R.id.rbPaypal:
                formaPagamento = "Paypal";
                break;

            case R.id.rbBoleto:
                formaPagamento = "Boleto";
                break;

            case R.id.rbCartao:
                formaPagamento = "Cartão de crédito";
                break;
        }
    }

    private void doarVaquinha() {

        filtro();

        // Pegando as informações necessárias
        final String uidVaquinha = recuperaExtrasVaquinha.getString("uid");
        float valor = Float.parseFloat(campoValor.getText().toString());
        String mensagem = campoMensagem.getText().toString();
        String pagamento = formaPagamento;

        GSDoacao doacao = new GSDoacao();

        doacao.setUid(uidVaquinha);
        doacao.setValor(valor);
        doacao.setMensagem(mensagem);
        doacao.setFormaPagamento(pagamento);

        final GSVaquinha vaquinha = new GSVaquinha();

        vaquinha.setUuid(uidVaquinha);
        vaquinha.setFoto(recuperaExtrasVaquinha.getString("foto"));
        vaquinha.setTitulo(recuperaExtrasVaquinha.getString("titulo"));
        vaquinha.setDescricao(recuperaExtrasVaquinha.getString("descricao"));
        vaquinha.setValor(recuperaExtrasVaquinha.getFloat("valor"));
        vaquinha.setTempo(recuperaExtrasVaquinha.getLong("d"));

        float valorFinal;
        valorFinal = recuperaExtrasVaquinha.getFloat("valorRecebido") + valor;
        vaquinha.setValorRecebido(valorFinal);

        FirebaseFirestore.getInstance().collection("vaquinha")
                .document("Doação")
                .collection(uidVaquinha)
                .add(doacao)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        FirebaseFirestore.getInstance().collection("vaquinha")
                                .document(uidVaquinha)
                                .set(vaquinha)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, new Vaquinha());
                        transaction.addToBackStack(null);
                        transaction.commit();

                        Toast.makeText(getContext(), "Doação feita com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Teste", e.getMessage()); // Mostrar a falha no Logcat
                    }
                });
    }

    public static Doar newInstance() {
        return new Doar();
    }
}