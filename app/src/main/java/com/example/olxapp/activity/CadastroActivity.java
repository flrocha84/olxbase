package com.example.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.olxapp.R;
import com.example.olxapp.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail , campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        iniciarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();
                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {
                        //verifica o estado do swith
                        if (tipoAcesso.isChecked()){//cadastro
                        autenticacao.createUserWithEmailAndPassword(
                                email,senha
                        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                    //direcionar para a tela principal



                                }else{
                                    String erroExcessao="";
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthWeakPasswordException e){
                                        erroExcessao="Digite umasenha mais forte!";
                                    }catch (FirebaseAuthInvalidCredentialsException e){
                                        erroExcessao="Digite um e-mail valido!";
                                    }catch (FirebaseAuthUserCollisionException e){
                                        erroExcessao="Usuário já cadastrado!";
                                    }catch (Exception e){
                                        erroExcessao = "ao cadastrar usuário: "+ e.getMessage();
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(CadastroActivity.this, "Erro: "+erroExcessao, Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                        }else{//login
                            autenticacao.signInWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(CadastroActivity.this, "Logado com sucesso!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),AnunciosActivity.class));
                                    }else {
                                        Toast.makeText(CadastroActivity.this, "Erro ao realizar login!", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        }



                    } else{
                        Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void iniciarComponentes(){
        campoEmail=findViewById(R.id.editCadastroEmail);
        campoSenha=findViewById(R.id.editCadastroSenha);
        botaoAcessar=findViewById(R.id.buttonAcesso);
        tipoAcesso=findViewById(R.id.switchAcesso);
    }
}
