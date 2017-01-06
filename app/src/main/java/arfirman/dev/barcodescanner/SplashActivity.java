package arfirman.dev.barcodescanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by alodokter-it on 06/01/17.
 */

public class SplashActivity extends AppCompatActivity {
    private Handler handlerSplash;
    private Runnable runSplash;
    private long timeOutSplash = 3000;
    private Context myContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        doingSplash();
    }

    private void doingSplash() {
        handlerSplash = new Handler();
        runSplash = new Runnable() {
            @Override
            public void run() {
                goToMain();
            }
        };
        handlerSplash.postDelayed(runSplash, timeOutSplash);
    }

    private void goToMain() {
        MainActivity.startIntent(myContext);
    }
}
