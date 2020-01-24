package com.zeroner.bledemo.bean;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.lsjwzh.widget.recyclerviewpager.FragmentStatePagerAdapter;

import java.util.LinkedHashMap;

/**
 * 作者：hzy on 2017/12/22 12:24
 * <p>
 * 邮箱：hezhiyuan@iwown.com
 */

public class FragmentsAdapter extends FragmentStatePagerAdapter {
    LinkedHashMap<Integer, Fragment> mFragmentCache = new LinkedHashMap<>();

    public FragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    public FragmentsAdapter(FragmentManager fm, LinkedHashMap<Integer, Fragment> mFragmentCache) {
        super(fm);
        this.mFragmentCache = mFragmentCache;
    }



    @Override
    public Fragment getItem(int position, Fragment.SavedState savedState) {
        Fragment f = mFragmentCache.containsKey(position) ? mFragmentCache.get(position) : new Fragment();
        Log.e("test", "getItem:" + position + " from cache" + mFragmentCache.containsKey
                (position));
        if (savedState == null || f.getArguments() == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("index", position);
            f.setArguments(bundle);
            Log.e("test", "setArguments:" + position);
        } else if (!mFragmentCache.containsKey(position)) {
            f.setInitialSavedState(savedState);
            Log.e("test", "setInitialSavedState:" + position);
        }
        mFragmentCache.put(position, f);
        return f;
    }

    @Override
    public void onDestroyItem(int position, Fragment fragment) {
        // onDestroyItem
        while (mFragmentCache.size() > 1) {
            Object[] keys = mFragmentCache.keySet().toArray();
            mFragmentCache.remove(keys[0]);
        }
    }


    @Override
    public int getItemCount() {
        return 1;
    }
}
