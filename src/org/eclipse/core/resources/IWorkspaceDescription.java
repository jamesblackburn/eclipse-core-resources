/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * A workspace description represents the workspace preferences. It can be
 * used to query the current preferences and set new ones.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkspace#getDescription()
 * @see IWorkspace#setDescription(IWorkspaceDescription)
 */
public interface IWorkspaceDescription {
	/**
	 * Returns the order in which projects in the workspace should be built.
	 * The returned value is <code>null</code> if the workspace's default build
	 * order is being used.
	 *
	 * @return the names of projects in the order they will be built, 
	 *   or <code>null</code> if the default build order should be used
	 * @see #setBuildOrder(String[])
	 */
	public String[] getBuildOrder();

	/**
	 * Returns the maximum length of time, in milliseconds, a file state should be 
	 * kept in the local history.  
	 *
	 * @return the maximum time a file state should be kept in the local history
	 *   represented in milliseconds
	 * @see #setFileStateLongevity(long)
	 */
	public long getFileStateLongevity();

	/**
	 * Returns the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * 
	 * @return the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * @see #setMaxBuildIterations(int)
	 * @since 2.1
	 */
	public int getMaxBuildIterations();

	/**
	 * Returns the maximum number of states per file that can be stored in the local history.
	 *
	 * @return the maximum number of states per file that can be stored in the local history
	 * @see #setMaxFileStates(int)
	 */
	public int getMaxFileStates();

	/**
	 * Returns the maximum permited size of a file, in bytes, to be stored in the
	 * local history.
	 *
	 * @return the maximum permited size of a file to be stored in the local history
	 * @see #setMaxFileStateSize(long)
	 */
	public long getMaxFileStateSize();

	/**
	 * Returns the interval between automatic workspace snapshots.
	 *
	 * @return the amount of time in milliseconds between automatic workspace snapshots
	 * @see #setSnapshotInterval(long)
	 * @since 2.0
	 */
	public long getSnapshotInterval();

	/**
	 * Returns whether this workspace performs auto-builds.
	 *
	 * @return <code>true</code> if auto-building is on, otherwise
	 *		<code>false</code>
	 * @see #setAutoBuilding(boolean)
	 */
	public boolean isAutoBuilding();

	/**
	 * Records whether this workspace performs auto-builds.
	 * <p>
	 * When auto-build is on, any changes made to a project and its
	 * resources automatically triggers an incremental build of that
	 * project. If resources in several projects are changed within the
	 * scope of a workspace runnable, the affected projects are auto-built
	 * in no particular order.
	 * </p>
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param value <code>true</code> to turn on auto-building,
	 *  and <code>false</code> to turn it off
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #isAutoBuilding()
	 */
	public void setAutoBuilding(boolean value);

	/**
	 * Sets the order in which projects in the workspace should be built.
	 * Projects not named in this list are built in a default order defined
	 * by the workspace.  Set this value to <code>null</code> to use the
	 * default ordering for all projects.  Projects not named in the list are 
	 * built in unspecified order after all ordered projects.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param value the names of projects in the order in which they are built,
	 *   or <code>null</code> to use the workspace's default order for all projects
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getBuildOrder()
	 */
	public void setBuildOrder(String[] value);

	/**
	 * Sets the maximum time, in milliseconds, a file state should be kept in the
	 * local history.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param time the maximum number of milliseconds a file state should be 
	 * 		kept in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getFileStateLongevity()
	 */
	public void setFileStateLongevity(long time);

	/**
	 * Sets the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param number the maximum number of times that the workspace should rebuild
	 * when builders affect projects that have already been built.
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxBuildIterations()
	 * @since 2.1
	 */
	public void setMaxBuildIterations(int number);

	/**
	 * Sets the maximum number of states per file that can be stored in the local history.
	 * If the maximum number is reached, older states are removed in favor of
	 * new ones.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param number the maximum number of states per file that can be stored in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxFileStates()
	 */
	public void setMaxFileStates(int number);

	/**
	 * Sets the maximum permited size of a file, in bytes,  to be stored in the
	 * local history.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param size the maximum permited size of a file to be stored in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxFileStateSize()
	 */
	public void setMaxFileStateSize(long size);

	/**
	 * Sets the interval between automatic workspace snapshots.  The new interval
	 * will only take effect after the next snapshot.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param delay the amount of time in milliseconds between automatic workspace snapshots
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getSnapshotInterval()
	 * @since 2.0
	 */
	public void setSnapshotInterval(long delay);
}