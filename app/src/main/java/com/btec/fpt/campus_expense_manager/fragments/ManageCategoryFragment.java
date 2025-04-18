package com.btec.fpt.campus_expense_manager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoryFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerViewCategories;
    private Button addCategoryButton;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

    public ManageCategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_category, container, false);

        dbHelper = new DatabaseHelper(getContext());
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);

        // Setup RecyclerView
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Load categories
        loadCategories();

        // Add category dialog
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        return view;
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(dbHelper.getAllCategoryByEmail(DataStatic.email));
        categoryAdapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString().trim();
            if (categoryName.isEmpty()) {
                categoryNameEditText.setError("Category name cannot be empty");
                return;
            }

            boolean isInserted = dbHelper.insertCategory(DataStatic.email, categoryName);
            if (isInserted) {
                Toast.makeText(getContext(), "Category added successfully!", Toast.LENGTH_SHORT).show();
                loadCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Category already exists or failed to add", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showUpdateCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_category, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        categoryNameEditText.setText(category.getName());

        AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(v -> {
            String newCategoryName = categoryNameEditText.getText().toString().trim();
            if (newCategoryName.isEmpty()) {
                categoryNameEditText.setError("Category name cannot be empty");
                return;
            }

            boolean isUpdated = dbHelper.updateCategory(category.getId(), newCategoryName, DataStatic.email);
            if (isUpdated) {
                Toast.makeText(getContext(), "Category updated successfully!", Toast.LENGTH_SHORT).show();
                loadCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to update category", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private List<Category> categories;

        public CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.categoryName.setText(category.getName());
            holder.categoryImage.setImageResource(R.drawable.ic_coin);

            holder.updateButton.setOnClickListener(v -> showUpdateCategoryDialog(category));

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean isDeleted = dbHelper.deleteCategory(category.getId());
                            if (isDeleted) {
                                Toast.makeText(getContext(), "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView categoryImage;
            TextView categoryName;
            Button updateButton, deleteButton;

            public ViewHolder(View itemView) {
                super(itemView);
                categoryImage = itemView.findViewById(R.id.category_image);
                categoryName = itemView.findViewById(R.id.category_name);
                updateButton = itemView.findViewById(R.id.update_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }
}