package com.minhhoang.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.minhhoang.vpn.R;

public class MenuAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_about);

        showNative();
    }

    private void showNative(){
        SharedPreferences pref = getSharedPreferences("key1", Context.MODE_PRIVATE);
        String id_native = pref.getString("ads_native", "");

        AdLoader adLoader = new AdLoader.Builder(MenuAbout.this, id_native)
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
