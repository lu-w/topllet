// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.utils;

import java.util.logging.Logger;

import openllet.core.exceptions.TimeoutException;
import openllet.core.exceptions.TimerInterruptedException;
import openllet.shared.tools.Log;

/**
 * <p>
 * Class used to keep track how much time is spent for a specific operation. Timers are primarily used to display info about performance. A timer is started at
 * the beginning of a function and is stopped at the _end of that function (special care needed when there are multiple return commands in a function because
 * the status of unstopped timers is undefined). A timer also stores how many times the timer has been started so average time spent in a function can be
 * computed.
 * </p>
 * <p>
 * When a timer is used in a recursive function it will typically be started multiple times. Timer class will only measure the time spent in the first call.
 * This is done by counting how many times a timer is started and time spent is computed only when the number of stop() calls evens out the start() calls. It is
 * the programmer's responsibility to make sure each start() is stopped by a stop() call.
 * </p>
 * <p>
 * Each timer may be associated with a timeout limit. This means that time spent between start() and stop() calls should be less than the _timeout specified.
 * Timeouts will only be checked when check() function is called. If check() function is not called setting timeouts has no effect. It is up to the programmer
 * to decide when and how many times a timer will be checked.
 * </p>
 * <p>
 * There may be a dependency between timers. For example, classification, realization and entailment operations all use consistency checks. If something goes
 * wrong inside a consistency check and that operation does not finish in a reasonable time, the _timeout on the _parent timer may expire. To handle such cases,
 * a timer may be associated with a _parent timer so every time a timer is checked for a _timeout, its _parent timer will also be checked. Normally, we would
 * like to associate many parents with a timer but for efficiency reasons (looping over an array each time is expensive) each timer is allowed to have only one
 * parent.
 * </p>
 * <p>
 * {@link Timers Timers} class stores a set of timers and provides functions to start, stop and check timers.
 * </p>
 *
 * @see Timers
 * @author Evren Sirin
 */
public class Timer
{
	private final static Logger _logger = Log.getLogger(Timer.class);

	public final static long NOT_STARTED = -1;
	public final static long NO_TIMEOUT = 0;

	private final String _name; // _name to identify what we are timing
	private long _totalTime; // total time that has elapsed when the timer was running
	private long _startTime; // last time timer was started
	private long _count; // number of times the timer was started and stopped
	private long _startCount; // if we are timing recursive functions timer may be started
	// multiple times. we only want to measure time spent in the
	// upper most function call so we need to discard other starts
	private long _timeout; // Point at which a call to check throws an exception
	private long _lastTime; // time that has elapsed between last start()-_stop() period
	private boolean interrupted; // Tells whether this timer has been interrupted

	private final Timer _parent; // the _parent timer

	/**
	 * Create a timer with no name and no parent.
	 */
	public Timer()
	{
		this("", null);
	}

	/**
	 * Create a timer with no parent.
	 *
	 * @param name
	 */
	public Timer(final String name)
	{
		this(name, null);
	}

	/**
	 * Create a timer that has the specified parent timer.
	 *
	 * @param name
	 * @param parent
	 */
	public Timer(final String name, final Timer parent)
	{
		_name = name;
		_parent = parent;

		_timeout = NO_TIMEOUT;
		reset();
	}

	/**
	 * Update the total time elapsed and number of counts by adding the values from another timer. This is especially useful if we are running
	 *
	 * @param timer
	 */
	public void add(final Timer timer)
	{
		_totalTime += timer._totalTime;
		_count += timer._count;
	}

	/**
	 * Start time timer by recording the time this function is called. If timer is running when this function is called time is not recorded and only an
	 * internal counter is updated.
	 */
	public void start()
	{
		if (_startCount == 0)
			_startTime = System.currentTimeMillis();

		_startCount++;
	}

	/**
	 * Stop the timer, increment the _count and update the total time spent. If timer has been started multiple times this function will only decrement the
	 * internal counter. Time information is updated only when all starts are evened out by stops.
	 *
	 * @return Return the total time spent after last start(), -1 if timer is still running, -Long.MAX_VALUE on error
	 */
	public long stop()
	{
		if (!isStarted())
		{
			_logger.fine(() -> String.format("Ignoring attempt to stop a timer (\"%s\") that is not running. Timer results are incorrect for multi-threaded code.", _name));
			return -Long.MAX_VALUE;
		}

		// Decrement start counter.
		_startCount--;

		if (!isStarted())
		{
			_lastTime = System.currentTimeMillis() - _startTime;
			_totalTime += _lastTime;
			_startTime = NOT_STARTED;
			_count++;
			return _lastTime;
		}

		return -1;
	}

