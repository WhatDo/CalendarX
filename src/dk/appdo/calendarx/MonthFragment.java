package dk.appdo.calendarx;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.appdo.calendarx.view.MonthView;

import java.util.Calendar;

public class MonthFragment {

//	private static final String LOG_TAG = "MonthFrag";
//
//
//	// Delete this when DAyView has been created.
//	// Temp for having 3 pages
//	private int mCalendarType;
//
//	private Calendar mTempDate = Calendar.getInstance();
//
//	private MonthView mMonthView;
//
//	public static MonthFragment newInstance(int type) {
//		MonthFragment frag = new MonthFragment();
//		frag.mCalendarType = type;
//
//		return frag;
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//		TextView tv;
//		ViewGroup v;
//
//		switch (mCalendarType) {
//			case PAGE_MONTH:
//				mMonthView = new MonthView(getActivity());
//				mMonthView.setOnFocusMonthChangedListener(this);
//				return mMonthView;
//
//			case PAGE_DAY:
//				tv = new TextView(getActivity());
//				tv.setText("Hello DAY");
//				v = new LinearLayout(getActivity());
//				v.addView(tv);
//				return v;
//
//			default:
//				return null;
//		}
//	}
//
//	protected int getFocusedWeekNumber() {
//		return mMonthView.getFocusedWeekNumber();
//	}
//
//	@Override
//	public void onFocusMonthChanged(long newTimeInMillis) {
//		if (mOnTitleChangedListener != null) {
//			mTempDate.setTimeInMillis(newTimeInMillis);
//			String newTitle = DateUtils.getMonthString(mTempDate.get(Calendar.MONTH), DateUtils.LENGTH_LONG) + " " + mTempDate.get(Calendar.YEAR);
//			mOnTitleChangedListener.onTitleChanged(newTitle, PAGE_MONTH);
//		}
//	}
//
//	public void onPageScrolled(boolean focusWeek, int xAmount) {
//		mMonthView.slideFocusedView(xAmount);
//		mMonthView.setWeekIsFocused(focusWeek);
//	}
//
//	public int getFocusedTopOffset() {
//		return mMonthView.getFocusedViewYPos();
//	}
}
