package vn.tungdx.mediapicker.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import vn.tungdx.mediapicker.imageloader.NImageLoader;

/**
 * Created by TungDX on 6/4/2015.
 */
public class BaseFragment extends Fragment {
    protected Context mContext;
    protected NImageLoader mImageLoader;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FragmentHost host = (FragmentHost) activity;
        mImageLoader = host.getImageLoader();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }
}
