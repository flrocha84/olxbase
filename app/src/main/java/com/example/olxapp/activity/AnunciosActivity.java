package com.example.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.olxapp.R;
import com.example.olxapp.adapter.AdapterAnuncios;
import com.example.olxapp.helper.ConfiguracaoFirebase;
import com.example.olxapp.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {
private FirebaseAuth autenticacao ;
private RecyclerView recyclerAnunciosPublicos;
private Button buttonRegiao, buttonCategoria;
private AdapterAnuncios adapterAnuncios;
private List<Anuncio> ListaAnuncios = new ArrayList<>();
private DatabaseReference anunciosPublicosRef;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        inicializarComponentes();
        //configurações iniciais

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase().child("anuncios");

        //configurar recyclerView
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(ListaAnuncios,this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);
        recuperarAnunciosPublicos();
    }
public void filtrarPorEstado(View view){
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");
        //configurar o spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);

    final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
    String[] estados = getResources().getStringArray(R.array.estados);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,android.R.layout.simple_spinner_item,estados
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerEstado.setAdapter(adapter);



            dialogEstado.setView(viewSpinner);
        dialogEstado.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnunciosPorEstado();
                filtrandoPorEstado = true;
            }
        });
        dialogEstado.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
}
    public void filtrarPorCategoria(View view){

        if (filtrandoPorEstado){
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejada");
            //configurar o spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);

            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] estados = getResources().getStringArray(R.array.categoria);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,android.R.layout.simple_spinner_item,estados
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);



            dialogEstado.setView(viewSpinner);
            dialogEstado.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnunciosPorCategoria();

                }
            });
            dialogEstado.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();

        }else{
            Toast.makeText(this,"Escolha primeiro uma região",Toast.LENGTH_SHORT).show();
        }





    }























    public void recuperarAnunciosPorEstado(){
        //configurar nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ListaAnuncios.clear();
                for (DataSnapshot categorias : snapshot.getChildren()){
                    for (DataSnapshot anuncios : categorias.getChildren()){
                        Anuncio anuncio =anuncios.getValue(Anuncio.class);
                        ListaAnuncios.add(anuncio);

                    }
                }
                Collections.reverse(ListaAnuncios);
                adapterAnuncios.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }







    public void recuperarAnunciosPorCategoria(){
        //configurar nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ListaAnuncios.clear();
                for (DataSnapshot anuncios : snapshot.getChildren()){
                    Anuncio anuncio =anuncios.getValue(Anuncio.class);
                    ListaAnuncios.add(anuncio);

                }
                Collections.reverse(ListaAnuncios);
                adapterAnuncios.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }












    public void recuperarAnunciosPublicos(){
        dialog = new SpotsDialog.Builder()
                .setContext(this).setMessage("Recuperando anúncios").setCancelable(false).build();
        dialog.show();



        ListaAnuncios.clear();
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot estados : snapshot.getChildren()){
                    for (DataSnapshot categorias : estados.getChildren()){
                        for (DataSnapshot anuncios : categorias.getChildren()){
                            Anuncio anuncio =anuncios.getValue(Anuncio.class);
                            ListaAnuncios.add(anuncio);

                        }
                    }

                }
                Collections.reverse(ListaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autenticacao.getCurrentUser()==null){ //deslogado
            menu.setGroupVisible(R.id.group_deslogado,true);
        }else {//logado
            menu.setGroupVisible(R.id.group_logado,true);

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_cadastrar :
                startActivity(new Intent(getApplicationContext(),CadastroActivity.class));
                break;
            case R.id.menu_sair :
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case R.id.menu_anuncios :
                startActivity(new Intent(getApplicationContext(),MeusAnunciosActivity.class));
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    public void inicializarComponentes(){
        recyclerAnunciosPublicos=findViewById(R.id.recyclerAnunciosPublicos);

    }

}
