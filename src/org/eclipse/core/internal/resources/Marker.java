package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import java.util.Map;
/**
 * An abstract marker implementation.
 * Subclasses must implement the <code>clone</code> method, and
 * are free to declare additional field and method members.
 * <p>
 * Note: Marker objects do not store whether they are "standalone"
 * vs. "attached" to the workspace. This information is maintained
 * by the workspace.
 * </p>
 *
 * @see IMarker
 */
public class Marker extends PlatformObject implements IMarker {

	/** Marker identifier. */
	protected long id;

	/** Resource with which this marker is associated. */
	protected IResource resource;
/**
 * Constructs a new marker object. 
 */
Marker(IResource resource, long id) {
	Assert.isLegal(resource != null);
	this.resource = resource;
	this.id = id;
}
/**
 * Checks the given marker info to ensure that it is not null.
 * Throws an exception if it is.
 */
private void checkInfo(MarkerInfo info) throws CoreException {
	if (info == null) {
		String message = Policy.bind("resources.markerNotFound", Long.toString(id));
		throw new ResourceException(new ResourceStatus(IResourceStatus.MARKER_NOT_FOUND, resource.getFullPath(), message));
	}
}
/**
 * @see IMarker#delete
 */
public void delete() throws CoreException {
	try {
		getWorkspace().prepareOperation();
		getWorkspace().beginOperation(true);
		getWorkspace().getMarkerManager().removeMarker(getResource(), getId());
	} finally {
		getWorkspace().endOperation(false, null);
	}
}
/**
 * @see IMarker#equals
 */
public boolean equals(Object object) {
	if (!(object instanceof IMarker))
		return false;
	IMarker other = (IMarker) object;
	return (id == other.getId() && resource.equals(other.getResource()));
}
/**
 * @see IMarker#exists
 */
public boolean exists() {
	return getInfo() != null;
}
/**
 * @see IMarker#getAttribute
 */
public Object getAttribute(String attributeName) throws CoreException {
	Assert.isNotNull(attributeName);
	MarkerInfo info = getInfo();
	checkInfo(info);
	return info.getAttribute(attributeName);
}
/**
 * @see IMarker#getAttribute
 */
public int getAttribute(String attributeName, int defaultValue) {
	Assert.isNotNull(attributeName);
	MarkerInfo info = getInfo();
	if (info == null)
		return defaultValue;
	Object value = info.getAttribute(attributeName);
	if (value != null && value instanceof Integer)
		return ((Integer) value).intValue();
	return defaultValue;
}
/**
 * @see IMarker#getAttribute
 */
public String getAttribute(String attributeName, String defaultValue) {
	Assert.isNotNull(attributeName);
	MarkerInfo info = getInfo();
	if (info == null)
		return defaultValue;
	Object value = info.getAttribute(attributeName);
	if (value != null && value instanceof String)
		return (String) value;
	return defaultValue;
}
/**
 * @see IMarker#getAttribute
 */
public boolean getAttribute(String attributeName, boolean defaultValue) {
	Assert.isNotNull(attributeName);
	MarkerInfo info = getInfo();
	if (info == null)
		return defaultValue;
	Object value = info.getAttribute(attributeName);
	if (value != null && value instanceof Boolean)
		return ((Boolean) value).booleanValue();
	return defaultValue;
}
/**
 * @see IMarker#getAttributes()
 */
public Map getAttributes() throws CoreException {
	MarkerInfo info = getInfo();
	checkInfo(info);
	return info.getAttributes();
}
/**
 * @see IMarker#getAttributes(String[])
 */
public Object[] getAttributes(String[] attributeNames) throws CoreException {
	Assert.isNotNull(attributeNames);
	MarkerInfo info = getInfo();
	checkInfo(info);
	return info.getAttributes(attributeNames);
}
/**
 * @see IMarker#getId
 */
public long getId() {
	return id;
}
protected MarkerInfo getInfo() {
	return getWorkspace().getMarkerManager().findMarkerInfo(resource, id);
}
/**
 * @see IMarker#getResource
 */
public IResource getResource() {
	return resource;
}
/**
 * @see IMarker#getType
 */
public String getType() throws CoreException {
	MarkerInfo info = getInfo();
	checkInfo(info);
	return info.getType();
}
/**
 * Returns the workspace which manages this marker.  Returns
 * <code>null</code> if this resource does not have an associated
 * resource.
 */
private Workspace getWorkspace() {
	return resource == null ? null : (Workspace) resource.getWorkspace();
}
public int hashCode() {
	return (int) id + resource.hashCode();
}
/**
 * @see IMarker#isSubtypeOf
 */
public boolean isSubtypeOf(String type) throws CoreException {
	return getWorkspace().getMarkerManager().getCache().isSubtype(getType(), type);
}
/**
 * @see IMarker#setAttribute
 */
public void setAttribute(String attributeName, int value) throws CoreException {
	setAttribute(attributeName, new Integer(value));
}
/**
 * @see IMarker#setAttribute
 */
public void setAttribute(String attributeName, Object value) throws CoreException {
	Assert.isNotNull(attributeName);
	Workspace workspace = getWorkspace();
	try {
		workspace.prepareOperation();
		workspace.beginOperation(true);
		ResourceInfo resourceInfo = ((Resource)resource).getResourceInfo(false ,true);
		MarkerInfo markerInfo = (MarkerInfo)resourceInfo.getMarkers().get(id);
		checkInfo(markerInfo);
		markerInfo.setAttribute(attributeName, value);
		resourceInfo.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
		workspace.getMarkerManager().changedMarker(IResourceDelta.CHANGED, resource, resourceInfo, markerInfo);
	} finally {
		workspace.endOperation(false, null);
	}
}
/**
 * @see IMarker#setAttribute
 */
public void setAttribute(String attributeName, boolean value) throws CoreException {
	setAttribute(attributeName, value ? Boolean.TRUE : Boolean.FALSE);
}
/**
 * @see IMarker#setAttributes
 */
public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
	Assert.isNotNull(attributeNames);
	Assert.isNotNull(values);
	try {
		getWorkspace().prepareOperation();
		getWorkspace().beginOperation(true);
		MarkerInfo info = getInfo();
		checkInfo(info);
		info.setAttributes(attributeNames, values);
		((Resource) resource).getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
		IMarkerDelta[] change = new IMarkerDelta[] { new MarkerDelta(IResourceDelta.CHANGED, resource, info)};
		getWorkspace().getMarkerManager().changedMarkers(resource, change);
	} finally {
		getWorkspace().endOperation(false, null);
	}
}
/**
 * @see IMarker#setAttributes
 */
public void setAttributes(Map values) throws CoreException {
	try {
		getWorkspace().prepareOperation();
		getWorkspace().beginOperation(true);
		MarkerInfo info = getInfo();
		checkInfo(info);
		info.setAttributes(values);
		((Resource) resource).getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
		IMarkerDelta[] change = new IMarkerDelta[] { new MarkerDelta(IResourceDelta.CHANGED, resource, info)};
		getWorkspace().getMarkerManager().changedMarkers(resource, change);
	} finally {
		getWorkspace().endOperation(false, null);
	}
}
void setId(int value) {
	id = value;
}
}
