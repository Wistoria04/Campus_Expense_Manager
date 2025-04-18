package com.btec.fpt.campus_expense_manager.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.MainActivity;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Transaction;
import com.btec.fpt.campus_expense_manager.models.BalanceInfor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private TextView tvFullName, tvBalance, tvBudget, tvHello;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    public HomeFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        tvFullName = view.findViewById(R.id.tvFullname);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvBudget = view.findViewById(R.id.tvBudget);
        tvHello = view.findViewById(R.id.tv_name);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Setup RecyclerView for transactions
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionAdapter);

        // Load user info and budget
        loadUserInfo();
        loadBudget();
        loadTransactions();

        // Buttons
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnDisplay = view.findViewById(R.id.btnDisplay);
        Button btnAddCategory = view.findViewById(R.id.btnAddCategory);
        Button btnChart = view.findViewById(R.id.btnChart);

        btnLogout.setOnClickListener(v -> handleLogout());

        btnDisplay.setOnClickListener(v -> loadFragment(new DisplayExpenseFragment(), true));

        btnAddCategory.setOnClickListener(v -> loadFragment(new ManageCategoryFragment(), true));

        btnChart.setOnClickListener(v -> loadFragment(new ChartDisplayFragment(), true));

        return view;
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        DataStatic.email = email;

        if (email != null) {
            BalanceInfor balanceInfor = databaseHelper.getBalanceFromEmail(email);
            if (balanceInfor != null) {
                String fullName = balanceInfor.getFirstName() + " " + balanceInfor.getLastName();
                tvFullName.setText(fullName);
                tvHello.setText("Hello " + balanceInfor.getFirstName());
            }
        }
    }

    private void loadBudget() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        float budget = prefs.getFloat("monthly_budget", 0);
        DecimalFormat df = new DecimalFormat("#,###.## $");
        tvBudget.setText(df.format(budget));
        tvBalance.setText(df.format(budget)); // Initial balance
    }

    private void loadTransactions() {
        transactionList.clear();
        List<Transaction> transactions = databaseHelper.getAllTransactionsByEmail(DataStatic.email);
        transactionList.addAll(transactions);
        transactionAdapter.notifyDataSetChanged();

        // Calculate remaining budget based on Outcome transactions
        float totalOutcome = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == 0) { // Outcome
                totalOutcome += transaction.getAmount();
            }
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        float budget = prefs.getFloat("monthly_budget", 0);
        float remainingBudget = budget - totalOutcome;
        DecimalFormat df = new DecimalFormat("#,###.## $");
        tvBalance.setText(df.format(remainingBudget));
    }

    private void handleLogout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadFragment(Fragment fragment, boolean isSwipeRight) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (isSwipeRight) {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        loadBudget();
        loadTransactions();
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private List<Transaction> transactions;

        public TransactionAdapter(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transactions, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);
            holder.tvDate.setText(transaction.getDate());
            holder.tvCategory.setText(transaction.getCategory());
            holder.tvAmount.setText("$" + transaction.getAmount());
            holder.tvType.setText(transaction.getType() == 0 ? "Outcome" : "Income");
            holder.ivIcon.setImageResource(transaction.getType() == 0 ? R.drawable.ic_outcome : R.drawable.ic_income);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvCategory, tvAmount, tvType;
            ImageView ivIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvType = itemView.findViewById(R.id.tvType);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}