/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 3:39 PM
 *
 */

package com.rheotv.android.utils;

import com.rheotv.android.BuildConfig;

public final class AppConstants {

    public static final int API_STATUS_CODE_LOCAL_ERROR = 0;

    public static final String DB_NAME = "mojotimes";

    public static final long NULL_INDEX = -1L;

    public static final String PREF_NAME = "sharedmojopref";

    public static final int STATUS_CODE_FAILED = 500;

    public static final int STATUS_CODE_SUCCESS = 200;

    public static final int FIREBASE_DYNAMIC_LINK_MINIMUM_VERSION = 395;

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    //Request Header Constants
    public static final String DEVICE_ID = "Device-id";
    public static final String AUTHORIZATION = "Authorization";
    public static final String VERSION_NAME = "Version-name";
    public static final String VERSION_CODE = "Version-code";
    public static final String CLIENT_ID = "Client-id";
    public static final String CONNECTION_QUALITY = "Connection-quality";
    public static final String NETWORK_TYPE = "Network-Type";
    public static final String CLIENT_SECRET = "Client-secret";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String APP_NAME_KEY = "App-Name";

    public static final String SELECTED_ITEM_ID = "SELECTED_ITEM_ID";
    public static final String VIDEO_PAGE_SOURCE = "VIDEO_PAGE_SOURCE";
    public static final String IS_VERSION_SUPPORTED = "IS_VERSION_SUPPORTED";
    public static final int APP_REQUEST_CODE = 991;
    public static final String AUTHOR_NAME = "AUTHOR_NAME";
    public static final String AUTHOR_PROFILE = "AUTHOR_PROFILE";
    public static final int READ_PHONE_STATE_CODE = 999;
    public static final String CHECK_READ_PHONE_STATE = "check_read_phone_state";

    public static final String FCM_TOKEN = "fcmToken";
    public static final String FCM_TOKEN_SENT = "fcmTokenSent";
    public static final String USER_NAME = "userName";
    public static final String IS_CONNECTED_ON_WHATSAPP = "isConnectedOnWhatsapp";
    public static final String APP_OPEN_COUNT = "appOpenCount";
    public static final String LAST_APP_OPENED = "lastAppOpened";
    public static final String IS_OPENED_FIRST_TIME_TODAY = "isOpenedFirstTimeToday";
    public static final String IS_APP_INSTALL_SENT = "isAppInstallSent";
    public static final String VIDEO_PLAYED_COUNT = "videoPlayedCount";
    public static final String SHOW_FEEDBACK_DIALOG = "showFeedbackDialog";
    public static final String WHATSAPP = "Whatsapp";
    public static final String WHATSAPP_CHAT_LINK = "https://wa.me/91";

    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_KEY = "categoryKey";
    public static final String VIDEO_PLAYED = "videoPlayed";
    public static final String TOTAL_MOJO_COINS = "totalMojoCoins";


    public static final String UPLOAD_URL_VIDEO = "UPLOAD_URL_VIDEO";
    public static final String VIDEO_FILE_NAME = "VIDEO_FILE_NAME";
    public static final String IS_INTRO_SHOWN = "IS_INTRO_SHOWN";
    public static final String MIME_TYPE = "mime_type";
    public static final String SHOULD_COMPRESS = "should_compress";
    public static final String STORAGE_TYPE = "storage_type";
    public static final String STORAGE_ = "storage_type";
    public static final int AZURE_STORAGE = 0;
    public static final int S3_STORAGE = 1;

    public static final String VIDEO_DIRECTORY = "/demonuts";
    public static final int RESULT_CODE = 1;
    public static final int PERMISSION_REQUEST_CODE = 200;
    public static final int READ_PERMISSION_REQUEST_CODE = 202;
    public static final int WRITE_PERMISSION_REQUEST_CODE = 204;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 201;
    public static final String BRANCH_SHARE_TYPE = "branch_share_type";
    public static final String BRANCH_SHARE_TYPE_LIVE_STREAM = "player live stream";
    public static final String BRANCH_SHARE_TYPE_PROFILE = "player profile";
    public static final String BRANCH_SHARE_TYPE_CLIP = "clip";
    public static final String BRANCH_SHARE_TYPE_STORY = "story";
    public static final String BRANCH_SHARE_TYPE_REDEEM = "redeem";

