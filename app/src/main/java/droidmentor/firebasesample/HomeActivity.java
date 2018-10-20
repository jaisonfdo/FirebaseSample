package droidmentor.firebasesample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private static final String FB_RC_KEY_TITLE="update_title";
    private static final String FB_RC_KEY_DESCRIPTION="update_description";
    private static final String FB_RC_KEY_FORCE_UPDATE_VERSION="force_update_version";
    private static final String FB_RC_KEY_LATEST_VERSION="latest_version";
    String TAG = "HomeActivity";

    AppUpdateDialog appUpdateDialog;

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkAppUpdate();
    }

    public void checkAppUpdate() {

        final int versionCode = BuildConfig.VERSION_CODE;

        final HashMap<String, Object> defaultMap = new HashMap<>();
        defaultMap.put(FB_RC_KEY_TITLE, "Update Available");
        defaultMap.put(FB_RC_KEY_DESCRIPTION, "A new version of the application is available please click below to update the latest version.");
        defaultMap.put(FB_RC_KEY_FORCE_UPDATE_VERSION, ""+versionCode);
        defaultMap.put(FB_RC_KEY_LATEST_VERSION, ""+versionCode);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mFirebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build());

        mFirebaseRemoteConfig.setDefaults(defaultMap);

        Task<Void> fetchTask=mFirebaseRemoteConfig.fetch(BuildConfig.DEBUG?0: TimeUnit.HOURS.toSeconds(4));

        fetchTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // After config data is successfully fetched, it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();

                    String title=getValue(FB_RC_KEY_TITLE,defaultMap);
                    String description=getValue(FB_RC_KEY_DESCRIPTION,defaultMap);
                    int forceUpdateVersion= Integer.parseInt(getValue(FB_RC_KEY_FORCE_UPDATE_VERSION,defaultMap));
                    int latestAppVersion= Integer.parseInt(getValue(FB_RC_KEY_LATEST_VERSION,defaultMap));

                    boolean isCancelable=true;

                    if(latestAppVersion>versionCode)
                    {
                        if(forceUpdateVersion>versionCode)
                           isCancelable=false;

                        appUpdateDialog = new AppUpdateDialog(HomeActivity.this, title, description, isCancelable);
                        appUpdateDialog.setCancelable(false);
                        appUpdateDialog.show();

                        Window window = appUpdateDialog.getWindow();
                        assert window != null;
                        window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

                    }

                } else {
                    Toast.makeText(HomeActivity.this, "Fetch Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String getValue(String parameterKey,HashMap<String, Object> defaultMap)
    {
        String value=mFirebaseRemoteConfig.getString(parameterKey);
        if(TextUtils.isEmpty(value))
            value= (String) defaultMap.get(parameterKey);

        return value;
    }
}
