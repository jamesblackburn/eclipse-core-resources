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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class Folder extends Container implements IFolder {
	protected Folder(IPath path, Workspace container) {
		super(path, container);
	}

	protected void assertCreateRequirements(IPath location, int updateFlags) throws CoreException {
		checkDoesNotExist();
		Container parent = (Container) getParent();
		ResourceInfo info = parent.getResourceInfo(false, false);
		parent.checkAccessible(getFlags(info));
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), message, null);
		}

		java.io.File localFile = location.toFile();
		final boolean force = (updateFlags & IResource.FORCE) != 0;
		if (!force && localFile.exists()) {
			//return an appropriate error message for case variant collisions
			if (!CoreFileSystemLibrary.isCaseSensitive()) {
				String name = getLocalManager().getLocalName(localFile);
				if (name != null && !localFile.getName().equals(name)) {
					String msg = Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString()); //$NON-NLS-1$
					throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), msg, null);
				}
			}
			String msg = Policy.bind("resources.fileExists", localFile.getAbsolutePath()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), msg, null);
		}
	}

	/* (non-Javadoc)
	 * Changes this folder to be a file in the resource tree and returns the newly
	 * created file.  All related properties are deleted.  It is assumed that on
	 * disk the resource is already a file so no action is taken to delete the disk
	 * contents.
	 * <p>
	 * <b>This method is for the exclusive use of the local refresh mechanism</b>
	 *
	 * @see org.eclipse.core.internal.localstore.RefreshLocalVisitor#folderToFile(UnifiedTreeNode, Resource)
	 */
	public IFile changeToFile() throws CoreException {
		getPropertyManager().deleteProperties(this, IResource.DEPTH_INFINITE);
		IFile result = workspace.getRoot().getFile(path);
		if (isLinked()) {
			IPath location = getRawLocation();
			delete(IResource.NONE, null);
			result.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);
		} else {
			workspace.deleteResource(this);
			workspace.createResource(result, false);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see IFolder#create(int, boolean, IProgressMonitor)
	 */
	public void create(int updateFlags, boolean local, IProgressMonitor monitor) throws CoreException {
		final boolean force = (updateFlags & IResource.FORCE) != 0;
		monitor = Policy.monitorFor(monitor);
		try {
			String message = Policy.bind("resources.creating", getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);
			checkValidPath(path, FOLDER, true);
			final ISchedulingRule rule = workspace.getRuleFactory().createRule(this);
			try {
				workspace.prepareOperation(rule, monitor);
				IPath location = getLocalManager().locationFor(this);
				assertCreateRequirements(location, updateFlags);
				workspace.beginOperation(true);
				java.io.File localFile = location.toFile();
				if (force && !CoreFileSystemLibrary.isCaseSensitive() && localFile.exists()) {
					String name = getLocalManager().getLocalName(localFile);
					if (name == null || localFile.getName().equals(name)) {
						delete(true, null);
					} else {
						// The file system is not case sensitive and a case variant exists at this location
						String msg = Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString()); //$NON-NLS-1$
						throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), msg, null);
					}
				}
				internalCreate(force, local, Policy.subMonitorFor(monitor, Policy.opWork));
				workspace.getAliasManager().updateAliases(this, getLocation(), IResource.DEPTH_ZERO, monitor);
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IFolder#create(boolean, boolean, IProgressMonitor)
	 */
	public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		// funnel all operations to central method
		create((force ? IResource.FORCE : IResource.NONE), local, monitor);
	}

	/** 
	 * Ensures that this folder exists in the workspace. This is similar in
	 * concept to mkdirs but it does not work on projects.
	 * If this folder is created, it will be marked as being local.
	 */
	public void ensureExists(IProgressMonitor monitor) throws CoreException {
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		if (exists(flags, true))
			return;
		if (exists(flags, false)) {
			String message = Policy.bind("resources.folderOverFile", getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.RESOURCE_WRONG_TYPE, getFullPath(), message, null);
		}
		Container parent = (Container) getParent();
		if (parent.getType() == PROJECT) {
			info = parent.getResourceInfo(false, false);
			parent.checkExists(getFlags(info), true);
		} else
			((Folder) parent).ensureExists(monitor);
		internalCreate(true, true, monitor);
	}

	/* (non-Javadoc)
	 * @see IContainer#getDefaultCharset(boolean)
	 */
	public String getDefaultCharset(boolean checkImplicit) throws CoreException {
		// non-existing resources default to parent's charset
		if (!exists())
			return checkImplicit ? workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true) : null;
		return workspace.getCharsetManager().getCharsetFor(getFullPath(), checkImplicit);
	}

	/* (non-Javadoc)
	 * @see IResource#getType()
	 */
	public int getType() {
		return FOLDER;
	}

	public void internalCreate(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = Policy.bind("resources.creating", getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);
			workspace.createResource(this, false);
			if (local) {
				try {
					getLocalManager().write(this, force, Policy.subMonitorFor(monitor, Policy.totalWork * 75 / 100));
				} catch (CoreException e) {
					// a problem happened creating the folder on disk, so delete from the workspace
					workspace.deleteResource(this);
					throw e; // rethrow
				}
			}
			setLocal(local, DEPTH_ZERO, Policy.subMonitorFor(monitor, Policy.totalWork * 25 / 100));
			if (!local)
				getResourceInfo(true, true).clearModificationStamp();
		} finally {
			monitor.done();
		}
	}
}