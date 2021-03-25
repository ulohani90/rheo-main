package com.rheotv.android.ui.activities.profile.viewprofile.view

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
import com.rheotv.android.databinding.FragmentUserWalletBinding
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserWalletViewModel
import com.rheotv.android.ui.activities.rank.RankActivity
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletDetailsInputActivity
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletTransactionRecyclerAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.decorators.SimpleDividerItemDecoration
import com.rheotv.android.ui.fragments.LiveStreamingDialogFragment
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import javax.inject.Inject

class UserWalletFragment : BaseFragment<FragmentUserWalletBinding, UserWalletViewModel>() {

    private var liveStreamingDialogFragment: LiveStreamingDialogFragment? = null
    private var loginDialogFragment: LoginFragmentBottomDialog? = null

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun getLayoutId() = R.layout.fragment_user_wallet

    override fun getViewModel() = ViewModelProvider(this, mViewModelFactory).get(UserWalletViewModel::class.java)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.username = arguments?.getString(AppConstants.AUTHOR_NAME) ?: ""
        viewModel.loadWallet()
        viewModel.wallet.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            viewDataBinding.wallet = it.data
            it.data?.redeemStatement?.let { rs ->
                (viewDataBinding.transactionRecyclerList.adapter as? WalletTransactionRecyclerAdapter)?.submitList(rs)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_WALLET_CHECK_PROGRESS_CLICKED, hashMapOf<String, Any?>(
                    "amount" to viewModel.wallet.value?.data?.availableRedeemBalance,
                    "screenSource" to SegmentConstants.SCREEN_USER_WALLET
            ))
            viewModel.wallet.value?.data?.paymentModel?.let {
                RankActivity.startMe(this, activity, it, viewModel.wallet.value?.data?.user?.id
                        ?: 0, viewModel.wallet.value?.data?.toLevelType, "")
            }
        }.also {
            viewDataBinding.seeMoreTextView.setOnClickListener(it)
            viewDataBinding.medalClickableArea.setOnClickListener(it)
        }
        viewDataBinding.redeemButton.setOnClickListener {
            SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_WALLET_REDEEM_CLICKED, hashMapOf<String, Any?>(
                    "amount" to viewModel.wallet.value?.data?.availableRedeemBalance,
                    "screenSource" to SegmentConstants.SCREEN_USER_WALLET
            ))
            if (viewModel.wallet.value?.data?.canRedeem == true) {
                WalletDetailsInputActivity
                        .startMeForResult(this,
                                activity,
                                viewModel.wallet.value?.data?.availableRedeemBalance ?: 0,
                                viewModel.wallet.value?.data?.currentRheoDiamondValue ?: 0f,
                                REQUEST_CODE_WALLET_INPUT_FOR_RESULT)
            } else {
                context?.let { ctx ->
                    Toast.makeText(ctx,
                            "Minimum redeem Diamond is " + "${viewModel.wallet.value?.data?.minimumRedeemBalance ?: 0f}"
                            , Toast.LENGTH_LONG).show()
                }
            }
        }
        viewDataBinding.transactionRecyclerList.adapter = WalletTransactionRecyclerAdapter()
        viewDataBinding.transactionRecyclerList.addItemDecoration(SimpleDividerItemDecoration(ContextCompat.getDrawable(requireContext(), R.drawable.avd_divider), SimpleDividerItemDecoration.Orientation.Horizontal))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            viewModel.loadWallet()
        }
    }

    private fun downloadStatement() {
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_WALLET_DOWNLOAD_STATEMENT_CLICKED, hashMapOf<String, Any?>(
                "amount" to viewModel.wallet.value?.data?.availableRedeemBalance,
                "screenSource" to SegmentConstants.SCREEN_USER_WALLET
        ))
        viewModel.downloadStatement()
    }

    private fun goLiveClicked(source: String?) {
        try {
            SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_GO_LIVE_CLICKED, hashMapOf<String, Any?>(
                    "amount" to viewModel.wallet.value?.data?.availableRedeemBalance,
                    "screenSource" to SegmentConstants.SCREEN_USER_WALLET
            ))
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

        fun newInstance(authorUsername: String?): UserWalletFragment {
            val fragment = UserWalletFragment()
            val bundle = Bundle()
            bundle.putString(AppConstants.AUTHOR_NAME, authorUsername)
            fragment.arguments = bundle
            return fragment
        }
    }
}