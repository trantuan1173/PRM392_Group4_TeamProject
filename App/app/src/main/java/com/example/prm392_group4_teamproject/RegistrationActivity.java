package com.example.prm392_group4_teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.model.RegisterRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class RegistrationActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Spinner genderSpinner, daySpinner, monthSpinner, yearSpinner;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        // Inputs
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        registerButton = findViewById(R.id.registerButton);

        // Gender
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Day Spinner
        Integer[] days = new Integer[31];
        for (int i = 1; i <= 31; i++) days[i - 1] = i;
        daySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days));

        // Month Spinner
        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        monthSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months));

        // Year Spinner
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Integer[] years = new Integer[83]; // từ 18 đến 100 tuổi
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 18 - i; // ví dụ: 2006 -> 1924
        }
        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years));

        // Register
        registerButton.setOnClickListener(v -> registerUser());

        // Sign In
        TextView signInLink = findViewById(R.id.signInLink);
        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString().toLowerCase();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get DOB
        int day = (Integer) daySpinner.getSelectedItem();
        int month = monthSpinner.getSelectedItemPosition(); // 0-based
        int year = (Integer) yearSpinner.getSelectedItem();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        // Format to yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dob = sdf.format(calendar.getTime());

        RegisterRequest request = new RegisterRequest(name, email, password, dob, gender);

        Retrofit retrofit = ApiClient.getClient();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.registerUser(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("API_ERROR", errorBody);
                        Toast.makeText(RegistrationActivity.this, "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegistrationActivity.this, "Failed to parse error response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
