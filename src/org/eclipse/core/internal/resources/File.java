/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;

import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class File extends Resource implements IFile {

protected File(IPath path, Workspace container) {
	super(path, container);
}

/*
 * @see IFile
 */
public void appendContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
	final boolean force = (updateFlags & IResource.FORCE) != 0;
	final boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.settingContents", getFullPath().toString()); //$NON-NLS-1$
		monitor.beginTask(message, Policy.totalWork);
		Assert.isNotNull(content, "Content cannot be null."); //$NON-NLS-1$
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			if (workspace.shouldValidate) {
				workspace.validateSave(this);
				info = getResourceInfo(false, false);
				checkAccessible(getFlags(info));
			}
			internalSetContents(content, getLocalManager().locationFor(this), force, keepHistory, true, Policy.subMonitorFor(monitor, Policy.opWork));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}

/**
 * @see IFile
 */
public void appendContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
	appendContents(content, updateFlags, monitor);
}

/**
 * Changes this file to be a folder in the resource tree and returns
 * the newly created folder.  All related
 * properties are deleted.  It is assumed that on disk the resource is
 * already a folder/directory so no action is taken to delete the disk
 * contents.
 * <p>
 * <b>This method is for the exclusive use of the local resource manager</b>
 *
 * @see FileSystemResourceManager#reportChanges
 */
public IFolder changeToFolder() throws CoreException {
	getPropertyManager().deleteProperties(this, IResource.DEPTH_ZERO);
	workspace.deleteResource(this);
	IFolder result = workspace.getRoot().getFolder(path);
	workspace.createResource(result, false);
	return result;
}

/**
 * @see IFile
 */
public void create(InputStream content, int updateFlags, IProgressMonitor monitor)	throws CoreException {
	final boolean force = (updateFlags & IResource.FORCE) != 0;
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.creating", getFullPath().toString()); //$NON-NLS-1$
		monitor.beginTask(message, Policy.totalWork);
		checkValidPath(path, FILE);
		try {
			workspace.prepareOperation();
			checkDoesNotExist();
			Container parent = (Container) getParent();
			ResourceInfo info = parent.getResourceInfo(false, false);
			parent.checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			IPath location = getLocalManager().locationFor(this);
			//location can be null if based on an undefined variable
			if (location == null) {
				message = Policy.bind("localstore.locationUndefined", getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, getFullPath(), message, null);
			}
			java.io.File localFile = location.toFile();
			if (force) {
				if (!CoreFileSystemLibrary.isCaseSensitive()) {
					if (localFile.exists()) {
						String name = getLocalManager().getLocalName(localFile);
						if (name == null || localFile.getName().equals(name)) {
							delete(true, null);
						} else {
							// The file system is not case sensitive and there is already a file
							// under this location.
							message = Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString()); //$NON-NLS-1$
							throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), message, null);
						}
					}
				}
			} else {
				if (localFile.exists()) {
					//return an appropriate error message for case variant collisions
					if (!CoreFileSystemLibrary.isCaseSensitive()) {
						String name = getLocalManager().getLocalName(localFile);
						if (name != null && !localFile.getName().equals(name)) {
							message =  Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString()); //$NON-NLS-1$
							throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), message, null);
						}
					}
					message = Policy.bind("resources.fileExists", localFile.getAbsolutePath()); //$NON-NLS-1$
					throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), message, null);
				}
			}
			monitor.worked(Policy.opWork * 40 / 100);

			workspace.createResource(this, false);
			boolean local = content != null;
			if (local) {
				try {
					internalSetContents(content, location, force, false, false, Policy.subMonitorFor(monitor, Policy.opWork * 40 / 100));
				} catch (CoreException e) {
					// a problem happened creating the file on disk, so delete from the workspace and disk
					workspace.deleteResource(this);
					location.toFile().delete();
					throw e; // rethrow
				}
			}
			setLocal(local, DEPTH_ZERO, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
			if (!local)
				getResourceInfo(true, true).setModificationStamp(IResource.NULL_STAMP);
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
		ensureClosed(content);
	}
}

/**
 * @see IFile
 */
public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	create(content, (force ? IResource.FORCE : IResource.NONE), monitor);
}

/**
 * IFile API methods require that the stream be closed regardless
 * of the success of the method.  This method makes a best effort
 * at closing the stream, and ignores any resulting IOException.
 */
protected void ensureClosed(InputStream stream) {
	if (stream != null) {
		try {
			stream.close();
		} catch (IOException e) {
		}
	}
}
/**
 * @see IFile#getContents
 */
public InputStream getContents() throws CoreException {
	return getContents(false);
}
/**
 * @see IFile
 */
public InputStream getContents(boolean force) throws CoreException {
	ResourceInfo info = getResourceInfo(false, false);
	int flags = getFlags(info);
	checkAccessible(flags);
	checkLocal(flags, DEPTH_ZERO);
	return getLocalManager().read(this, force, null);
}
/**
 * @see IFile#getEncoding()
 */
public int getEncoding() throws CoreException {
	ResourceInfo info = getResourceInfo(false, false);
	int flags = getFlags(info);
	checkAccessible(flags);
	checkLocal(flags, DEPTH_ZERO);
	return getLocalManager().getEncoding(this);
}
/**
 * @see IFile#getHistory
 */
public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
	// FIXME: monitor is not used
	return getLocalManager().getHistoryStore().getStates(getFullPath());
}
public int getType() {
	return FILE;
}
protected void internalSetContents(InputStream content, IPath location, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
	if (content == null)
		content = new ByteArrayInputStream(new byte[0]);
	getLocalManager().write(this, location, content, force, keepHistory, append, monitor);
	ResourceInfo info = getResourceInfo(false, true);
	info.incrementContentId();
	workspace.updateModificationStamp(info);
	updateProjectDescription();
	workspace.getAliasManager().updateAliases(this, location, IResource.DEPTH_ZERO, monitor);
}

/*
 * @see IFile
 */
public void setContents(IFileState content, int updateFlags, IProgressMonitor monitor) throws CoreException {
	setContents(content.getContents(), updateFlags, monitor);
}

/*
 * @see IFile
 */
public void setContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
	final boolean force = (updateFlags & IResource.FORCE) != 0;
	final boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.settingContents", getFullPath().toString()); //$NON-NLS-1$
		monitor.beginTask(message, Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			if (workspace.shouldValidate) {
				workspace.validateSave(this);
				info = getResourceInfo(false, false);
				checkAccessible(getFlags(info));
			}				
			internalSetContents(content, getLocalManager().locationFor(this), force, keepHistory, false, Policy.subMonitorFor(monitor, Policy.opWork));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
		ensureClosed(content);
	}
}
/**
 * If this file represents a project description file (.project), then force
 * an update on the project's description.
 * 
 * This method is called whenever it is discovered that a file has
 * been modified (added, removed, or changed).
 */
public void updateProjectDescription() throws CoreException {
	if (path.segmentCount() == 2 && path.segment(1).equals(IProjectDescription.DESCRIPTION_FILE_NAME))
		((Project)getProject()).updateDescription();
}

/**
 * @see IFile
 */
public void setContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
	setContents(content, updateFlags, monitor);
}

/**
 * @see IFile#setContents
 */
public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
	setContents(source.getContents(), updateFlags, monitor);
}




}
