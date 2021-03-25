package com.rheotv.android.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rheotv.android.R;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ScreenUtils;
import com.rheotv.android.utils.SharedPrefsUtils;

import static android.app.Activity.RESULT_OK;

public class LiveStreamingPermissionDialog extends BaseDialog {
    Activity activity;
    int xCutSize = 10;
    int yCutSize = 30;
    private static final int REQUEST_PERMISSIONS = 10;
    private static final int REQUEST_CODE = 1000;
    boolean showErrorMessage = false;
    View rootView;
    View mobileNumberButton;
    SharedPrefsUtils sharedPrefsUtils;

    @Override
    public void onStart() {
        activity = getActivity();
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        int currentWidth = ScreenUtils.getScreenWidth(activity);
        int currentHeight = ScreenUtils.getScreenHeight(activity);
        int width = currentWidth - ((currentWidth * xCutSize) / 100);
//        int height = currentHeight - ((currentHeight * yCutSize) / 100);
        assert window != null;
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        String source = getArguments().getString("source");
        if(source == "send_news"){
            this.showErrorMessage = true;
        }
    }

    public void addTermsAndConditions(View rootView){
        TextView txt = rootView.findViewById(R.id.terms_and_conditions); //txt is object of TextView
        txt.setMovementMethod(LinkMovementMethod.getInstance());
        txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.rheotv.com/static/privacy_policy.pdf"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.live_streaming_permission, container);
        setCancelable(false);

        View cancelButton = rootView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> dismiss());
        paintView();
        addTermsAndConditions(rootView);

        return rootView;
    }

    private void paintView(){
        TextView enableCamera = rootView.findViewById(R.id.enable_camera);
        if (CommonUtils.isPermissionGrantedForStreaming(getContext())) {
            enableCamera.setText("ALLOW PERMISSION");
        }else{
            enableCamera.setText("START STREAMING");
        }
        enableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    String permissions [] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                    ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSIONS);
                }else{

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
