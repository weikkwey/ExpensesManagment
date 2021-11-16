package com.example.user.expensesmanagment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;


public class ChangePasswordFragment extends Fragment {

    private EditText edt_oldPass;
    private EditText edt_newPass;

    private Button btnChange;

    private ProgressDialog mDialog;


    //Firebase
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_change_password, container, false);

        edt_oldPass = myview.findViewById(R.id.old_password);
        edt_newPass = myview.findViewById(R.id.new_password);
        btnChange = myview.findViewById(R.id.btn_change);

        mDialog = new ProgressDialog(getContext());


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();



        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUser != null) {

                    mDialog.setMessage("Processing...");
                    mDialog.setCancelable(false);
                    mDialog.show();

                    String oldPass = edt_oldPass.getText().toString();
                    final String newPass = edt_newPass.getText().toString().trim();

                    String email = mUser.getEmail();

                    if (!TextUtils.isEmpty(oldPass) && !TextUtils.isEmpty(email)) {

                        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);




                        mUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    mUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mDialog.dismiss();
                                                Toast.makeText(getContext(), "Password Changed. Please login again...", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                startActivity(new Intent(getContext(),MainActivity.class));

                                            } else {

                                                String errorMsg = task.getException().getMessage();

                                                mDialog.dismiss();
                                                Toast.makeText(getContext(), "ERROR Occurred: " + errorMsg, Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                                } else {

                                    String errorMsg = task.getException().getMessage();

                                    mDialog.dismiss();
                                    Toast.makeText(getContext(), "ERROR Occurred: " + errorMsg, Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                    } else if(TextUtils.isEmpty(oldPass)){

                        mDialog.dismiss();
                        Toast.makeText(getContext(), "Old password is null", Toast.LENGTH_SHORT).show();

                    } else {

                        mDialog.dismiss();
                        Toast.makeText(getContext(), "Email is null", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
            }


        });



        return myview;
    }

}
