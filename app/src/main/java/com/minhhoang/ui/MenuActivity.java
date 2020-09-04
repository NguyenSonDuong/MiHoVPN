package com.minhhoang.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minhhoang.MainActivity;
import com.minhhoang.vpn.BuildConfig;
import com.minhhoang.vpn.R;

public class MenuActivity extends AppCompatActivity {

    TextView menu_privacy_policy, menu_terms ,menu_about, menu_rate, version, check;

    ImageView backbutton;

    FirebaseAuth firebaseAuth;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        firebaseAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences pref1 = getSharedPreferences("key1", Context.MODE_PRIVATE);
        String privacy = pref1.getString("privacy", "");

        SharedPreferences pref2 = getSharedPreferences("key1", Context.MODE_PRIVATE);
        String term = pref2.getString("term", "");

        showNative();

        backbutton = findViewById(R.id.backbutton);
        menu_privacy_policy = findViewById(R.id.menu_privacy_policy);
        menu_terms = findViewById(R.id.menu_terms);
        menu_about = findViewById(R.id.menu_about);
        menu_rate = findViewById(R.id.menu_rate);
        version = findViewById(R.id.version);
        check = findViewById(R.id.check);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        /**
         * version
         */
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version "+versionName);

        /**
         * check latest version
         */
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refcheck = database.getReference("version");
        refcheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String version = dataSnapshot.getValue().toString();

                if(version.equals(versionName)){
                    check.setText("Latest Version");
                }else{
                    check.setText("Need Update");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        menu_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateUs();
            }
        });

        menu_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("link", privacy);

                startActivity(intent);

            }
        });

        menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("link", term);

                startActivity(intent);

            }
        });

        menu_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MenuAbout.class));
            }
        });
    }

    protected void rateUs(){
        //ur google play URL
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flag to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    private void showNative(){
        SharedPreferences pref = getSharedPreferences("key1", Context.MODE_PRIVATE);
        String id_native = pref.getString("ads_native", "");

        AdLoader adLoader = new AdLoader.Builder(MenuActivity.this, id_native)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_menu, null);
                        mapUnifiedNativeAdLayout(unifiedNativeAd, unifiedNativeAdView);

                        FrameLayout nativeAdLayout1 = findViewById(R.id.id_native_ad1);
                        nativeAdLayout1.removeAllViews();
                        nativeAdLayout1.addView(unifiedNativeAdView);
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void mapUnifiedNativeAdLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {

        myAdView.setMediaView((MediaView) myAdView.findViewById(R.id.ad_media));
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));

        if (adFromGoogle.getHeadline() == null){
            myAdView.getHeadlineView().setVisibility(View.GONE);
        }else {
            ((TextView)myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());
        }

        if (adFromGoogle.getAdvertiser() == null){
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        }else {
            ((TextView)myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);

    }
}
