package com.example.joaoafonsopereira.ambiunit;

import android.app.AlertDialog;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.joaoafonsopereira.ambiunit.DatabaseConnection.DATABASE_NAME;

/**
 * Created by Asus on 14/03/2019.
 */

public class LogIn extends Fragment {

    private static final String TAG = "Log In";

    DatabaseConnection db;
    private Button login_btn;
    private EditText username;
    private EditText password;
    private TextView forgot_pass;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.login, container, false);

        db = new DatabaseConnection(LogIn.this.getActivity());
        login_btn = (Button) view.findViewById(R.id.login_btn);
        username = (EditText) view.findViewById(R.id.username_login);
        password = (EditText) view.findViewById(R.id.pass_login);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();

                Boolean valid_login = db.checkUser(user, pass);

                if(valid_login == true)
                {
                    Intent main = new Intent(getActivity(), MainActivity.class);
                    main.putExtra("username", (CharSequence) user);
                    startActivity(main);
                }
                else
                {
                    Toast.makeText(LogIn.this.getActivity(),"Login Error",Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgot_pass = (TextView) view.findViewById(R.id.forgot_pass);
        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(final View view) {

                if (username.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getContext(),
                            "Fill username first", Toast.LENGTH_LONG).show();
                }
                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Enter your account e-mail");

                    // Set up the input
                    final EditText input = new EditText(getActivity());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String email = input.getText().toString();
                            if (db.getEmail(username.getText().toString().trim()).equals(email)) {
                                Toast.makeText(getContext(),
                                        "Your password is: " + db.getPassword(username.getText().toString().trim()), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getContext(),
                                        "Wrong e-mail or username", Toast.LENGTH_LONG).show();
                            }



                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                    /* ------------------- ENVIAR MENSAGEM ---------------------------------------------


                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Enter your phone number");

                    // Set up the input
                    final EditText input = new EditText(getActivity());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            num = input.getText().toString();
                            //Getting intent and PendingIntent instance
                            Intent intent=new Intent(getContext(),MainActivity.class);
                            PendingIntent pi=PendingIntent.getActivity(getContext(), 0, intent,0);

                            //Get the SmsManager instance and call the sendTextMessage method to send message
                            SmsManager sms=SmsManager.getDefault();
                            sms.sendTextMessage(num, null, "hello javatpoint", pi,null);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                    ---------------------------------------------------------------------------------------------------- */
                }

            }

        });

        return view;

    }

}
