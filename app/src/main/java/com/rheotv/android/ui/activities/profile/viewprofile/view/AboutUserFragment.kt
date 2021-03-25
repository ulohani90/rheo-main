package com.rheotv.android.ui.activities.profile.viewprofile.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentAboutUserBinding
import com.rheotv.android.ui.activities.profile.model.SocialMedia
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.FeaturedPhotoAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameRuleAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameWiseUserAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.OnlinePresenceAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.WebviewActivity
import com.rheotv.android.utils.AppConstants.WHATSAPP
import com.rheotv.android.utils.AppConstants.WHATSAPP_CHAT_LINK
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.openLink
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

class AboutUserFragment : BaseFragment<FragmentAboutUserBinding, UserProfileViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var featuredPhotoAdapter: FeaturedPhotoAdapter

    @Inject
    lateinit var onlinePresenceAdapter: OnlinePresenceAdapter

    @Inject
    lateinit var gameWiseUserAdapter: GameWiseUserAdapter

    @Inject
    lateinit var gameRuleAdapter: GameRuleAdapter

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.fragment_about_user
    /*override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment
            ?: this, mViewModelFactory).get(UserProfileViewModel::class.java)
            .also {
                it.source = arguments?.getString(AppConstants.SCREEN_SOURCE)
            }*/

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment?.parentFragment
            ?: this, mViewModelFactory).get(UserProfileViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            socialMediaRecyclerView.adapter = onlinePresenceAdapter.also { adapter ->
                adapter.setInEdit(false)
                adapter.onItemClick = { s, _ -> openSocialMedia(s) }
            }

            val constraintSet = ConstraintSet()
            constraintSet.clone(containerAbout)
            // online presence is moved to top
            if (CommonUtils.isSelectedUser()) {
                constraintSet.connect(bioTitleTextView.id, ConstraintSet.TOP, viewDivider.id, ConstraintSet.BOTTOM)
                constraintSet.connect(onlinePresenceTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(configurationUsedTitleTextView.id, ConstraintSet.TOP, photoCountButton.id, ConstraintSet.BOTTOM)
            } else {
                // online presence is moved to center
                constraintSet.connect(bioTitleTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                constraintSet.connect(onlinePresenceTextView.id, ConstraintSet.TOP, photoCountButton.id, ConstraintSet.BOTTOM)
                constraintSet.connect(configurationUsedTitleTextView.id, ConstraintSet.TOP, viewDivider.id, ConstraintSet.BOTTOM)
            }
            constraintSet.applyTo(containerAbout)

            photoRecyclerView.adapter = featuredPhotoAdapter.also {
                it.onFeaturedPhotoClick = { pos ->
                    val args = Bundle()
                    args.putString("photoPosition", pos.toString())
                    NavHostFragment.findNavController(this@AboutUserFragment).navigate(R.id.featuredPhotoFragment, args)
                }
            }
            photoCountButton.setOnClickListener {
                val args = Bundle()
                args.putString("photoPosition", 0.toString())
                NavHostFragment.findNavController(this@AboutUserFragment).navigate(R.id.featuredPhotoFragment, args)
            }
            gameRuleRecyclerView.adapter = gameRuleAdapter
            photoRecyclerView.adapter = featuredPhotoAdapter.also { it.disableEditMode() }
            gameUserRecyclerView.adapter = gameWiseUserAdapter
            donationButton.setOnClickListener { context?.openLink(viewModel?.profile?.get()?.profileDetail?.donation?.link) }
            setReminderButton.setOnClickListener { viewModel?.onReminderClicked(WeakReference(this@AboutUserFragment)) }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            profile.get()?.profileDetail?.let { profile ->
                profile.gameUserNames?.let { gameWiseUserAdapter.submitList(it) }
                profile.socialMediaList?.let { onlinePresenceAdapter.submitList(it) }
                profile.featuredPhotos?.let { featuredPhotoAdapter.submitList(it) }
                profile.gameRules?.let { gameRuleAdapter.submitList(it) }
            }
        }
    }

    private fun openSocialMedia(socialMedia: SocialMedia) {
        when (socialMedia.text?.toLowerCase(Locale.getDefault())) {
            WHATSAPP.toLowerCase(Locale.getDefault()) -> context?.openLink("$WHATSAPP_CHAT_LINK${socialMedia.link}")
            else -> openPartnerFlow(socialMedia.link)
        }
    }

    fun openPartnerFlow(url: String?) {
        val intent = Intent(activity, WebviewActivity::class.java)
        intent.putExtra("URL", url)
        startActivity(intent)
    }

    companion object {
        fun newInstance() = AboutUserFragment()
    }

}
