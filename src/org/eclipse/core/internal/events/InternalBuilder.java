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
package org.eclipse.core.internal.events;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

/**
 * This class is the internal basis for all builders. Plugin developers should not
 * subclass this class.
 * 
 * @see IncrementalProjectBuilder
 */
public abstract class InternalBuilder {
	private static BuildManager buildManager = ((Workspace) ResourcesPlugin.getWorkspace()).getBuildManager();
	private boolean forgetStateRequested = false;
	private IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
	/**
	 * Human readable builder name for progress reporting.
	 */
	private String label;
	private String natureId;
	private ElementTree oldState;
	private IPluginDescriptor pluginDescriptor;
	private IProject project;
	/**
	 *  @see IncrementalProjectBuilder#build
	 */
	protected abstract IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException;
	/**
	 * Clears the request to forget last built states.
	 */
	final void clearForgetLastBuiltState() {
		forgetStateRequested = false;
	}
	/**
	 * @see IncrementalProjectBuilder#forgetLastBuiltState
	 */
	protected void forgetLastBuiltState() {
		oldState = null;
		forgetStateRequested = true;
	}
	/**
	 * @see IncrementalProjectBuilder#forgetLastBuiltState
	 */
	protected IResourceDelta getDelta(IProject project) {
		return buildManager.getDelta(project);
	}
	final IProject[] getInterestingProjects() {
		return interestingProjects;
	}
	final String getLabel() {
		return label;
	}
	final ElementTree getLastBuiltTree() {
		return oldState;
	}
	/**
	 * Returns the ID of the nature that owns this builder. Returns null if the
	 * builder does not belong to a nature.
	 */
	final String getNatureId() {
		return natureId;
	}
	final IPluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
	}
	/**
	 * Returns the project for this builder
	 */
	protected IProject getProject() {
		return project;
	}
	/**
	 * @see IncrementalProjectBuilder#hasBeenBuilt
	 */
	protected boolean hasBeenBuilt(IProject project) {
		return buildManager.hasBeenBuilt(project);
	}
	/**
	 * @see IncrementalProjectBuilder#isInterrupted
	 */
	public boolean isInterrupted() {
		return buildManager.autoBuildJob.isInterrupted();
	}
	/**
	 * @see IncrementalProjectBuilder#needRebuild
	 */
	protected void needRebuild() {
		buildManager.requestRebuild();
	}
	final void setInterestingProjects(IProject[] value) {
		interestingProjects = value;
	}
	final void setLabel(String value) {
		this.label = value;
	}
	final void setLastBuiltTree(ElementTree value) {
		oldState = value;
	}
	final void setNatureId(String id) {
		this.natureId = id;
	}
	final void setPluginDescriptor(IPluginDescriptor value) {
		pluginDescriptor = value;
	}
	/**
	 * Sets the project for which this builder operates.
	 * @see #getProject
	 */
	final void setProject(IProject value) {
		Assert.isTrue(project == null);
		project = value;
	}
	/**
	 * @see IncrementalProjectBuilder#startupOnInitialize
	 */
	protected abstract void startupOnInitialize();
	/**
	 * Returns true if the builder requested that its last built state be
	 * forgetten, and false otherwise.
	 */
	final boolean wasForgetStateRequested() {
		return forgetStateRequested;
	}
}