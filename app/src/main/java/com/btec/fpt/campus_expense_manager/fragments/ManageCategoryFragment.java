package com.btec.fpt.campus_expense_manager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private Button addCategoryButton;
    private ListView categoryListView;
    private ArrayList<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private DatabaseHelper dbHelper;

    public ManageCategoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_category, container, false);

        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        categoryListView = view.findViewById(R.id.categoryListView);

        dbHelper = new DatabaseHelper(getContext());

        loadCategories();

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
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
                .setPositiveButton("Add", null) // We override this later
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        // Override the "Add" button click to prevent auto-closing on invalid input
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
                    loadCategories(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), "Category already exists or failed to add", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
