package com.example.joaoafonsopereira.ambiunit;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * Created by Asus on 14/03/2019.
 */

public class SignUp extends Fragment {
    private static final String TAG = "Sign Up";

    private DatabaseConnection db;
    private Button signup_btn;
    private EditText name;
    private EditText email;
    private EditText username;
    private EditText password;
    private EditText confirm_password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);

        db = new DatabaseConnection(SignUp.this.getActivity());
        signup_btn = (Button) view.findViewById(R.id.signup_btn);
        name = (EditText) view.findViewById(R.id.name_signup);
        email = (EditText) view.findViewById(R.id.email_signup);
        username = (EditText) view.findViewById(R.id.username_signup);
        password = (EditText) view.findViewById(R.id.pass_signup);
        confirm_password = (EditText) view.findViewById(R.id.pass_conf_signup);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString().trim();
                String e = email.getText().toString().trim();
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String conf_pass = confirm_password.getText().toString().trim();

                if (n.isEmpty() || e.isEmpty() || user.isEmpty() || pass.isEmpty() || conf_pass.isEmpty()) {
                    Toast.makeText(SignUp.this.getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (db.usernameIsValid(user)) {

                        if (pass.equals(conf_pass)) {
                            long val = db.addUser(user, pass, n, e);
                            if (val > 0) {
                                Toast.makeText(SignUp.this.getActivity(), "Success!", Toast.LENGTH_LONG).show();
                                Intent main = new Intent(getActivity(), MainActivity.class);
                                main.putExtra("username", (CharSequence) user);
                                startActivity(main);
                            } else {
                                Toast.makeText(SignUp.this.getActivity(), "Registration Error", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(SignUp.this.getActivity(), "Password is not matching", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUp.this.getActivity(), "Invalid username", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        return view;
    }
}
