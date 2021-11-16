package com.example.user.expensesmanagment;


import android.app.AlertDialog;
import android.content.EntityIterator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    //Floating buttons
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;

    //Floating button textview..
    private TextView fab_income_txt;
    private TextView fab_expense_txt;


    private boolean isOpen = false;

    //Animation
    private Animation FadeOpen, FadeClose;

    //Income and Expense total
    private TextView totalIncome;
    private TextView totalExpense;
    private TextView totalBalance;
    private TextView monthIncome;
    private TextView monthExpense;


    //variable store total income and expense value
    public double tIncome=0;
    public double tExpense=0;
    public double tBalance=0;


    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;
    private FirebaseRecyclerAdapter incomeAdapter;
    private FirebaseRecyclerAdapter expenseAdapter;




    //Recycler view
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard,container,false);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);


        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        //Connect floating buttons to layout
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_button);

        //Connect floating text
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);

        //Income and expense total
        totalIncome = myview.findViewById(R.id.income_set_result);
        totalExpense = myview.findViewById(R.id.expense_set_result);
        monthIncome=myview.findViewById(R.id.monthIncome_set_result);
        monthExpense=myview.findViewById(R.id.monthExpense_set_result);
        totalBalance=myview.findViewById(R.id.balance_set_result);


        //Connect animation
        FadeOpen = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_open);
        FadeClose = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_close);

        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addData();

                ftAnimation();

            }
        });




        //Count total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double mIncome = 0;
                tIncome=0;
                tBalance=0;
                for(DataSnapshot mySnapshot:dataSnapshot.getChildren()){
                    Data data = mySnapshot.getValue(Data.class);
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
                    String a=data.getDate();
                    try{
                        Date date = formatter.parse(a);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        // set day to minimum
                        calendar.set(Calendar.DAY_OF_MONTH,
                                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date date1=calendar.getTime();
                        if(date.getTime()>=date1.getTime()){
                            mIncome += data.getAmount();

                        }
                    } catch (ParseException e){
                        e.printStackTrace();
                    }
                    tIncome += data.getAmount();
                    tBalance = tIncome-tExpense;
                    String strMonthIncome = String.valueOf(String.format("%.2f", mIncome));
                    String strIncome = String.valueOf(String.format("%.2f", tIncome));
                    String strBalance = String.valueOf(String.format("%.2f", tBalance));
                    monthIncome.setText(strMonthIncome);
                    totalIncome.setText(strIncome);
                    totalBalance.setText(strBalance);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Count total expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double mExpense=0;
                tExpense=0;
                tBalance=0;
                for(DataSnapshot mySnapshot:dataSnapshot.getChildren()){
                    Data data = mySnapshot.getValue(Data.class);
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
                    String a=data.getDate();
                    try{
                        Date date = formatter.parse(a);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        // set day to minimum
                        calendar.set(Calendar.DAY_OF_MONTH,
                                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date date1=calendar.getTime();
                        if(date.getTime()>=date1.getTime()){
                            mExpense += data.getAmount();

                        }
                    } catch (ParseException e){
                        e.printStackTrace();
                    }
                    tExpense += data.getAmount();
                    tBalance = tIncome-tExpense;
                    String strMonthExpense = String.valueOf(String.format("%.2f", mExpense));
                    String strExpense = String.valueOf(String.format("%.2f", tExpense));
                    String strBalance = String.valueOf(String.format("%.2f", tBalance));
                    monthExpense.setText(strMonthExpense);
                    totalExpense.setText(strExpense);
                    totalBalance.setText(strBalance);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        //RecyclerV
        mRecyclerIncome = myview.findViewById(R.id.recycler_income);
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense);


        //RecyclerV
        //Income
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);

        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        //Expense
        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);

        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);






        return myview;
    }

    //Floating button animation
    private void ftAnimation(){
        //Close and open floating action button
        if(isOpen){

            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen = false;

        } else {

            fab_income_btn.startAnimation(FadeOpen);
            fab_expense_btn.startAnimation(FadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadeOpen);
            fab_expense_txt.startAnimation(FadeOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen = true;

        }

    }

    private void addData(){

        //Income
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeDataInsert();
            }
        });

        //Expense
        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                expenseDataInsert();

            }
        });
    }

    //Income data insertion
    public void incomeDataInsert(){

        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata,null);
        myDialog.setView(myview);
        final AlertDialog dialog = myDialog.create();

        dialog.setCancelable(false);

        final EditText etAmount = myview.findViewById(R.id.amount_et);
        final EditText etType = myview.findViewById(R.id.type_et);
        final EditText etNote = myview.findViewById(R.id.note_et);

        Button btnSave = myview.findViewById(R.id.btn_save);
        Button btnCancel = myview.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String type = etType.getText().toString().trim();
                String amount = etAmount.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                if(TextUtils.isEmpty(type)){

                    etType.setError("This is a required field...");
                    return;

                }

                if(TextUtils.isEmpty(amount)){

                    etAmount.setError("This is a required field...");
                    return;

                }

                double amountDouble = Double.parseDouble(amount);

                if(TextUtils.isEmpty(note)){

                    etNote.setError("This is a required field...");
                    return;

                }

                //Save income data into database
                String id = mIncomeDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(amountDouble, type, note, id, mDate);

                mIncomeDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data INSERTED", Toast.LENGTH_SHORT).show();

                ftAnimation();
                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftAnimation();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    //Expense data insertion
    public void expenseDataInsert(){

        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata,null);

        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        final EditText amount = myview.findViewById(R.id.amount_et);
        final EditText type = myview.findViewById(R.id.type_et);
        final EditText note = myview.findViewById(R.id.note_et);

        Button btnSave = myview.findViewById(R.id.btn_save);
        Button btnCancel = myview.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tmAmount = amount.getText().toString().trim();
                String tmType = type.getText().toString().trim();
                String tmNote = note.getText().toString().trim();

                if(TextUtils.isEmpty(tmAmount)){
                    amount.setError("This field is required...");
                    return;
                }

                double amountDoub = Double.parseDouble(tmAmount);

                if(TextUtils.isEmpty(tmType)){
                    type.setError("This field is required...");
                    return;
                }

                if(TextUtils.isEmpty(tmNote)){
                    note.setError("This field is required...");
                    return;
                }

                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(amountDoub, tmType, tmNote, id, mDate);

                mExpenseDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data INSERTED", Toast.LENGTH_SHORT).show();

                ftAnimation();
                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftAnimation();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    public void onStart(){

        super.onStart();

        //Income
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mIncomeDatabase, Data.class)
                .build();

        incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {

            public IncomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new IncomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false));
            }

            @Override
            protected void onBindViewHolder(IncomeViewHolder holder, final int position, Data model) {

                holder.setIncomeType(model.getType());
                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());

            }


        };

        incomeAdapter.startListening();
        mRecyclerIncome.setAdapter(incomeAdapter);

        //Expense
        FirebaseRecyclerOptions<Data> options2 = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        expenseAdapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(options2) {

            public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false));
            }

            @Override
            protected void onBindViewHolder(ExpenseViewHolder holder, final int position, Data model) {

                holder.setExpenseType(model.getType());
                holder.setExpenseAmount(model.getAmount());
                holder.setExpenseDate(model.getDate());

            }


        };

        expenseAdapter.startListening();
        mRecyclerExpense.setAdapter(expenseAdapter);

    }

    //Income data
    public static class IncomeViewHolder extends  RecyclerView.ViewHolder{

        View mIncomeView;

        public IncomeViewHolder(View itemView){

            super(itemView);
            mIncomeView = itemView;

        }

        public void setIncomeType(String type){

            TextView mType = mIncomeView.findViewById(R.id.type_income_dsh);
            mType.setText(type);

        }

        public void setIncomeAmount(double amount){

            TextView mAmount = mIncomeView.findViewById(R.id.amount_income_dsh);
            String amountStr = String.valueOf(String.format("%.2f", amount));
            mAmount.setText(amountStr);

        }

        public void setIncomeDate(String date){

            TextView mDate = mIncomeView.findViewById(R.id.date_income_dsh);
            mDate.setText(date);

        }

    }

    //Expense data
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

        View mExpenseView;

        public ExpenseViewHolder(@NonNull View itemView) {

            super(itemView);
            mExpenseView = itemView;

        }

        public void setExpenseType(String type){

            TextView mType = mExpenseView.findViewById(R.id.type_expense_dsh);
            mType.setText(type);

        }

        public void setExpenseAmount(double amount){

            TextView mAmount = mExpenseView.findViewById(R.id.amount_expense_dsh);

            String amountStr = String.valueOf(String.format("%.2f", amount));

            mAmount.setText(amountStr);

        }

        public void setExpenseDate(String date){

            TextView mDate = mExpenseView.findViewById(R.id.date_expense_dsh);
            mDate.setText(date);

        }
    }

}


