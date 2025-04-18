package com.btec.fpt.campus_expense_manager;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.btec.fpt.campus_expense_manager.fragments.AddExpenseFragment;
import com.btec.fpt.campus_expense_manager.fragments.DisplayExpenseFragment;
import com.btec.fpt.campus_expense_manager.fragments.HomeFragment;
import com.btec.fpt.campus_expense_manager.fragments.SetBudgetFragment;
import com.btec.fpt.campus_expense_manager.fragments.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        loadFragment(fragments[0], false);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        if (currentPosition > 0) {
                            currentPosition--;
                            loadFragment(fragments[currentPosition], false);
                        }
                    } else {
                        if (currentPosition < fragments.length - 1) {
                            currentPosition++;
                            loadFragment(fragments[currentPosition], true);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        View fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int newPosition = currentPosition;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = fragments[0];
                newPosition = 0;
            } else if (item.getItemId() == R.id.nav_expense_tracking) {
                selectedFragment = fragments[1];
                newPosition = 1;
            } else if (item.getItemId() == R.id.nav_budget_setting) {
                selectedFragment = fragments[2];
                newPosition = 2;
            } else if (item.getItemId() == R.id.nav_displayExpense) {
                selectedFragment = fragments[3];
                newPosition = 3;
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = fragments[4];
                newPosition = 4;
            }
            if (selectedFragment != null) {
                boolean isSwipeRight = newPosition > currentPosition;
                loadFragment(selectedFragment, isSwipeRight);
                currentPosition = newPosition;
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean isSwipeRight) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isSwipeRight) {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        } else {
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}