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


public class IncomeFragment extends Fragment {

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;

    //RecyclerView
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private TextView totalIncome;

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
        View myview = inflater.inflate(R.layout.fragment_income, container, false);


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

        mIncomeDatabase.keepSynced(true);

        totalIncome = myview.findViewById(R.id.income_txt_result);

        recyclerView = myview.findViewById(R.id.recycler_id_income);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        //Get all children data and total up
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                for(DataSnapshot mySnapshot:dataSnapshot.getChildren()){

                    Data data = mySnapshot.getValue(Data.class);

                    total += data.getAmount();

                    String strTotal = String.valueOf(String.format("%.2f", total));

                    totalIncome.setText(strTotal);

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
                .setQuery(mIncomeDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {



            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false));
            }

            protected void onBindViewHolder(MyViewHolder holder, final int position, final Data model) {
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

    private void updateDataItem(){

        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.update_data_item, null);
        myDialog.setView(myview);

        edtAmount = myview.findViewById(R.id.amount_edt);
        edtType = myview.findViewById(R.id.type_edt);
        edtNote = myview.findViewById(R.id.note_edt);

        //Pass selected data to EDT
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

                mIncomeDatabase.child(post_key).setValue(data);

                dialog.dismiss();

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mIncomeDatabase.child(post_key).removeValue();
                dialog.dismiss();

            }
        });

        dialog.show();

    }
}


class MyViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }

    void setType(String type) {
        TextView mType = mView.findViewById(R.id.type_txt_income);
        mType.setText(type);
    }

    void setNote(String note) {

        TextView mNote = mView.findViewById(R.id.note_txt_income);
        mNote.setText(note);
    }

    void setDate(String date) {
        TextView mDate = mView.findViewById(R.id.date_txt_income);
        mDate.setText(date);
    }

    void setAmount(double amount) {
        TextView mAmount = mView.findViewById(R.id.amount_txt_income);
        String strAmount = String.valueOf(String.format("%.2f", amount));
        mAmount.setText(strAmount);
    }

}
