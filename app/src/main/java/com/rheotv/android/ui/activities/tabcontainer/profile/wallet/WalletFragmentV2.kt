package com.rheotv.android.ui.activities.tabcontainer.profile.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rheotv.android.R
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.data.network.models.useProfile.responses.RedeemStatement
import com.rheotv.android.databinding.WalletFragmentLayoutV2Binding
import com.rheotv.android.factories.ViewModelProviderFactoryV2
import com.rheotv.android.ui.activities.rank.RankActivity
import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.decorators.SimpleDividerItemDecoration
import com.rheotv.android.ui.fragments.LiveStreamingDialogFragment
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import java.util.*
import javax.inject.Inject

class WalletFragmentV2 : BaseFragment<WalletFragmentLayoutV2Binding, ProfileContainerViewModel>() {

    private var liveStreamingDialogFragment: LiveStreamingDialogFragment? = null
    private var loginDialogFragment: LoginFragmentBottomDialog? = null
    lateinit var mViewModel: ProfileContainerViewModel

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable(): Int {
        return com.rheotv.android.BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.wallet_fragment_layout_v2
    }

    override fun getViewModel(): ProfileContainerViewModel {

        mViewModel = ViewModelProvider(if (parentFragment != null) parentFragment!! else this, mViewModelFactory).get(ProfileContainerViewModel::class.java)
        if (mViewModel.profileData == null) {
            mViewModel.fetchProfile(mViewModel.authorName)
        }
        mViewModel.walletViewStateMutableLiveData.observe(this, Observer {
            val result = it ?: return@Observer
            when (result) {
                WalletViewState.Loading -> {
                    viewDataBinding.loader.visibility = View.VISIBLE
                }
                is WalletViewState.Error -> {
                    viewDataBinding.loader.visibility = View.GONE
                    viewDataBinding.errorView.visibility = View.VISIBLE
                    viewDataBinding.errorTextVoew.text = result.error
                }
                is WalletViewState.Success -> {
                    viewDataBinding.loader.visibility = View.GONE
                    viewDataBinding.errorView.visibility = View.GONE
                    (viewDataBinding.transactionRecyclerList.adapter as? WalletTransactionRecyclerAdapter)?.submitList(result.list)
                    viewDataBinding.redeemButton.backgroundTintList = ContextCompat.getColorStateList(viewDataBinding.root.context, if (result.canRedeemAmount) R.color.color_accent else R.color.color_remaining_target)
                    mViewModel.redeemAmount.set("${result.walletAmount} Rheo Diamond${if (result.walletAmount <= 1) "" else "s"}")
                    mViewModel.redeemDate.set(result.redeemDate)
                    when (result.level.toLowerCase(Locale.getDefault())) {
                        "bronze" -> mViewModel.setBronzePaymentBadge()
                        "silver" -> mViewModel.setSilverPaymentBadge()
                        "gold" -> mViewModel.setGoldPaymentBadge()
                        else -> mViewModel.setNoLevelAssigned()
                    }
                }
            }
        })
        return mViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateViewState(WalletViewState.Loading)
        viewDataBinding.isBronze = mViewModel.isBronze
        viewDataBinding.isSilver = mViewModel.isSilver
        viewDataBinding.isGold = mViewModel.isGold
        viewDataBinding.redeemAmount = mViewModel.redeemAmount
        viewDataBinding.redeemDate = mViewModel.redeemDate
        viewDataBinding.actionTextView.text = mViewModel.authorProfileData?.get()?.streamingOpeningStatement
        viewDataBinding.streamNowButton.setOnClickListener {
            goLiveClicked(null)
        }
        View.OnClickListener {
            downloadStatement()
        }.also {
            viewDataBinding.downloadStatementImageView.setOnClickListener(it)
            viewDataBinding.downloadStatementLabel.setOnClickListener(it)
        }
        View.OnClickListener {
            mViewModel.authorProfileData.get()?.let { data ->
                RankActivity.startMe(this, activity, data.paymentModel, data.user.id, mViewModel.level, "")
            }
        }.also {
            viewDataBinding.seeMoreTextView.setOnClickListener(it)
            viewDataBinding.medalClickableArea.setOnClickListener(it)
        }
        viewDataBinding.redeemButton.setOnClickListener {
            if (mViewModel.authorProfileData.get()?.canRedeemBalance == true &&
                    (mViewModel.authorProfileData.get()?.redeemBalance ?: 0) >=
                    (mViewModel.authorProfileData.get()?.minimumRedeemBalance ?: 0)) {
                WalletDetailsInputActivity
                        .startMeForResult(this,
                                activity,
                                mViewModel.authorProfileData.get()?.redeemBalance ?: 0,
                                mViewModel.authorProfileData.get()?.rheoDiamondValue ?: 0f,
                                REQUEST_CODE_WALLET_INPUT_FOR_RESULT)
            } else {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Minimum redeem Diamond is ${(mViewModel.authorProfileData.get()?.minimumRedeemBalance
                            ?: 0)}", Toast.LENGTH_LONG).show()
                }
            }
        }
        viewDataBinding.transactionRecyclerList.adapter = WalletTransactionRecyclerAdapter()
        viewDataBinding.transactionRecyclerList.addItemDecoration(SimpleDividerItemDecoration(ContextCompat.getDrawable(requireContext(), R.drawable.avd_divider), SimpleDividerItemDecoration.Orientation.Horizontal))
        mViewModel.authorProfileData.get()?.let {
            if ((it.redeemBalance.toDouble()) >= 0.toDouble()) {
                updateViewState(WalletViewState
                        .Success(walletAmount = it.redeemBalance,
                                canRedeemAmount = it.canRedeemBalance ?: false,
                                level = if (it.isLevelAssigned) it.level ?: "" else "",
                                redeemDate = "1 Rheo Diamond = ₹ ${it.rheoDiamondValue}",
                                list = it.redeemStatement ?: listOf()))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            mViewModel.fetchProfile(mViewModel.authorName)
        }
    }

