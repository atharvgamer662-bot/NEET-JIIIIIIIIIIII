package com.neetjiiii.birthday;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;

public class MainActivity extends Activity {

    private WebView webView;
    private boolean exactSettingsOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(new AlarmBridge(), "AndroidAlarm");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/birthday.html");

        requestNotificationPermission();

        if ("com.neetjiiii.birthday.TEST_NOTIFICATION".equals(getIntent().getAction())) {
            scheduleTestNotification();
        }
    }

    private void scheduleTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) getSystemService(ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms()) return;
        }

        android.app.AlarmManager alarmManager =
            (android.app.AlarmManager) getSystemService(ALARM_SERVICE);

        Intent testIntent = new Intent(this, AlarmReceiver.class);

        PendingIntent testPendingIntent = PendingIntent.getBroadcast(
            this,
            2001,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60_000,
            testPendingIntent
        );
    }

    private class AlarmBridge {
        @JavascriptInterface
        public void enableAlarm() {
            runOnUiThread(() -> requestNotificationPermission());
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                101
            );
        } else {
            checkExactAlarmPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            checkExactAlarmPermission();
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) getSystemService(ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms()) {
                if (!exactSettingsOpened) {
                    exactSettingsOpened = true;
                    Intent intent = new Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())
                    );
                    startActivity(intent);
                }
                return;
            }
        }

        AlarmScheduler.scheduleNextBirthday(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            checkExactAlarmPermission();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
