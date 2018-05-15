package com.proyekakhir.realapps.utils;

/**
 * Copyright (c) 2006 Richard Rodgers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TimeUtil is a utility class with static methods to convert times in various
 * formats into other formats
 */
public class TimeUtil {
    private static final int MINS_PER_DAY = 60 * 24;
    private static final long MS_PER_DAY = 1000 * 60 * MINS_PER_DAY;

    private static final int SEC = 1000;
    private static final int MIN = SEC * 60;
    private static final int HOUR = MIN * 60;
    private static final int DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;
    private static final long YEAR = WEEK * 52;

    private static final long[] buckets = { YEAR, WEEK, DAY, HOUR, MIN, SEC };
    private static final String[] bucketNames = { "year", "week", "day",
            "hour", "minute", "second" };

    private static GregorianCalendar statFmtCal = new GregorianCalendar();

    private static final String ts24Pat = "H:mm:ss yy-MM-dd";

    /**
     * convert time in milliseconds into a display string of the form [h]h:mm
     * [am|pm] (traditional) or hh:mm (24 hour format) if using traditional
     * format, the leading 'h' & 'm' will be padded with a space to ensure
     * constant length if less than 10 24 hour format
     *
     * @param msecs
     *            a millisecond time
     * @return TimeString the formatted time string
     */
    public static String stringFormat(long msecs, Context context) {
        GregorianCalendar cal = new GregorianCalendar();
        StringBuffer sBuf = new StringBuffer(8);

        cal.setTime(new Date(msecs));

        int hour = cal.get(Calendar.HOUR);

        if(hour>24){
            CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, msecs);
            return (String) date;
        }

        if (hour == 0)
            hour = 12;

        if (hour < 10)
            sBuf.append(" ");

        sBuf.append(Integer.toString(hour));
        sBuf.append(":");

        int minute = cal.get(Calendar.MINUTE);

        if (minute < 10)
            sBuf.append("0");

        sBuf.append(Integer.toString(minute));
        sBuf.append(" ");
        sBuf.append(cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

        return (sBuf.toString());
    }

}

