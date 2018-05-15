package com.proyekakhir.realapps.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.proyekakhir.realapps.fragment.RecentChatFragment;
import com.proyekakhir.realapps.fragment.SearchChatFragment;

public class PageAdapter extends FragmentStatePagerAdapter {
    private String mSearchTerm;
    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public PageAdapter(FragmentManager fm, int tabCount, String searchTerm) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
        this.mSearchTerm= searchTerm;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                RecentChatFragment tab1 = RecentChatFragment.newInstance(mSearchTerm);
                return tab1;
            case 1:
                SearchChatFragment tab2 = SearchChatFragment.newInstance(mSearchTerm);
                return tab2;
            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

    public void setTextQueryChanged(String newText) {
        mSearchTerm = newText;
    }
}
