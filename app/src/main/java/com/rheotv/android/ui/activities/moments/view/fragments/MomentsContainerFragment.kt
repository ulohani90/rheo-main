package com.rheotv.android.ui.activities.moments.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.databinding.FragmentMomentsContainerBinding
import com.rheotv.android.ui.activities.moments.adapter.MomentsPagerAdapter
import com.rheotv.android.ui.activities.moments.viewmodel.MomentsContainerViewModel
import com.rheotv.android.ui.activities.player.activity.ViewPagerMediator
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.EventBusModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject

class MomentsContainerFragment : BaseFragment<FragmentMomentsContainerBinding, MomentsContainerViewModel>() {

    @Inject
    lateinit var mViewModel: MomentsContainerViewModel
    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_moments_container

    override fun getViewModel(): MomentsContainerViewModel = mViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding?.viewPager?.adapter = MomentsPagerAdapter("Moments", childFragmentManager, lifecycle)
        viewDataBinding?.viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private var mLastSelection = -1
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewDataBinding?.viewPager?.apply {
                    if (adapter?.itemCount != 0 && currentItem >= (adapter?.itemCount
                                    ?: return@apply) - 4 && mViewModel.mNextUrl != null && mViewModel.loading.value == false) {

                        mViewModel.fetchMoments(false)
                    }
                }
                if (mLastSelection != -1)
                    EventBus.getDefault().post(EventBusModel.RemoveMomentsView)
                mLastSelection = position
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(mViewModel) {
            loading.observe(viewLifecycleOwner, {
                viewDataBinding?.loader?.visibility = if (it ?: return@observe)
                    View.VISIBLE else View.GONE
            })
            momentListData.observe(viewLifecycleOwner, {
                (viewDataBinding?.viewPager?.adapter as? MomentsPagerAdapter)
                        ?.addListItem(it ?: return@observe)
                if (isContentModerator()) {
                    viewDataBinding.searchAutocomplete.visibility = View.VISIBLE
                    viewDataBinding.backButton.visibility = View.VISIBLE
                    viewDataBinding.clearSearch.visibility = View.VISIBLE
                } else {
                    viewDataBinding.searchAutocomplete.visibility = View.GONE
                    viewDataBinding.backButton.visibility = View.GONE
                    viewDataBinding.clearSearch.visibility = View.GONE
                }
            })
            viewDataBinding.searchAutocomplete.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        hideKeyboard()
                        authorUsername = viewDataBinding.searchAutocomplete.text.toString()
                        (viewDataBinding?.viewPager?.adapter as? MomentsPagerAdapter)?.clearAllData()
                        fetchMoments(true)
                    }
                    return true
                }

            })
            viewDataBinding.clearSearch.setOnClickListener {
                mViewModel.authorUsername = null;
                (viewDataBinding?.viewPager?.adapter as? MomentsPagerAdapter)?.clearAllData()
                fetchMoments(true)
            }
            viewDataBinding.backButton.setOnClickListener {
                activity?.onBackPressed()
            }
            fetchMoments(true)
        }
    }

    fun getCurrentFragmentData(): PostObject? {
        return (viewDataBinding?.viewPager?.adapter as? MomentsPagerAdapter)?.getItem(0)?.postObject
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ViewPagerMediator.PageChange) {
        viewDataBinding?.apply {
            if (viewPager.adapter?.itemCount ?: return > (viewPager.currentItem + 1)) {
                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            }
        }

    }

    companion object {
        const val TAG = "MomentsContainerFragment"

        class Builder {
            private val mContext: Context? = null
            private var mPostList: ArrayList<PostObject>? = null
            private var mPostIndex = -1
            private var mPaginationUrl: String? = null
            private var mSourceName: String? = null
            private var mLoadMore = true
            private var mGameId: String? = null
            private var mIsFromDeepLink = false
            private var isForCustomRoom = false
            private var shouldShowTagOptions = false

            fun addPostList(postList: ArrayList<PostObject>): Builder {
                if (!mPostList.isNullOrEmpty()) {
                    while (mPostList?.iterator()?.hasNext() == true) {
                        val item = mPostList?.iterator()?.next()
                        for (newItem in postList) {
                            if (item?.id?.equals(newItem.id, ignoreCase = true) == true) {
                                mPostList?.remove(item)
                            }
                        }
                    }
                    mPostList?.addAll(postList)
                    return this
                }
                mPostList = postList
                return this
            }

            fun addPost(postObject: PostObject): Builder {
                if (mPostList == null) {
                    mPostList = ArrayList()
                }
                if ((mPostList?.indexOf(postObject) ?: 0) < 0) {
                    mPostList?.add(postObject)
                }
                mPostIndex = mPostList?.indexOf(postObject) ?: 0
                return this
            }

            fun addPost(postId: String?): Builder {
                if (postId.isNullOrEmpty()) {
                    return this
                }
                addPost(PostObject(postId))
                return this
            }

            fun addPaginationUrl(paginationUrl: String?): Builder {
                mPaginationUrl = paginationUrl
                if (mPaginationUrl != null) {
                    mLoadMore = true
                }
                return this
            }

            fun addSourceScreenName(sourceName: String?): Builder {
                mSourceName = sourceName
                return this
            }

            fun addLoadMore(loadMore: Boolean): Builder {
                mLoadMore = loadMore
                return this
            }

            fun addGameId(gameId: String?): Builder {
                mGameId = gameId
                return this
            }

            fun addFromDeepLink(isFromDeepLink: Boolean): Builder {
                mIsFromDeepLink = isFromDeepLink
                return this
            }

            fun setForCustomRoom(isForCustomRoom: Boolean): Builder {
                this.isForCustomRoom = isForCustomRoom
                return this
            }

            fun setShowTagOptions(shouldShowTagOptions: Boolean): Builder {
                this.shouldShowTagOptions = shouldShowTagOptions
                return this
            }

            fun buildExtras(): Bundle {
                val intent = Bundle()
                if (!mPostList.isNullOrEmpty()) {
                    intent.putParcelableArrayList(AppConstants.ARG_POST_LIST, mPostList)
                }
                if (mPostIndex >= 0) {
                    intent.putInt(AppConstants.ARG_POST_POSITION, mPostIndex)
                }
                if (!mPaginationUrl.isNullOrEmpty()) {
                    intent.putString(AppConstants.ARG_NEXT_URL, mPaginationUrl)
                }
                if (mSourceName != null) {
                    intent.putString(AppConstants.SCREEN_SOURCE, mSourceName)
                }
                if (mGameId != null) {
                    intent.putString(AppConstants.ARG_GAME_ID, mGameId)
                }
                intent.putBoolean(AppConstants.ARG_FOR_CUSTOM_ROOM, isForCustomRoom)
                intent.putBoolean(AppConstants.ARG_LOAD_MORE, mLoadMore)
                intent.putBoolean(AppConstants.ARG_FROM_DEEPLINK, mIsFromDeepLink)
                intent.putBoolean(AppConstants.ARG_SHOW_TAG_OPTIONS, shouldShowTagOptions)
                return intent
            }

            fun build(): MomentsContainerFragment {
                val streamPlayerContainerFragment = MomentsContainerFragment()
                streamPlayerContainerFragment.arguments = buildExtras()
                return streamPlayerContainerFragment
            }
        }
    }
}