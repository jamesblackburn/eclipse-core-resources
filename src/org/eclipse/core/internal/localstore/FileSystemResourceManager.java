package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
/**
 * Manages the synchronization between the workspace's view and the file system.  
 */
public class FileSystemResourceManager implements ICoreConstants, IManager {
	protected Workspace workspace;
	protected HistoryStore historyStore;
	protected FileSystemStore localStore;
public FileSystemResourceManager(Workspace workspace) {
	this.workspace = workspace;
	localStore = new FileSystemStore();
}
/**
 * Returns a container for the given file system location or null if there
 * is no mapping for this path. If the path has only one segment, then an 
 * <code>IProject</code> is returned.  Otherwise, the returned object
 * is a <code>IFolder</code>.  This method does NOT check the existence
 * of a folder in the given location. Location cannot be null.
 */
public IContainer containerFor(IPath location) {
	IPath path = resourcePathFor(location);
	if (path == null)
		return null;
	if (path.segmentCount() == 1)
		return getWorkspace().getRoot().getProject(path.segment(0));
	else
		return getWorkspace().getRoot().getFolder(path);
}
public void copy(IResource target, IResource destination, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int totalWork = ((Resource) target).countResources(IResource.DEPTH_INFINITE, false);
		String title = Policy.bind("copying", new String[] { target.getFullPath().toString()});
		monitor.beginTask(title, totalWork);
		// use locationFor() instead of getLocation() to avoid null 
		if (locationFor(destination).toFile().exists())
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, destination.getFullPath(), Policy.bind("resourceExists", null), null);
		CopyVisitor visitor = new CopyVisitor(target, destination, force, monitor);
		UnifiedTree tree = new UnifiedTree(target);
		tree.accept(visitor, IResource.DEPTH_INFINITE);
		IStatus status = visitor.getStatus();
		if (!status.isOK())
			throw new ResourceException(status);
	} finally {
		monitor.done();
	}
}
public void delete(IResource target, boolean force, boolean convertToPhantom, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		Resource resource = (Resource) target;
		int totalWork = resource.countResources(IResource.DEPTH_INFINITE, false);
		totalWork *= 2;
		String title = Policy.bind("deleting", new String[] { resource.getFullPath().toString()});
		monitor.beginTask(title, totalWork);
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Policy.bind("deleteProblem", null), null);
		List skipList = null;
		UnifiedTree tree = null;
		if (target.getType() == IResource.PROJECT)
			tree = new ProjectUnifiedTree((IProject) target);
		else
			tree = new UnifiedTree(target);
		if (!force) {
			IProgressMonitor sub = Policy.subMonitorFor(monitor, totalWork / 2);
			sub.beginTask("", 10000);
			RefreshLocalWithStatusVisitor refreshVisitor = new RefreshLocalWithStatusVisitor(Policy.bind("deleteProblem", null), Policy.bind("resourcesDifferent", null), sub);
			tree.accept(refreshVisitor, IResource.DEPTH_INFINITE);
			status.merge(refreshVisitor.getStatus());
			skipList = refreshVisitor.getAffectedResources();
		}
		DeleteVisitor deleteVisitor = new DeleteVisitor(skipList, force, convertToPhantom, keepHistory, Policy.subMonitorFor(monitor, force ? totalWork : (totalWork / 2)));
		tree.accept(deleteVisitor, IResource.DEPTH_INFINITE);
		status.merge(deleteVisitor.getStatus());
		if (!status.isOK())
			throw new ResourceException(status);
	} finally {
		monitor.done();
	}
}
/**
 * Returns an IFile for the given file system location or null if there
 * is no mapping for this path. This method does NOT check the existence
 * of a file in the given location. Location cannot be null.
 */
