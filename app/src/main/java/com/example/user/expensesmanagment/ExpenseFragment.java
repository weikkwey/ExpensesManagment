package com.example.user.expensesmanagment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.user.expensesmanagment.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;


public class ExpenseFragment extends Fragment {

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //Recycler View
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private TextView totalExpense;

    //Update data
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    //button update & delete
    private Button btnUpdate;
    private Button btnDelete;

    //Data item value
    private String type;
    private String note;
    private double amount;

    private String post_key;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        mExpenseDatabase.keepSynced(true);

        totalExpense = myview.findViewById(R.id.expense_txt_result);

        recyclerView = myview.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                for(DataSnapshot mySnapshot:dataSnapshot.getChildren()){

                    Data data = mySnapshot.getValue(Data.class);

                    total += data.getAmount();

                    String strTotal = String.valueOf(String.format("%.2f", total));

                    totalExpense.setText(strTotal);

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return myview;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, myViewHolder>(options) {



            public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new myViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false));
            }

            protected void onBindViewHolder(myViewHolder holder, final int position, final Data model) {
                holder.setAmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(position).getKey();

                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        updateDataItem();

                    }
                });
            }

        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);


    }

    private static class myViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public myViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        private void setDate(String date){

            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);

        }

        private void setType(String type){

            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);

        }

        private void setNote(String note){

            TextView mNote = mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);

        }

        private void setAmount(double amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_expense);
            String strAmount = String.valueOf(String.format("%.2f", amount));
            mAmount.setText(strAmount);
        }
    }

    private void updateDataItem(){

        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.update_data_item,null);
        myDialog.setView(myview);

        edtAmount = myview.findViewById(R.id.amount_edt);
        edtType = myview.findViewById(R.id.type_edt);
        edtNote = myview.findViewById(R.id.note_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());



        btnUpdate = myview.findViewById(R.id.btn_update);
        btnDelete = myview.findViewById(R.id.btn_delete);

        final AlertDialog dialog = myDialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String updAmount = String.valueOf(amount);

                updAmount = edtAmount.getText().toString().trim();

                double myAmount = Double.parseDouble(updAmount);

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(myAmount, type, note, post_key, mDate);

                mExpenseDatabase.child(post_key).setValue(data);

                dialog.dismiss();

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();

            }
        });

        dialog.show();

    }

}


