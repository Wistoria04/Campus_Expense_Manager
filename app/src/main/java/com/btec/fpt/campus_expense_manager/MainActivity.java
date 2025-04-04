package com.btec.fpt.campus_expense_manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.btec.fpt.campus_expense_manager.fragments.AddExpenseFragment;
import com.btec.fpt.campus_expense_manager.fragments.ChangePasswordFragment;
import com.btec.fpt.campus_expense_manager.fragments.ForgotPasswordFragment;
import com.btec.fpt.campus_expense_manager.fragments.LoginFragment;
import com.btec.fpt.campus_expense_manager.fragments.RegisterFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);


        if (isLoggedIn) {
            // Navigate to HomeActivity if already logged in
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to this screen
        } else {
            setContentView(R.layout.activity_main);

            // Show LoginFragment by default
            loadFragment(new LoginFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