public IFile fileFor(IPath location) {
	IPath path = resourcePathFor(location);
	return path != null ? getWorkspace().getRoot().getFile(path) : null;
}
public HistoryStore getHistoryStore() {
	return historyStore;
}
protected IPath getProjectDefaultLocation(IProject project) {
	return Platform.getLocation().append(project.getFullPath());
}
public FileSystemStore getStore() {
	return localStore;
}
protected Workspace getWorkspace() {
	return workspace;
}
public IPath locationFor(IResource target) {
	switch (target.getType()) {
		case IResource.ROOT :
			return Platform.getLocation();
		case IResource.PROJECT :
			Project project = (Project) target.getProject();
			IProjectDescription description = project.internalGetDescription();
			if (description != null && description.getLocation() != null)
				return description.getLocation();
			return getProjectDefaultLocation(project);
		default :
			IPath location = locationFor(target.getProject());
			location = location.append(target.getFullPath().removeFirstSegments(1));
			return location;
	}
}
public void move(IResource target, IPath destination, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("moving", new String[] {target.getFullPath().toString()}), Policy.totalWork);
		IResource resource = null;
		switch (target.getType()) {
			case IResource.PROJECT :
				return; // do nothing
			case IResource.FOLDER :
				resource = getWorkspace().getRoot().getFolder(destination);
				break;
			case IResource.FILE :
				resource = getWorkspace().getRoot().getFile(destination);
				break;
		}
		IPath sourceLocation = locationFor(target);
		IPath destinationLocation = locationFor(resource);
		if (keepHistory) {
			if (target.getType() == IResource.FOLDER) {
				IResource[] children = ((IFolder) target).members();
				destinationLocation.toFile().mkdirs();
				int work = Policy.totalWork / Math.max(children.length, 1);
				for (int i = 0; i < children.length; i++)
					move(children[i], destination.append(children[i].getName()), keepHistory, Policy.subMonitorFor(monitor, work));
				if (!sourceLocation.toFile().delete()) {
					String message = "Could not delete file";
					throw new ResourceException(IResourceStatus.FAILED_DELETE_LOCAL, sourceLocation, message, null);
				}
			} else {
				long lastModified = target.getLocation().toFile().lastModified();
				getHistoryStore().addState(target.getFullPath(), sourceLocation, lastModified, false);
				getStore().move(sourceLocation.toFile(), destinationLocation.toFile(), false, Policy.subMonitorFor(monitor, Policy.totalWork));
			}
		} else
			getStore().move(sourceLocation.toFile(), destinationLocation.toFile(), false, Policy.subMonitorFor(monitor, Policy.totalWork));
	} finally {
		monitor.done();
	}
}
public InputStream read(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
	// thread safety: (the location can be null if the project for this file does not exist)
	IPath location = locationFor(target);
	if (location == null)
		 ((Project) target.getProject()).checkExists(NULL_FLAG, true);
	java.io.File localFile = location.toFile();
	if (!localFile.exists())
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), Policy.bind("fileNotFound", null), null);
	if (!force) {
		ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
		int flags = ((Resource) target).getFlags(info);
		((Resource) target).checkExists(flags, true);
		if (CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath()) != info.getLocalSyncInfo())
			throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), Policy.bind("fileChanged", null), null);
	}
	return getStore().read(localFile);
}
public boolean refresh(IResource target, int depth, IProgressMonitor monitor) throws CoreException {
	switch (target.getType()) {
		case IResource.PROJECT :
			return refreshProject((IProject) target, depth, monitor);
		case IResource.ROOT :
			return refreshRoot((IWorkspaceRoot) target, depth, monitor);
		case IResource.FOLDER :
		case IResource.FILE :
			return refreshResource(target, depth, monitor);
	}
	return false;
}
protected boolean refreshProject(IProject project, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	int totalWork = ((Project) project).countResources(depth, false) + 5000;
	String title = Policy.bind("refreshing", new String[] { project.getFullPath().toString()});
	try {
		monitor.beginTask(title, totalWork);
		if (!project.isAccessible())
			return false;
		RefreshLocalVisitor visitor = new RefreshLocalVisitor(monitor);
		ProjectUnifiedTree tree = new ProjectUnifiedTree(project);
		tree.accept(visitor, depth);
		return visitor.resourcesChanged();
	} finally {
		monitor.done();
	}
}
protected boolean refreshResource(IResource target, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	int totalWork = ((Resource) target).countResources(depth, false) + 5000;
	String title = Policy.bind("refreshing", new String[] {target.getFullPath().toString()});
	try {
		monitor.beginTask(title, totalWork);
		RefreshLocalVisitor visitor = new RefreshLocalVisitor(monitor);
		UnifiedTree tree = new UnifiedTree(target);
		tree.accept(visitor, depth);
		return visitor.resourcesChanged();
	} finally {
		monitor.done();
	}
}
/**
 * Synchronizes the entire workspace with the local filesystem.
 * The current implementation does this by synchronizing each of the
 * projects currently in the workspace.  A better implementation may
 * be possible.
 */
protected boolean refreshRoot(IWorkspaceRoot target, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	IProject[] projects = target.getProjects();
	int totalWork = projects.length;
	String title = Policy.bind("refreshing", new String[] { target.getFullPath().toString()});
	try {
		monitor.beginTask(title, totalWork);
		// if doing depth zero, there is nothing to do (can't refresh the root).  
		// Note that we still need to do the beginTask, done pair.
		if (depth == IResource.DEPTH_ZERO)
			return false;
		boolean changed = false;
		// drop the depth by one level since processing the root counts as one level.
		depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
		for (int i = 0; i < projects.length; i++)
			changed |= refreshProject(projects[i], depth, Policy.subMonitorFor(monitor, 1));
		return changed;
	} finally {
		monitor.done();
	}
}
/**
 * Returns null if there is no mapping for this path or the resource
 * does not exist in the file system.
 */
public IResource resourceFor(IPath location) throws CoreException {
	IPath resourcePath = resourcePathFor(location);
	if (resourcePath == null)
		return null;
	if (resourcePath.equals(Path.ROOT))
		return workspace.getRoot();
	// check the workspace first
	IResource target = getWorkspace().getRoot().findMember(resourcePath);
	if (target != null)
		return target;

	// couldn't find it in the workspace so look in the filesystem
	if (location.toFile().isFile())
		return getWorkspace().getRoot().getFile(resourcePath);
	if (location.toFile().isDirectory())
		return getWorkspace().getRoot().getFolder(resourcePath);

	// can't find any trace of this resource so return null
	return null;
}
/**
 * Returns a resource path to the given local location. Returns null if
 * it is not under a project's location.
 */
