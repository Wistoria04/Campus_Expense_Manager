package com.btec.fpt.campus_expense_manager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.views.PieChartView;
import com.btec.fpt.campus_expense_manager.views.BarChartView;

import java.util.HashMap;

public class ChartDisplayFragment extends Fragment {
    private PieChartView pieChartView;
    private BarChartView barChartView;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_display, container, false);

        pieChartView = view.findViewById(R.id.pieChartView);
        barChartView = view.findViewById(R.id.barChartView);
        dbHelper = new DatabaseHelper(getContext());

        loadData();

        return view;
    }

    private void loadData() {
        HashMap<String, Double> spendingData = dbHelper.getSpendingByCategory(DataStatic.email);
        HashMap<String, Double> fluctuationData = dbHelper.getCategoryFluctuations(DataStatic.email);

        pieChartView.setData(spendingData);
        barChartView.setData(fluctuationData);
    }
}
