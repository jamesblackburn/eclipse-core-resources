package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.internal.properties.PropertyStore;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.watson.IElementTreeData;
import java.io.*;
import java.util.*;

public class ResourceInfo implements IElementTreeData, ICoreConstants {

	/** Set of flags which reflect various states of the info (dirty, transient, ...). */
	protected int flags = 0;

	/** Unique content identifier */
	protected int contentId = 0;

	/** Unique modification stamp */
	// thread safety: (Concurrency004)
	protected volatile long modificationStamp = IResource.NULL_STAMP;

	/** Unique node identifier */
	// thread safety: (Concurrency004)
	protected volatile long nodeId = 0;

	/** Local sync info */
	// thread safety: (Concurrency004)
	protected volatile long localInfo = I_NULL_SYNC_INFO;

	/** The generation count for sync info changes. */
	protected int syncInfoGenerationCount = 0;
	
	/** The table of sync infos */
	protected HashMap syncInfo = null;

	/** The properties which are maintained for the lifecycle of the workspace */
	protected HashMap sessionProperties = null;

	/** The generation count for marker changes. */
	protected int markerGenerationCount = 0;
	
	/** The collection of markers for this resource. */
	protected MarkerSet markers = null;
/** 
 * Clears all of the bits indicated by the mask.
 */
public void clear(int mask) {
	flags &= ~mask;
}
public synchronized void clearSessionProperties() {
	sessionProperties = null;
}
public Object clone() {
	try {
		return super.clone();
	} catch (CloneNotSupportedException e) {
		return null; // never gets here.
	}
}
/** 
 * Returns the integer value stored in the indicated part of this info's flags.
 */
protected static int getBits(int flags, int mask, int start) {
	return (flags & mask) >> start;
}
public int getContentId() {
	return contentId;
}
/** 
 * Returns the set of flags for this info.
 */
public int getFlags() {
	return flags;
}
/** 
 * Gets the local-relative sync information.
 */
public long getLocalSyncInfo() {
	return localInfo;
}
/** 
 * Returns the marker generation count.
 * The count is incremented whenever markers on the resource change.
 */
public int getMarkerGenerationCount() {
	return markerGenerationCount;
}
/** 
 * Returns the collection of makers on this resource.
 * <code>null</code> is returned if there are none.
 */
public MarkerSet getMarkers() {
	return markers;
}
public long getModificationStamp() {
	return modificationStamp;
}
public long getNodeId() {
	return nodeId;
}
/**
 * Returns the property store associated with this info.  The return value may be null.
 */
public PropertyStore getPropertyStore() {
	return null;
}
/** 
 * Returns the value of the identified session property
 */
public Object getSessionProperty(QualifiedName name) {
	// thread safety: (Concurrency001)
	HashMap temp = sessionProperties;
	if (temp == null)
		return null;
	return temp.get(name);
}
public synchronized byte[] getSyncInfo(QualifiedName id, boolean makeCopy) {
	// thread safety: (Concurrency001)
	byte[] b;
	if (syncInfo == null)
		return null;
	b = (byte[]) syncInfo.get(id);
	return b == null ? null : (makeCopy ? (byte[]) b.clone() : b);
}
public synchronized HashMap getSyncInfo(boolean makeCopy) {
	if (syncInfo == null)
		return null;
	return makeCopy ? (HashMap) syncInfo.clone() : syncInfo;
}
/** 
 * Returns the sync information generation count.
 * The count is incremented whenever sync info on the resource changes.
 */
public int getSyncInfoGenerationCount() {
	return syncInfoGenerationCount;
}
/** 
 * Returns the type setting for this info.  Valid values are 
 * FILE, FOLDER, PROJECT, 
 */
public int getType() {
	return getType(flags);
}
/** 
 * Returns the type setting for this info.  Valid values are 
 * FILE, FOLDER, PROJECT, 
 */
public static int getType(int flags) {
	return getBits(flags, M_TYPE, M_TYPE_START);
}
/** 
 * Mark this resource info as having changed content
 */
public void incrementContentId() {
	contentId += 1;
}
/** 
 * Increments the marker generation count.
 * The count is incremented whenever markers on the resource change.
 */
public void incrementMarkerGenerationCount() {
	++markerGenerationCount;
}
/** 
 * Increments the sync information generation count.
 * The count is incremented whenever sync info on the resource changes.
 */
public void incrementSyncInfoGenerationCount() {
	++syncInfoGenerationCount;
}
/** 
 * Returns true if all of the bits indicated by the mask are set.
 */
public boolean isSet(int mask) {
	return isSet(flags, mask);
}
/** 
 * Returns true if all of the bits indicated by the mask are set.
 */
public static boolean isSet(int flags, int mask) {
	return (flags & mask) != 0;
}
public void readFrom(int flags, DataInput input) throws IOException {
	// The flags for this info are read by the visitor (flattener). 
	// See Workspace.readElement().  This allows the reader to look ahead 
	// and see what type of info is being loaded.
	this.flags = flags;
	localInfo = input.readLong();
	nodeId = input.readLong();
	contentId = input.readInt();
	modificationStamp = input.readLong();
}
/** 
 * Sets all of the bits indicated by the mask.
 */
public void set(int mask) {
	flags |= mask;
}
/** 
 * Sets the value of the indicated bits to be the given value.
 */
protected void setBits(int mask, int start, int value) {
	int baseMask = mask >> start;
	int newValue = (value & baseMask) << start;
	// thread safety: (guarantee atomicity)
	int temp = flags;
	temp &= ~mask;
	temp |= newValue;
	flags = temp;
}
/** 
 * Sets the flags for this info.
 */
protected void setFlags(int value) {
	flags = value;
}
/** 
 * Sets the local-relative sync information.
 */
public void setLocalSyncInfo(long info) {
	localInfo = info;
}
/** 
 * Sets the collection of makers for this resource.
 * <code>null</code> is passed in if there are no markers.
 */
public void setMarkers(MarkerSet value) {
	markers = value;
}
/** 
 *
 */
public void setModificationStamp(long stamp) {
	modificationStamp = stamp;
}
/** 
 *
 */
public void setNodeId(long id) {
	nodeId = id;
}
/**
 * Sets the property store associated with this info.  The value may be null.
 */
public void setPropertyStore(PropertyStore value) {
	// needs to be implemented on subclasses
}
/** 
 * Sets the identified session property to the given value.  If
 * the value is null, the property is removed.
 */
public synchronized void setSessionProperty(QualifiedName name, Object value) {
	// thread safety: (Concurrency001)
	if (value == null) {
		if (sessionProperties == null)
			return;
		HashMap temp = (HashMap) sessionProperties.clone();
		temp.remove(name);
		if (temp.isEmpty())
			sessionProperties = null;
		else
			sessionProperties = temp;
	} else {
		HashMap temp = sessionProperties;
		if (temp == null)
			temp = new HashMap(5);
		else
			temp = (HashMap) sessionProperties.clone();
		temp.put(name, value);
		sessionProperties = temp;
	}
}
protected void setSyncInfo(HashMap syncInfo) {
	this.syncInfo = syncInfo;
}
public synchronized void setSyncInfo(QualifiedName id, byte[] value) {
	if (value == null) {
		//delete sync info
		if (syncInfo == null)
			return;
		syncInfo.remove(id);
		if (syncInfo.isEmpty())
			syncInfo = null;
	} else {
		//add sync info
		if (syncInfo == null)
			syncInfo = new HashMap(5);
		syncInfo.put(id, value.clone());
	}
}
/** 
 * Sets the type for this info to the given value.  Valid values are 
 * FILE, FOLDER, PROJECT
 */
public void setType(int value) {
	setBits(M_TYPE, M_TYPE_START, value);
}
public void writeTo(DataOutput output) throws IOException {
	// The flags for this info are written by the visitor (flattener). 
	// See Workspace.writeElement().  This allows the reader to look ahead 
	// and see what type of info is being loaded.
	output.writeLong(localInfo);
	output.writeLong(nodeId);
	output.writeInt(contentId);
	output.writeLong(modificationStamp);
}
}
