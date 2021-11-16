package com.example.user.expensesmanagment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private Button btnReset;
    private EditText edtEmail;
    private TextView signIn;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        mAuth = FirebaseAuth.getInstance();

        btnReset = findViewById(R.id.btn_reset);
        edtEmail = findViewById(R.id.email_reset);

        signIn = findViewById(R.id.signin_here);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userEmail = edtEmail.getText().toString();

                if(TextUtils.isEmpty(userEmail)){

                    edtEmail.setError("This is a required field...");

                } else {

                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(ResetActivity.this, "Reset password E-mail sent. Please check your E-mail", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetActivity.this, MainActivity.class));

                            } else {

                                String errorMsg = task.getException().getMessage();

                                Toast.makeText(ResetActivity.this, "ERROR Occurred: " + errorMsg, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResetActivity.this, MainActivity.class));
            }
        });
    }
}
