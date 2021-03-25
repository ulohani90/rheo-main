package com.rheotv.android.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.rheotv.android.helpers.ShareTaskHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseDynamicLinkUtils {

    public static void createDynamicLink(Context context, @Nullable String campaignInfo,
                                         String identifier, String title, String description, String imageUrl,
                                         HashMap<String, String> map, String shareUrl, boolean containsiOSParams,
                                         String packagename, boolean isLive, String authorUsername) {
        ProgressDialog progressDialog = ProgressDialog.show(context, null, "Generating short link..");

        if (shareUrl.indexOf('?') == -1)
            shareUrl = shareUrl +
                    "?utm_medium=" + identifier;
        else
            shareUrl = shareUrl +
                    "&utm_medium=" + identifier;

        if (CommonUtils.isUserLoggedin() && CommonUtils.getUserName() != null && !CommonUtils.getUserName().isEmpty())
            shareUrl = shareUrl +
                    "&utm_campaign=" + CommonUtils.getUserName();

        if (identifier != null && identifier.equals("player_live_share")) {
            if (isLive)
                shareUrl = shareUrl +
                        "&isLive=" + "true";
            else
                shareUrl = shareUrl +
                        "&isLive=" + "false";
        }

        if (authorUsername != null && !authorUsername.equals("")) {
            shareUrl = shareUrl +
                    "&author_name=" + authorUsername;
        }

        /*for (String key : map.keySet()) {
            shareUrl+="&"+key+"="+map.get(key);
        }*/

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(shareUrl))
                .setDomainUriPrefix(AppConstants.DOMAIN_URI_PREFIX)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.rheotv.android")
                                .setMinimumVersion(AppConstants.FIREBASE_DYNAMIC_LINK_MINIMUM_VERSION)
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setMedium(identifier)
                                .setCampaign(CommonUtils.getUserName())
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title)
                                .setDescription(description + " #rheoapp")
                                .setImageUrl(Uri.parse(imageUrl))
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();

                        progressDialog.dismiss();
                        if (Objects.equals(identifier, "story_share"))
                            ShareTaskHelper.getNewInstance(context).share(context, shortLink.toString(), packagename);
                        else
                            ShareTaskHelper.getNewInstance(context).share(context, shortLink.toString(), ShareTaskHelper.ShareTarget.Others);

                    } else {
                        // Error
                        // ...
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error while Generating Short Link", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void createDynamicLink(Context context, @NonNull FirebaseDynamicLinkData firebaseDynamicLinkData, ShareLinkGenerateListener shareLinkGenerateListener) {

        ProgressDialog progressDialog = ProgressDialog.show(context, null, "Generating short link..");

        String shareUrl = firebaseDynamicLinkData.shareUrl;

        if (shareUrl == null || shareUrl.isEmpty()) return;

        if (shareUrl.indexOf('?') == -1) {
            if (!shareUrl.endsWith("/")) {
                shareUrl += "/";
            }
            shareUrl = shareUrl +
                    "?utm_medium=" + firebaseDynamicLinkData.identifier;
        } else
            shareUrl = shareUrl +
                    "&utm_medium=" + firebaseDynamicLinkData.identifier;


        if (CommonUtils.isUserLoggedin() && CommonUtils.getUserName() != null && !CommonUtils.getUserName().isEmpty())
            shareUrl = shareUrl +
                    "&utm_campaign=" + CommonUtils.getUserName();

        if (firebaseDynamicLinkData.identifier != null && firebaseDynamicLinkData.identifier.equals("player_live_share")) {
            if ("true".equalsIgnoreCase(firebaseDynamicLinkData.isLive))
                shareUrl = shareUrl +
                        "&isLive=" + "true";
            else
                shareUrl = shareUrl +
                        "&isLive=" + "false";
        }

        if (firebaseDynamicLinkData.authorName != null && !firebaseDynamicLinkData.authorName.equals("")) {
            shareUrl = shareUrl +
                    "&author_name=" + firebaseDynamicLinkData.authorName;
        }
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(shareUrl))
                .setDomainUriPrefix(AppConstants.DOMAIN_URI_PREFIX)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.rheotv.android")
                                .setMinimumVersion(AppConstants.FIREBASE_DYNAMIC_LINK_MINIMUM_VERSION)
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setMedium(firebaseDynamicLinkData.identifier)
                                .setCampaign(CommonUtils.getUserName())
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(firebaseDynamicLinkData.title)
                                .setDescription(firebaseDynamicLinkData.description + " #rheoapp")
                                .setImageUrl(Uri.parse(firebaseDynamicLinkData.imageUrl))
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult() != null ? task.getResult().getShortLink() : null;
                        if (shortLink == null) {
                            if (shareLinkGenerateListener != null)
                                shareLinkGenerateListener.onLinkGenerationFailure("Error while Generating Short Link");
                            return;
                        }

                        if (shareLinkGenerateListener != null)
                            shareLinkGenerateListener.onLinkGenerationSuccess(shortLink.toString());

                    } else {
                        // Error
                        // ...
                        if (shareLinkGenerateListener != null)
                            shareLinkGenerateListener.onLinkGenerationFailure("Error while Generating Short Link");
                    }
                    progressDialog.dismiss();
                });
    }

    public static void share(Context context, @Nullable String campaignInfo, String identifier, String title, String description, String imageUrl, HashMap<String, String> map, String shareUrl, boolean containsiOSParams) {
        createDynamicLink(context, campaignInfo, identifier, title, description, imageUrl, map, shareUrl, containsiOSParams, "", false, null);
    }

    public static void share(Context context, @Nullable String campaignInfo, String identifier, String title, String description, String imageUrl, HashMap<String, String> map, String shareUrl, boolean containsiOSParams, boolean isLive, String authorUsername) {
        createDynamicLink(context, campaignInfo, identifier, title, description, imageUrl, map, shareUrl, containsiOSParams, "", isLive, authorUsername);
    }


    public static void share(Context context, @Nullable String campaignInfo, String identifier, String title, String description, String imageUrl, HashMap<String, String> map, String shareUrl, String packageName) {

        createDynamicLink(context, campaignInfo, identifier, title, description, imageUrl, map, shareUrl, true, packageName, false, null);
    }

    //for clips
    public static void share(Context context, @Nullable String campaignInfo, String identifier, String title, String description, String imageUrl, HashMap<String, String> map, String shareUrl, String packageName, String authorUsername) {

        createDynamicLink(context, campaignInfo, identifier, title, description, imageUrl, map, shareUrl, true, packageName, false, authorUsername);
    }

    public interface ShareLinkGenerateListener {
        void onLinkGenerationSuccess(String shareUrl);

        void onLinkGenerationFailure(String errorMessage);
    }

    public static void generateShareLink(Context context, FirebaseDynamicLinkData firebaseDynamicLinkData,
                                         ShareLinkGenerateListener shareLinkGenerateListener) {
        if (firebaseDynamicLinkData == null) return;

        String shareUrl = "";
        for (String key : firebaseDynamicLinkData.getMap().keySet()) {
            if (URLUtil.isValidUrl(firebaseDynamicLinkData.getMap().get(key)))
                shareUrl = firebaseDynamicLinkData.getMap().get(key);
        }
        if (shareUrl != null && shareUrl.equals("")) return;


        assert shareUrl != null;
        if (shareUrl.charAt(shareUrl.length() - 1) != '/')
            shareUrl = shareUrl + '/';

        if (firebaseDynamicLinkData.getIdentifier() == null)
            firebaseDynamicLinkData.setIdentifier("null");

        if (shareUrl.indexOf('?') == -1)
            shareUrl = shareUrl +
                    "?utm_medium=" + firebaseDynamicLinkData.getIdentifier();
        else
            shareUrl = shareUrl +
                    "&utm_medium=" + firebaseDynamicLinkData.getIdentifier();

        if (CommonUtils.isUserLoggedin() && CommonUtils.getUserName() != null && !CommonUtils.getUserName().equals(""))
            shareUrl = shareUrl +
                    "&utm_campaign=" + CommonUtils.getUserName();

        if (firebaseDynamicLinkData.getIdentifier() != null && firebaseDynamicLinkData.getIdentifier().equals("player_live_share")) {
            if (firebaseDynamicLinkData.getIsLive() != null && firebaseDynamicLinkData.getIsLive().equals("true"))
                shareUrl = shareUrl +
                        "&isLive=" + "true";
            else
                shareUrl = shareUrl +
                        "&isLive=" + "false";
        }

        if (firebaseDynamicLinkData.getAuthorName() != null && !firebaseDynamicLinkData.getAuthorName().equals("")) {
            shareUrl = shareUrl +
                    "&author_name=" + firebaseDynamicLinkData.getAuthorName();
        }

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(shareUrl))
                .setDomainUriPrefix(AppConstants.DOMAIN_URI_PREFIX)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.rheotv.android")
                                .setMinimumVersion(AppConstants.FIREBASE_DYNAMIC_LINK_MINIMUM_VERSION)
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setMedium(firebaseDynamicLinkData.getIdentifier())
                                .setCampaign(CommonUtils.getUserName())
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(firebaseDynamicLinkData.getTitle())
                                .setDescription(firebaseDynamicLinkData.getDescription() + " #rheoapp")
                                .setImageUrl(Uri.parse(firebaseDynamicLinkData.getImageUrl()))
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();


                            shareLinkGenerateListener.onLinkGenerationSuccess(shortLink.toString());

                            // ShareTaskHelper.getNewInstance(context).share(context, shortLink.toString(), ShareTaskHelper.ShareTarget.Others);

                            Uri flowchartLink = task.getResult().getPreviewLink();
                        } else {
                            // Error

                            // ...

                            Toast.makeText(context, "Error while Generating Short Link", Toast.LENGTH_SHORT).show();
                            shareLinkGenerateListener.onLinkGenerationFailure("Error while Generating Short Link");

                        }
                    }
                });
    }

    public static void shareToExternal(Context context, FirebaseDynamicLinkData firebaseDynamicLinkData, ShareLinkGenerateListener shareLinkGenerateListener) {

        if (firebaseDynamicLinkData == null) return;
        // ProgressDialog progressDialog = ProgressDialog.show(context, null, "Generating short link..");

        generateShareLink(context, firebaseDynamicLinkData, shareLinkGenerateListener);
    }

    public static class FirebaseDynamicLinkData {
        private String campaignInfo;
        private String identifier;
        private String title;
        private String description;
        private String imageUrl;
        private Map<String, String> map;
        private String packageName;
        private String shareUrl;
        private boolean containsIosParams;
        private String authorName;
        private String isLive;

        public FirebaseDynamicLinkData(String campaignInfo, String identifier, String title, String description,
                                       String imageUrl, Map<String, String> map, String packageName, String shareUrl,
                                       boolean containsIosParams, String authorName, String isLive) {
            this.campaignInfo = campaignInfo;
            this.identifier = identifier;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.map = map;
            this.packageName = packageName;
            this.shareUrl = shareUrl;
            this.containsIosParams = containsIosParams;
            this.authorName = authorName;
            this.isLive = isLive;
        }

        public FirebaseDynamicLinkData() {
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getCampaignInfo() {
            return campaignInfo;
        }

        public void setCampaignInfo(String campaignInfo) {
            this.campaignInfo = campaignInfo;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getShareUrl() {
            return shareUrl;
        }

        public void setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
        }

        public boolean isContainsIosParams() {
            return containsIosParams;
        }

        public String getIsLive() {
            return isLive;
        }

        public void setIsLive(String isLive) {
            this.isLive = isLive;
        }

        public void setContainsIosParams(boolean containsIosParams) {
            this.containsIosParams = containsIosParams;
        }
    }
}
