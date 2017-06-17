package qiyi.compomentprj;

import android.app.Application;
import android.content.Context;

/**
 * Created by jiangjingbo on 2017/6/17.
 */

public class AppAplication extends Application {
    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static Context getContext() {
        return sContext;
    }
}

