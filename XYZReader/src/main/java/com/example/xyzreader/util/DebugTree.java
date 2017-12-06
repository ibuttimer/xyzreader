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

package com.example.xyzreader.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import timber.log.Timber;

/**
 * DebugTree implementation for Timber library<br>
 * <br>
 * The default log level is INFO in android {@link android.util.Log#isLoggable(String,int)}<br>
 * <br>
 * On some physical devices setting the system property to levels below INFO using<br>
 * <ul>
 *     <li>adb shell</li>
 *     <li>setprop log.tag.'your tag' 'level'</li>
 * </ul>
 * doesn't seem to work.<br>
 * <br>
 * This DebugTree logs lower level messages at INFO level if necessary.
 */

public class DebugTree extends Timber.DebugTree {

    private int mLogLevel;          // min log level
    private boolean mRaiseLevel;    // raise level flag

    /**
     * Constructor
     * @param logLevel  Minimum log level; one of Log.INFO etc.
     */
    public DebugTree(int logLevel) {
        switch (logLevel) {
            case Log.VERBOSE:
            case Log.DEBUG:
            case Log.INFO:
            case Log.WARN:
            case Log.ERROR:
            case Log.ASSERT:
                this.mLogLevel = logLevel;
                break;
            default:
                this.mLogLevel = Log.INFO;
                break;
        }

        mRaiseLevel = false;
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            // raise level on all physical devices, but not for the emulator
            mRaiseLevel = !manufacturer.toLowerCase().contains("unknown");
        }
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return (mLogLevel <= priority);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (mRaiseLevel) {
            switch (priority) {
                case Log.VERBOSE:
                case Log.DEBUG:
                    priority = Log.INFO;
                    break;
            }
        }
        super.log(priority, tag, message, t);
    }
}
