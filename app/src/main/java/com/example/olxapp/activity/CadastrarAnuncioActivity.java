package com.example.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.olxapp.R;
import com.example.olxapp.helper.ConfiguracaoFirebase;
import com.example.olxapp.helper.Permissoes;
import com.example.olxapp.model.Anuncio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener{
    private EditText campoTitulo , campoDescricao;
    private CurrencyEditText campoValor;
    private ImageView imagem1,imagem2,imagem3;
    private Spinner campoEstado, campoCategoria;
    private MaskEditText campoTelefone;
    private StorageReference storage;
    private AlertDialog dialog;

    private String[]  permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotos = new ArrayList<>();

    private Anuncio anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);
        //configuracoes iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        //validar permissoes
        Permissoes.validarPermissoes(permissoes,this,1);
        //inicializar
        inicializarComponentes();
        carregarDadosSpinner();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageCadastro1 :
                escolherImagem(1);
                break;
            case R.id.imageCadastro2 :
                escolherImagem(2);
                break;
            case R.id.imageCadastro3 :
                escolherImagem(3);
                break;
        }
    }
public void escolherImagem(int requestCode){
    Intent i  = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(i,requestCode);
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            //recupeerar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();
            //configura imagem no ImageView
            if (requestCode==1){
                imagem1.setImageURI(imagemSelecionada);
                listaFotosRecuperadas.add(caminhoImagem);
            }else if (requestCode==2){
                imagem2.setImageURI(imagemSelecionada);
            }else if (requestCode==3) {
                imagem3.setImageURI(imagemSelecionada);
            }
            listaFotosRecuperadas.add(caminhoImagem);
        }
    }
private void carregarDadosSpinner(){
String[] estados = getResources().getStringArray(R.array.estados);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_item,estados
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
campoEstado.setAdapter(adapter);

    String[] categorias = getResources().getStringArray(R.array.categoria);
    ArrayAdapter<String> adapterC = new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_item,categorias
    );
    adapterC.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    campoCategoria.setAdapter(adapterC);

};


    private void inicializarComponentes(){
        campoTitulo=findViewById(R.id.editTitulo);
        campoDescricao=findViewById(R.id.editDescricao);
        campoValor=findViewById(R.id.editValor);
        campoTelefone=findViewById(R.id.editTelefone);
        imagem1=findViewById(R.id.imageCadastro1);
        imagem2=findViewById(R.id.imageCadastro2);
        imagem3=findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        campoEstado=findViewById(R.id.spinnerEstado);
        campoCategoria=findViewById(R.id.spinnerCategoria);

        //configurar para pt-BR (opcional pq o android faz sozinho
        Locale locale = new Locale("pt","BR");
        campoValor.setLocale(locale);
    }
    public void salvarAnuncio(){
                dialog = new SpotsDialog.Builder()
                .setContext(this).setMessage("Salvando anúncio").setCancelable(false).build();
                dialog.show();

            //salvar imagem no storage
            for (int i =0; i<listaFotosRecuperadas.size();i++){
                String urlImagem = listaFotosRecuperadas.get(i);
                int tamanhoLista = listaFotosRecuperadas.size();
                salvarFotoStorage(urlImagem,tamanhoLista,i);
            }
    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){
        //criando nó no Storage
        final StorageReference imagemAnuncio = storage.child("imagens")
                                                .child("anuncios")
                                                .child(anuncio.getIdAnuncio())
                                                .child("imagem"+contador);
        //fazer upload
        UploadTask uploadTask = imagemAnuncio.putFile( Uri.parse(urlString) );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagemAnuncio.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri firebaseUrl = task.getResult();
                            String urlConvertida = firebaseUrl.toString();

                            listaUrlFotos.add(urlConvertida);
                            if (totalFotos ==listaUrlFotos.size()){
                                anuncio.setFotos(listaUrlFotos);
                                anuncio.salvar();
                                dialog.dismiss();
                                finish();
                            }
                        }
                    });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload!");
                Log.i("INFO","Falha ao fazer upload  " +e.getMessage());
            }
        });

    }

    private Anuncio configurarAnuncio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();
       Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);
        return anuncio;

    }

    public void validarDadosAnuncio(View view){
        anuncio = configurarAnuncio();
        String valor = String.valueOf( campoValor.getRawValue());
        String fone ="";

        if (campoTelefone.getUnMasked() !=null){
            fone = campoTelefone.getUnMasked();
        }



        if (listaFotosRecuperadas.size()!=0){
            if (!anuncio.getEstado().isEmpty() ){
                if (!anuncio.getCategoria().isEmpty()){
                    if (!anuncio.getTitulo().isEmpty()){
                        if (!valor.isEmpty() && !valor.equals("0")){
                            if (!anuncio.getTelefone().isEmpty()&& fone.length()>=11){
                                if (!anuncio.getDescricao().isEmpty()){
                                    salvarAnuncio();
                                }else{
                                    exibirMensagemErro("Preencha a descrição.");
                                }

                            }else{
                                exibirMensagemErro("Preencha o telefone.");
                            }

                        }else{
                            exibirMensagemErro("Preencha o valor.");
                        }


                    }else {
                        exibirMensagemErro("Preencha o titulo .");
                    }
                }else {
                    exibirMensagemErro("Selecione a categoria.");
                }
            }else {
                exibirMensagemErro("Selecione o estado.");
            }

        }else{
            exibirMensagemErro("Selecione ao menos uma foto.");
        }
    }

    private void exibirMensagemErro(String mensagem){
        Toast.makeText(this,mensagem,Toast.LENGTH_SHORT).show();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado: grantResults){
            if (permissaoResultado== PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }
    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
