package com.btec.fpt.campus_expense_manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    private GestureDetector gestureDetector;
    private int currentPosition = 0;
    private Fragment[] fragments = {
        new HomeFragment(),
        new AddExpenseFragment(),
        new SetBudgetFragment(),
        new DisplayExpenseFragment(),
        new SettingFragment()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show HomeFragment by default
        loadFragment(fragments[0], false);

        // Initialize gesture detector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        // Swipe right
                        if (currentPosition > 0) {
                            currentPosition--;
                            loadFragment(fragments[currentPosition], true);
                        }
                    } else {
                        // Swipe left
                        if (currentPosition < fragments.length - 1) {
                            currentPosition++;
                            loadFragment(fragments[currentPosition], false);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        // Set up touch listener for the fragment container
        View fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Ánh xạ BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Xử lý sự kiện khi người dùng chọn mục
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = fragments[0];
                currentPosition = 0;
            } else if (item.getItemId() == R.id.nav_expense_tracking) {
                selectedFragment = fragments[1];
                currentPosition = 1;
            } else if (item.getItemId() == R.id.nav_budget_setting){
                selectedFragment = fragments[2];
                currentPosition = 2;
            } else if (item.getItemId() == R.id.nav_displayExpense) {
                selectedFragment = fragments[3];
                currentPosition = 3;
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = fragments[4];
                currentPosition = 4;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment, false);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean isSwipeRight) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        if (isSwipeRight) {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,  // enter
                R.anim.slide_out_right  // exit
            );
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.slide_out_left  // exit
            );
        }
        
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}