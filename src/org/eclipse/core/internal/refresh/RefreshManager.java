/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.refresh;

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.IRefreshResult;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

/**
 * Manages auto-refresh functionality, including maintaining the active
 * set of monitors and controlling the job that performs periodic refreshes
 * on out of sync resources.
 * 
 * @since 3.0
 */
public class RefreshManager implements IRefreshResult, IManager, Preferences.IPropertyChangeListener {
	public static boolean DEBUG = Policy.DEBUG_AUTO_REFRESH;
	public static final String DEBUG_PREFIX = "Auto-refresh: "; //$NON-NLS-1$
	MonitorManager monitors;
	private RefreshJob refreshJob;

	/**
	 * The workspace.
	 */
	private IWorkspace workspace;

	public RefreshManager(IWorkspace workspace) {
		this.workspace = workspace;
	}

	/*
	 * Starts or stops auto-refresh depending on the auto-refresh preference.
	 */
	protected void manageAutoRefresh(boolean enabled) {
		if (enabled) {
			refreshJob.start();
			monitors.start();
		} else {
			refreshJob.stop();
			monitors.stop();
		}
	}

	public void monitorFailed(IRefreshMonitor monitor, IResource resource) {
	}

	/**
	 * Checks for changes to the the PREF_AUTO_UPDATE property.
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (ResourcesPlugin.PREF_AUTO_REFRESH.equals(property)) {
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			boolean autoRefresh = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
			manageAutoRefresh(autoRefresh);
		}
	}

	/* (non-Javadoc)
	 * @see IRefreshResult#refresh
	 */
	public void refresh(IResource resources) {
		refreshJob.refresh(resources);
	}

	/**
	 * Shuts down the refresh manager.  This only happens when
	 * the resources plugin is going away.
	 */
	public void shutdown(IProgressMonitor monitor) {
		ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(this);
		if (monitors != null) {
			monitors.stop();
			monitors = null;
		}
		if (refreshJob != null) {
			refreshJob.stop();
			refreshJob = null;
		}
	}

	/**
	 * Initializes the refresh manager. This does a minimal amount of work
	 * if autobuild is turned off.
	 */
	public void startup(IProgressMonitor monitor) {
		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		preferences.setDefault(ResourcesPlugin.PREF_AUTO_REFRESH, false);
		preferences.addPropertyChangeListener(this);

		refreshJob = new RefreshJob();
		monitors = new MonitorManager(workspace, this);
		boolean autoRefresh = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
		if (autoRefresh)
			manageAutoRefresh(autoRefresh);
	}
}