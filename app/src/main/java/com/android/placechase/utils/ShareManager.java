package com.android.placechase.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.placechase.R;
import com.android.placechase.database.DBOperations;
import com.android.placechase.enums.EShareType;
import com.android.placechase.model.ContactShareModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 10/23/2016.
 *
 * ShareManager is responsible for building and sending share message and data
 * to a respective client.  THe current supported clients are e-mal
 * and sms. but this can be extended to handle any client listening to the
 * "SHARE" intent
 *
 */
public class ShareManager {

    private final String TAG = ShareManager.this.getClass().getName();

    private final String mTitle;
    private final String mMessage;
    private final String mName;
    private String mPlaceName;
    private String mEmail;
    private String mPhone;
    private String mPlaceId;

    private ShareManager(ShareBuilder builder) {
        this.mTitle     = builder.mBuilderTitle;
        this.mMessage   = builder.mBuilderMessage;
        this.mName      = builder.mBuilderNameId;
        this.mPlaceName = builder.mBuilderPlaceName;
        this.mEmail     = builder.mBuilderEmail;
        this.mPhone     = builder.mBuilderPhone;
        this.mPlaceId   = builder.mBuilderPlaceId;
    }

    /**
     * Creates an share intent to send and e-mail or text message.  If both fields are
     * populated a menu is shown to chose between the two methods.  The share is also saved
     * in share DB.
     *
     * @param context context of the caller
     */
    public void shareMessage(Context context) {
        if(!mEmail.isEmpty() && !mPhone.isEmpty())
            showShareSelect(context);
        else if(!mPhone.isEmpty())
            sendMessage(context);
        else
            sendEmail(context);
    }

    private ContactShareModel getDBShareModel() {
        ContactShareModel model = new ContactShareModel();
        model.setType(EShareType.SHARE_TYPE_SEND.name());
        model.setPlaceName(mPlaceName);
        model.setPlaceId(mPlaceId);
        model.setName(mName);
        return model;
    }

    private void sendEmail(final Context context) {
        DBOperations shareDBOperation = new DBOperations(context);
        shareDBOperation.open();

        /*  Construct an email intent.  This intent is intended for email clients
            but we do not have control over te listening applications any any application can be shown
         */
        String[] TO = {mEmail};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                new StringBuilder(mMessage).append(getPlaceIntentUrl(mPlaceId, mName)).toString());
        shareDBOperation.addShareModel(getDBShareModel());
        try {
            /*There is no sure way to determine if the share was successful. We make an assumption that
            All shares are "attempts" and mark them is shared*/
            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.share_manager_send_email)));
        }
        catch (Exception ex) {
            Log.e(TAG,ex.toString());
        }
        shareDBOperation.close();
    }

    private void sendMessage(final Context context) {
        DBOperations shareDBOperation = new DBOperations(context);
        shareDBOperation.open();
        String smsBody = new StringBuilder(mMessage).append(getPlaceIntentUrl(mPlaceId, mName)).toString();
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        /*  Construct an sms intent.  This intent is intended for sms clients
            but we do not have control over te listening applications any any application can be shown
         */
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", mPhone);
        smsIntent.putExtra("sms_body",smsBody);
        shareDBOperation.addShareModel(getDBShareModel());
        try {
            context.startActivity(smsIntent);
        }
        catch (Exception ex) {
            //Some messaging apps must fall back to this method
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + Uri.encode(mPhone)));
            intent.putExtra("sms_body", smsBody);
            context.startActivity(intent);
        }
        shareDBOperation.close();
    }

    private void showShareSelect(final Context context) {
        List<String> methods = new ArrayList<>();
        methods.add(context.getString(R.string.share_manager_selector_email));
        methods.add(context.getString(R.string.share_manager_selector_text));

        final CharSequence[] methodSeq = methods.toArray(new String[methods.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(context.getString(R.string.share_manager_selector_title));
        dialogBuilder.setItems(methodSeq, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        sendEmail(context);
                        break;
                    case 1:
                        sendMessage(context);
                        break;
                }
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }

    /**
     * Creates a application intent that can open the application directly to a place
     * given a place id.  The sender name is also stored.
     *
     * @param placeId Id of google place
     * @param name name of sender
     *
     * @return The uri of the url intent
     */
    public static String getPlaceIntentUrl(String placeId, String name) {
        StringBuilder sb = new StringBuilder();
        return sb.append("\n http://placechase.com/share").
                append(addParameter("?", Constants.EXTRA_PLACE_ID, placeId)).
                append(addParameter("&", Constants.EXTRA_NAME, name)).toString();
    }

    /**
     * Creates a application intent that can open the application directly to a place
     * given a place id.  The sender name is also stored. This method adds a no share flag
     * to denot that the share should not be saved
     *
     * @param placeId Id of google place
     * @param name name of sender
     *
     * @return The uri of the url intent
     */
    public static String getPlaceIntentUrlNoShare(String placeId, String name) {
        StringBuilder sb = new StringBuilder();
        return sb.append(getPlaceIntentUrl(placeId,name)).
                append(addParameter("&", Constants.EXTRA_NO_SHARE, "true")).toString();
    }


    private static String addParameter(String prepend, String key, String value)  {
        StringBuilder sb = new StringBuilder(prepend);
        try {
            sb.append(key).append("=").append(URLEncoder.encode(value,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static class ShareBuilder {
        private final String mBuilderTitle;
        private final String mBuilderPlaceId;
        private final String mBuilderNameId;
        private String mBuilderMessage;
        private String mBuilderEmail = "";
        private String mBuilderPhone = "";
        private String mBuilderPlaceName = "";


        public ShareBuilder(Context context, String title, String placeId, String name) {
            this.mBuilderTitle   = title;
            this.mBuilderMessage = context.getString(R.string.share_message);
            this.mBuilderPlaceId = placeId;
            this.mBuilderNameId  = name;
        }

        public ShareBuilder placeName(String placeName) {
            this.mBuilderPlaceName = placeName;
            return this;
        }


        public ShareBuilder email(String email) {
            this.mBuilderEmail = email;
            return this;
        }

        public ShareBuilder phone(String phone) {
            this.mBuilderPhone = phone;
            return this;
        }

        public ShareManager build() {
            return new ShareManager(this);
        }
    }
}
