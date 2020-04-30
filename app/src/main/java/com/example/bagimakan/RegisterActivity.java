package com.example.bagimakan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bagimakan.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputName;
    private EditText inputKontak;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputRePassword;
    private Button btnSignup;
    ;
    private TextView txtLogin;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String name, kontak, email, password, repassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputName = findViewById(R.id.inputName_signup);
        inputKontak = findViewById(R.id.inputHandphone_signup);
        inputEmail = findViewById(R.id.inputEmail_signup);
        inputPassword = findViewById(R.id.inputPassword_signup);
        inputRePassword = findViewById(R.id.inputRePassword_signup);
        btnSignup = findViewById(R.id.btnSignup);
        txtLogin = findViewById(R.id.txtLogin);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            finish();
        }

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = inputName.getText().toString();
                kontak = inputKontak.getText().toString();
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                repassword = inputRePassword.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    inputName.setError("Please enter name");
                } else if (TextUtils.isEmpty(kontak)) {
                    inputKontak.setError("Please enter no handphone");
                } else if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Please enter email");
                } else if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Please enter password");
                } else if (TextUtils.isEmpty(repassword)) {
                    inputRePassword.setError("Please confirm password");
                } else if (!password.equals(repassword)) {
                    inputPassword.setError("Password doesn't match");
                } else {
                    registerUser(name, kontak, email, password);
                }
            }
        });
    }

    private void registerUser(final String name, final String kontak, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Display Name", "Success");
                            } else {
                                Log.e("Display Name", "Error");
                            }
                        }
                    });

                    firebaseFirestore = FirebaseFirestore.getInstance();

                    User userBaru = new User(kontak);

                    firebaseFirestore.collection("users")
                            .document(user.getUid())
                            .set(userBaru);

                    Toast.makeText(RegisterActivity.this, "Registrasi berhasil", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Register Gagal", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
