package com.btec.fpt.campus_expense_manager.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.models.DataStatic;
import com.btec.fpt.campus_expense_manager.views.BarChartView;
import com.btec.fpt.campus_expense_manager.views.PieChartView;

import java.util.HashMap;
import java.util.Map;

public class ChartDisplayFragment extends Fragment {
    private PieChartView pieChartView;
    private BarChartView barChartView;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_display, container, false);

        // Initialize views
        pieChartView = view.findViewById(R.id.pieChartView);
        barChartView = view.findViewById(R.id.barChartView);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(getContext());

        // Load expense data
        loadExpenseData();

        return view;
    }

    private void loadExpenseData() {
        // Check if user is logged in
        if (DataStatic.email.isEmpty()) {
            Toast.makeText(getContext(), "Please sign in to view expenses", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get expense data grouped by category for the current user
        Map<String, Double> expenseData = dbHelper.getSpendingByCategory(DataStatic.email);

        // Log data for debugging
        if (expenseData == null || expenseData.isEmpty()) {
            Log.d("ChartDisplayFragment", "No expense data found for email: " + DataStatic.email);
            Toast.makeText(getContext(), "No expenses found. Add some expenses to see charts.", Toast.LENGTH_LONG).show();
        } else {
            Log.d("ChartDisplayFragment", "Expense data: " + expenseData.toString());
        }

        // Set data to charts
        pieChartView.setData(expenseData);
        barChartView.setData(expenseData);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment is resumed
        loadExpenseData();
    }
}