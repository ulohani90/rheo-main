package com.rheotv.android.ui.activities.home.view

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.network.NetworkChangeReceiver
import com.rheotv.android.data.network.models.gamify.Reward
import com.rheotv.android.databinding.ActivityHomeBinding
import com.rheotv.android.helpers.MyFirebaseMessagingService
import com.rheotv.android.ui.activities.audioroom.view.AudioRoomListFragment
import com.rheotv.android.ui.activities.home.viewmodel.HomeActivityViewModel
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import com.rheotv.android.ui.activities.profile.view.ProfileContainerFragment
import com.rheotv.android.ui.activities.story.CreateStoryActivity
import com.rheotv.android.ui.activities.tabcontainer.clips.ClipsFragment
import com.rheotv.android.ui.activities.tabcontainer.posts.PostListFragment
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.ui.fragments.*
import com.rheotv.android.utils.*
import com.rheotv.android.utils.customview.AnimatedScratchCardView
import com.rheotv.android.utils.customview.AnimatedScratchCardView.Companion.TAG
import com.rheotv.android.utils.customview.AnimatedScratchCardView.Companion.getPathInAnimator
import com.rheotv.android.utils.customview.AnimatedScratchCardView.Companion.getSlideOutAnimation
import com.rheotv.android.utils.customview.AnimatedScratchCardView.ScratchCardVisibilityListener
import com.rheotv.android.utils.keyboardCheck.ActivityKeyboardEventListener
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Runnable
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeActivityViewModel>(), HasAndroidInjector, RateUsFragment.RateUsListener {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var mViewModel: HomeActivityViewModel

    private val sharedPrefsUtils: SharedPrefsUtils = SharedPrefsUtils()
    private var mCurrentSelectedItem = R.id.navigation_feeds
    private var ratingActionPerformed = false
    private var isBackPressed = false

    private var mNetworkChangeReceiver: NetworkChangeReceiver? = null
    private var mBottomSheet: BottomSheetDialogFragment? = null
    private var mDialogFragment: DialogFragment? = null
    private var mCurrentFragment: Fragment? = null

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false, animate: Boolean = false, container: Int = R.id.fragment_container) {
        /* if(mCurrentFragment!=null){
             supportFragmentManager.beginTransaction().remove(mCurrentFragment!!).commit()
         }*/
        mCurrentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            try {
                if (addToBackStack) {
                    if (animate) {
                        transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_in_up)
                    }
                    transaction.replace(container, fragment).addToBackStack(fragment.tag)
                } else {
                    if (animate) {
                        transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_in_up)
                    }
                    transaction.replace(container, fragment)
                }
                transaction.commit()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
            transaction.show(fragment)
        }
    }

    private fun getPlayerFragment(): StreamPlayerContainerFragment =
            if (intent.hasExtra(AppConstants.ARG_POST_LIST)) {
                Log.i(TAG, "Post id " + intent.getStringExtra(AppConstants.ARG_POST_LIST))
                StreamPlayerContainerFragment.Builder().build().also { frag ->
                    frag.arguments = intent.extras
                    frag.arguments?.putString(AppConstants.GAME_ID, AppConstants.LIVE_GAME_ID)
                }
            } else {
                val builder = StreamPlayerContainerFragment.Builder().setShowTagOptions(true).addGameId(AppConstants.LIVE_GAME_ID)
                if (intent?.hasExtra("open_url") == true &&
                        intent?.getStringExtra("open_url")?.contains("post", ignoreCase = true) == true) {
                    val startDuraton = LinkHandler.getQueryParamValue(intent.getStringExtra("open_url"), AppConstants.POST_START_DURATION_KEY)?.toLong()
                    builder.addPost(LinkHandler.getPostId(intent.getStringExtra("open_url")), startDuraton
                            ?: 0)
                }
                builder.build()

            }

    private val mBottomNavigationSelectedListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            if (item.itemId != R.id.navigation_feeds)
                isBackPressed = false
            when (item.itemId) {
                R.id.navigation_feeds -> {
                    SegmentTracker.getInstance(this@HomeActivity).trackEvent(SegmentConstants.EVENT_HOME_FEED_TAB_CLICKED, HashMap(viewModel.properties))
                    return if (mCurrentSelectedItem != item.itemId) {
                        mCurrentSelectedItem = item.itemId
                        loadFragment(getPlayerFragment())
                        true
                    } else {
                        refreshPlayerPage()
                        false
                    }
                }
                R.id.navigation_search -> {
                    return if (mCurrentSelectedItem != item.itemId) {
                        mCurrentSelectedItem = item.itemId
                        if (CommonUtils.getSearchBadgeVisibilityAppUpCount() == 2 && mCurrentSelectedItem == R.id.navigation_search) {
                            viewDataBinding.bottomNavigation.getOrCreateBadge(mCurrentSelectedItem)?.isVisible = false
                        }
                        loadFragment(PostListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE))
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_community -> {
                    return if (mCurrentSelectedItem != item.itemId) {
                        mCurrentSelectedItem = item.itemId
                        viewDataBinding.bottomNavigation.getOrCreateBadge(mCurrentSelectedItem)?.isVisible = false
                        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_GROUPS_SECTION_CLICKED, hashMapOf())
                        loadFragment(AudioRoomListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE))
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_profile -> {
//                    generateNotification()
                    return if (mCurrentSelectedItem != item.itemId) {
                        mCurrentSelectedItem = item.itemId
                        loadFragment(ProfileContainerFragment.newInstance(null, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE, false))
                        true
                    } else {
                        false
                    }
                }
                R.id.navigation_go_live -> {
                    if (isNetworkConnected) {
                        openActionBottomOptions()
                    } else
                        showToast("No internet connection!")
                    return false
                }
            }
            requestedOrientation = when (mCurrentSelectedItem) {
                R.id.navigation_feeds -> ActivityInfo.SCREEN_ORIENTATION_USER
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            return false
        }
    }

    private fun setupTransparentToolbar(orientation: Int) {
        window?.apply {
            statusBarColor = Color.TRANSPARENT
//            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_home

    override fun getViewModel(): HomeActivityViewModel = mViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setupTransparentToolbar(Resources.getSystem().configuration.orientation)
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        CommonUtils.updateSearchBadgeVisibilityCount()
        setupOnClickEvents()
        setupLiveDataObservers()
        setupInitialPage()
        performStartupTasks()
        viewDataBinding.gradientView.background = ViewUtils.getRectangularDrawable(Color.parseColor("#0009192C"),
                Color.parseColor("#CC09192C"), 0)

//        if (!PermissionUtils.hasWriteStoragePermission(this)) {
//            PermissionUtils.requestWriteStoragePermission(this)
//        }
    }

    private var inActiveSessionTime: Long = 0
    override fun onStart() {
        super.onStart()
        sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.CONTAINER_IS_SECOND_LAUNCH, true)
        RewardManager.getInstance().updateNonLoggedInScratchCardShown(this)
        if (inActiveSessionTime > 0 && System.currentTimeMillis() - inActiveSessionTime > 5 * 60 * 1000) {
            refreshPlayerPage()
        }
    }

    override fun onStop() {
        super.onStop()
        inActiveSessionTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        /*viewDataBinding?.ongoingVideoCallTextView?.visibility = if (VideoChatViewActivity.isOngoingCall) {
            viewDataBinding?.ongoingVideoCallTextView?.setOnClickListener {
                startActivity(Intent(this, VideoChatViewActivity::class.java))
            }
            View.VISIBLE
        } else View.GONE*/
        loadDailyRewards()
        registerRatingHandler()
        ActivityKeyboardEventListener(this) { isOpen: Boolean ->
            if (isOpen || Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewDataBinding?.bottomNavigationGroup?.visibility = View.GONE
            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(300)
                withContext(Dispatchers.Main) {
                    if (!isOpen && Resources.getSystem().configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        viewDataBinding?.bottomNavigationGroup?.visibility = View.VISIBLE
                    }
                }
            }
        }
        while (mActionQueue.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed(mActionQueue.poll(), 3000)
        }
    }

    override fun onDestroy() {
        mNetworkChangeReceiver?.let { unregisterReceiver(it) }
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (hasOtherOpenedFragment()) {
            super.onBackPressed()
            return
        }
        if (mCurrentSelectedItem != R.id.navigation_feeds) {
            viewDataBinding.bottomNavigation.selectedItemId = R.id.navigation_feeds
            return
        }
        if (!isBackPressed && !PlayerHeadServiceHelper.getInstance().isServiceRunning) {
            (mCurrentFragment as? StreamPlayerContainerFragment)?.playerFragment?.let {

                if (resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                    return
                }

                PlayerHeadServiceHelper.getInstance().checkAndStartService(it, this,
                        (mCurrentFragment as? StreamPlayerContainerFragment)?.baseProperties, false)
                return
            }
        }
        if (ratingActionPerformed) {
            startAudioService()
            super.onBackPressed()
            return
        }
        if (!isBackPressed && !PlayerHeadServiceHelper.getInstance().isServiceRunning) {
            backPressedOnce()
        } else {
            startAudioService()
            super.onBackPressed()
        }
    }

    private fun backPressedOnce() {
//        refreshPlayerPage()
        isBackPressed = true
        showToast("Press back again to close the app!")
        CoroutineScope(Dispatchers.IO).launch {
            delay(10000)
            withContext(Dispatchers.Main) {
                if (isDestroyed || isFinishing ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return@withContext
                isBackPressed = false
            }
        }
    }

    fun startAudioService() {
        Log.i(TAG, "play_video_1: ${CommonUtils.getStreamQuality().equals("audio", ignoreCase = true)}")
        if (CommonUtils.getStreamQuality().equals("audio", ignoreCase = true)) {
            (mCurrentFragment as? StreamPlayerContainerFragment)?.playerFragment?.playerHolder?.let {
                if (!PlayerHeadServiceHelper.getInstance().startAudioService(it))
                    checkAndShowAudioModeToast()
            }
        } else {
            Log.i(TAG, "play_video_2: ${(mCurrentFragment as? StreamPlayerContainerFragment)?.playerFragment == null}")
            (mCurrentFragment as? StreamPlayerContainerFragment)?.playerFragment?.let {
                PlayerHeadServiceHelper.getInstance().checkAndRunVideoWidgetService(
                        it, this, (mCurrentFragment as? StreamPlayerContainerFragment)?.baseProperties, false
                )
            }
        }
    }

    private fun checkAndShowAudioModeToast() {
        val count = CommonUtils.getAudioToastCount()
        if (count < 3) {
            this.showToast("Rheo switched to Audio mode.\nTo change audio settings go to Profile -> Settings")
            CommonUtils.setAudioToastCount(count + 1)
        }
    }

    fun stopAudioService() {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBusModel: EventBusModel.UpdateBackPress) {
        if (isDestroyed || isFinishing ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
        if (!eventBusModel.value)
            isBackPressed = false
        else {
            backPressedOnce()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hasOtherOpenedFragment(): Boolean {
        if (mCurrentSelectedItem == R.id.navigation_profile) {
            (mCurrentFragment as? ProfileContainerFragment)
                    ?.apply {
                        if (this.isStateSaved || !this.isAdded || this.isDetached) return false
                        for (fragment in childFragmentManager.fragments) {
                            (fragment as? NavHostFragment)
                                    ?.navController()
                                    ?.also {
                                        return CommonUtils.isUserLoggedin() && it.backStack.size > 2
                                    }
                        }
                    }
        }
        return false
    }

    private val mActionQueue: Queue<Runnable> = LinkedList()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LoginFragmentBottomDialog.RC_SIGN_IN) {
            if (mBottomSheet is LoginFragmentBottomDialog) {
                mBottomSheet?.onActivityResult(requestCode, resultCode, data)
            }
        }
        if (requestCode == PlayerHeadServiceHelper.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            mActionQueue?.add(Runnable {
                (mCurrentFragment as? StreamPlayerContainerFragment)?.playerFragment?.also {
                    PlayerHeadServiceHelper.getInstance().checkPermission(it, this, false)
                }
            })
        }
    }

//    override fun onResumeFragments() {
//        super.onResumeFragments()
//        LinkHandler.triggerLink(this)
//    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewDataBinding.bottomNavigationGroup.visibility =
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
                    View.VISIBLE
                else
                    View.GONE
        setupTransparentToolbar(newConfig.orientation)
    }

    override fun onSubmitClick(rating: Int, feedback: String?) {
        ratingActionPerformed = true
        CommonUtils.markAppRated(this)
        viewModel.rateApp(rating, feedback)
    }

    private fun generateNotification() {
        val data = hashMapOf<String, String>(
                "author_username" to "Helli",
                "image_url" to "https://rheovideos.blob.core.windows.net/rheovideos/Story/6f8756f89a5f499eb950564ec84869b4.jpg",
                "body" to "Tap to watch Helli's Story",
                "type" to "story_notification",
                "title" to "Your friend Helli added new story",
                "target_url" to "https://www.rheotv.com/content/story/author/1e3813b0-e53d-4007-fd9892acce9f/",
                "story_data" to "1e3813b0-e53d-4007-fd9892acce9f"
        )
        MyFirebaseMessagingService().buildNotification(data, this)
    }

    override fun onPlayStoreClick(rating: Int, feedback: String?) {
        ratingActionPerformed = true
        CommonUtils.markAppRated(this)
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                Toast.makeText(this, "No Application found to handle this action", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.rateApp(rating, feedback)
    }

    override fun onRatingCancelClick() {
        ratingActionPerformed = true
        viewModel.rateApp(0, null)
    }

    private fun registerRatingHandler() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            withContext(Dispatchers.Main) {
                if (isFinishing || isDestroyed) return@withContext
                askForRating()
            }
        }
    }

    private fun refreshPlayerPage() {
        (mCurrentFragment as? StreamPlayerContainerFragment)?.also { it.refreshPage() }
    }

    private fun askForRating() {
        if (mBottomSheet == null) {
            if (CommonUtils.shouldRateNow(this)) {
                mDialogFragment?.dismiss()
                val fragment = RateUsFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                mDialogFragment = fragment
                if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
                fragment.show(supportFragmentManager, AppConstants.RATING_FRAGMENT_TAG)
            }
        } else {
            registerRatingHandler()
        }
    }

    private fun setupLiveDataObservers() {
        viewModel.actionsLiveData.observe(this, Observer {
            when (it ?: return@Observer) {
                Action.ForceUpdate -> showForceUpdateDialog()
                Action.Update -> showUpdateOptions()
                Action.RewardUpdate -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (CommonUtils.isSelectedUser() && CommonUtils.isNewAppUser() && !CommonUtils.getUserWelcomed())
                            delay(10 * 1000)
                        else
                            delay(2 * 1000)
                        withContext(Dispatchers.Main) { checkRewardAvailable() }
                    }
                }
            }
        })
    }

    private fun setupInitialPage() {
        loadFragment(getPlayerFragment())
    }

    private fun performStartupTasks() {
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "home_screen")
        initNetworkReceiver()
        //DeviceBandwidthSampler.getInstance().stopSampling()
        checkUpdateAppMsgState()
        if (intent != null && intent.hasExtra(AppConstants.SCREEN_SOURCE)) {
            viewModel.properties[AppConstants.SCREEN_SOURCE] = intent.getStringExtra(AppConstants.SCREEN_SOURCE)
        }
        if (CommonUtils.isFirstEventHomeViewNotTracked()) {
            CommonUtils.setFirstEventHomeViewEventTracked()
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_FIRST_HOMEVIEW, viewModel.properties)
        }
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_HOMEVIEW, viewModel.properties)
        if (NetworkUtils.isNetworkConnected(this)) {
            if (CommonUtils.isUserLoggedin()) {
                viewModel.fetchProfile(CommonUtils.getUserName())
            }
            viewModel.getAnalyticsEventsList()
        } else {
            viewDataBinding.bottomNavigation.findViewById<View?>(R.id.navigation_community)?.performClick()
        }
    }

    private fun initNetworkReceiver() {
        mNetworkChangeReceiver = NetworkChangeReceiver().also {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(it, filter)
        }
    }

    private fun openActionBottomOptions() =
            with(viewDataBinding) {
                val screenHeight = (ViewUtils.getScreenHeightInPx(this@HomeActivity) * 0.35).toInt()
                AnimatorSet().also { set ->
                    set.play(actionButton.getColorAnimator(
                            Color.parseColor("#dd352e"),
                            Color.parseColor("#32425c"),
                            500))
                            .with(actionButtonIcon.getRotateAnimator(0f, 45f, 500))
                            .with(gradientView.getExpandCollapseAnimator(0, screenHeight, 500)
                                    ?.also {
                                        it.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationStart(animation: Animator?) {
                                                gradientView.visibility = View.VISIBLE
                                            }

                                            override fun onAnimationCancel(animation: Animator?) = Unit
                                            override fun onAnimationRepeat(animation: Animator?) = Unit
                                            override fun onAnimationEnd(animation: Animator?) = Unit
                                        })
                                    })
                    set.duration = 500
                    set.start()
                }
                gradientView.visibility = View.VISIBLE
                mBottomSheet?.dismiss()
                mDialogFragment?.dismiss()
                val priority = when (mCurrentFragment) {
                    is ClipsFragment -> FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.UploadClip
                    is ProfileContainerFragment -> FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.ShareStory
                    else -> FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.GoLive
                }
                if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
                FloatingActionMenu(this@HomeActivity, priority).showPopup(this@HomeActivity, priority, {
                    when (it) {
                        FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.ShareStory.actionId -> {
                            mBottomSheet?.dismiss()
                            if (CommonUtils.isUserLoggedin())
                                CreateStoryActivity.startMe(this@HomeActivity, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                            else
                                launchLogInFragment()
                        }
                        FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.UploadClip.actionId -> {
                            mBottomSheet?.dismiss()
                            if (CommonUtils.isUserLoggedin())
                                mBottomSheet = VideoUploadFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                                        .apply { show(supportFragmentManager, VideoUploadFragment.TAG) }
                            else
                                launchLogInFragment()
                        }
                        FloatingActionMenu.FloatingActionAdapter.FloatingActionMenuIntent.GoLive.actionId -> {
                            mBottomSheet?.dismiss()
                            goLiveClicked(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                        }
                    }
                }) {
                    AnimatorSet().also { set ->
                        set.play(actionButton.getColorAnimator(
                                Color.parseColor("#32425c"),
                                Color.parseColor("#dd352e"),
                                500))
                                .with(actionButtonIcon.getRotateAnimator(45f, 0f, 500))
                                .with(gradientView.getExpandCollapseAnimator(screenHeight, 0, 200)
                                        ?.also {
                                            it.addListener(object : Animator.AnimatorListener {
                                                override fun onAnimationStart(animation: Animator?) = Unit
                                                override fun onAnimationCancel(animation: Animator?) = Unit
                                                override fun onAnimationRepeat(animation: Animator?) = Unit
                                                override fun onAnimationEnd(animation: Animator?) {
                                                    gradientView.visibility = View.GONE
                                                }
                                            })
                                        })
                        set.duration = 500
                        set.start()
                    }
                }
            }

    private fun setupOnClickEvents() {
        with(viewDataBinding) {
            bottomNavigation.setOnNavigationItemSelectedListener(mBottomNavigationSelectedListener)
            if (CommonUtils.getSearchBadgeVisibilityAppUpCount() == 2) {
                bottomNavigation.getOrCreateBadge(R.id.navigation_search)?.isVisible = true
            }
            bottomNavigation.getOrCreateBadge(R.id.navigation_community)?.also {
                it.backgroundColor = ContextCompat.getColor(this@HomeActivity, R.color.color_green_button)
                it.isVisible = true
            }
            updateStrip.setOnClickListener {
                openPlayStore()
                SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_UPDATE_AVAILABLE_CLICKED, viewModel.properties)
            }
        }
    }

    private fun loadDailyRewards() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)
                withContext(Dispatchers.Main) { viewModel.loadDailyRewards() }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showLoginStreak() {
        if (CommonUtils.isUserLoggedin()) {
            mBottomSheet?.dismiss()
            mDialogFragment?.dismiss()
            mBottomSheet = RewardProgressDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE).also {
                if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
                it.show(supportFragmentManager, AppConstants.REWARD_STREAK_FRAGMENT_TAG)
            }
        }
    }

    private fun checkUpdateAppMsgState() {
        val showUpdateMsg = intent.getBooleanExtra("show_update_msg", false)
        viewDataBinding.updateStrip.visibility = if (showUpdateMsg) View.VISIBLE else View.GONE
        if (viewDataBinding.updateStrip.visibility == View.VISIBLE) SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_UPDATE_AVAILABLE_SHOWN, viewModel.properties)
        if (intent.hasExtra("version_check") && intent.getBooleanExtra("check_version", false)) viewModel.checkVersionSupport()
    }

    private fun checkRewardAvailable() {
        if (CommonUtils.isUserLoggedin()) {
            if (RewardManager.getInstance().isLoginOrSeventhDayAvailable)
                showScratchCardNotification(AppConstants.REWARD_TYPE_DAILY_LOGIN, AppConstants.REWARD_TYPE_SEVENTH_DAY)
        }

        // removing non login reward
//        else {
//            if (!RewardManager.getInstance().isNonLoggedInScratchCardShown(this))
//                showScratchCardNotification()
//        }
    }

    private fun showForceUpdateDialog() {
        if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.force_update_app))
                .setMessage(getString(R.string.force_update_msg))
                .setPositiveButton(getString(R.string.update_text)) { dialog, _ ->
                    openPlayStore()
                    dialog?.dismiss()
                }
                .setCancelable(false)
                .show()
    }

    private fun goLiveClicked(source: String?) {
        try {
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_GO_LIVE_CLICKED, viewModel.properties)
            mBottomSheet?.dismiss()
            if (!CommonUtils.isUserLoggedin()) {
                val loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                if (loginDialogFragment.isAdded) {
                    return
                }
                loginDialogFragment.setmCallback(loginCallback)
                mBottomSheet = loginDialogFragment
                if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
                loginDialogFragment.show(this.supportFragmentManager, AppConstants.LOGIN_FRAGMENT_TAG)
                return
            }
            val args = Bundle()
            args.putString(AppConstants.SCREEN_SOURCE, source)
            val liveStreamingDialogFragment = LiveStreamingDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
            liveStreamingDialogFragment.arguments = args
            mBottomSheet = liveStreamingDialogFragment
            if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
            liveStreamingDialogFragment.show(supportFragmentManager, null)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateOptions() {
        Handler(Looper.getMainLooper()).postDelayed({ viewDataBinding.updateStrip.visibility = View.VISIBLE }, 300)
    }

    private fun showScratchCardNotification(vararg rewardTypes: String?) {
        try {
//            showLoginStreak()
            var reward: Reward? = null
            if (rewardTypes.isNotEmpty()) {
                reward = RewardManager.getInstance().getAvailableReward(rewardTypes)
            }
            val bottomScratchCardView: AnimatedScratchCardView = viewDataBinding.container.findViewWithTag(TAG)
                    ?: AnimatedScratchCardView(this)
            viewDataBinding.container.removeView(bottomScratchCardView)
            val scratchCardImage = bottomScratchCardView.setRandomScratchCard()
            bottomScratchCardView.setAction(object : View.OnClickListener {
                private var currentReward: Reward? = null
                fun setCurrentReward(currentReward: Reward?): View.OnClickListener? {
                    this.currentReward = currentReward
                    return this
                }

                override fun onClick(v: View) {
                    val scratchDialogFragment: ScratchDialogFragment?
                    if (CommonUtils.isUserLoggedin()) {
                        if (currentReward != null) {
                            scratchDialogFragment = ScratchDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE, currentReward, scratchCardImage, AppConstants.REWARD_TYPE_DAILY_LOGIN, AppConstants.REWARD_TYPE_SEVENTH_DAY)
                            if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
                            scratchDialogFragment.show(supportFragmentManager, AppConstants.SCRATCH_FRAGMENT_TAG, scratchCardListener, *rewardTypes)
                        }
                    } else {
                        launchLogInFragment(getString(R.string.new_reward_message))
                    }
                }
            }.setCurrentReward(reward))
            bottomScratchCardView.addTo(viewDataBinding.container, AppConstants.PORTRAIT_SCRATCH_CARD_BOTTOM_MARGIN,
                    AppConstants.SCRATCH_CARD_END_MARGIN, getPathInAnimator(),
                    getSlideOutAnimation(), object : ScratchCardVisibilityListener {
                private var currentReward: Reward? = null
                fun setCurrentReward(currentReward: Reward?): ScratchCardVisibilityListener? {
                    this.currentReward = currentReward
                    return this
                }

                override fun performAction() {
                    if (!CommonUtils.isUserLoggedin()) RewardManager.getInstance().updateNonLoggedInScratchCard(this@HomeActivity)
                    if (currentReward != null) viewModel.updateScratchCardStatusShown(currentReward?.id)
                }
            }.setCurrentReward(reward))
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun launchLogInFragment(rewardMessage: String? = null) {
        val loginDialogFragment = if (supportFragmentManager.findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG) != null) {
            supportFragmentManager.findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG) as LoginFragmentBottomDialog?
        } else {
            LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
        }
        if (mBottomSheet != null && (mBottomSheet?.isAdded == true || mBottomSheet?.isVisible == true)) {
            return
        }
        if (rewardMessage != null && mBottomSheet != null) {
            loginDialogFragment?.setRewardText(rewardMessage)
        }
        loginDialogFragment?.setmCallback(loginCallback)
        try {
            mBottomSheet?.dismiss()
            mBottomSheet = loginDialogFragment
            if (isDestroyed || isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isActivityTransitionRunning)) return
            loginDialogFragment?.showNoAddToBackStack(this.supportFragmentManager, AppConstants.LOGIN_FRAGMENT_TAG)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    val REQUEST_CAMERA_PERMISSION = 1;

    fun checkPermissionForCamera(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                broadcastCameraPermissionReceivedAction();
            }
        }
    }

    private fun broadcastCameraPermissionReceivedAction() {
        val intent = Intent("ACTION_CAMERA_PERMISSION")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private val loginCallback = object : LoginFragmentBottomDialog.LoginFragmentCallback {
        override fun onLoginSuccess() {
            viewModel.loadDailyRewards()
        }

        override fun onLoginDialogClose() {
        }
    }

    private val scratchCardListener =
            ScratchCardNavigator { rewardType -> viewModel.updateScratchCard(rewardType); }

    companion object {
        fun startActivity(context: Context, bundle: Bundle, flags: List<Int>) =
                context.startActivity(Intent(context, HomeActivity::class.java)
                        .also {
                            flags.forEach { flag -> it.addFlags(flag) }
                            it.putExtras(bundle)
                        })
    }

    sealed class Action {
        object ForceUpdate : Action()
        object Update : Action()
        object RewardUpdate : Action()
        object ShowLoginStreak : Action()
    }
}