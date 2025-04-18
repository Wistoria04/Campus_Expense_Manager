package com.btec.fpt.campus_expense_manager.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;
import com.btec.fpt.campus_expense_manager.entities.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DisplayExpenseFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private ListView expensesListView;
    private Button clearHistoryButton;
    private ArrayList<Transaction> transactionList;
    private TransactionAdapter adapter;

    public DisplayExpenseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_expense, container, false);
        dbHelper = new DatabaseHelper(getContext());

        EditText editStartDate = view.findViewById(R.id.editStartDate);
        EditText editEndDate = view.findViewById(R.id.editEndDate);
        EditText editCategory = view.findViewById(R.id.editCategory);
        Button searchButton = view.findViewById(R.id.searchButton);
        expensesListView = view.findViewById(R.id.expensesListView);
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(getContext(), transactionList);
        expensesListView.setAdapter(adapter);

        loadExpenses();

        searchButton.setOnClickListener(v -> {
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();
            String category = editCategory.getText().toString().trim();
            filterTransactions(startDate, endDate, category);
        });

        clearHistoryButton.setOnClickListener(v -> clearHistory());

        return view;
    }

    private void loadExpenses() {
        transactionList.clear();
        transactionList.addAll(dbHelper.getAllTransactionsByEmail(DataStatic.email));
        adapter.notifyDataSetChanged();
        if (transactionList.isEmpty()) {
            Toast.makeText(getContext(), "No transactions found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTransaction(Transaction transaction) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_transaction, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText edtAmount = dialogView.findViewById(R.id.amountEditText);
        EditText edtDescription = dialogView.findViewById(R.id.descriptionEditText);
        EditText edtDate = dialogView.findViewById(R.id.dateEditText);
        Spinner spnType = dialogView.findViewById(R.id.typeSpinner);
        Spinner spnCategory = dialogView.findViewById(R.id.spinner);

        // Populate existing values
        edtAmount.setText(String.valueOf(transaction.getAmount()));
        edtDescription.setText(transaction.getDescription());
        edtDate.setText(transaction.getDate());

        // Setup Type Spinner
        ArrayList<String> typeOptions = new ArrayList<>();
        typeOptions.add("Outcome");
        typeOptions.add("Income");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeOptions);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(typeAdapter);
        spnType.setSelection(transaction.getType());

        // Setup Category Spinner
        List<Category> categories = dbHelper.getAllCategoryByEmail(DataStatic.email);
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(categoryAdapter);
        int categoryIndex = categoryNames.indexOf(transaction.getCategory());
        if (categoryIndex >= 0) {
            spnCategory.setSelection(categoryIndex);
        }

        builder.setPositiveButton("Update", (dialog, which) -> {
            String amountStr = edtAmount.getText().toString();
            String description = edtDescription.getText().toString();
            String date = edtDate.getText().toString();
            String category = spnCategory.getSelectedItem() != null ? spnCategory.getSelectedItem().toString() : transaction.getCategory();
            int type = spnType.getSelectedItemPosition(); // 0 = Outcome, 1 = Income

            if (amountStr.isEmpty() || description.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                boolean success = dbHelper.updateTransaction(
                        transaction.getId(), amount, description, date, type, transaction.getEmail(), category
                );
                if (success) {
                    Toast.makeText(getContext(), "Transaction updated!", Toast.LENGTH_SHORT).show();
                    loadExpenses();
                } else {
                    Toast.makeText(getContext(), "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount format!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteTransaction(transaction.getId());
                    loadExpenses();
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void clearHistory() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all transaction history?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.clearAllTransactions();
                    loadExpenses();
                    Toast.makeText(getContext(), "All transactions cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void filterTransactions(String startDate, String endDate, String category) {
        transactionList.clear();
        transactionList.addAll(dbHelper.getFilteredTransactions(startDate, endDate, category));
        adapter.notifyDataSetChanged();
        if (transactionList.isEmpty()) {
            Toast.makeText(getContext(), "No transactions found.", Toast.LENGTH_SHORT).show();
        }
    }

    private class TransactionAdapter extends ArrayAdapter<Transaction> {
        public TransactionAdapter(Context context, ArrayList<Transaction> transactions) {
            super(context, 0, transactions);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_transaction, parent, false);
            }

            Transaction transaction = getItem(position);

            TextView tvTransactionInfo = convertView.findViewById(R.id.tvTransactionInfo);
            Button btnEdit = convertView.findViewById(R.id.btnEdit);
            Button btnDelete = convertView.findViewById(R.id.btnDelete);

            if (transaction != null) {
                String info = transaction.getDate() + " - " + transaction.getCategory() + ": $" + transaction.getAmount() + " (" + (transaction.getType() == 0 ? "Outcome" : "Income") + ")";
                tvTransactionInfo.setText(info);

                btnEdit.setOnClickListener(v -> updateTransaction(transaction));
                btnDelete.setOnClickListener(v -> deleteTransaction(transaction));
            }

            return convertView;
        }
    }
}