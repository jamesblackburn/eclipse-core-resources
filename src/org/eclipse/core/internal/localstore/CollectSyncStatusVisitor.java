package org.eclipse.core.internal.localstore;

 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import java.util.ArrayList;
import java.util.List;
//
/**
 * Visits a unified tree, and collects local sync information in 
 * a multistatus.  At the end of the visit, the resource tree will NOT
 * be synchronized with the filesystem, but all discrepancies between
 * the two will be recorded in the returned status.
 */
public class CollectSyncStatusVisitor extends RefreshLocalVisitor {
	protected MultiStatus status;
	protected List affectedResources;
/**
 * Creates a new visitor, whose sync status will have the given title.
 */
public CollectSyncStatusVisitor(String multiStatusTitle, IProgressMonitor monitor) {
	super(monitor);
	status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INFO, multiStatusTitle, null);
	affectedResources = new ArrayList(20);
}
protected void changed(Resource target) {
	String message = Policy.bind("localstore.resourceIsOutOfSync", target.getFullPath().toString());
	status.add(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message));
	affectedResources.add(target);
	resourceChanged = true;
}
/**
 * Returns the list of resources that were not synchronized with
 * the local filesystem.
 */
public List getAffectedResources() {
	return affectedResources;
}
/**
 * Returns the sync status that has been collected as a result of this visit.
 */
public MultiStatus getSyncStatus() {
	return status;
}
protected void createResource(UnifiedTreeNode node, Resource target) throws CoreException {
	changed(target);
}
protected void deleteResource(UnifiedTreeNode node, Resource target) throws CoreException {
	changed(target);
}
protected void fileToFolder(UnifiedTreeNode node, Resource target) throws CoreException {
	changed(target);
}
protected void folderToFile(UnifiedTreeNode node, Resource target) throws CoreException {
	changed(target);
}
protected void resourceChanged(Resource target, long lastModified) throws CoreException {
	changed(target);
}
}