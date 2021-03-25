package com.rheotv.android.ui.activities.tabcontainer.profile.bio;

public interface BioFragmentNavigator {
    void setupViewsForLoggedinUser();

    void setupViewsForNonLoggedinUser();

    void showLoader(boolean b);

    void setBio(String bio);
}
