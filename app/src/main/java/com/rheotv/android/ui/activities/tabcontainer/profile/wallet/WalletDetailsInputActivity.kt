package com.rheotv.android.ui.activities.tabcontainer.profile.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.R
import com.rheotv.android.databinding.ActivityWalletDetailsInputBinding
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import javax.inject.Inject

class WalletDetailsInputActivity :
        BaseActivity<ActivityWalletDetailsInputBinding, WalletDetailsInputActivityViewModel>(), WalletDetailsInputActivityNavigator {

    @Inject
    lateinit var mViewModel: WalletDetailsInputActivityViewModel


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.navigator = this
        viewDataBinding.loaderVisibility = viewModel.loaderObservable
        val maxRedeemableCoin: Int = intent.getIntExtra(AppConstants.ARG_MAX_REDEEMABLE_COIN, 0)
        val rheoCoinValue: Float = intent.getFloatExtra(AppConstants.ARG_RHEO_COIN_VALUE, 0f)
        val maxRedeemableAmount: Float = (maxRedeemableCoin.toFloat() * rheoCoinValue).coerceAtLeast(0f)
        viewDataBinding.maxAmount = maxRedeemableAmount
        viewDataBinding.submitButton.setOnClickListener {
            CommonUtils.hideKeyboard(this)
            if (validateCredentials()) {
                viewModel.loaderObservable.set(true)
                viewModel.requestRedeem(viewDataBinding.upiEditText.text?.toString(),
                        viewDataBinding.mobileNumberEditText.text?.toString(),
                        maxRedeemableCoin
                )
            }
        }
    }

    private fun validateCredentials(): Boolean {
        if (viewDataBinding.mobileNumberEditText.text?.isEmpty() == true) {
            showToast("Please fill the mobile number!")
            return false
        }
        if (viewDataBinding.upiEditText.text?.isEmpty() == true) {
            showToast("Please fill the UPI id!")
            return false
        }
        if ((viewDataBinding.mobileNumberEditText.text?.length ?: 0) < 10) {
            showToast("Please fill correct mobile number!")
            return false
        }
        if (viewDataBinding.upiEditText.text?.contains("@") == false) {
            showToast("Please fill correct UPI id!")
            return false
        }
        return true
    }

    private fun showToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun getBindingVariable(): Int = com.rheotv.android.BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_wallet_details_input

    override fun getViewModel(): WalletDetailsInputActivityViewModel = mViewModel

    override fun onPaymentSuccess(amount: Int) {
        viewModel.loaderObservable.set(false)
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onPaymentFailure(errorMessage: String) {
        viewModel.loaderObservable.set(false)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//        setResult(Activity.RESULT_CANCELED)
    }

    companion object {
        fun startMe(fragment: Fragment?, activity: Activity?, maxRedeemableCoin: Int, rheoCoinValue: Float) {
            startMeForResult(fragment, activity, maxRedeemableCoin, rheoCoinValue, requestCode = -1)
        }

        fun startMeForResult(fragment: Fragment?, activity: Activity?, maxRedeemableCoin: Int, rheoCoinValue: Float, requestCode: Int = -1) {
            fragment?.let {
                val intent = Intent(it.context, WalletDetailsInputActivity::class.java).apply {
                    putExtra(AppConstants.ARG_MAX_REDEEMABLE_COIN, maxRedeemableCoin)
                    putExtra(AppConstants.ARG_RHEO_COIN_VALUE, rheoCoinValue)
                }
                if (requestCode == -1) {
                    it.startActivity(intent)
                } else {
                    it.startActivityForResult(intent, requestCode)
                }
                return
            }
            activity?.let {
                val intent = Intent(it, WalletDetailsInputActivity::class.java).apply {
                    putExtra(AppConstants.ARG_MAX_REDEEMABLE_COIN, maxRedeemableCoin)
                    putExtra(AppConstants.ARG_RHEO_COIN_VALUE, rheoCoinValue)
                }
                if (requestCode == -1) {
                    it.startActivity(intent)
                } else {
                    it.startActivityForResult(intent, requestCode)
                }
            }
        }
    }
}
