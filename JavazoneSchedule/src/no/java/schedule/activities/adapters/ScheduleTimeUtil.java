package no.java.schedule.activities.adapters;

import java.util.*;

public class ScheduleTimeUtil {

    static {
        createStartDates();
    }

    private static Long[] startTimes;
    private static Long[] endTimes;


    public static long findBlockStart(long time) {
        long midnightDelta = toMidnightDelta(time);
        for (Long startTime : startTimes) {
            if (startTime <= midnightDelta){
                return time-midnightDelta+startTime;
            }
        }
        throw new IllegalStateException("error in slot time resolution");
    }

    public static long findBlockEnd(long time) {
        long midnightDelta = toMidnightDelta(time);

        for (Long endTime : endTimes) {
            if (endTime >= midnightDelta){
                return time-midnightDelta+endTime;
            }
        }
        throw new IllegalStateException("error in slot time resolution");
    }


    private static void createStartDates() {

        // TODO replace this with data from the event feed

        List<Long> startTimesTmp = new ArrayList<Long>();
        List<Long> endTimesTmp = new ArrayList<Long>();

        final GregorianCalendar time = new GregorianCalendar(0, 0, 0, 9, 0); // 0900

        final GregorianCalendar midnight =  new GregorianCalendar(0, 0, 0, 0, 0);
        final long base = midnight.getTimeInMillis();

        // 09:00 - 10:00
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 10:15 - 11:15
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,30);  // First long break

        // 11:45 - 12:45
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 13:00 - 14:00
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 14:15 - 15:15
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,30); // Second long break

        // 15:45 - 16:45
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);

        // 17:00 - 18:00
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);


        // 18:15 - 19:00
        startTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,60);
        endTimesTmp.add(time.getTimeInMillis() - base);
        time.add(GregorianCalendar.MINUTE,15);


        startTimes = startTimesTmp.toArray(new Long[startTimesTmp.size()]);
        Arrays.sort(startTimes, Collections.reverseOrder());


        //for (Long startTime : startTimesTmp) {
        //    Log.d("startTime",new SimpleDateFormat("hh:mm").format(new Date(startTime)));
        //}

        //for (Long endTime : endTimesTmp) {
        //    Log.d("endTime",new SimpleDateFormat("hh:mm").format(new Date(endTime)));
        //
        //}

        endTimes = endTimesTmp.toArray(new Long[endTimesTmp.size()]);
        Arrays.sort(endTimes);


    }

    private static long toMidnightDelta(long startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startTime);

        Calendar midnight = new GregorianCalendar();
        midnight.setTimeInMillis(startTime);
        midnight.set(Calendar.HOUR_OF_DAY,0);
        midnight.set(Calendar.MINUTE,0);
        midnight.set(Calendar.MILLISECOND,0);


        return calendar.getTimeInMillis() - midnight.getTimeInMillis();

    }
}
