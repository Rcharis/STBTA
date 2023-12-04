package com.ta.stb_03;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SecurityPopup {
    private Context context;
    private Dialog dialog;
    private EditText passwordEditText;
    private Button submitButton, cancelButton;
    private OnPasswordEnteredListener passwordEnteredListener;

    public SecurityPopup(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.security_popup);

        passwordEditText = dialog.findViewById(R.id.passwordEditText);
        submitButton = dialog.findViewById(R.id.submitButton);
        cancelButton = dialog.findViewById(R.id.cancelButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredPassword = passwordEditText.getText().toString();
                if (enteredPassword.equals("cobatanyayangbuat")) {
                    if (passwordEnteredListener != null) {
                        passwordEnteredListener.onPasswordEntered();
                    }
                } else {
                    // Password is incorrect, show a Toast message
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setOnPasswordEnteredListener(OnPasswordEnteredListener listener) {
        this.passwordEnteredListener = listener;
    }

    private boolean isDialogOpen = false;

    public boolean isDialogOpen() {
        return isDialogOpen;
    }

    public void show() {
        dialog.show();
        isDialogOpen = true;
    }

    public void dismiss() {
        dialog.dismiss();
        isDialogOpen = false;
    }

    public interface OnPasswordEnteredListener {
        void onPasswordEntered();
    }
}
