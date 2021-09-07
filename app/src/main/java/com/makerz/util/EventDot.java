package com.makerz.util;

import android.content.Context;
import android.util.Log;

import com.makerz.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

import androidx.core.content.ContextCompat;
// EventDot is the red dots that indicate events in Attendance calendar
public class EventDot implements DayViewDecorator {

    private DotSpan mDotSpan;
    private HashSet<CalendarDay> dates;
    private Context mContext;
    public EventDot(DotSpan drawable, Collection<CalendarDay> dates,Context mContext) {
        mDotSpan = drawable;
        this.mContext=mContext;
        this.dates = new HashSet<>(dates);
    }
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(mContext,R.drawable.empty));
            view.addSpan(mDotSpan);
    }
}
