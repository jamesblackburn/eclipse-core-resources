package org.eclipse.core.internal.utils;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import java.util.*;
/**
 * A placeholder for statistics about the runtime behaviour
 * of a particular plugin.  PluginStats objects are used internally
 * by the CoreStats mechanism.
 */
class PluginStats {
	protected String pluginID;
	protected long notificationRunningTime = 0;
	protected long buildRunningTime = 0;
	protected int notificationCount = 0;
	protected int buildCount = 0;
	protected Vector exceptions = new Vector();
/**
 * PluginStats constructor comment.
 */
PluginStats(String pluginID) {
	this.pluginID = pluginID;
}
void addBuild(long elapsed) {
	buildCount++;
	buildRunningTime += elapsed;
}
void addException(Exception e) {
	exceptions.addElement(e);
}
void addNotify(long elapsed) {
	notificationCount++;
	notificationRunningTime += elapsed;
}
int getBuildCount() {
	return buildCount;
}
long getBuildRunningTime() {
	return buildRunningTime;
}
Enumeration getCoreExceptions() {
	Vector runtime = new Vector();
	for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
		Exception next = (Exception)e.nextElement();
		if (next instanceof CoreException) {
			runtime.addElement(next);
		}
	}
	return runtime.elements();
}
int getExceptionCount() {
	return exceptions.size();
}
String getName() {
	return pluginID;
}
int getNotifyCount() {
	return notificationCount;
}
long getNotifyRunningTime() {
	return notificationRunningTime;
}
Enumeration getRuntimeExceptions() {
	Vector runtime = new Vector();
	for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
		Exception next = (Exception) e.nextElement();
		if (next instanceof RuntimeException) {
			runtime.addElement(next);
		}
	}
	return runtime.elements();
}
long getTotalRunningTime() {
	return notificationRunningTime + buildRunningTime;
}
}