    public static final String IDENTIFIER_PROFILE_SHARE = "self profile share";
    public static final String SHARE_TITLE_PROFILE = "Join me on Rheo TV";
    public static final String SHARE_TITLE_PROFILE_OTHER = "Watch player streaming live on Rheo TV!";
    public static final String SHARE_DESCRIPTION_PROFILE_OTHER = "Hit follow button to get notified when player comes live next.";
    public static final String SHARE_DESCRIPTION_PROFILE = "Hey Mate!!!! I have started streaming live on Rheo TV. Check this out and do not forget to follow me.";
    public static final String SHARE_MODERATOR_TITLE_PROFILE = "Vote for me to become moderator on Rheo";
    public static final String SHARE_MODERATOR_DESCRIPTION_PROFILE = "Hi I want to be a moderator on Rheo. Please click on the link to vote for me on Rheo.";
    public static final String ARG_NEXT_AUTHOR_URL = "next_author_+url";
    public static final String ARG_AUTHOR_ID = "author_id";
    public static final String ARG_STORIES = "stories";
    public static final String ARG_MAX_REDEEMABLE_COIN = "max_redeemable_coin";
    public static final String ARG_RHEO_COIN_VALUE = "rheo_coin_value";
    public static final String ARG_REDEEMED_AMOUNT = "redeemed_amount";
    public static final String ARG_SHOW_TAG_OPTIONS = "show_tag_options";
    public static int GALLERY = 1;
    public static int CAM = 2;
    public static final String IMAGE = "IMAGE";
    public static final String AUDIO = "AUDIO";
    public static final String VIDEO = "VIDEO";
    public static final String GIF = "GIF";

    public static final int SHARE_SOURCE_FACEBOOK = 1;
    public static final int SHARE_SOURCE_NA = -1;

    public static final int VIEW_TYPE_FIRST = 0;
    public static final int VIEW_TYPE_NON_FIRST = 1;
    public static final String VOTE_FIRST = "VOTE_FIRST";
    public static final String VOTE_SECOND = "VOTE_SECOND";

    public static final int VIEW_TYPE_EMPTY = 1;
    public static final int VIEW_TYPE_NORMAL = 0;
    public static final int VIEW_TYPE_CAROUSEL = 2;
    public static final int VIEW_TYPE_LEADERBOARD = 5;
    public static final int VIEW_TYPE_JOB = 3;
    public static final int VIEW_TYPE_MULTI_ITEM_CARD = 4;
    public static final int VIEW_TYPE_ALERT = 6;
    public static final int VIEW_TOP_STREAMERS = 7;
    public static final int VIEW_TYPE_LOADING_FOOTER = 8;
    public static final int VIEW_TYPE_CHAT_MESSAGE = 9;
    public static final int VIEW_TYPE_CHAT_MEDIA = 11;
    public static final int VIEW_TYPE_SUPER_PRIME_STREAMER = 10;
    public static final int VIEW_TYPE_UPCOMING_STREAM = 10;
    public static final int ROLE_TYPE_POST = 0;
    public static final int ROLE_TYPE_POLL = 1;
    public static final int ROLE_TYPE_SPORTS_SCORE = 2;
    public static final int VIEW_TYPE_INVOICE_CARD = 6;
    public static final int VIEW_TYPE_CHAT_ITEM = 7;
    public static final int VIEW_TYPE_TOP_GAMES = 11;
    public static final int VIEW_TYPE_REWARD = 12;
    public static final int VIEW_TYPE_STORY = 13;
    public static final int SHIMMER_LOADER = 999;

    public static final int DISTRICT_VIEW_TYPE_POPULAR = 0;
    public static final int DISTRICT_VIEW_TYPE_NORMAL = 1;

    public static int POST_SHARE_COUNT = 0;

