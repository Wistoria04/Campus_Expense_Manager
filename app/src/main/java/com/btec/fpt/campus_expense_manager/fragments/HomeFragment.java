package com.btec.fpt.campus_expense_manager.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.ItemAdapter;
import com.btec.fpt.campus_expense_manager.MainActivity;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.entities.Category;
import com.btec.fpt.campus_expense_manager.models.BalanceInfor;
import com.btec.fpt.campus_expense_manager.models.Item;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment  extends Fragment {


    public  HomeFragment(){

    }

    DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        TextView tvFullName = view.findViewById(R.id.tvFullname);
        TextView tvBalance = view.findViewById(R.id.tvBalance);
        Button btnChart = view.findViewById(R.id.btnChart);
        Button btnCategory = view.findViewById(R.id.btnAddCategory);



        TextView tvHello = view.findViewById(R.id.tv_name);



        SharedPreferences sharedPreferences =  getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Lay ve thong tin mat khau va email

        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null); // Retrieve the hashed/encrypted version


        DataStatic.email  = email;
        DataStatic.password = password;
        BalanceInfor balanceInfor = databaseHelper.getBalanceFromEmail(email);

        tvFullName.setText(balanceInfor.getFirstName() +" " + balanceInfor.getLastName());
        tvBalance.setText(balanceInfor.getBalance() +"");
        tvHello.setText("Hello " + balanceInfor.getFirstName());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        // Set LayoutManager for RecyclerView (Linear Layout)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize item list
        List<Item> itemList = new ArrayList<>();

        List<Category> categoryList = databaseHelper.getAllCategoryByEmail(email);

        for(Category category: categoryList){

            itemList.add(new Item(R.drawable.ic_coin, category.getName()));

        }

        // Add data to the list (image resource + text)
//        itemList.add(new Item(R.drawable.item1, "Item 1"));
//        itemList.add(new Item(R.drawable.item2, "Item 2"));
//        itemList.add(new Item(R.drawable.item3, "Item 3"));
//
//        // Add data to the list (image resource + text)
//        itemList.add(new Item(R.drawable.item4, "Item 4"));
//        itemList.add(new Item(R.drawable.item5, "Item 5"));
//        itemList.add(new Item(R.drawable.item6, "Item 6"));

        // Initialize the adapter and set it to the RecyclerView
        ItemAdapter itemAdapter = new ItemAdapter(getContext(), itemList);
        recyclerView.setAdapter(itemAdapter);


        Button btnLogout = view.findViewById(R.id.btnLogout);
//        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        Button btnDisplayAll = view.findViewById(R.id.btnDisplay);
        @SuppressLint("CutPasteId") Button btnAddCategory = view.findViewById(R.id.btnAddCategory);

        btnLogout.setOnClickListener(v -> handleLogout());

        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                loadFragment(new ChartDisplayFragment());

            }
        });


        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFragment(new ManageCategoryFragment());

            }
        });


        return  view;

    }


    private void handleLogout() {
        // Clear session details
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate back to LoginFragment
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Prevent returning to the Home screen
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
