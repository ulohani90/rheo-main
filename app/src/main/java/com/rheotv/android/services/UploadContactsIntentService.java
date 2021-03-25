package com.rheotv.android.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.data.network.UserContactObj;
import com.rheotv.android.data.network.requestLayer.ApiService;
import com.rheotv.android.data.network.requestLayer.MyServiceInterceptor;
import com.rheotv.android.di.module.AppModule;
import com.rheotv.android.utils.CommonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadContactsIntentService extends IntentService {

    String TAG_ANDROID_CONTACTS = "Android_Contacts";

    ApiService apiService;

    public UploadContactsIntentService() {
        super("UploadContactsService");
    }

    public UploadContactsIntentService(String name) {
        super(name);
    }

    public String UPLOAD_NOTIFICATION_CHANNEL_ID = "upload contacts notification channel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(2, showNotification("Rheo is Syncing contacts", "Rheo is Syncing contacts", true));
    }

    public Notification showNotification(String title, String tickerText, boolean onGoing) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setOngoing(onGoing)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                .setTicker(tickerText)
                .setSound(null);
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = notifManager.getNotificationChannel(UPLOAD_NOTIFICATION_CHANNEL_ID);

            if (mChannel == null) {
                mChannel = new NotificationChannel(UPLOAD_NOTIFICATION_CHANNEL_ID, " Rheo Upload Contacts", NotificationManager.IMPORTANCE_HIGH);
            }
            mChannel.setSound(null, null);
            builder.setChannelId(UPLOAD_NOTIFICATION_CHANNEL_ID);
            notifManager.createNotificationChannel(mChannel);
        }
        return (builder.build());
    }

    public void notifyWithNotification(String title, String tickerText) {
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(2, showNotification(title, tickerText, false));
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        buildEventService();
        List<UserContactObj> contacts = getAllContacts();
        makeUploadContactsApiCall(contacts);
        Log.i("Contacts_fetched", contacts.size() + "");
    }

    private List<UserContactObj> getAllContacts() {
        try {

            // Get all raw contacts id list.
            List<Integer> rawContactsIdList = getRawContactsIdList();

            int contactListSize = rawContactsIdList.size();

            List<Long> contactIds = new ArrayList<>();

            ContentResolver contentResolver = getContentResolver();

            List<UserContactObj> userContactsList = new ArrayList<>();

            // Loop in the raw contacts list.
            for (int i = 0; i < contactListSize; i++) {
                // Get the raw contact id.
                Integer rawContactId = rawContactsIdList.get(i);

                Log.d(TAG_ANDROID_CONTACTS, "raw contact id : " + rawContactId.intValue());

                // Data content uri (access data table. )
                Uri dataContentUri = ContactsContract.Data.CONTENT_URI;

                // Build query columns name array.
                List<String> queryColumnList = new ArrayList<String>();

                // ContactsContract.Data.CONTACT_ID = "contact_id";
                queryColumnList.add(ContactsContract.Data.CONTACT_ID);

                // ContactsContract.Data.MIMETYPE = "mimetype";
                queryColumnList.add(ContactsContract.Data.MIMETYPE);

                queryColumnList.add(ContactsContract.Data.DATA1);
                queryColumnList.add(ContactsContract.Data.DATA2);
                queryColumnList.add(ContactsContract.Data.DATA3);
                queryColumnList.add(ContactsContract.Data.DATA4);
                queryColumnList.add(ContactsContract.Data.DATA5);
                queryColumnList.add(ContactsContract.Data.DATA6);
                queryColumnList.add(ContactsContract.Data.DATA7);
                queryColumnList.add(ContactsContract.Data.DATA8);
                queryColumnList.add(ContactsContract.Data.DATA9);
                queryColumnList.add(ContactsContract.Data.DATA10);
                queryColumnList.add(ContactsContract.Data.DATA11);
                queryColumnList.add(ContactsContract.Data.DATA12);
                queryColumnList.add(ContactsContract.Data.DATA13);
                queryColumnList.add(ContactsContract.Data.DATA14);
                queryColumnList.add(ContactsContract.Data.DATA15);

                // Translate column name list to array.
                String queryColumnArr[] = queryColumnList.toArray(new String[queryColumnList.size()]);

                // Build query condition string. Query rows by contact id.
                StringBuffer whereClauseBuf = new StringBuffer();
                whereClauseBuf.append(ContactsContract.Data.RAW_CONTACT_ID);
                whereClauseBuf.append("=");
                whereClauseBuf.append(rawContactId);

                // Query data table and return related contact data.
                Cursor cursor = contentResolver.query(dataContentUri, queryColumnArr, whereClauseBuf.toString(), null, null);

            /* If this cursor return database table row data.
               If do not check cursor.getCount() then it will throw error
               android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0.
               */
                if (cursor != null && cursor.getCount() > 0) {
                    StringBuffer lineBuf = new StringBuffer();
                    cursor.moveToFirst();

                    lineBuf.append("Raw Contact Id : ");
                    lineBuf.append(rawContactId);

                    long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    lineBuf.append(" , Contact Id : ");
                    lineBuf.append(contactId);
                    lineBuf.append(" , ");

                    List<String> numbers = new ArrayList<>();
                    if (!contactIds.contains(contactId)) {
                        contactIds.add(contactId);
                        String userContactName = null;
                        do {
                            // First get mimetype column value.

                            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                            if (mimeType.equalsIgnoreCase(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                String number = getNumberFromCursorData(cursor);
                                lineBuf.append(number);
                                lineBuf.append(" , ");
                                numbers.add(number);
                        /*List<String> dataValueList = getColumnValueByMimetype(cursor, mimeType);
                        int dataValueListSize = dataValueList.size();
                        for (int j = 0; j < dataValueListSize; j++) {
                            String dataValue = dataValueList.get(j);
                            numbers.add(dataValue);
                            //phoneNumbers.add(dataValue);
                            lineBuf.append(" , ");
                            lineBuf.append(dataValue);
                        }*/
                            } else if (mimeType.equalsIgnoreCase(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                                userContactName = getNameFromCursorData(cursor);
                                lineBuf.append(userContactName);
                                lineBuf.append(" , ");
                            }
                    /*lineBuf.append(" \r\n , MimeType : ");
                    lineBuf.append(mimeType);

                    List<String> dataValueList = getColumnValueByMimetype(cursor, mimeType);
                    int dataValueListSize = dataValueList.size();
                    for (int j = 0; j < dataValueListSize; j++) {
                        String dataValue = dataValueList.get(j);
                        phoneNumbers.add(dataValue);
                        //phoneNumbers.add(dataValue);
                        lineBuf.append(" , ");
                        lineBuf.append(dataValue);
                    }*/

                        } while (cursor.moveToNext());
                        if (numbers.size() > 0) {
                            UserContactObj contactObj = new UserContactObj(userContactName, numbers);
                            userContactsList.add(contactObj);
                        }
                        Log.d(TAG_ANDROID_CONTACTS, lineBuf.toString());
                    }
                    cursor.close();

                }

                Log.d(TAG_ANDROID_CONTACTS, "=========================================================================");
            }

            return userContactsList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public String getNumberFromCursorData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    public String getNameFromCursorData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
    }

    /*
     *  Return data column value by mimetype column value.
     *  Because for each mimetype there has not only one related value,
     *  such as Organization.CONTENT_ITEM_TYPE need return company, department, title, job description etc.
     *  So the return is a list string, each string for one column value.
     * */
    private List<String> getColumnValueByMimetype(Cursor cursor, String mimeType) {
        List<String> ret = new ArrayList<String>();

        switch (mimeType) {
            // Get email data.
            case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                // Email.ADDRESS == data1
                String emailAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                // Email.TYPE == data2
                int emailType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                String emailTypeStr = getEmailTypeString(emailType);

                ret.add("Email Address : " + emailAddress);
                ret.add("Email Int Type : " + emailType);
                ret.add("Email String Type : " + emailTypeStr);
                break;

            // Get im data.
            case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE:
                // Im.PROTOCOL == data5
                String imProtocol = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
                // Im.DATA == data1
                String imId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

                ret.add("IM Protocol : " + imProtocol);
                ret.add("IM ID : " + imId);
                break;

            // Get nickname
            case ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
                // Nickname.NAME == data1
                String nickName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                ret.add("Nick name : " + nickName);
                break;

            // Get organization data.
            case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
                // Organization.COMPANY == data1
                String company = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                // Organization.DEPARTMENT == data5
                String department = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
                // Organization.TITLE == data4
                String title = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                // Organization.JOB_DESCRIPTION == data6
                String jobDescription = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION));
                // Organization.OFFICE_LOCATION == data9
                String officeLocation = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION));

                ret.add("Company : " + company);
                ret.add("department : " + department);
                ret.add("Title : " + title);
                ret.add("Job Description : " + jobDescription);
                ret.add("Office Location : " + officeLocation);
                break;

            // Get phone number.
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                // Phone.NUMBER == data1
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                // Phone.TYPE == data2
                int phoneTypeInt = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String phoneTypeStr = getPhoneTypeString(phoneTypeInt);

                ret.add(phoneNumber);
                //ret.add("Phone Type Integer : " + phoneTypeInt);
                //ret.add("Phone Type String : " + phoneTypeStr);
                break;

            // Get sip address.
            case ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE:
                // SipAddress.SIP_ADDRESS == data1
                String address = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                // SipAddress.TYPE == data2
                int addressTypeInt = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                String addressTypeStr = getEmailTypeString(addressTypeInt);

                ret.add("Address : " + address);
                ret.add("Address Type Integer : " + addressTypeInt);
                ret.add("Address Type String : " + addressTypeStr);
                break;

            // Get display name.
            case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                // StructuredName.DISPLAY_NAME == data1
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                // StructuredName.GIVEN_NAME == data2
                String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                // StructuredName.FAMILY_NAME == data3
                String familyName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));

                ret.add(displayName);
                //ret.add("Given Name : " + givenName);
                //ret.add("Family Name : " + familyName);
                break;

            // Get postal address.
            case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
                // StructuredPostal.COUNTRY == data10
                String country = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                // StructuredPostal.CITY == data7
                String city = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                // StructuredPostal.REGION == data8
                String region = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                // StructuredPostal.STREET == data4
                String street = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                // StructuredPostal.POSTCODE == data9
                String postcode = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                // StructuredPostal.TYPE == data2
                int postType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                String postTypeStr = getEmailTypeString(postType);

                ret.add("Country : " + country);
                ret.add("City : " + city);
                ret.add("Region : " + region);
                ret.add("Street : " + street);
                ret.add("Postcode : " + postcode);
                ret.add("Post Type Integer : " + postType);
                ret.add("Post Type String : " + postTypeStr);
                break;

            // Get identity.
            case ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE:
                // Identity.IDENTITY == data1
                String identity = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.IDENTITY));
                // Identity.NAMESPACE == data2
                String namespace = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.NAMESPACE));

                ret.add("Identity : " + identity);
                ret.add("Identity Namespace : " + namespace);
                break;

            // Get photo.
            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                // Photo.PHOTO == data15
                String photo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
                // Photo.PHOTO_FILE_ID == data14
                String photoFileId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));

                ret.add("Photo : " + photo);
                ret.add("Photo File Id: " + photoFileId);
                break;

            // Get group membership.
            case ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE:
                // GroupMembership.GROUP_ROW_ID == data1
                int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                ret.add("Group ID : " + groupId);
                break;

            // Get website.
            case ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE:
                // Website.URL == data1
                String websiteUrl = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                // Website.TYPE == data2
                int websiteTypeInt = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
                String websiteTypeStr = getEmailTypeString(websiteTypeInt);

                ret.add("Website Url : " + websiteUrl);
                ret.add("Website Type Integer : " + websiteTypeInt);
                ret.add("Website Type String : " + websiteTypeStr);
                break;

            // Get note.
            case ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE:
                // Note.NOTE == data1
                String note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                ret.add("Note : " + note);
                break;

        }

        return ret;
    }

    // Return all raw_contacts _id in a list.
    private List<Integer> getRawContactsIdList() {
        List<Integer> ret = new ArrayList<Integer>();
        try {

            ContentResolver contentResolver = getContentResolver();

            // Row contacts content uri( access raw_contacts table. ).
            Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI;
            // Return _id column in contacts raw_contacts table.
            String queryColumnArr[] = {ContactsContract.RawContacts._ID};
            // Query raw_contacts table and return raw_contacts table _id.
            Cursor cursor = contentResolver.query(rawContactUri, queryColumnArr, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int idColumnIndex = cursor.getColumnIndex(ContactsContract.RawContacts._ID);
                        int rawContactsId = cursor.getInt(idColumnIndex);
                        ret.add(new Integer(rawContactsId));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        return ret;
    }

    /*
     *  Get email type related string format value.
     * */
    private String getEmailTypeString(int dataType) {
        String ret = "";

        if (ContactsContract.CommonDataKinds.Email.TYPE_HOME == dataType) {
            ret = "Home";
        } else if (ContactsContract.CommonDataKinds.Email.TYPE_WORK == dataType) {
            ret = "Work";
        }
        return ret;
    }

    /*
     *  Get phone type related string format value.
     * */
    private String getPhoneTypeString(int dataType) {
        String ret = "";

        if (ContactsContract.CommonDataKinds.Phone.TYPE_HOME == dataType) {
            ret = "Home";
        } else if (ContactsContract.CommonDataKinds.Phone.TYPE_WORK == dataType) {
            ret = "Work";
        } else if (ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE == dataType) {
            ret = "Mobile";
        }
        return ret;
    }


    private void buildEventService() {
        MyServiceInterceptor interceptor = AppModule.getServiceInterceptor(this);
        HttpLoggingInterceptor httpLoggingInterceptor = AppModule.httpLoggingInterceptor();
        Cache cache = AppModule.provideCache(this);
        OkHttpClient client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache);
        apiService = AppModule.provideApiService(client, new Gson());
    }

    private void makeUploadContactsApiCall(List<UserContactObj> phoneNumber) {
        try {

            JSONObject phoneNumberJsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray(phoneNumber);
            phoneNumberJsonObject.put("", jsonArray);
            HashMap map = new HashMap();
            map.put("phone_numbers", phoneNumber);

            apiService.uploadUserContacts(map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response != null && response.body() != null) {
                        CommonUtils.setContactsUploadSuccess(true);
                        Log.i("UploadContactsService", "Success");
                        notifyWithNotification("Rheo synced contacts successfully", "Contacts Synced Successfully");
                    } else {
                        Log.i("UploadContactsService", "Failure");
                        notifyWithNotification("Rheo could not sync contacts", "Contacts Sync failed");
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("UploadContactsService", "Failure::" + t.getLocalizedMessage());
                    notifyWithNotification("Rheo could not sync contacts", "Contacts Sync failed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}
