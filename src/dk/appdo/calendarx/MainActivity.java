package dk.appdo.calendarx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends FragmentActivity {

	private enum Direction {
		LEFT, RIGHT, NIL
	}

	public static final int PAGE_COUNT = 2;

	public static final int PAGE_MONTH = 0;

	public static final int PAGE_WEEK = 1;

	private static final String LOG_TAG = "Activity";

	private TitlePageIndicator mIndicator;

	private ViewPager mViewPager;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CalendarPagerAdapter adapter = new CalendarPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(adapter);
		mViewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mViewPager.requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});

		mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(adapter);
	}

	private class CalendarPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, CalendarFragment.OnTitleChangedListener {

		private String mMonthTitle = "month";

		private CalendarFragment mMonthFragment;

		private CalendarFragment mWeekFragment;

		private int mScrollState;

		private int mPreviousState;

		private Direction mDirection;

		private int mPreviousScrollAmount = 0;

		public CalendarPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			CalendarFragment frag = CalendarFragment.newInstance(i);

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

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case PAGE_MONTH:
					return mMonthTitle;
			}
			return "Hello Month";
		}

		public void setMonthTitle(String title) {
			Log.d(LOG_TAG, title);
			mMonthTitle = title;
			mIndicator.invalidate();
		}

		@Override
		public void onPageScrolled(int i, float v, int i2) {

//			mDirection = mPreviousScrollAmount < i2 ? Direction.RIGHT : Direction.LEFT;
//			mPreviousScrollAmount = i2;
//			boolean moveRight = mDirection == Direction.RIGHT;
//
//			String state = (mScrollState == ViewPager.SCROLL_STATE_DRAGGING ? "Scroll_State_Dragging" : (mScrollState == ViewPager.SCROLL_STATE_IDLE ? "Scroll_State_Idle" : "Scroll_State_Settling"));
//
//			Log.d(LOG_TAG, "SCROLLED " + state);
//			Log.d(LOG_TAG, "AMOUNT " + v);
//			Log.d(LOG_TAG, "PIXELS " + i2);
//			if (i < PAGE_MONTH + 1 && mMonthFragment != null) {
//				mWeekFragment.setShownWeek(mMonthFragment.getFocusedWeekNumber());
//				switch (mScrollState) {
//					case ViewPager.SCROLL_STATE_DRAGGING:
//						if (mPreviousState == ViewPager.SCROLL_STATE_IDLE && moveRight) {
//							mWeekFragment.onAnimationStarted();
//						}
//						mMonthFragment.onPageScrolled(moveRight, i2);
//						mWeekFragment.onPageScrolled(moveRight, i2, mMonthFragment.getFocusedTopOffset());
//						break;
//
////					case ViewPager.SCROLL_STATE_IDLE:
////						mMonthFragment.onPageScrolled(false, 0);
////						mWeekFragment.onPageScrolled(false, i2);
////						break;
//
//					case ViewPager.SCROLL_STATE_SETTLING:
//						mMonthFragment.onPageScrolled(moveRight, i2);
//						mWeekFragment.onPageScrolled(moveRight, i2, mMonthFragment.getFocusedTopOffset());
//						break;
//				}
//			}
		}

		@Override
		public void onPageSelected(int i) {
		}

		@Override
		public void onPageScrollStateChanged(int i) {
//			mPreviousState = mScrollState;
//			mScrollState = i;
//
//			if (mMonthFragment != null) {
//				switch (mScrollState) {
//					case ViewPager.SCROLL_STATE_DRAGGING:
//						break;
//
//					case ViewPager.SCROLL_STATE_IDLE:
//						mDirection = Direction.NIL;
//						mWeekFragment.onAnimationComplete();
//						mWeekFragment.onPageScrolled(false, WeekFragment.VIEW_WIDTH, mMonthFragment.getFocusedTopOffset());
//						mMonthFragment.onPageScrolled(false, 0);
//						break;
//
////					case ViewPager.SCROLL_STATE_SETTLING:
////						mMonthFragment.onPageScrolled(false, 0);
////						break;
//				}
//			}
		}

		@Override
		public void onTitleChanged(String newTitle, int page) {
			switch (page) {
				case PAGE_MONTH:
					setMonthTitle(newTitle);
					break;
			}
		}
	}
}
