package com.btec.fpt.campus_expense_manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.btec.fpt.campus_expense_manager.R;

import java.util.List;
import java.util.Map;

public class BudgetCategoryAdapter extends RecyclerView.Adapter<BudgetCategoryAdapter.BudgetViewHolder> {

    private List<Map.Entry<String, Double>> budgetList;

    public BudgetCategoryAdapter(List<Map.Entry<String, Double>> budgetList) {
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Map.Entry<String, Double> entry = budgetList.get(position);
        String category = entry.getKey();
        double budget = entry.getValue();

        holder.tvCategory.setText(category);
        holder.tvRemainingBudget.setText(String.format("%.0f VND", budget));
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvRemainingBudget;

        BudgetViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvRemainingBudget = itemView.findViewById(R.id.tv_remaining_budget);
        }
    }

    public void updateData(List<Map.Entry<String, Double>> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged();
    }
}