    // platforms
    public static final String WHATSAPP_PACKAGE = "com.whatsapp";
    public static final String WHATSAPP_PACKAGE_BUSINESS = "com.whatsapp.w4b";
    public static final String FACEBOOK_LITE_PACKAGE = "com.facebook.mlite";
    public static final String FACEBOOK_PACKAGE = "com.facebook.android";
    public static final String FACEBOOK_KATANA_PACKAGE = "com.facebook.katana";
    public static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    public static final String INSTAGRAM_STORY = "com.instagram.share.ADD_TO_STORY";
    public static final String FACEBOOK_STORY = "com.facebook.stories.ADD_TO_STORY";
    public static final String YOUTUBE_PACKAGE = "com.google.android.youtube";

    public static final int VIDEO_UPLOAD_COIN_COST = 50;
    public static final int VIDEO_SHARE_COIN_EARN = 10;
    public static final int FRIEND_INVITE_COIN_EARN = 10;

    public static final String TAG = "MIRAGE";

    public static final String SEE_ALL_TYPE = "SEE_ALL_TYPE";
    public static final String SEE_ALL_TYPE_INVOICE = "SEE_ALL_TYPE_INVOICE";
    public static final String SEE_ALL_TYPE_CHAT = "SEE_ALL_TYPE_CHAT";
    public static final String SEE_ALL_TYPE_ID = "SEE_ALL_TYPE_ID";

    public static final String LOGIN_FRAGMENT_TAG = "login_fragment_tag";
    public static final String SCRATCH_FRAGMENT_TAG = "scratch_fragment_tag";
    public static final String REWARD_STREAK_FRAGMENT_TAG = "reward_streak_fragment_tag";
    public static final String ALERT_VIDEO_REWARD_TAG = "alert video reward fragment tag";
    public static final String DOWNLOAD_VIDEO_FORM_TAG = "download video tag";
    public static final String RATING_FRAGMENT_TAG = "Rate Us Fragment";
    public static final String REQUEST_ACCEPT_DIALOG = "Request To Play Dialog Fragment";

    public static final String BRANCH_POST_SOURCE_URL = "post_source_url";
    public static final String BRANCH_PROFILE_URL_SHARE = "profile_share_url";
    public static final String BRANCH_CLIP_URL_SHARE = "clip_share_url";
    public static final String BRANCH_STORY_URL_SHARE = "story_share_url";
    public static final String BRANCH_REDEEM_URL_SHARE = "redeem_share_url";

    public static final String DOMAIN_URI_PREFIX = "https://app.rheotv.com";
    public static final String DOMAIN_URI = "https://rheotv.com";


    public static final int TYPE_HORIZONTAL_TAGS = 101;
    public static final int TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS = 102;
    public static final int TYPE_VIDEO_SNIPPETS = 103;
    public static final int TYPE_TOP_GAMES = 104;

    public static final int TYPE_SEARCH_STREAMER = 102;


    public static final String KEY_ALERT_ITEM = "alert_item";

    public static final int CONTEST_DATE_STATE_BEFORE_1_DAY = 1;
    public static final int CONTEST_DATE_STATE_WITHIN_1_DAY = 2;
    public static final int CONTEST_DATE_STATE_END = 3;
    public static final int CONTEST_DATE_STATE_LIVE = 4;

    public static final int REQUEST_CODE_EDIT_PROFILE = 200;

    // app reward constants
    public static final String REWARD_TYPE_DAILY_LOGIN = "scratch_login";
    public static final String REWARD_TYPE_TEN_MINUTE_STREAM = "video_watch";
    public static final String REWARD_TYPE_SEVENTH_DAY = "streak_login";
    public static final String REWARD_TYPE_FIRST_COMMENT = "comment_once";
    public static final String REWARD_TYPE_SHARE = "share_once";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ACTIVATED = "ACTIVATED";
    public static final String STATUS_SHOWN = "SHOWN";
    public static String TYPE_AVAILABLE = "available";
    public static String TYPE_MILESTONE = "milestone";

    public static final int MEDAL_BRONZE = 0;
    public static final int MEDAL_SILVER = 1;
    public static final int MEDAL_GOLD = 2;

    //    play request view-type constants
    public static final int PLAYABLE_VIEW_TYPE_REQUEST_NOW = 0;
    public static final int PLAYABLE_VIEW_TYPE_PLAYER = 1;
    public static final int PLAYABLE_VIEW_TYPE_STREAMER_STATE = 2;
    public static final int PLAYABLE_VIEW_TYPE_VIEWER_PENDING = 3;
    public static final int PLAYABLE_VIEW_TYPE_STREAMER_PENDING = 4;
    public static final int CUSTOM_ROOM_ACCEPTED_VIEW_TYPE = 5;

