package com.makerz.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.LineBackgroundSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


// Event Decorator is to add the green tick background to the attended day
public class EventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final Drawable drawable1;
    private final DotSpan mDotSpan;


    public EventDecorator(Drawable drawable, Collection<CalendarDay> dates) {
        this.mDotSpan=null;
        drawable1 = drawable;
        this.dates = new HashSet<>(dates);
    }
    public EventDecorator(DotSpan drawable, Collection<CalendarDay> dates) {
        this.drawable1=null;
        mDotSpan = drawable;
        this.dates=new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {

        view.setBackgroundDrawable(drawable1);
    }
}

