package com.btec.fpt.campus_expense_manager.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.btec.fpt.campus_expense_manager.HomeActivity;
import com.btec.fpt.campus_expense_manager.R;
import com.btec.fpt.campus_expense_manager.database.DatabaseHelper;
import com.btec.fpt.campus_expense_manager.models.DataStatic;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    DatabaseHelper databaseHelper = null;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        databaseHelper = new DatabaseHelper(getContext());

        // Find buttons
        Button loginButton = view.findViewById(R.id.login_button);
        Button registerButton = view.findViewById(R.id.goto_register_button);
        Button changePasswordButton = view.findViewById(R.id.goto_change_password_button);

        EditText edtEmail = view.findViewById(R.id.email);
        EditText edtPassword = view.findViewById(R.id.password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String pwd = edtPassword.getText().toString();

                if (!email.isEmpty() && !pwd.isEmpty()) {
                    boolean check = databaseHelper.signIn(email, pwd);

                    if (check) {
                        // Lưu email vào DataStatic
                        DataStatic.setEmail(email);

                        // Lưu email, password và trạng thái đăng nhập vào SharedPreferences
                        editor.putString("email", email);
                        editor.putString("password", pwd); // Lưu mật khẩu đã mã hóa trong thực tế
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        // Chuyển hướng sang HomeActivity
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        showToastCustom("Email or password incorrect!");
                    }
                } else {
                    showToastCustom("Email or password is invalid !!!");
                }
            }
        });

        // Set up button to go to RegisterFragment
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new RegisterFragment());
            }
        });

        // Set up button to go to ChangePasswordFragment
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ChangePasswordFragment());
            }
        });

        return view;
    }

    void showToastCustom(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, view.findViewById(R.id.custom_toast_layout));
        // Set the icon
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.icon_x);  // Set your desired icon

        // Set the text
        TextView text = layout.findViewById(R.id.toast_message);
        text.setText(message);

        // Create and show the toast
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void showCustomToastMessage(String message) {
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_LONG);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.custom_toast_layout));
        ImageView icon = layout.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.insta_icon);  // Set your desired icon
        // Set the text
        TextView text = layout.findViewById(R.id.tv_content);
        text.setText(message);
        toast.setView(layout);
        toast.show();
    }

    void showMes(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alert Dialog Title");
        builder.setMessage("This is a message to alert the user.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "You clicked OK", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCustomAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(customView);

        final EditText input = customView.findViewById(R.id.dialog_input);
        TextView title = customView.findViewById(R.id.dialog_title);
        Button okButton = customView.findViewById(R.id.dialog_button_ok);

        title.setText("Forgot password!");

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = input.getText().toString();
                if (isValidEmail(userInput)) {
                    loadFragment(new ForgotPasswordFragment());
                } else {
                    showToastCustom("Email is invalid !");
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showAlertDialogExample() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alert Dialog Title");
        builder.setMessage("This is a message to alert the user.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "You clicked OK", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}