    public static final String PLAY_VIEW_TYPE_REQUEST_NOW = "play_request_now";
    public static final String CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED = "custom_room_request_accepted";
    public static final String PLAY_VIEW_TYPE_REQUESTED = "play_requested";
    public static final String PLAY_VIEW_TYPE_PENDING = "play_request_pending";


    public static String PLAY_REQUEST_ACCEPT = "accept";
    public static String PLAY_REQUEST_REJECT = "reject";
    public static String PLAY_REQUEST_REFUND = "refund";

    public static String IS_LIVE = "is_live";
    public static String IS_LITE = "is_lite";
    public static String GAME_ID = "game_id";
    public static String USER_ID = "user_id";

    public static String ARG_GLOBAL_VIDEO_REWARD_TIME = "time_remaining_to_video_reward";

    public static final String KEY_POST_ID = "post_id";

    public static final String KEY_LANDSCAPE_MIN_PLAYER_CLICK = "land_min_player_click";

    public static final int SEARCH_ITEM_TYPE_TRENDING = 107;
    public static final int SEARCH_ITEM_TYPE_STREAMER = 106;
    public static final int SEARCH_ITEM_TYPE_POST = 105;
    public static final int SEARCH_ITEM_TYPE_RECENT_SEARCHES = 108;

    public static final String ARG_REWARD_META = "arg_reward_meta";

    public static final String BRANCH_SELECTED_LANGUAGE = "selected_language";

    public static final int REQUEST_CODE_ADD_MODERATORS = 0x091;

    public static final String SCREEN_NAME = "screenName";
    public static final String SCREEN_SOURCE = "screenSource";
    public static final String IS_EMULATOR = "is_emulator";
    public static final String USER_LANGUAGE = "user_language";
    public static final String EXTRA_INFO = "extra_info";
    public static final String DIRECT_VIDEO_USER = "direct_video_user";
    public static final String IS_USER_OFFLINE = "is_user_offline";
    public static final String SOURCE = "source";

    // profile args constants
    public static final String ARG_PROFILE_AVATAR = "profile_pic";
    public static final String ARG_PROFILE_COVER_PIC = "cover_pic";
    public static final String ARG_FIRST_NAME = "first_name";
    public static final String ARG_LAST_NAME = "last_name";
    public static final String ARG_USERNAME = "username";
    public static final String ARG_IS_FOLLOW_SCREEN = "is_follow_screen";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_BIO = "bio";
    public static final String ARG_LANGUAGE = "language_objs";
    public static final String ARG_STORY_ID = "story_id";
    public static final String ARG_WATCH_COUNT = "watch_count";
    public static final String ARG_TITLE = "title";
    public static final String ARG_THUMBNAIL = "thumbnail";
    public static final String ARG_HEART_COUNT = "heart_count";
    public static final String ARG_FROM_DEEPLINK = "is_from_deeplink";
    public static final String ARG_LISTENER = "listener";
    public static final String ARG_REWARD = "reward";
    public static final String ARG_SCRATCH_CARD_IMAGE = "scratch_card_image";
    public static final String MSG_SCORE = "score_card";
    public static final String MSG_PIN = "pin_message";
    public static final String MSG_UNPIN = "unpin_message";
    public static final String MSG_DYNAMIC_PLAYER_ACTIONS = "dynamic_player_actions";
    public static final String ARG_SCORECARD_TEAMS = "scorecard_team";

    public static final String MSG_TYPE_DELETED = "deleted";
    public static final String MSG_TYPE_BLOCKED = "blocked";
    public static final String MSG_TYPE_BLOCK_FROM_AUDIO_ROOM = "exit_audio_room";
    public static final String MSG_HEART = "rheo_457_heart";
    public static final String MSG_TYPE_CUSTOM_ROOM_UPDATE = "update_custom_room";
    public static final String MSG_DYNAMIC_FEED = "dynamic_feed";
    public static final String MSG_TYPE_TEXT = "text_message";
    public static final String MSG_TYPE_AUDIO_ROOM = "audio_room";
    public static final String MSG_TYPE_IMAGE = "image";
    public static final String MSG_TYPE_VIDEO = "video";