protected IPath resourcePathFor(IPath location) {
	if (Platform.getLocation().equals(location))
		return Path.ROOT;
	IProject[] projects = getWorkspace().getRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		IProject project = projects[i];
		IPath projectLocation = project.getLocation();
		if (projectLocation != null && projectLocation.isPrefixOf(location)) {
			int segmentsToRemove = projectLocation.segmentCount();
			return project.getFullPath().append(location.removeFirstSegments(segmentsToRemove));
		}
	}
	return null;
}
public void shutdown(IProgressMonitor monitor) throws CoreException {
	historyStore.shutdown(monitor);
}
public void startup(IProgressMonitor monitor) throws CoreException {
	IPath location = workspace.getMetaArea().getHistoryStoreLocation();
	location.toFile().mkdirs();
	historyStore = new HistoryStore(workspace, location, 256);
	historyStore.startup(monitor);
}
/**
 * The ResourceInfo must be mutable.
 */
public void updateLocalSync(ResourceInfo info, long localSyncInfo, boolean isFile) {
	info.setLocalSyncInfo(localSyncInfo);

	if (localSyncInfo == I_NULL_SYNC_INFO)
		info.clear(M_LOCAL_EXISTS);
	else
		info.set(M_LOCAL_EXISTS);

	if (isFile)
		info.set(M_LOCAL_IS_FILE);
	else
		info.clear(M_LOCAL_IS_FILE);
}
/**
 * The target must exist in the workspace. The content InputStream is
 * closed even if the method fails. If the force flag is false we only write
 * the file if it does not exist or if it is already local and the timestamp
 * has NOT changed since last synchronization, otherwise a CoreException
 * is thrown.
 */
public void write(IFile target, InputStream content, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(null);
	try {
		IPath location = locationFor(target);
		java.io.File localFile = location.toFile();
		long lastModified = CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath());
		if (!force) {
			if (target.isLocal(IResource.DEPTH_ZERO)) {
				// test if timestamp is the same since last synchronization
				ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
				if (lastModified != info.getLocalSyncInfo()) {
					refresh(target, IResource.DEPTH_ZERO, monitor);
					throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), Policy.bind("fileChanged", null), null);
				}
			} else
				if (localFile.exists()) {
					refresh(target, IResource.DEPTH_ZERO, monitor);
					throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), Policy.bind("resourceExists", null), null);
				}
		}
		// add entry to History Store.
		if (keepHistory && localFile.exists())
			historyStore.addState(target.getFullPath(), location, lastModified, !append);
		getStore().write(localFile, content, append, monitor);
		// get the new last modified time and stash in the info
		lastModified = CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath());
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified, true);
	} finally {
		try {
			content.close();
		} catch (IOException e) {
		}
	}
}
/**
 * If force is false, this method fails if there is already a resource in
 * target's location.
 */
public void write(IFolder target, boolean force, IProgressMonitor monitor) throws CoreException {
	java.io.File file = locationFor(target).toFile();
	if (!force)
		if (file.isDirectory())
			throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), Policy.bind("resourceExists", null), null);
		else
			if (file.exists())
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), Policy.bind("fileExists", null), null);
	getStore().writeFolder(file);
	long lastModified = CoreFileSystemLibrary.getLastModified(file.getAbsolutePath());
	ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
	updateLocalSync(info, lastModified, false);
}
/**
 * The target must exist in the workspace.
 */
public void write(IProject target, IProgressMonitor monitor) throws CoreException {
	IPath location = locationFor(target);
	if (location != null)
		getStore().writeFolder(location.toFile());
	getWorkspace().getMetaArea().write((Project) target);
	long lastModified = CoreFileSystemLibrary.getLastModified(getWorkspace().getMetaArea().getDescriptionLocationFor((Project) target).toOSString());
	// get the info and update the timestamp etc.  Note that we do not open the info here.
	// This method is called from save which need not be in an operation so the
	// tree is not mutable.  It doesn't actually matter since the slots changed do not
	// affect deltas.
	ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
	info.setLocalSyncInfo(lastModified);
	info.set(M_LOCAL_EXISTS);
}
private void addFilesToHistoryStore(IPath key, IPath localLocation, boolean move) throws CoreException {
	java.io.File localFile = localLocation.toFile();
	if (!localFile.exists())
		return;
	long lastModified = CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath());
	if (localFile.isFile()) {
		historyStore.addState(key, localLocation, lastModified, move);
		return;
	}
	String[] children = localFile.list();
	if (children == null)
		return;
	for (int i = 0; i < children.length; i++)
		addFilesToHistoryStore(key.append(children[i]), localLocation.append(children[i]), move);
}}