package com.btec.fpt.campus_expense_manager.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.btec.fpt.campus_expense_manager.DataStatic;
import com.btec.fpt.campus_expense_manager.MainActivity;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.models.BalanceInfor;

public class SettingFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private TextView tvHello;
    private Button btnLogout, btnChangePass;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        // Initialize UI components
        tvHello = view.findViewById(R.id.tv_name);
        btnLogout = view.findViewById(R.id.btnlogout);
        btnChangePass = view.findViewById(R.id.btnchangepass);

        // Get email and password from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        // Set global DataStatic values
        DataStatic.email = email;
        DataStatic.password = password;

        // Handle case where email is not found in SharedPreferences
        if (email == null) {
            Toast.makeText(getContext(), "User not logged in. Redirecting to login...", Toast.LENGTH_LONG).show();
            handleLogout();
            return view;
        }

        // Get user balance information
        BalanceInfor balanceInfor = databaseHelper.getBalanceFromEmail(email);
        if (balanceInfor != null && balanceInfor.getFirstName() != null) {
            tvHello.setText("Hello " + balanceInfor.getFirstName());
        } else {
            tvHello.setText("Hello User");
            Toast.makeText(getContext(), "User information not found. Please update your profile.", Toast.LENGTH_LONG).show();
        }

        // Set up button listeners
        btnLogout.setOnClickListener(v -> handleLogout());
        btnChangePass.setOnClickListener(v -> loadFragment(new ChangePasswordFragment()));

        return view;
    }

    private void handleLogout() {
        // Clear session details
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate back to MainActivity (Login screen)
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