	/**
	 * Reset all the internal counts associated with this timer. After this function call it will be like timer has never been used.
	 */
	public void reset()
	{
		_totalTime = 0;
		_startTime = NOT_STARTED;
		_startCount = 0;
		_count = 0;
		interrupted = false;
	}

	/**
	 * If started _stop the timer and then start it again.
	 */
	public void restart()
	{
		if (isStarted())
			stop();
		start();
	}

	/**
	 * Check if the elapsed time is greater than the timeout limit and throw a TimeoutException if that is the case. Check the parent timer if there is one.
	 *
	 * @throws TimeoutException
	 * @throws TimerInterruptedException
	 */
	public void check() throws TimeoutException, TimerInterruptedException
	{

		if (interrupted)
		{
			interrupted = false;
			throw new TimerInterruptedException("Timer " + getName() + " interrupted.");
		}

		final long elapsed = getElapsed();

		if (_timeout != NO_TIMEOUT && elapsed > _timeout)
			throw new TimeoutException("Running time of " + _name + " exceeded timeout of " + _timeout);

		if (_parent != null)
			_parent.check();
	}

	/**
	 * Interrupt timer so that the next check() call will throw an InterruptedException
	 */
	public void interrupt()
	{
		interrupted = true;
	}

	/**
	 * @return true if timer has been started with a {@link #start()} call but not has been stopped with a {@link #stop()} call.
	 */
	public boolean isStarted()
	{
		return _startCount > 0;
	}

	/**
	 * @return the name of this timer.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @return the time elapsed (in milliseconds) since the last time this timer was started. If the timer is not running now 0 is returned.
	 */
	public long getElapsed()
	{
		return isStarted() ? System.currentTimeMillis() - _startTime : 0;
	}

	/**
	 * @return the total time (in milliseconds) spent while this timer was running. If the timer is running when this function is called time elapsed will be
	 *         discarded. Therefore, it is advised to use this function only with stopped timers.
	 */
	public long getTotal()
	{
		return _totalTime;
	}

	/**
	 * @return the total number of times this timer has been started and stopped. Note that recursive start operations are computed only once so actual number
	 *         of times {@link #start()} function is called may be greater than this amount.
	 */
	public long getCount()
	{
		return _count;
	}

	/**
	 * @return the timeout associated with this timer.
	 */
	public long getTimeout()
	{
		return _timeout;
	}

	/**
	 * @return the total time spent (in milliseconds) divided by the number of times this timer has been ran. If the timer is still running elapsed time is
	 *         discarded. Therefore, it is advised to use this function only with stopped timers.
	 */
	public double getAverage()
	{
		return _totalTime / (_count == 0 ? 1.0 : _count);
	}

	/**
	 * @return the total time spent between last start()-stop() period.
	 */
	public long getLast()
	{
		return _lastTime;
	}

	/**
	 * Set a timeout limit for this timer. Set the timeout to 0 to disable timeout checking
	 *
	 * @param timeout
	 */
	public void setTimeout(final long timeout)
	{
		if (timeout < 0)
			throw new IllegalArgumentException("Cannot set the timeout to a negative value!");

		_timeout = timeout;
	}

	@Override
	public String toString()
	{
		if (_startCount > 0)
			return "Timer " + _name + " Avg: " + getAverage() + " Count: " + _count + " Total: " + getTotal() + " Still running: " + _startCount;

		return "Timer " + _name + " Avg: " + getAverage() + " Count: " + _count + " Total: " + getTotal();
	}

	/**
	 * Return the parent timer of this timer depends on. Parent timers are checked hierarchically for timeouts.
	 *
	 * @return Parent timer or null if there is no such timer.
	 */
	public Timer getParent()
	{
		return _parent;
	}

	public String format()
	{
		return DurationFormat.LONG.format(getTotal() + getElapsed());
	}
}
