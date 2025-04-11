package com.btec.fpt.campus_expense_manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.btec.fpt.campus_expense_manager.fragments.AddExpenseFragment;
import com.btec.fpt.campus_expense_manager.fragments.DisplayExpenseFragment;
import com.btec.fpt.campus_expense_manager.fragments.HomeFragment;
import com.btec.fpt.campus_expense_manager.fragments.SetBudgetFragment;
import com.btec.fpt.campus_expense_manager.fragments.SettingFragment;
import com.btec.fpt.campus_expense_manager.models.Item;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show LoginFragment by default
        loadFragment(new HomeFragment());

        // Ánh xạ BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Xử lý sự kiện khi người dùng chọn mục
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_expense_tracking) {
                selectedFragment = new AddExpenseFragment();
            } else if (item.getItemId() == R.id.nav_budget_setting){
                selectedFragment = new SetBudgetFragment();
            } else if (item.getItemId() == R.id.nav_displayExpense) {
                selectedFragment = new DisplayExpenseFragment();
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = new SettingFragment();
            }if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}