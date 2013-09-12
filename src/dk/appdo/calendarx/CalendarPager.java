package dk.appdo.calendarx;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CalendarPager extends ViewPager {

	private static final int PAGE_COUNT = 2;

	private static final int PAGE_MONTH = 0;

	private static final int PAGE_WEEK = 1;

	public CalendarPager(FragmentActivity context) {
		this(context, null);
	}

	public CalendarPager(FragmentActivity context, AttributeSet attrs) {
		super(context, attrs);
		FragmentActivity activity = context;

		setAdapter(new CalendarPagerAdapter(activity.getSupportFragmentManager()));
	}

	private class CalendarPagerAdapter extends FragmentPagerAdapter {

		private CalendarFragment mMonthFragment;

		private CalendarFragment mWeekFragment;

		public CalendarPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			CalendarFragment frag = CalendarFragment.newInstance();

			switch (i) {
				case PAGE_MONTH:
					mMonthFragment = frag;
					break;

				case PAGE_WEEK:
					mWeekFragment = frag;
					break;
			}
			return frag;
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}
	}

	private static class CalendarFragment extends Fragment {

		public static CalendarFragment newInstance() {
			CalendarFragment frag = new CalendarFragment();
			return frag;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			return null;
		}
	}
}
