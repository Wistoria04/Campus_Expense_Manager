package com.btec.fpt.campus_expense_manager.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.R;

public class SetBudgetFragment extends Fragment {

    private EditText budgetEditText;
    private Button saveBudgetButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget_setting, container, false);

        // Initialize UI components
        budgetEditText = view.findViewById(R.id.budgetEditText);
        saveBudgetButton = view.findViewById(R.id.saveBudgetButton);

        // Load previously saved budget (if any)
        loadSavedBudget();

        // Set onClickListener for the Save button
        saveBudgetButton.setOnClickListener(v -> saveBudget());

        return view;
    }

    private void saveBudget() {
        String budgetInput = budgetEditText.getText().toString().trim();

        if (budgetInput.isEmpty()) {
            // Show an error message if the input is empty
            Toast.makeText(getContext(), "Please enter a valid budget!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse the budget value to a double
            double budget = Double.parseDouble(budgetInput);

            if (budget <= 0) {
                Toast.makeText(getContext(), "Budget must be greater than 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the budget using SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BudgetPrefs", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("monthly_budget", (float) budget);
            editor.apply();

            // Show a confirmation message
            Toast.makeText(getContext(), "Budget saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            // Show an error message if the input is not a valid number
            Toast.makeText(getContext(), "Invalid budget format!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedBudget() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BudgetPrefs", getContext().MODE_PRIVATE);
        float savedBudget = sharedPreferences.getFloat("monthly_budget", 0);

        if (savedBudget > 0) {
            // Set the saved budget to the EditText field
            budgetEditText.setText(String.valueOf(savedBudget));
        }
    }

    // Function to check if the user has exceeded their budget
    public boolean isOverBudget(double expenseAmount) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BudgetPrefs", getContext().MODE_PRIVATE);
        float savedBudget = sharedPreferences.getFloat("monthly_budget", 0);

        return expenseAmount > savedBudget;
    }
}