    private fun updateViewState(walletViewState: WalletViewState) {
        if (walletViewState != mViewModel.lastWalletViewState) {
            mViewModel.setWalletViewState(walletViewState)
        }
    }

    private fun downloadStatement() {
        mViewModel.downloadStatement(context)
    }

    private fun goLiveClicked(source: String?) {
        try {
//            SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_GO_LIVE_CLICKED, baseProperties)
            if (!CommonUtils.isUserLoggedin()) {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                if (loginDialogFragment?.isAdded == true) {
                    return
                }
                loginDialogFragment?.setmCallback(object : LoginFragmentBottomDialog.LoginFragmentCallback {
                    override fun onLoginSuccess() {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER)) }
                    }

                    override fun onLoginDialogClose() {

                    }
                })
                loginDialogFragment?.show(this.childFragmentManager, AppConstants.LOGIN_FRAGMENT_TAG)
                return
            }
            val args = Bundle()
            args.putString(AppConstants.SCREEN_SOURCE, source)
            liveStreamingDialogFragment = LiveStreamingDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
            liveStreamingDialogFragment?.arguments = args
            liveStreamingDialogFragment?.show(childFragmentManager, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        const val REQUEST_CODE_WALLET_INPUT_FOR_RESULT = 0x000
        const val MIN_BALANCE = 200f
        fun newInstance(authorUsername: String?): WalletFragmentV2 {
            val fragment = WalletFragmentV2()
            val bundle = Bundle()
            bundle.putString("author", authorUsername)
            fragment.arguments = bundle
            return fragment
        }

        @JvmStatic
        fun newInstance(profileResult: ProfileResult?, authorUsername: String?): WalletFragmentV2 {
            val fragment = WalletFragmentV2()
            val bundle = Bundle()
            bundle.putParcelable(AppConstants.AUTHOR_PROFILE, profileResult)
            bundle.putString("author", authorUsername)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var viewState: ViewModelProviderFactoryV2<*>? = null
}

sealed class WalletViewState {
    data class Success(val walletAmount: Int = 0, val redeemDate: String = "",
                       val canRedeemAmount: Boolean = false, val level: String = "",
                       val list: List<RedeemStatement> = emptyList()) : WalletViewState()

    data class Error(val error: String) : WalletViewState()
    object Loading : WalletViewState()
}