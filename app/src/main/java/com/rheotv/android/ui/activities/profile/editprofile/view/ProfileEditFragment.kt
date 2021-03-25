package com.rheotv.android.ui.activities.profile.editprofile.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.databinding.ProfileDetailFragmentBinding
import com.rheotv.android.ui.activities.profile.model.Selectable
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.FeaturedPhotoAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.PictureAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option
import com.rheotv.android.utils.*
import com.rheotv.android.utils.AppConstants.UPLOAD_URL_VIDEO
import com.rheotv.android.utils.AppConstants.VIDEO_FILE_NAME
import com.rheotv.android.utils.segmentTracker.SegmentConstants.*
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject

class ProfileEditFragment : BaseFragment<ProfileDetailFragmentBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var featuredPhotoAdapter: FeaturedPhotoAdapter

    @Inject
    lateinit var pictureAdapter: PictureAdapter

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.profile_detail_fragment

    private var profile: ProfileResult? = null

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment ?: this,
            viewModelFactory).get(UserProfileViewModel::class.java).also {
        it.source = arguments?.getString(AppConstants.SCREEN_SOURCE)
    }

    private val uploadFileListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val progress = intent.extras?.getInt("contentData")
            Log.i(javaClass.simpleName, "onEvent_called $progress")

            if (progress == 200) {
                viewModel?.audioUrlTask(
                        intent.getStringExtra(VIDEO_FILE_NAME),
                        intent.getStringExtra(UPLOAD_URL_VIDEO),
                        UserAction.SignedUrl
                )
                requireContext().unregisterReceiver(this)
            } else if (progress in 0..100) {
                // todo update progress %
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
            photoRecyclerView.adapter = featuredPhotoAdapter.also {
                it.onAddItem = { chooseImage(CODE_FEATURED_IMAGE) }
                it.onFeaturedPhotoClick = { pos ->
                    showOptions(pos)
                    // val action =ProfileEditFragmentDirections.actionProfileEditFragmentToFeaturedPhotoFragment(pos)
                    //navController()?.navigate(action)
                }
            }

            gameRecyclerView.adapter = pictureAdapter

            addAvatarImageView.setOnClickListener {
                chooseImage(CODE_PROFILE_PICTURE)
                trackEvent(EVENT_PROFILE_PICTURE_UPDATED)
            }
            addCoverImageView.setOnClickListener {
                chooseImage(CODE_COVER_PICTURE)
                trackEvent(EVENT_PROFILE_COVER_PICTURE_UPDATED)
            }
            //audioAddButton.setOnClickListener { chooseAudio(CODE_AUDIO_MESSAGE) }

            donationTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_donationEditFragment)
                trackEvent(EVENT_PROFILE_EDIT_DONATION__CLICKED)
            }
            gameWiseUsernameTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_gameWiseUserFragment)
                trackEvent(EVENT_PROFILE_EDIT_GAME_WISE_USERNAME_CLICK)
            }
            gameTimingTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_gameTimingFragment)
                trackEvent(EVENT_PROFILE_EDIT_GAME_TIMING_CLICK)
            }
            gameRuleTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_gameRuleFragment)
                trackEvent(EVENT_PROFILE_EDIT_GAME_RULE_CLICK)
            }
            socialMediaTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_onlinePresenceFragment)
                trackEvent(EVENT_PROFILE_EDIT_ONLINE_PRESENCE_CLICK)
            }

            gameTitleTextView.setOnClickListener {
                navController()?.navigate(R.id.action_profileEditFragment_to_preferredGameFragment)
                trackEvent(EVENT_PROFILE_EDIT_PREFERRED_GAME_CLICK)
            }

            toolbar.setNavigationOnClickListener { navController()?.popBackStack() }
            baseActivity.setSupportActionBar(toolbar)
        }
    }

    fun trackEvent(event: String) {
        SegmentTracker.getInstance().recordScreenName(event, HashMap(viewModel.analyticsProperties))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            profile.get()?.profileDetail?.featuredPhotos?.let { featuredPhotoAdapter.submitList(it) }
            featuredPhoto.observe(viewLifecycleOwner, Observer {

                if (it != null) {
                    if (!it.isDelete)
                        featuredPhotoAdapter.addItem(it)
                    else
                        featuredPhotoAdapter.deleteItem(it)
                }
            })

            onSave.observe(viewLifecycleOwner, Observer {
                if (it == Status.SUCCESS) {
                    viewModelScope.launch(Dispatchers.IO) {
                        delay(1000)
                        withContext(Dispatchers.Main) {
                            navController()?.popBackStack()
                            onSave.value = null
                        }
                    }
                }
            })

            profile.get()?.languages?.let {
                viewDataBinding.languageChipGroup.addChips(it as MutableList<Selectable>, onChipClick = {
                    apiSet.add(ENDPOINT_LANGUAGE)
                })
            }

            profile.get()?.selectedGames.let { games ->
                pictureAdapter.submitList(games?.map { g -> g.thumbnail } ?: emptyList())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val params = CommonUtils.getMediaDetailFromIntent(data, context)
                if (params == null) {
                    context?.showToast("No File Found")
                    return
                }
                when (requestCode) {
                    CODE_AUDIO_MESSAGE -> {
                        // todo block ui
                        viewModel.audioUrlTask(params[1], onFileUpload = requireContext()::uploadFile)
                    }
                    else -> File(params[1]).multipartFromUri().let { viewModel.uploadFile(part = it, path = params[1], code = requestCode) }
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity()?.menuInflater?.inflate(R.menu.menu_edit_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showOptions(pos: Int) {
        BottomSheetMenuDialog.Builder()
                .add(R.menu.menu_edit_profile_featured_photo)
                .setListener { tag, option -> this.onOptionsItemClicked(tag, option, pos) }
                .show(childFragmentManager, "BottomSheetMenuDialog")
    }

    private fun onOptionsItemClicked(tag: String, option: Option, pos: Int) {
        when (option.id) {
            R.id.action_view_photo -> {
                val action = ProfileEditFragmentDirections.actionProfileEditFragmentToFeaturedPhotoFragment(pos)
                navController()?.navigate(action)
            }

            R.id.action_delete_photo -> {
                if (viewModel.photoIds.isNullOrEmpty()) return
                viewModel.deletePhoto(viewModel.photoIds[pos])
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            trackEvent(EVENT_PROFILE_EDIT_SAVED)
            activity?.let { CommonUtils.hideKeyboard(activity) }
            viewModel?.saveUserProfile()
            return true
        } else if (item.itemId == android.R.id.home) {
            navController()?.popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(uploadFileListener, IntentFilter(VideoUploadFragment.FILTER_ACTION_KEY))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(uploadFileListener)
    }

    override fun onDestroy() {
        viewModel.featuredPhoto.value = null
        super.onDestroy()

    }

    companion object {
        const val CODE_COVER_PICTURE = 0x0000
        const val CODE_PROFILE_PICTURE = 0x0001
        const val CODE_AUDIO_MESSAGE = 0x0002
        const val CODE_FEATURED_IMAGE = 0x0003

        const val ENDPOINT_COVER_PHOTO = "set-cover-photo"
        const val ENDPOINT_PROFILE_PHOTO = "set-profile-photo"
        const val ENDPOINT_FEATURED_PHOTO = "featured-photos"

        const val ENDPOINT_BIO = "set-user-bio"
        const val ENDPOINT_LANGUAGE = "set-language"
        const val ENDPOINT_USER_INFO = "set-basic-info"
        const val ENDPOINT_PHONE = "set-phone"
        const val ENDPOINT_CONFIGURATION_USED = "configuration-used"

        fun newInstance(profile: ProfileResult?, source: String): ProfileEditFragment {
            val args = Bundle()
            args.putString(AppConstants.SCREEN_SOURCE, source)
            val fragment = ProfileEditFragment()
            fragment.profile = profile
            fragment.arguments = args
            return fragment
        }
    }

}