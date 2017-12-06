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

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.xyzreader.R;


/**
 * Dialog related utility class
 */
@SuppressWarnings("unused")
public class Dialog {

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msgResId          Resource id of message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     * @param negativeText      Resource id of negative button text
     * @param negativeListener  Negative button listener
     * @param neutralText       Resource id of neutral button text
     * @param neutralListener   Neutral button listener
     * @return  Alert dialog
     */
    public static AlertDialog buildAlert(Context context, int titleResId, int msgResId,
                                         int positiveText, DialogInterface.OnClickListener positiveListener,
                                         int negativeText, DialogInterface.OnClickListener negativeListener,
                                         int neutralText, DialogInterface.OnClickListener neutralListener) {
        AlertDialog.Builder builder = buildAlert(context, titleResId,
                                                    positiveText, positiveListener,
                                                    negativeText, negativeListener,
                                                    neutralText, neutralListener);
        builder.setMessage(msgResId);
        // Create the AlertDialog
        return builder.create();
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     * @param negativeText      Resource id of negative button text
     * @param negativeListener  Negative button listener
     * @param neutralText       Resource id of neutral button text
     * @param neutralListener   Neutral button listener
     * @return  Alert dialog
     */
    private static AlertDialog.Builder buildAlert(Context context, int titleResId,
                                                  int positiveText, DialogInterface.OnClickListener positiveListener,
                                                  int negativeText, DialogInterface.OnClickListener negativeListener,
                                                  int neutralText, DialogInterface.OnClickListener neutralListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Add the buttons
        if (positiveText != 0) {
            builder.setPositiveButton(positiveText, positiveListener);
        }
        if (negativeText != 0) {
            builder.setNegativeButton(negativeText, negativeListener);
        }
        if (neutralText != 0) {
            builder.setNeutralButton(neutralText, neutralListener);
        }
        builder.setTitle(titleResId);
        // Create the AlertDialog
        return builder;
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msg               Message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     * @param negativeText      Resource id of negative button text
     * @param negativeListener  Negative button listener
     * @param neutralText       Resource id of neutral button text
     * @param neutralListener   Neutral button listener
     * @return  Alert dialog
     */
    public static AlertDialog buildAlert(Context context, int titleResId, String msg,
                                         int positiveText, DialogInterface.OnClickListener positiveListener,
                                         int negativeText, DialogInterface.OnClickListener negativeListener,
                                         int neutralText, DialogInterface.OnClickListener neutralListener) {
        AlertDialog.Builder builder = buildAlert(context, titleResId,
                positiveText, positiveListener,
                negativeText, negativeListener,
                neutralText, neutralListener);
        builder.setMessage(msg);
        // Create the AlertDialog
        return builder.create();
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msgResId          Resource id of message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     * @param negativeText      Resource id of negative button text
     * @param negativeListener  Negative button listener
     * @param neutralText       Resource id of neutral button text
     * @param neutralListener   Neutral button listener
     */
    public static void showAlert(Context context, int titleResId, int msgResId,
                                 int positiveText, DialogInterface.OnClickListener positiveListener,
                                 int negativeText, DialogInterface.OnClickListener negativeListener,
                                 int neutralText, DialogInterface.OnClickListener neutralListener) {
        AlertDialog dialog = buildAlert(context, titleResId, msgResId,
                                        positiveText, positiveListener, negativeText, negativeListener,
                                        neutralText, neutralListener);
        dialog.show();
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msgResId          Resource id of message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     * @param negativeText      Resource id of negative button text
     * @param negativeListener  Negative button listener
     */
    public static void showAlert(Context context, int titleResId, int msgResId,
                                 int positiveText, DialogInterface.OnClickListener positiveListener,
                                 int negativeText, DialogInterface.OnClickListener negativeListener) {
        AlertDialog dialog = buildAlert(context, titleResId, msgResId,
                                        positiveText, positiveListener, negativeText, negativeListener,
                                        0, null);
        dialog.show();
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msgResId          Resource id of message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     */
    public static void showAlert(Context context, int titleResId, int msgResId,
                                 int positiveText, DialogInterface.OnClickListener positiveListener) {
        AlertDialog dialog = buildAlert(context, titleResId, msgResId,
                                        positiveText, positiveListener, 0, null, 0, null);
        dialog.show();
    }

    /**
     * Build an AlertDialog
     * @param context           The current context
     * @param titleResId        Resource id of title
     * @param msg               Message
     * @param positiveText      Resource id of positive button text
     * @param positiveListener  Positive button listener
     */
    public static void showAlert(Context context, int titleResId, String msg,
                                 int positiveText, DialogInterface.OnClickListener positiveListener) {
        AlertDialog dialog = buildAlert(context, titleResId, msg,
                                        positiveText, positiveListener, 0, null, 0, null);
        dialog.show();
    }

    /**
     * Display a basic alert dialog
     * @param context  The current context
     * @param msgId     The resource id of the message to display
     */
    public static void showAlertDialog(Context context, int msgId) {
        showAlert(context, android.R.string.dialog_alert_title, msgId,
                android.R.string.ok, null);
    }

    /**
     * Display a basic alert dialog
     * @param context  The current context
     * @param msg       Message
     */
    public static void showAlertDialog(Context context, String msg) {
        showAlert(context, android.R.string.dialog_alert_title, msg,
                android.R.string.ok, null);
    }

    /**
     * Display a basic no response received dialog
     * @param context  The current context
     */
    public static void showNoResponseDialog(Context context) {
        showAlertDialog(context, R.string.no_response);
    }

    /**
     * Display a network unavailable dialog
     * @param context  The current context
     */
    public static void showNoNetworkDialog(Context context) {
        showAlertDialog(context, R.string.network_na);
    }

    /**
     * Display a can't contact server dialog
     * @param context  The current context
     */
    public static void showCantContactServerDialog(Context context) {
        showAlertDialog(context, R.string.cant_contact_server);
    }



}
