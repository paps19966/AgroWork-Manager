package gr.agro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.aviran.cookiebar2.CookieBar;

public class LoginActivity extends AppCompatActivity {

    String adminCredentials = "admin@admin.com";
    EditText usernameEditTxt;
    EditText passwordEditTxt;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setTitle("Είσοδος στο " + getString(R.string.app_name));
        usernameEditTxt = findViewById(R.id.username);
        passwordEditTxt = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        //debug(adminCredentials);
    }


    private void debug(String userpass){
        usernameEditTxt.setText(userpass);
        passwordEditTxt.setText(userpass);
    }


    public void signClick(View view){
        if ( !Global.checkEditTexts(new EditText[]{usernameEditTxt, passwordEditTxt}) ){
            CookieBar.build(this)
            .setMessage(getString(R.string.fields_missing_error))
            .setBackgroundColor(R.color.red)
            .setCookiePosition(CookieBar.BOTTOM).show();
            return;
        }
        tryLogin();
    }

    private void tryLogin(){
        mAuth.signInWithEmailAndPassword(usernameEditTxt.getText().toString(), passwordEditTxt.getText().toString())
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.w(Global.TAG, task.getException());
                CookieBar.build(LoginActivity.this)
                .setMessage(getString(R.string.auth_fail))
                .setBackgroundColor(R.color.red)
                .setCookiePosition(CookieBar.BOTTOM).show();
            }
        });
    }

}