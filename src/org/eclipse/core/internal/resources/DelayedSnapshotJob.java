/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.events.EventStats;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class DelayedSnapshotJob extends Job {

	private static final String MSG_SNAPSHOT = Policy.bind("resources.snapshot"); //$NON-NLS-1$
	private SaveManager saveManager;

	public DelayedSnapshotJob(SaveManager manager) {
		super(MSG_SNAPSHOT);
		this.saveManager = manager;
	}
	/*
	 * @see Job#run()
	 */
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		if (ResourcesPlugin.getWorkspace() == null)
			return Status.OK_STATUS;
		IStatus result = null;
		try {
			EventStats.startSnapshot();
			result = saveManager.save(ISaveContext.SNAPSHOT, null, Policy.monitorFor(null));
		} catch (CoreException e) {
			result = e.getStatus();
		} finally {
			saveManager.operationCount = 0;
			saveManager.snapshotRequested = false;
			EventStats.endSnapshot();
		}
		return result;
	}
}