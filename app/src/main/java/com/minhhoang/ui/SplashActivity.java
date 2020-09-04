package com.minhhoang.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minhhoang.MainActivity;
import com.minhhoang.vpn.BuildConfig;
import com.minhhoang.vpn.R;
import com.onesignal.OneSignal;

public class SplashActivity extends AppCompatActivity {

    TextView version;

    private FirebaseAuth mAuth;

    private static final String TAG = "OnlineUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        version = findViewById(R.id.version);

        mAuth = FirebaseAuth.getInstance();

        /**
         * Onesignal Initation
         */
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        /**
         * set version in splashscreen
         */
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version "+versionName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                checkCode();

                checkOnline();

                checkNative();

                checkBanner();

                checkInterstitial();

                checkVersion();

                checkPrivacy();

                checkTerm();

            }

        },5000); //3000 mean 3 second, just change up to you
    }

    //update 1.3
    /**
     * checkOnline function
     */
    public void checkOnline(){

        /**
         * to can get data users online
         */
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "success");
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "failure", task.getException());

                }

            }
        });
    }

    public void checkCode(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("code");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String code = dataSnapshot.getValue().toString();

                /**
                 * always change +1 if you update to playstore and want make user update their apps
                 * you must be change in firebase too with same value
                 */
                String check = "1";

                if(check.equals(code)){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(SplashActivity.this, UpdateActivity.class));
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();

            }
        });
    }

    public void checkNative(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("native");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String native_ads = dataSnapshot.getValue().toString();

                SharedPreferences native_id = getSharedPreferences("key1",Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = native_id.edit();
                editor.putString("ads_native", native_ads);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void checkBanner(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("banner");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String banner_ads = dataSnapshot.getValue().toString();

                SharedPreferences banner_id = getSharedPreferences("key1", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = banner_id.edit();
                editor.putString("ads_banner", banner_ads);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void checkInterstitial(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("interstitial");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String inters_ads = dataSnapshot.getValue().toString();

                SharedPreferences inters_id = getSharedPreferences("key1", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = inters_id.edit();
                editor.putString("ads_inters", inters_ads);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });

    }

    public void checkVersion(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("version");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String version = dataSnapshot.getValue().toString();

                SharedPreferences version_id = getSharedPreferences("key1", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = version_id.edit();
                editor.putString("version", version);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void checkPrivacy(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("privacy");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String privacy = dataSnapshot.getValue().toString();

                SharedPreferences version_id = getSharedPreferences("key1", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = version_id.edit();
                editor.putString("privacy", privacy);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }

    public void checkTerm(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("term");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String term = dataSnapshot.getValue().toString();

                SharedPreferences version_id = getSharedPreferences("key1", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = version_id.edit();
                editor.putString("term", term);

                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());

            }
        });
    }
}
