/*
 * Copyright (c) 2017 Ian Buttimer.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.xyzreader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.xyzreader.util.DebugTree;
import com.facebook.stetho.Stetho;

import java.lang.ref.WeakReference;

import timber.log.Timber;


/**
 * Application class
 */
@SuppressWarnings("unused")
public class XYZReaderApp extends Application {

    private static WeakReference<Context> mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        mAppContext = new WeakReference<>(context);

        String mode;
        int logLevel;
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            mode = "debug";
            logLevel = Log.DEBUG;
        } else {
            mode = "release";
            logLevel = Log.INFO;
        }

        Timber.plant(new DebugTree(logLevel));
        Timber.i("Application launched in " + mode + " mode");

    }

    /**
     * Provide a weak reference to the application context for use by non-context classes<br>
     * @return  weak context reference
     */
    public static WeakReference<Context> getWeakApplicationContext() {
        return mAppContext;
    }
}
