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
 * Folders may be leaf or non-leaf resources and may contain files and/or other folders.
 * A folder resource is stored as a directory in the local file system.
 * <p>
 * Folders, like other resource types, may exist in the workspace but
 * not be local; non-local folder resources serve as placeholders for
 * folders whose properties have not yet been fetched from a repository.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Folders implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 */
public interface IFolder extends IContainer, IAdaptable {
	
/**
 * Creates a new folder resource as a member of this handle's parent resource.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   create((force ? FORCE : IResource.NONE), local, monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the folder has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param local a flag controlling whether or not the folder will be local
 *    after the creation
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The workspace contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The parent of this resource is a project that is not open.</li>
 * <li> The parent contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file (as opposed to a directory).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a folder and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification.  See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IFolder#create(int,boolean,IProgressMonitor)
 */
public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException;

/**
 * Creates a new folder resource as a member of this handle's parent resource.
 * <p>
 * The <code>FORCE</code> update flag controls how this method deals with
 * cases where the workspace is not completely in sync with the local file 
 * system. If <code>FORCE</code> is not specified, the method will only attempt
 * to create a directory in the local file system if there isn't one already. 
 * This option ensures there is no unintended data loss; it is the recommended
 * setting. However, if <code>FORCE</code> is specified, this method will 
 * be deemed a success even if there already is a corresponding directory.
 * </p>
 * <p>
 * Update flags other than <code>FORCE</code> are ignored.
 * </p>
 * <p>
 * This method synchronizes this resource with the local file system.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the folder has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param updateFlags bit-wise or of update flag constants
 *   (only <code>FORCE</code> is relevant here)
 * @param local a flag controlling whether or not the folder will be local
 *    after the creation
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The workspace contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The parent of this resource is a project that is not open.</li>
 * <li> The parent contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file (as opposed to a directory).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a folder and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification.  See IResourceChangeEvent for more details.</li>
 * </ul>
 * @since 2.0
 */
public void create(int updateFlags, boolean local, IProgressMonitor monitor) throws CoreException;
/**
 * Creates a new folder resource as a member of this handle's parent resource.
 * The folder's contents will be located in the directory specified by the given
 * file system path.
 * <p>
 * The <code>ALLOW_MISSING_LOCAL</code> update flag controls how this 
 * method deals with cases where the local file system directory to be linked does
 * not exist.  If <code>ALLOW_MISSING_LOCAL</code> is specified, the
 * operation will suceed even if the local directory is missing.  If 
 * <code>ALLOW_MISSING_LOCAL</code> is not specified, the operation
 * will fail in the case where the local file system directory does not exist.
 * </p>
 * <p>
 * Update flags other than <code>ALLOW_MISSING_LOCAL</code> are ignored.
 * </p>
 * <p>
 * This method synchronizes this resource with the local file system at the given
 * location.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the folder has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param updateFlags bit-wise or of update flag constants
 *   (only ALLOW_MISSING_LOCAL is relevant here)
 * @param localLocation a file system path where the folder should be linked
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The workspace contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The parent of this resource is not an open project</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system does not exist
 * 	and <code>ALLOW_MISSING_LOCAL</code> is not specified.</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file (as opposed to a directory).</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification.  See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IResource#isLink
 * @since 2.1
 */
public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException;
/**
 * Deletes this resource from the workspace.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   delete((keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this folder has been removed from its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
  * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource could not be deleted for some reason.</li>
 * <li> This resource is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 *
 * @see IResource#delete(int,IProgressMonitor)
 */
public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Returns a handle to the file with the given name in this folder.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * </p>
 *
 * @param name the string name of the member file
 * @return the (handle of the) member file
 * @see #getFolder
 */
public IFile getFile(String name);
/**
 * Returns a handle to the folder with the given name in this folder.
 * <p> 
 * This is a resource handle operation; neither the container
 * nor the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * </p>
 *
 * @param name the string name of the member folder
 * @return the (handle of the) member folder
 * @see #getFile
 */
public IFolder getFolder(String name);
/**
 * Moves this resource so that it is located at the given path.  
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this folder has been removed from its parent and a new folder
 * has been added to the parent of the destination.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param destination the destination path 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this resource could not be moved. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource or one of its descendents is not local.</li>
 * <li> The resource corresponding to the parent destination path does not exist.</li>
 * <li> The resource corresponding to the parent destination path is a closed 
 *      project.</li>
 * <li> A resource at destination path does exist.</li>
 * <li> A resource of a different type exists at the destination path.</li>
 * <li> This resource or one of its descendents is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> The workspace and the local file system are out of sync
 *      at the destination resource or one of its descendents.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 *
 * @see IResource#move(IPath,int,IProgressMonitor)
 */
public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;
}
