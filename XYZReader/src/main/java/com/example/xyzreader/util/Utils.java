package com.example.xyzreader.util;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * This class contains miscellaneous utility functions
 */
@SuppressWarnings("unused")
public class Utils {

    /** Value for fully transparent */
    public static final float TRANSPARENT = 0f;
    /** Value for fully opaque */
    public static final float OPAQUE = 1f;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US);
    // Use default locale format
    private static DateFormat outputFormat = SimpleDateFormat.getDateTimeInstance();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Parse a date string
     * @param dateStr   Date string to parse
     * @return  Date represented by the string or current date is an error occurred
     */
    public static Date parseDate(String dateStr) {
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException ex) {
            Timber.e("%s: passing today's date", ex.getMessage());
            date = new Date();
        }
        return date;
    }

    /**
     * Return a formatted date string
     * @param date  Date to get string for
     * @return  Date string
     */
    public static String getDateString(Date date) {
        String dateStr;
        if (!date.before(START_OF_EPOCH.getTime())) {
            dateStr = DateUtils.getRelativeTimeSpanString(
                            date.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString();

        } else {
            // If date is before 1902, just show the string
            dateStr = outputFormat.format(date);
        }
        return dateStr;
    }

    /**
     * Determine which colour in the specified array is the furthest euclidean distance from the base colour
     * @param base      Base colour
     * @param array     Array of colours to test
     * @return  Colour from array with greatest euclidean distance
     */
    @ColorInt public static int getFurthestColour(@ColorInt int base, @ColorInt int[] array) {
        @ColorInt int result = base;
        if ((array != null) && (array.length > 0)) {
            double distance = 0d;
            double[] baseLab = new double[3];
            double[] testLab = new double[3];

            // convert to CIE Lab representative components
            ColorUtils.RGBToLAB(
                    Color.red(base), Color.green(base), Color.blue(base),
                    baseLab);

            for (int colour : array) {
                ColorUtils.RGBToLAB(
                        Color.red(colour), Color.green(colour), Color.blue(colour),
                        testLab);
                double testDist = ColorUtils.distanceEuclidean(baseLab, testLab);
                if (testDist > distance) {
                    distance = testDist;
                    result = colour;
                }
            }
        }
        return result;
    }

    /**
     * Determine which colour in the specified array is the furthest euclidean distance from the base colour
     * @param base      Base colour
     * @param palette   Palette to select colour from
     * @return  Colour from array with greatest euclidean distance
     */
    @ColorInt public static int getFurthestColour(@ColorInt int base, Palette palette) {
        @ColorInt int[] array = new int[] {
                palette.getMutedColor(base),
                palette.getDarkMutedColor(base),
                palette.getLightMutedColor(base),
                palette.getVibrantColor(base),
                palette.getDarkVibrantColor(base),
                palette.getLightVibrantColor(base)
        };
        return getFurthestColour(base, array);
    }

    /**
     * Get the screen metrics.
     * @param activity  The current activity
     * @return  screen metrics
     */
    public static DisplayMetrics getScreenMetrics(Activity activity) {
        return getScreenMetrics(activity.getWindowManager());
    }

    /**
     * Get the display metrics.
     * @param manager  The current window manager
     * @return  screen metrics
     */
    private static DisplayMetrics getScreenMetrics(WindowManager manager) {
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * Get the available screen size in pixels.
     * @param activity  The current activity
     * @return  screen size
     */
    public static Point getScreenSize(Activity activity) {
        DisplayMetrics metrics = getScreenMetrics(activity);
        return new Point(metrics.widthPixels, metrics.heightPixels);
    }

    /**
     * Get the available screen width in pixels.
     * @param activity  The current activity
     * @return  screen width
     */
    public static int getScreenWidth(Activity activity) {
        Point size = getScreenSize(activity);
        return size.x;
    }

    /**
     * Get the available screen height in pixels.
     * @param activity  The current activity
     * @return  screen height
     */
    public static int getScreenHeight(Activity activity) {
        Point size = getScreenSize(activity);
        return size.y;
    }

    /**
     * Get the available screen size in density-independent pixels.
     * @param activity  The current activity
     * @return  screen size
     */
    @NonNull
    public static Point getScreenDp(Activity activity) {
        DisplayMetrics metrics = getScreenMetrics(activity);
        float dpWidth = metrics.widthPixels / metrics.density;
        float dpHeight = metrics.heightPixels / metrics.density;
        return new Point(Float.valueOf(dpWidth).intValue(), Float.valueOf(dpHeight).intValue());
    }

    /**
     * Get the available screen width in density-independent pixels.
     * @param activity  The current activity
     * @return  screen width
     */
    public static int getScreenDpWidth(Activity activity) {
        Point size = getScreenDp(activity);
        return size.x;
    }

    /**
     * Get the available screen height in density-independent pixels.
     * @param activity  The current activity
     * @return  screen height
     */
    public static int getScreenDpHeight(Activity activity) {
        Point size = getScreenDp(activity);
        return size.y;
    }

    /**
     * Convert density-independent pixels to pixels
     * @param context   The current context
     * @param dp        Dp to convert
     * @return  pixel size
     */
    public static int convertDpToPixels(Context context, int dp) {
        DisplayMetrics metrics = getScreenMetrics((WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        float pixels = dp * metrics.density;
        return Float.valueOf(pixels).intValue();
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isSize(Context context, int size) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= size;
    }

    /**
     * Determine if the device has an extra-large screen, i.e. at least approximately 720x960 dp units
     * @param context   The current context
     * @return <code>true</code> if device has an extra-large screen, <code>false</code> otherwise
     */
    public static boolean isXLargeScreen(Context context) {
        return isSize(context, Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    /**
     * Determine if the device has a large screen, i.e. at least approximately 480x640 dp units
     * @param context   The current context
     * @return <code>true</code> if device has a large screen, <code>false</code> otherwise
     */
    public static boolean isLargeScreen(Context context) {
        return isSize(context, Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    /**
     * Determine if the device has a normal screen, i.e. at least approximately 320x470 dp units
     * @param context   The current context
     * @return <code>true</code> if device has a normal screen, <code>false</code> otherwise
     */
    public static boolean isNormalScreen(Context context) {
        return isSize(context, Configuration.SCREENLAYOUT_SIZE_NORMAL);
    }

    /**
     * Determine if the device has a small screen, i.e. at least approximately 320x426 dp units
     * @param context   The current context
     * @return <code>true</code> if device has a small screen, <code>false</code> otherwise
     */
    public static boolean isSmallScreen(Context context) {
        return isSize(context, Configuration.SCREENLAYOUT_SIZE_SMALL);
    }

    /**
     * Determine if the device screen is in portrait orientation
     * @param context   The current context
     * @return <code>true</code> if screen is in portrait orientation, <code>false</code> otherwise
     */
    public static boolean isPotraitScreen(Context context) {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    /**
     * Determine if the device screen width is at least the specified number of pixels
     * @param activity  The current activity
     * @param width     Width to test in density-independent pixels
     * @return <code>true</code> if screen width at least specified size, <code>false</code> otherwise
     */
    public static boolean isScreenWidth(Activity activity, int width) {
        return (getScreenDpWidth(activity) >= width);
    }

    /**
     * Determine if the device screen height is at least the specified number of pixels
     * @param activity  The current activity
     * @param height    Height to test in density-independent pixels
     * @return <code>true</code> if screen width at least specified size, <code>false</code> otherwise
     */
    public static boolean isScreenHeight(Activity activity, int height) {
        return (getScreenDpHeight(activity) >= height);
    }

}
