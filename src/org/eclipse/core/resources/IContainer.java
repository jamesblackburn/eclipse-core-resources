/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.*;

/**
 * Interface for resources which may contain
 * other resources (termed its <em>members</em>). While the 
 * workspace itself is not considered a container in this sense, the
 * workspace root resource is a container.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Containers implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 * @see IProject
 * @see IFolder
 * @see IWorkspaceRoot
 */
public interface IContainer extends IResource, IAdaptable {
	
	/*====================================================================
	 * Constants defining which members are wanted:
	 *====================================================================*/
	
	/**
	 * Member constant (bit mask value 1) indicating that phantom member resources are
	 * to be included.
	 * 
	 * @see IResource#isPhantom
	 * @since 2.0
	 */
	public static final int INCLUDE_PHANTOMS = 1;

	/**
	 * Member constant (bit mask value 2) indicating that team private members are
	 * to be included.
	 * 
	 * @see IResource#isTeamPrivateMember
	 * @since 2.0
	 */
	public static final int INCLUDE_TEAM_PRIVATE_MEMBERS = 2;

	
/**
 * Returns whether a resource of some type with the given path 
 * exists relative to this resource.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.  Trailing separators are ignored.
 * If the path is empty this container is checked for existence.
 *
 * @param path the path of the resource
 * @return <code>true</code> if a resource of some type with the given path 
 *     exists relative to this resource, and <code>false</code> otherwise
 * @see IResource#exists
 */
public boolean exists(IPath path);
/**
 * Finds and returns the member resource (project, folder, or file)
 * with the given name in this container, or <code>null</code> if no such
 * resource exists.
 * 
 * <p> N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * resource existing at the calculated path in the workspace.
 * </p>
 *
 * @param name the string name of the member resource
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 */
public IResource findMember(String name);
/**
 * Finds and returns the member resource (project, folder, or file)
 * with the given name in this container, or <code>null</code> if 
 * there is no such resource.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only a member resource with the given name that exists will be returned.
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the method also returns a phantom member resource with 
 * the given name that the workspace is keeping track of.
 * </p>
 * <p>
 * Note that no attempt is made to exclude team-private member resources
 * as with <code>members</code>.
 * </p>
 * <p>
 * N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * existing resource (or phantom) in the workspace.
 * </p>
 *
 * @param name the string name of the member resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 * @see #members
 * @see IResource#isPhantom
 */
public IResource findMember(String name, boolean includePhantoms);
/**
 * Finds and returns the member resource identified by the given path in
 * this container, or <code>null</code> if no such resource exists.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.   Trailing separators are ignored.
 * If the path is empty this container is returned.
 * <p>
 * Note that no attempt is made to exclude team-private member resources
 * as with <code>members</code>.
 * </p>
 * <p> N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * resource existing at the calculated path in the workspace.
 * </p>
 *
 * @param path the path of the desired resource
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 */
public IResource findMember(IPath path);
/**
 * Finds and returns the member resource identified by the given path in
 * this container, or <code>null</code> if there is no such resource.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.  Trailing separators are ignored.
 * If the path is empty this container is returned.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only a resource that exists at the given path will be returned.
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the method also returns a phantom member resource at the given path
 * that the workspace is keeping track of.
 * </p>
 * <p>
 * Note that no attempt is made to exclude team-private member resources
 * as with <code>members</code>.
 * </p>
 * <p>
 * N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * existing resource (or phantom) at the calculated path in the workspace.
 * </p>
 *
 * @param path the path of the desired resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 * @see #members(boolean)
 * @see IResource#isPhantom
 */
public IResource findMember(IPath path, boolean includePhantoms);
/**
 * Returns a handle to the file identified by the given path in this
 * container.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * <p>
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource and is appended
 * to this container's full path to form the full path of the resultant resource.
 * A trailing separator is ignored. The path resulting resource will 
 * have at least 3 segments.
 * </p>
 *
 * @param path the path of the member file
 * @return the (handle of the) member file
 * @see #getFolder
 */
public IFile getFile(IPath path);
/**
 * Returns a handle to the folder identified by the given path in this
 * container.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * <p>
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource and is appended
 * to this container's full path to form the full path of the resultant resource.
 * A trailing separator is ignored. The path of the resulting resource will 
 * have at least 2 segments.
 * </p>
 *
 * @param path the path of the member folder
 * @return the (handle of the) member folder
 * @see #getFile
 */
public IFolder getFolder(IPath path);

/**
 * Returns a list of existing member resources (projects, folders and files)
 * in this resource, in no particular order.
 * <p>
 * This is a convenience method, fully equivalent to <code>members(IResource.NONE)</code>.
 * Team-private member resources are <b>not</b> included in the result.
 * </p>
 * <p>
 * Note that the members of a project or folder are the files and folders
 * immediately contained within it.  The members of the workspace root
 * are the projects in the workspace.
 * </p>
 *
 * @return an array of members of this resource
 * @exception CoreException if this request fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource is a project that is not open.</li>
 * </ul>
 * @see #findMember
 * @see IResource#isAccessible
 */
public IResource[] members() throws CoreException;

/**
 * Returns a list of all member resources (projects, folders and files)
 * in this resource, in no particular order.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   members(includePhantoms ? INCLUDE_PHANTOMS : IResource.NONE);
 * </pre>
 * Team-private member resources are <b>not</b> included in the result.
 * </p>
 *
 * @return an array of members of this resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest.
 * @exception CoreException if this request fails. Reasons include:
 * <ul>
 * <li> <code>includePhantoms</code> is <code>false</code> and
 *     this resource does not exist.</li>
 * <li> <code>includePhantoms</code> is <code>false</code> and
 *     this resource is a project that is not open.</li>
 * </ul>
 * @see #members(int)
 * @see IResource#exists
 * @see IResource#isPhantom
 */
public IResource[] members(boolean includePhantoms) throws CoreException;

/**
 * Returns a list of all member resources (projects, folders and files)
 * in this resource, in no particular order.
 * <p>
 * If the <code>INCLUDE_PHANTOMS</code> flag is not specified in the member 
 * flags (recommended), only member resources that exist will be returned.
 * If the <code>INCLUDE_PHANTOMS</code> flag is specified,
 * the result will also include any phantom member resources the
 * workspace is keeping track of.
 * </p>
 * <p>
 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is specified 
 * in the member flags, team private members will be included along with
 * the others. If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag
 * is not specified (recommended), the result will omit any team private
 * member resources.
 * </p>
 *
 * @param memberFlags bit-wise or of member flag constants
 *   (<code>INCLUDE_PHANTOMS</code> and <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code>)
 *   indicating which members are of interest
 * @return an array of members of this resource
 * @exception CoreException if this request fails. Reasons include:
 * <ul>
 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 *     this resource does not exist.</li>
 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
 *     this resource is a project that is not open.</li>
 * </ul>
 * @see IResource#exists
 * @since 2.0
 */
public IResource[] members(int memberFlags) throws CoreException;

/**
 * Returns a list of recently deleted files inside this container that are 
 * have one or more saved states in the local history. The depth parameter
 * determines how deep inside the container to look. This resource may or
 * may not exist in the workspace.
 * <p>
 * When applied to an existing project resource, this method returns recently 
 * deleted files with saved states in that project. Note that local history is
 * maintained with each individual project, and gets discarded when a project
 * is deleted from the workspace. If applied to a deleted project, this method
 * returns the empty list.
 * </p>
 * <p>
 * When applied to the workspace root resource (depth infinity), this method
 * returns all recently deleted files with saved states in all existing projects.
 * </p>
 * <p>
 * When applied to a folder (or project) resource (depth one),
 * this method returns all recently deleted member files with saved states.
 * </p>
 * <p>
 * When applied to a folder resource (depth zero),
 * this method returns an empty list unless there was a recently deleted file
 * with saved states with at same path as the folder.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param depth depth limit: one of <code>DEPTH_ZERO</code>, <code>DEPTH_ONE</code>
 *    or <code>DEPTH_INFINITE</code>
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return an array of recently deleted files
 * @exception CoreException if this method fails
 * @see IFile#getHistory
 * @since 2.0
 */
public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException;

}