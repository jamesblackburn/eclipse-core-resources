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
 
import java.util.*;

import org.eclipse.core.internal.localstore.HistoryStore;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public abstract class Container extends Resource implements IContainer {
protected Container(IPath path, Workspace container) {
	super(path, container);
}
/**
 * Converts this resource and all its children into phantoms by modifying
 * their resource infos in-place.
 */
public void convertToPhantom() throws CoreException {
	if (isPhantom())
		return;
	super.convertToPhantom();
	IResource[] members = members(IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	for (int i = 0; i < members.length; i++)
		 ((Resource) members[i]).convertToPhantom();
}
/** 
 * @see IContainer
 */
public boolean exists(IPath path) {
	return workspace.getResourceInfo(getFullPath().append(path), false, false) != null;
}
/**
 * @see IContainer#findMember(String)
 */
public IResource findMember(String name) {
	return findMember(name, false);
}
/**
 * @see IContainer#findMember(String, boolean)
 */
public IResource findMember(String name, boolean phantom) {
	IPath childPath = getFullPath().append(name);
	ResourceInfo info = workspace.getResourceInfo(childPath, phantom, false);
	return info == null ? null : workspace.newResource(childPath, info.getType());
}
/**
 * @see IContainer#findMember(IPath)
 */
public IResource findMember(IPath path) {
	return findMember(path, false);
}
/**
 * @see IContainer#findMember(IPath)
 */
public IResource findMember(IPath path, boolean phantom) {
	path = getFullPath().append(path);
	ResourceInfo info = workspace.getResourceInfo(path, phantom, false);
	return (info == null) ? null : workspace.newResource(path, info.getType());
}
/**
 */
protected void fixupAfterMoveSource() throws CoreException {
	super.fixupAfterMoveSource();
	if (!synchronizing(getResourceInfo(false, false)))
		return;
	IResource[] members = members(IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	for (int i = 0; i < members.length; i++)
		 ((Resource) members[i]).fixupAfterMoveSource();
}
protected IResource[] getChildren(Container parent, boolean phantom) {
	return getChildren(parent.getFullPath(), phantom);
}
protected IResource[] getChildren(IPath parentPath, boolean phantom) {
	IPath[] children = null;
	try {
		children = workspace.tree.getChildren(parentPath);
	} catch (IllegalArgumentException e) {
		//concurrency problem: the container has been deleted by another 
		//thread during this call.  Just return empty children set
	}
	if (children == null || children.length == 0)
		return ICoreConstants.EMPTY_RESOURCE_ARRAY;
	Resource[] result = new Resource[children.length];
	int j = 0;
	for (int i = 0; i < children.length; i++) {
		ResourceInfo info = workspace.getResourceInfo(children[i], phantom, false);
		if (info != null)
			result[j++] = workspace.newResource(children[i], info.getType());
	}
	if (j == result.length) {
		return result;
	} else {
		Resource[] trimmedResult = new Resource[j];
		System.arraycopy(result, 0, trimmedResult, 0, j);
		return trimmedResult;
	}
}
/** 
 * @see IContainer#getFile
 */
public IFile getFile(String name) {
	return (IFile)workspace.newResource(getFullPath().append(name), FILE);
}
/** 
 * @see IContainer#getFile
 */
public IFile getFile(IPath path) {
	return (IFile)workspace.newResource(getFullPath().append(path), FILE);
}
/** 
 * @see IContainer#getFolder
 */
public IFolder getFolder(String name) {
	return (IFolder)workspace.newResource(getFullPath().append(name), FOLDER);
}
/** 
 * @see IContainer#getFolder
 */
public IFolder getFolder(IPath path) {
	return (IFolder)workspace.newResource(getFullPath().append(path), FOLDER);
}
public boolean isLocal(int flags, int depth) {
	if (!super.isLocal(flags, depth))
		return false;
	if (depth == DEPTH_ZERO)
		return true;
	if (depth == DEPTH_ONE)
		depth = DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		if (!children[i].isLocal(depth))
			return false;
	return true;
}

/**
 * @see IContainer#members
 */
public IResource[] members() throws CoreException {
	// forward to central method
	return members(IResource.NONE);
}

/**
 * @see IContainer#members(boolean)
 */
public IResource[] members(boolean phantom) throws CoreException {
	// forward to central method
	return members(phantom ? INCLUDE_PHANTOMS : IResource.NONE);
}

/*
 * @see IContainer
 */
public IResource[] members(int memberFlags) throws CoreException {
	final boolean phantom = (memberFlags & INCLUDE_PHANTOMS) != 0;
	ResourceInfo info = getResourceInfo(phantom, false);
	checkExists(getFlags(info), true);
	IResource[] allMembers = getChildren(this, phantom);
	if ((memberFlags & INCLUDE_TEAM_PRIVATE_MEMBERS) != 0) {
		// if team-private members are wanted, return the whole list
		return allMembers;
	} else {
		// filter out team-private members (if any)
		int teamPrivateMemberCount = 0;
		// make a quick first pass to see if there is anything to exclude
		for (int i = 0; i < allMembers.length; i++) {
			Resource child = (Resource) allMembers[i];
			ResourceInfo childInfo = child.getResourceInfo(phantom, false);
			if (isTeamPrivateMember(getFlags(childInfo))) {
				teamPrivateMemberCount++;
				allMembers[i] = null;//null array entry so we know not to include it
			}
		}
		// common case: nothing to exclude
		if (teamPrivateMemberCount == 0) {
			return allMembers;
		}
		// make a second pass to copy the ones we want
		IResource[] reducedMembers = new IResource[allMembers.length - teamPrivateMemberCount];
		int nextPosition = 0;
		for (int i = 0; i < allMembers.length; i++) {
			Resource child = (Resource) allMembers[i];
			if (child != null) {
				reducedMembers[nextPosition++] = child;
			}
		}
		return reducedMembers;
	}		
}

/**
 * @see IContainer#findDeletedMembersWithHistory
 */
public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
	HistoryStore historyStore = getLocalManager().getHistoryStore();
	IPath basePath = getFullPath();
	IWorkspaceRoot root = getWorkspace().getRoot();
	Set deletedFiles = new HashSet();
	
	if (depth == IResource.DEPTH_ZERO) {
		// this folder might have been a file in a past life
		if (historyStore.getStates(basePath).length > 0) {
			IFile file = root.getFile(basePath);
			if (!file.exists()) {
				deletedFiles.add(file);
			}
		}
	} else {
		Set allFilePaths = historyStore.allFiles(basePath, depth);
		// convert IPaths to IFiles keeping only files that no longer exist
		for (Iterator it= allFilePaths.iterator(); it.hasNext(); ) {
			IPath filePath = (IPath) it.next();
			IFile file = root.getFile(filePath);
			if (!file.exists()) {
				deletedFiles.add(file);
			}
		}
	}
	return (IFile[]) deletedFiles.toArray(new IFile[deletedFiles.size()]);
}

}
