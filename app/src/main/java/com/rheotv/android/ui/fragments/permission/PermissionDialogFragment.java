package com.rheotv.android.ui.fragments.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.ui.activities.splash.SplashActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.utils.SharedPrefsUtils;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

public class PermissionDialogFragment extends DialogFragment {
    Activity activity;
    View rootView;

    public static final int X_PERCENT = 10;
    public static final int Y_PERCENT = 30;
    public static final int Y_SMALL_SCREEN_PERCENT = 15;

    final static int REQUEST_CODE_LOCATION_ACCESS = 101;
    final static int REQUEST_CODE_EXT_STORAGE_ACCESS = 102;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    int permissionCode;

    public void setPermissionsType(String permissionsType) {
        this.permissionsType = permissionsType;
    }

    private String permissionsType = "";

    public static PermissionDialogFragment newInstance(String permissionType) {
        PermissionDialogFragment f = new PermissionDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("permission_type", permissionType);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_permission, container);
        View sureButton = rootView.findViewById(R.id.sureButton);
        Bundle bundle = getArguments();
        if (bundle != null) {
            permissionsType = bundle.getString("permission_type");
        }

        if (permissionsType == null) {
            permissionsType = "";
        }

        if (permissionsType.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ((TextView) rootView.findViewById(R.id.permission_msg)).setText(getResources().getString(R.string.location_permission));
            permissionCode = REQUEST_CODE_LOCATION_ACCESS;
            setCancelable(false);
        } else {
            permissionCode = REQUEST_CODE_EXT_STORAGE_ACCESS;
            requestPermissions(new String[]{permissionsType}, permissionCode);
        }

//        Log.d(MojoTimesApplication.TAG, "activity: " +activity);

        sureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(MojoTimesApplication.TAG, "Dialog click");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{permissionsType}, permissionCode);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        /*Window window = getDialog().getWindow();
        Point dim    = UIHelper.getScreenDimentions( activity );
        int    width  = dim.x - ( ( dim.x * PermissionDialogFragment.X_PERCENT ) / 100 );
        int    height = dim.y - ( ( dim.y * PermissionDialogFragment.Y_SMALL_SCREEN_PERCENT ) / 100 );
        window.setLayout( width, height );
        window.setGravity( Gravity.CENTER );*/

        Window window = getDialog().getWindow();
        double height = ViewGroup.LayoutParams.WRAP_CONTENT;
        double width = ViewGroup.LayoutParams.WRAP_CONTENT;


        window.setLayout((int) width, (int) height);
        window.setGravity(Gravity.CENTER);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_EXT_STORAGE_ACCESS) {

        } else if (requestCode == REQUEST_CODE_LOCATION_ACCESS) {
            boolean isLocationSuccess = false;
            if (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                String latStr = null, longiStr = null;
                if (location != null) {
                    Double lat = location.getLatitude();
                    Double longi = location.getLongitude();
                    latStr = Double.toString(lat);
                    longiStr = Double.toString(longi);
                }
                if (!(latStr == null || longiStr == null || latStr.equals("null") || longiStr.equals("null"))) {
                    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
                    sharedPrefsUtils.setStringPreference(activity.getApplicationContext(), "lat", latStr);
                    sharedPrefsUtils.setStringPreference(activity.getApplicationContext(), "lng", longiStr);
                    isLocationSuccess = true;
                }

                if (true/*isLocationSuccess*/) {
                    if (activity instanceof SplashActivity) {
                        dismiss();
                        ((SplashActivity) activity).openMainActivity();
                        return;
                    } else if (activity instanceof TabContainerActivity) {
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_ID, null);
                        sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), "permission_asked", true);
//                        ((TabContainerActivity) activity).loadFragment(DynamicTabsFragment.newInstance(false), false, false, R.id.frame_container);
                    }
                } else {
                    //todo : ask user to select district.
                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        new SharedPrefsUtils().setBooleanPreference(RheoTvApp.getNonUiContext(), "loc_per_restrict", false);
                    } else {
                        new SharedPrefsUtils().setBooleanPreference(RheoTvApp.getNonUiContext(), "loc_per_restrict", true);
                    }
                }

                if (activity instanceof SplashActivity) {
                    getDialog().dismiss();
                    ((SplashActivity) activity).renderHomePage(false);
                    return;
                }
            }
        }
        getDialog().dismiss();
    }

}
