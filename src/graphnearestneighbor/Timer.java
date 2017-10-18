/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * basic timer class
 */
final class Timer {
	private long startTime;
	private double duration;
	
	Timer() {
		start();
	}
	
	void start() {
		startTime = getCpuTime();
	}
	
	double stop() {
		duration = (getCpuTime() - startTime)/ 1000000000.0;
		return duration;
	}

	static private long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadCpuTime( ) : 0L;
    }
}