    public static final String ARG_POST_LIST = "post_list";
    public static final String ARG_POST = "post";
    public static final String ARG_NEXT_URL = "next_url";
    public static final String ARG_POST_POSITION = "post_position";
    public static final String ARG_IS_MODERATOR = "is_moderator";
    public static final String ARG_GAME_ID = "game_id";
    public static final String ARG_LONG_START_DURATION = "start_duration";
    public static final String ARG_SHOW_LIVE = "show_live";
    public static final String ARG_LOAD_MORE = "load_more";
    public static final String LIVE_GAME_ID = "is_live";
    public static final String ARG_FOR_CUSTOM_ROOM = "is_for_custom_room";
    public static final String ARG_GAME_NAME = "game_name";
    public static final String ARG_DURATION = "duration";

    // stream event actions
    public static final String EVENT_RECENT_FOLLOWERS = "follow_list";
    public static final String EVENT_GREETING = "greeting";
    public static final String EVENT_CALL_REQUEST = "call_request";
    public static final String EVENT_PLAY_REQUEST = "play_request";
    public static final String EVENT_ANNOUNCEMENT = "announcement";
    public static final String EVENT_SHARE_MOMENT_AVAILABLE = "share_moment_available";
    public static final String EVENT_ON_POST_SHARE = "game_moment_shared";

    public static final String EVENT_REWARD_TIME = "reward_in_timer";
    public static final String EVENT_WON_REWARD = "won_reward";
    public static final String EVENT_CUSTOM_ROOM = "custom_room_username_password";
    public static final String EVENT_CUSTOM_ROOM_REJECT = "custom_room_reject";
    public static final String EVENT_CUSTOM_ROOM_REFUNDED = "custom_room_coins_refunded";
    public static final String EVENT_CUSTOM_ROOM_WINNER = "custom_room_winner";

    public static final String EVENT_FOLLOWED = "followed";
    public static final String ARG_PLAYER_HOLDER = "player_head_holder";


    public static final int MAX_STORY_LIMIT = 20;

    public static final String UPDATE_STORY_BROADCAST_FILTER = "update_story_broadcast_filter";
    public static final String TYPE_FOLLOWER = "get-followers";
    public static final String TYPE_FOLLOWING = "get-following";
    public static final String TYPE_PROFILE_VIEWERS = "get-profile-viewers";

    public static final int PORTRAIT_SCRATCH_CARD_BOTTOM_MARGIN = 64; // in Dp
    public static final int SCRATCH_CARD_END_MARGIN = 32; // in Dp
    public static final int PORTRAIT_PLAYER_SCRATCH_CARD_BOTTOM_MARGIN = 32; // in Dp
    public static final int LANDSCAPE_SCRATCH_CARD_BOTTOM_MARGIN = 60; // in Dp
    public static final int SCRATCH_CARD_VISIBILITY_DURATION = 5000; // im ms
    public static final String POLICY_LINK = "https://rheo.com/privacy-policy/";
    public static final String UGC_T_AND_C_LINK = "https://www.rheotv.com/ugc_policy";

    public static final String SHARE_TYPE_LIVE_STREAM = "player_live_share";
    public static final String DEFAULT_AVATAR = "https://storage.googleapis.com/unheard-files/common/default_gamer.png";
    public static final String DEFAULT_AVATAR_V2 = "https://rheovideos.blob.core.windows.net/rheovideos/cache/3e/fc/3efcbd6a37f3f5b206db35e55d49bfb7.png";
    public static final String DEFAULT_PROFILE_PIC = "https://rheotv.s.llnwi.net/common/default_gamer_profile.png";
    public static final String RHEO_STUDIO_PACKAGE_NAME = "com.rheostudio.android";
    public static final String FILTER_CUSTOM_ROOM = "com.rheotv.android.CUSTOM_ROOM";
    public static final String FILTER_PLAY_REQUEST = "com.rheotv.android.PLAY_REQUEST";
    public static final String FILTER_VIDEO_STATE = "com.rheotv.android.VIDEO_STATE";
    public static final String VIDEO_STATE = "video_playing";

