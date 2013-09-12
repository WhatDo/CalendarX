package dk.appdo.calendarx.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import dk.appdo.calendarx.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class CalendarView extends FrameLayout {

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

	private final Context mContext;

	private final int mDateTextSize;

	private Calendar mMaxDate;

	private Calendar mMinDate;

	private Calendar mTempDate;

	private Calendar mFirstDayOfMonth;

	private DateFormat mDateFormat;

	private Locale mCurrentLocale;

	private final ListView mListView;

	private final ViewGroup mHeader;

	private CalendarAdapter mAdapter;

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

		TypedArray t = context.obtainStyledAttributes(android.R.style.TextAppearance_Small, new int[]{android.R.attr.textSize});

		mDateTextSize = t.getDimensionPixelSize(0, 1);

		t.recycle();

		parseDate(DEFAULT_MAX_DATE, mMaxDate);
		parseDate(DEFAULT_MIN_DATE, mMinDate);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.calendar_view, null, false);

		addView(content);

		mHeader = (ViewGroup) content.findViewById(R.id.day_names_header);
		mListView = (ListView) content.findViewById(R.id.calendar_list);

		setUpHeader();
		setUpListView();
	}

	private void setUpHeader() {

	}

	private void setUpListView() {
		mAdapter = new CalendarAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
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
	 * Gets a calendar for locale bootstrapped with the value of a given calendar.
	 *
	 * @param oldCalendar The old calendar.
	 * @param locale The locale.
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

	private class CalendarAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}
	}
}
