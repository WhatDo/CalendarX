package dk.appdo.calendarx.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import dk.appdo.calendarx.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarView extends FrameLayout {

	public interface OnFocusMonthChangeListener {
		void onFocusMonthChanged(long newTimeInMillis);
	}

	private static final String LOG_TAG = "CalendarView";

	/**
	 * The default minimal date.
	 */
	private static final String DEFAULT_MIN_DATE = "01/01/1900";

	/**
	 * The default maximal date.
	 */
	private static final String DEFAULT_MAX_DATE = "01/01/2100";

	/**
	 * The date format to parse dates.
	 */
	private static final String DATE_FORMAT = "dd/MM/yyyy";

	private static final int DEFAULT_DAYS_PER_WEEK = 7;

	private static final int DEFAULT_SHOWN_ROWS_COUNT = 6;

	private static final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;

	private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

	private static final long MILLIS_IN_WEEK = MILLIS_IN_DAY * 7;

	private static final int GOTO_SCROLL_DURATION = 1000;

	private static final boolean DEFAULT_SHOW_WEEK_NUMBERS = false;

	private final Context mContext;

	private OnFocusMonthChangeListener mOnFocusMonthChangeListener;

	// For drawing the dates
	private Paint mDatePaint;

	private Paint mDateBGPaint;

	private int mBackgroundColor;

	private RectF mTempRect;

	// To control the calendar
	private Calendar mMaxDate;

	private Calendar mMinDate;

	private Calendar mTempDate;

	private Calendar mFirstDayOfMonth;

	private DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

	private Locale mCurrentLocale;

	private int mCurrentMonthDisplayed;

	private int mSelectedWeek;

	private int mFocusedMonth;

	// Views
	private final ListView mListView;

	private final ViewGroup mHeader;

	private CalendarAdapter mAdapter;

	private int mListCount;

	private int mRows;

	private int mRowHeight;

	private String[] mDayLabels;

	// Styles
	private boolean mShowWeekNumber = DEFAULT_SHOW_WEEK_NUMBERS;

	private final int mDateTextSize;

	private int mDaysPerWeek = DEFAULT_DAYS_PER_WEEK;

	private int mFirstDayOfWeek = DEFAULT_FIRST_DAY_OF_WEEK;

	private int mDaysPerRow = DEFAULT_DAYS_PER_WEEK;


	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, 0);
		mContext = context;

		setCurrentLocale(Locale.getDefault());
		parseDate(DEFAULT_MAX_DATE, mMaxDate);
		parseDate(DEFAULT_MIN_DATE, mMinDate);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		TypedArray t = context.obtainStyledAttributes(android.R.style.TextAppearance_Small, new int[]{android.R.attr.textSize});

		mDateTextSize = t.getDimensionPixelSize(0, 1);

		mFirstDayOfWeek = preferences.getInt("firstDayOfWeek", DEFAULT_DAYS_PER_WEEK);

		mRows = DEFAULT_SHOWN_ROWS_COUNT;

		mListCount = getWeeksSinceMinDate(mMaxDate);

		mBackgroundColor = getResources().getColor(android.R.color.background_dark);

		t.recycle();

		mTempRect = new RectF();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.calendar_view, null, false);

		addView(content);

		mHeader = (ViewGroup) content.findViewById(R.id.day_names_header);
		mListView = (ListView) content.findViewById(R.id.calendar_list);

		setUpHeader();
		setUpListView();
		setUpPaints();
	}

	/**
	 * Inits the view to go to today
	 */
	public void init() {
		mTempDate.setTimeInMillis(System.currentTimeMillis());
		goTo(mTempDate, false);
		invalidate();
	}

	/**
	 * Sets up the paints used by <code>RowItem</code>
	 */
	private void setUpPaints() {
		mDatePaint = new Paint();
		mDatePaint.setTextSize(mDateTextSize);
		mDatePaint.setAntiAlias(true);
		mDatePaint.setFakeBoldText(true);
		mDatePaint.setTextAlign(Paint.Align.CENTER);

		mDateBGPaint = new Paint();
		mDateBGPaint.setColor(mBackgroundColor);
	}

	/**
	 * Sets up the week strings in the header
	 */
	private void setUpHeader() {
		mDayLabels = new String[mDaysPerWeek];
		for (int i = mFirstDayOfWeek, count = mFirstDayOfWeek + mDaysPerWeek; i < count; i++) {
			int calendarDay = (i > Calendar.SATURDAY) ? i - Calendar.SATURDAY : i;
			mDayLabels[i - mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay,
					DateUtils.LENGTH_SHORT);
		}

		TextView label = (TextView) mHeader.getChildAt(0);
		if (mShowWeekNumber) {
			label.setVisibility(View.VISIBLE);
		} else {
			label.setVisibility(View.GONE);
		}
		for (int i = 1, count = mHeader.getChildCount(); i < count; i++) {
			label = (TextView) mHeader.getChildAt(i);

			if (i < mDaysPerWeek + 1) {
				label.setText(mDayLabels[i - 1]);
				label.setVisibility(View.VISIBLE);
			} else {
				label.setVisibility(View.GONE);
			}
		}
		mHeader.invalidate();
	}

	/**
	 * Sets up the <code>ListView</code> containing the
	 * actual calendar.
	 */
	private void setUpListView() {
		mAdapter = new CalendarAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setDividerHeight(0);
	}

	/**
	 * This moves to the specified time in the view. If the time is not already
	 * in range it will move the list so that the first of the month containing
	 * the time is at the top of the view. If the new time is already in view
	 * the list will not be scrolled unless forceScroll is true. This time may
	 * optionally be highlighted as selected as well.
	 *
	 * @param date        The time to move to.
	 * @param animate     Whether to scroll to the given time or just redraw at the
	 *                    new location.
	 * @throws IllegalArgumentException of the provided date is before the
	 *                                  range start of after the range end.
	 */
	private void goTo(Calendar date, boolean animate) {
		if (date.before(mMinDate) || date.after(mMaxDate)) {
			throw new IllegalArgumentException("Time not between " + mMinDate.getTime()
					+ " and " + mMaxDate.getTime());
		}

		mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
		mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

		setMonthDisplayed(mFirstDayOfMonth);

		int position;

		// the earliest time we can scroll to is the min date
		if (mFirstDayOfMonth.before(mMinDate)) {
			position = 0;
		} else {
			position = getWeeksSinceMinDate(mFirstDayOfMonth);
		}
		if (animate) {
			mListView.smoothScrollToPositionFromTop(position, 0,
					GOTO_SCROLL_DURATION);
		} else {
			mListView.setSelection(position);
		}

	}

	/**
	 * Parses the given <code>date</code> and in case of success sets
	 * the result to the <code>outDate</code>.
	 *
	 * @return True if the date was parsed.
	 */
	private boolean parseDate(String date, Calendar outDate) {
		try {
			outDate.setTime(mDateFormat.parse(date));
			return true;
		} catch (ParseException e) {
			Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
			return false;
		}
	}

	/**
	 * Sets the month displayed at the top of this view based on time. Override
	 * to add custom events when the title is changed.
	 *
	 * @param calendar A day in the new focus month.
	 */
	private void setMonthDisplayed(Calendar calendar) {
		mCurrentMonthDisplayed = calendar.get(Calendar.MONTH);
		mAdapter.setFocusMonth(calendar);
	}

	/**
	 * Gets a calendar for locale bootstrapped with the value of a given calendar.
	 *
	 * @param oldCalendar The old calendar.
	 * @param locale      The locale.
	 */
	private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
		if (oldCalendar == null) {
			return Calendar.getInstance(locale);
		} else {
			final long currentTimeMillis = oldCalendar.getTimeInMillis();
			Calendar newCalendar = Calendar.getInstance(locale);
			newCalendar.setTimeInMillis(currentTimeMillis);
			return newCalendar;
		}
	}

	/**
	 * Sets the current locale.
	 *
	 * @param locale The current locale.
	 */
	private void setCurrentLocale(Locale locale) {
		if (locale.equals(mCurrentLocale)) {
			return;
		}

		mCurrentLocale = locale;

		mTempDate = getCalendarForLocale(mTempDate, locale);
		mFirstDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
		mMinDate = getCalendarForLocale(mMinDate, locale);
		mMaxDate = getCalendarForLocale(mMaxDate, locale);
	}

	/**
	 * @return Returns the number of weeks between the current <code>date</code>
	 *         and the <code>mMinDate</code>.
	 */
	private int getWeeksSinceMinDate(Calendar date) {
		if (date.before(mMinDate)) {
			throw new IllegalArgumentException("fromDate: " + mMinDate.getTime()
					+ " does not precede toDate: " + date.getTime());
		}
		long endTimeMillis = date.getTimeInMillis()
				+ date.getTimeZone().getOffset(date.getTimeInMillis());
		long startTimeMillis = mMinDate.getTimeInMillis()
				+ mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
		long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
				* MILLIS_IN_DAY;
		return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
	}

	public void setRowCount(int numberOfRows) {
		if (numberOfRows <= 0) {
			throw new IllegalArgumentException("Row count can't be less than 1");
		}

		for (int i = 0; i < mListView.getChildCount(); i++) {
			mListView.getChildAt(i).setMinimumHeight(mListView.getChildAt(i).getHeight() * mRows / numberOfRows);
		}
		mRows = numberOfRows;
	}

	public void setOnFocusMonthChangeListener(OnFocusMonthChangeListener onFocusMonthChangeListener) {
		this.mOnFocusMonthChangeListener = onFocusMonthChangeListener;
	}

	private class CalendarAdapter extends BaseAdapter {

		private final Calendar mSelectedDate = Calendar.getInstance();

		@Override
		public int getCount() {
			return mListCount;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CalendarRow row = (CalendarRow) convertView;

			if (row == null) {
				row = new CalendarRow(mContext);
			}

			row.init(position);
			return row;
		}

		/**
		 * Updates the selected day and related parameters.
		 *
		 * @param selectedDay The time to highlight
		 */
		public void setSelectedDay(Calendar selectedDay) {
			if (selectedDay.get(Calendar.DAY_OF_YEAR) == mSelectedDate.get(Calendar.DAY_OF_YEAR)
					&& selectedDay.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)) {
				return;
			}
			mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
			mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
			mFocusedMonth = mSelectedDate.get(Calendar.MONTH);
			notifyDataSetChanged();
		}

		/**
		 * Changes which month is in focus and updates the view.
		 *
		 * @param calendar The calendar containing the month to show as in focus
		 */
		public void setFocusMonth(Calendar calendar) {
			int month = calendar.get(Calendar.MONTH);
			if (mFocusedMonth != month) {
				mFocusedMonth = month;
				notifyDataSetChanged();
				final long millis = calendar.getTimeInMillis();

				if (mOnFocusMonthChangeListener != null) {
					mOnFocusMonthChangeListener.onFocusMonthChanged(millis);
				}
			}
		}
	}

	private class CalendarRow extends LinearLayout {

		private int mDrawHeight;

		private int mDrawY;

		private TextView mWeekNumber;

		private RowItem[] mRowItems = new RowItem[mDaysPerRow];

		public CalendarRow(Context context) {
			this(context, null);
		}

		public CalendarRow(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public CalendarRow(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, 0);
			mWeekNumber = new TextView(mContext);

			if (!mShowWeekNumber) {
				mWeekNumber.setVisibility(INVISIBLE);
			}

			LinearLayout.LayoutParams weekNumberParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			addView(mWeekNumber, weekNumberParams);

			for (int i = 0; i < mDaysPerRow; i++) {
				mRowItems[i] = new RowItem(mContext);
				LinearLayout.LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
				addView(mRowItems[i], params);
			}
		}

		public void init(int pos) {
			mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());
			mTempDate.add(Calendar.DAY_OF_YEAR, pos * mDaysPerRow);

			for (int i = 0; i < mDaysPerRow; i++) {
				mRowItems[i].init(mTempDate.getTimeInMillis());

				mTempDate.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		public void setDrawHeight(int height) {
			mDrawHeight = height;
		}

		public void setDrawY(int y) {
			mDrawY = y;
		}

		private class RowItem extends View {

			private int mDay;
			private int mMonth;
			private int mYear;

			public RowItem(Context context) {
				this(context, null);
			}

			private RowItem(Context context, AttributeSet attrs) {
				this(context, attrs, 0);
			}

			private RowItem(Context context, AttributeSet attrs, int defStyle) {
				super(context, attrs, defStyle);
			}

			public void init(long dateInMillis) {
				mTempDate.setTimeInMillis(dateInMillis);

				mDay = mTempDate.get(Calendar.DAY_OF_MONTH);
				mMonth = mTempDate.get(Calendar.MONTH);
				mYear = mTempDate.get(Calendar.YEAR);
			}

			@Override
			protected void onDraw(Canvas canvas) {

				// Our point zero
				int halfHeight = canvas.getHeight() / 2;
				int halfWidth = canvas.getWidth() / 2;

				mTempRect.set(0, halfHeight + mDrawHeight / 2, canvas.getWidth(), halfHeight - mDrawHeight / 2);
				canvas.drawRect(mTempRect, mDateBGPaint);

				canvas.drawText(mDay + "", halfWidth, halfHeight, mDatePaint);

			}

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				mRowHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
						.getPaddingBottom()) / mRows;
				setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight);

				mDrawHeight = mRowHeight;
			}
		}
	}
}
