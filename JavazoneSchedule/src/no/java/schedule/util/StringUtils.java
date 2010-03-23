/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey, Filip Maelbrancke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.schedule.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;

/**
 * String utilities
 */
public class StringUtils
{
	/**
	 * Get a readable string displaying the time
	 * 
	 * @context The context
	 * @param format
	 *            The format
	 * @param time
	 *            The time in seconds (unixtime)
	 * 
	 * @return The time string
	 */
	public static String getTimeAsString( Context context, SimpleDateFormat format, long time)
	{
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format( new Date( time*1000 ));
	}

	public static final SimpleDateFormat DAY_HOUR_TIME = new SimpleDateFormat( "EEE k:mm");
	public static final SimpleDateFormat HOUR_MIN_TIME = new SimpleDateFormat( "k:mm");
	public static final SimpleDateFormat MONTH_DAY = new SimpleDateFormat( "EEE, MMM d");
}
