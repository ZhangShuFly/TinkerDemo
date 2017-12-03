package com.ilyzs.tinkerdemo.base;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.ilyzs.tinkerdemo.util.BaseApplicationContext;
import com.ilyzs.tinkerdemo.util.TinkerManager;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by zhangshu on 2017/11/30.
 */

@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.ilyzs.tinkerdemo.BaseApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class BaseApplicationLike extends DefaultApplicationLike {
    public BaseApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    /**
     * install multiDex before install tinker
     * so we don't need to put the tinker lib classes in the main dex
     *
     * @param base
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        BaseApplicationContext.application = getApplication();
        BaseApplicationContext.context = getApplication();

        MultiDex.install(base);
        TinkerManager.setTinkerApplicationLike(this);
        TinkerManager.setUpgradeRetryEnable(true);
        TinkerManager.sampleInstallTinker(this);
        Tinker tinker = Tinker.with(getApplication());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }
}