    public static final String GET_DIRECT_VIDEO = "get-hero-carousel-card";
    public static final String ARG_NOTIFICATION_ID = "notification_id";

    public static final String ACTION_CHAT_UPDATE = "";
    public static final String GIPHY_BASE_URL = "https://api.giphy.com/v1/gifs/random?api_key=" + BuildConfig.GIPHY_API_KEY + "&rating=pg" + "&tag=";
    public static final String TENOR_BASE_URL = "https://api.tenor.com/v1/search";

    public static final String SHARE_PENDING = "pending";
    public static final String SHARE_AVAILABLE = "available";
    public static final String SHARE_DONE = "done";

    public static final String MSG_TYPE_STICKER = "STICKER";
    public static final String STICKER_TYPE_GREETING = "GREETING";

    public static final String GAME_NAME_PUBG_MOBILE = "PUBG Mobile";
    public static final String GAME_NAME_PUBG_LITE = "Pubg Lite";
    public static final String GAME_NAME_FREE_FIRE = "Free Fire";
    public static final String GAME_NAME_COD = "Call Of Duty";

    public static final int CHOOSE_IMAGE = 1000;
    public static final int CHOOSE_AUDIO = 1100;
    public static final int CHOOSE_MEDIA = 1200;
    public static final int CHOOSE_IMAGE_VIDEO = 1300;
    public static final String UPLOAD_CONTACTS_DIALOG_FRAGMENT = "UPLOAD_CONTACTS_DIALOG_FRAGMENT";
    public static final String CAN_CHAT = "can_chat";
    public static final String CHAT_CRITERIA_MESSAGE = "chat_criteria_message";
    public static final int PLAYER_MODE_NON = 0;
    public static final int PLAYER_MODE_AUDIO = 1;
    public static final int PLAYER_MODE_VIDEO = 2;
    public static final String EVENT_IMAGE_URL = "event_image_url";
    public static final String EVENT_POST_ID = "event_post_id";
    public static final String INTENT_FILTER_CLOSE_CALLING_ACTIVITY = "close_calling_activity";
    public static final String INTENT_FILTER_DENIED_CALLING_REQUEST = "denied_calling_request";
    public static final String START_TIME = "start_time";
    public static final String EVENT_REDIRECT_URL = "event_redirect_url";

    //room states
    public static final String STATUS_MUTE = "participant_muted";
    public static final String STATUS_UNMUTE = "participant_unmuted";
    public static final String STATUS_JOINED = "participant_joined";
    public static final String STATUS_LEFT = "participant_left";
    public static final String STATUS_CHATROOM_ACTIVATED = "chatroom_activated";
    public static final String STATUS_CHATROOM_ENDED = "chatroom_ended";
    public static final String STATUS_BLOCKED = "participant_blocked";
    public static final String STATUS_STREAMER_WENT_LIVE = "owner_live_streaming";
    public static final String STATUS_HIGHLIGHTED = "highlight";
    public static final String STATUS_UN_HIGHLIGHTED = "unhighlight";
    public static final String STATUS_GAME_STARTED = "game_started";
    public static final String STATUS_GAME_ENDED = "game_ended";

    // remote config
    public static final String ONBOARD_PREFERRED_LANGUAGE = "onboard_preferred_language";

    // app language
    public static final String LANG_HINDI = "hindi";

    public static final String VIDEO_CALL_STATE_REQUESTED = "1";
    public static final String VIDEO_CALL_STATE_INITIATED = "2";
    public static final String VIDEO_CALL_STATE_IN_PROGRESS = "3";
    public static final String VIDEO_CALL_STATE_ENDED = "4";
    public static final String VIDEO_CALL_STATE_DENIED = "5";
    public static final String VIDEO_CALL_STATE_REFUNDED = "6";

    public static final int PURCHASE_REQUEST_CODE = 1111;

    public static final String FEMALE_ONLY_GROUP = "35b2c788-1a58-4761-b223-8dc9d6c8efb7";

    public static final String AMONG_US_PACKAGE_NAME = "com.innersloth.spacemafia";
    public static final String AMONG_US_APP_NAME = "Among Us";

    public static final String POST_START_DURATION_KEY = "start_duration";
    public static final String ARG_IS_RELOGIN = "is_relogin";

    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}
