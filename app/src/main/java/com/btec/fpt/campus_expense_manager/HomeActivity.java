package com.btec.fpt.campus_expense_manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
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

        // Show HomeFragment by default
        loadFragment(new HomeFragment());

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Handle tab selection with animations
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_expense_tracking:
                    selectedFragment = new AddExpenseFragment();
                    break;
                case R.id.nav_budget_setting:
                    selectedFragment = new SetBudgetFragment();
                    break;
                case R.id.nav_displayExpense:
                    selectedFragment = new DisplayExpenseFragment();
                    break;
                case R.id.nav_setting:
                    selectedFragment = new SettingFragment();
                    break;
            }

            if (selectedFragment != null) {
                applyTabTransitionEffect();
                loadFragment(selectedFragment);
            }
            return true;
        });

    }

    private void applyTabTransitionEffect() {
        View fragmentContainer = findViewById(R.id.fragment_container);
        TranslateAnimation slideAnimation = new TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0.0f
        );
        slideAnimation.setDuration(300);
        fragmentContainer.startAnimation(slideAnimation);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}