package com.btec.fpt.campus_expense_manager.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private EditText amountEditText, descriptionEditText, dateEditText, emailEditText;
    private Spinner categorySpinner, typeSpinner;
    private float userBudget = 0;
    private String selectCategory = "Food";
    private int selectType = 0; // 0 = Outcome, 1 = Income

    public AddExpenseFragment() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        dbHelper = new DatabaseHelper(getContext());
        amountEditText = view.findViewById(R.id.amountEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        dateEditText = view.findViewById(R.id.dateEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        categorySpinner = view.findViewById(R.id.spinner);
        typeSpinner = view.findViewById(R.id.typeSpinner);

        // Load budget from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        userBudget = sharedPreferences.getFloat("monthly_budget", 0);

        // Setup Category Spinner
        List<Category> categoryList = dbHelper.getAllCategoryByEmail(DataStatic.email);
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectCategory = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected: " + selectCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup Type Spinner
        ArrayList<String> typeOptions = new ArrayList<>();
        typeOptions.add("Outcome");
        typeOptions.add("Income");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeOptions);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectType = position; // 0 = Outcome, 1 = Income
                Toast.makeText(getContext(), "Selected: " + typeOptions.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup DatePicker for dateEditText
        dateEditText.setOnClickListener(v -> showDatePickerDialog());
        dateEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawable = dateEditText.getCompoundDrawables()[0]; // drawableLeft
                if (drawable != null && event.getX() <= (dateEditText.getLeft() + drawable.getBounds().width() + dateEditText.getCompoundDrawablePadding())) {
                    showDatePickerDialog();
                    return true;
                }
            }
            return false;
        });

        // Buttons
        Button addButton = view.findViewById(R.id.addButton);
        Button btnDisplay = view.findViewById(R.id.btnDisplay);

        addButton.setOnClickListener(v -> addExpense());
        btnDisplay.setOnClickListener(v -> loadFragment(new DisplayExpenseFragment(), true));

        return view;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateEditText.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountEditText.getText().toString());
            String description = descriptionEditText.getText().toString();
            String date = dateEditText.getText().toString();

            if (description.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount > userBudget) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Over budget")
                        .setMessage("This transaction exceeds your set budget. Do you want to continue?")
                        .setPositiveButton("Continue", (dialog, which) -> saveExpense(amount))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(getContext(), "Transaction cancelled.", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                saveExpense(amount);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveExpense(double amount) {
        String description = descriptionEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String email = DataStatic.email;

        boolean inserted = dbHelper.insertTransaction(amount, description, date, selectType, email, selectCategory);
        if (inserted) {
            Toast.makeText(getContext(), "Transaction added.", Toast.LENGTH_SHORT).show();
            amountEditText.setText("");
            descriptionEditText.setText("");
            dateEditText.setText("");
            categorySpinner.setSelection(0);
            typeSpinner.setSelection(0);
        } else {
            Toast.makeText(getContext(), "Error adding transaction.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment, boolean isSwipeRight) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (isSwipeRight) {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        } else {
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        userBudget = sharedPreferences.getFloat("monthly_budget", 0);
    }
}