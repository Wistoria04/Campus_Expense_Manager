package com.btec.fpt.campus_expense_manager.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;

import java.util.ArrayList;

public class ManageCategoryFragment extends Fragment {

    private Button addCategoryButton, deleteCategoryButton, updateCategoryButton;
    private ListView categoryListView;
    private ArrayList<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private DatabaseHelper dbHelper;

    public ManageCategoryFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_category, container, false);

        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        categoryListView = view.findViewById(R.id.categoryListView);
        deleteCategoryButton = view.findViewById(R.id.delete_button);
        updateCategoryButton = view.findViewById(R.id.update_button);

        dbHelper = new DatabaseHelper(getContext());

        loadCategories();

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });

        // Set up long click listener for category selection
        categoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(position);
                return true;
            }
        });

        deleteCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This button can be used for alternative deletion method if needed
                Toast.makeText(getContext(), "Long press a category to delete", Toast.LENGTH_SHORT).show();
            }
        });

        updateCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update functionality to be implemented
            }
        });

        return view;
    }

    private void loadCategories() {
        categoryList = (ArrayList<Category>) dbHelper.getAllCategoryByEmail(DataStatic.email);
        ArrayList<String> categoryNames = new ArrayList<>();

        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categoryNames);
        categoryListView.setAdapter(categoryAdapter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        final EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);

        builder.setTitle("Add New Category")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String categoryName = categoryNameEditText.getText().toString().trim();

                if (TextUtils.isEmpty(categoryName)) {
                    categoryNameEditText.setError("Category name cannot be empty");
                    return;
                }

                boolean isInserted = dbHelper.insertCategory(DataStatic.email, categoryName);

                if (isInserted) {
                    Toast.makeText(getContext(), "Category added successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadCategories();
                } else {
                    Toast.makeText(getContext(), "Category already exists or failed to add", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryList.get(position).getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Category categoryToDelete = categoryList.get(position);
                    boolean isDeleted = dbHelper.deleteCategory(categoryToDelete.getId());

                    if (isDeleted) {
                        Toast.makeText(getContext(), "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadCategories(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}