package com.btec.fpt.campus_expense_manager.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;
import com.btec.fpt.campus_expense_manager.entities.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DisplayExpenseFragment extends Fragment {
    public DisplayExpenseFragment() {
    }

    private DatabaseHelper dbHelper;
    private ListView expensesListView;
    private Button clearHistoryButton;

    private ArrayList<Transaction> transactionList;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_expense, container, false);
        dbHelper = new DatabaseHelper(getContext());

        EditText editStartDate = view.findViewById(R.id.editStartDate);
        EditText editEndDate = view.findViewById(R.id.editEndDate);
        EditText editCategory = view.findViewById(R.id.editCategory);
        Button searchButton = view.findViewById(R.id.searchButton);
        expensesListView = view.findViewById(R.id.expensesListView);
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton);

        loadExpenses();

        searchButton.setOnClickListener(v -> {
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();
            String category = editCategory.getText().toString().trim();
            filterTransactions(startDate, endDate, category);
        });

        // Long click listener for updating and deleting transactions
        expensesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Transaction selectedTransaction = transactionList.get(position);

                // Show options to Update or Delete
                new AlertDialog.Builder(getContext())
                        .setTitle("Choose Action")
                        .setMessage("Would you like to update or delete this transaction?")
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateTransaction(selectedTransaction);
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteTransaction(selectedTransaction);
                            }
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
                return true;
            }
        });

        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        return view;
    }

    private void loadExpenses() {
        transactionList = (ArrayList<Transaction>) dbHelper.getAllTransactionsByEmail(DataStatic.email);
        ArrayList<String> expenseDescriptions = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            String description = transaction.getDate() + " - " + transaction.getCategory() + ": $" + transaction.getAmount();
            expenseDescriptions.add(description);
        }

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, expenseDescriptions);
        expensesListView.setAdapter(adapter);
    }

    private void updateTransaction(final Transaction transaction) {
        // Open a dialog to allow the user to update the transaction details
        // For now, just show a Toast for simplicity
//        Toast.makeText(getContext(), "Updating transaction: " + transaction.getDescription(), Toast.LENGTH_SHORT).show();

        // Call dbHelper.updateTransaction(updatedTransaction) here after the user makes changes


        // Inflate the dialog view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_update_transaction, null);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Update Transaction");
        builder.setView(dialogView);

        // Find views in dialog
        EditText edtAmount = dialogView.findViewById(R.id.amountEditText);
        EditText edtDescription = dialogView.findViewById(R.id.descriptionEditText);
        EditText edtDate = dialogView.findViewById(R.id.dateEditText);
        Spinner spnCategory = dialogView.findViewById(R.id.spinner);
        EditText rgType = dialogView.findViewById(R.id.typeEditText);

        // Populate existing values
        edtAmount.setText(String.valueOf(transaction.getAmount()));
        edtDescription.setText(transaction.getDescription());
        edtDate.setText(transaction.getDate());

        // Populate category spinner
        List<Category> categories = dbHelper.getAllCategoryByEmail(DataStatic.email);

        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName()); // Assuming getCategoryName() is the getter for the name
        }

        // Convert the ArrayList to a String array, if needed
        String[] categoryNameArray = categoryNames.toArray(new String[0]);
        
        ArrayAdapter<String> adapter = new ArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_item, // Default Spinner layout
                categoryNameArray
        );

        // Set the dropdown view resource for the adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(adapter);
        spnCategory.setSelection(categories.indexOf(transaction.getCategory()));



        // Set dialog buttons
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Validate and update
            String amountStr = edtAmount.getText().toString();
            String description = edtDescription.getText().toString();
            String date = edtDate.getText().toString();
            String category = spnCategory.getSelectedItem().toString();
            int type = Integer.parseInt(rgType.getText().toString());


            if (amountStr.isEmpty() || description.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // Update transaction in the database

            boolean success = dbHelper.updateTransaction(
                    transaction.getId(),
                    amount,
                    description,
                    date,
                    type,
                    transaction.getEmail(),
                    category
            );

            if (success) {
                Toast.makeText(getContext(), "Transaction updated!", Toast.LENGTH_SHORT).show();
                loadExpenses(); // Reload the list
            } else {
                Toast.makeText(getContext(), "Failed to update transaction", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteTransaction(final Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteTransaction(transaction.getId());
                        loadExpenses(); // Reload the list after deletion
                        Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void clearHistory() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all transaction history?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.clearAllTransactions();
                        loadExpenses(); // Reload the list after clearing history
                        Toast.makeText(getContext(), "All transactions cleared", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void filterTransactions(String startDate, String endDate, String category) {
        // Clear the current list
        transactionList.clear();

        // Fetch filtered transactions from the database
        transactionList = (ArrayList<Transaction>) dbHelper.getFilteredTransactions(startDate, endDate, category);

        ArrayList<String> filteredDescriptions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            String description = transaction.getDate() + " - " + transaction.getCategory() + ": $" + transaction.getAmount();
            filteredDescriptions.add(description);
        }

        // Update the adapter
        adapter.clear();
        adapter.addAll(filteredDescriptions);
        adapter.notifyDataSetChanged();

        // Show a message if no transactions are found
        if (transactionList.isEmpty()) {
            Toast.makeText(getContext(), "No transactions found.", Toast.LENGTH_SHORT).show();
        }
    }




}
