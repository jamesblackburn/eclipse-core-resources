package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.resources.*;

public interface ICoreConstants {
	
	// Standard resource SessionProperties
	/** map of builders to their last built state. */
	public static final QualifiedName K_BUILD_MAP = new QualifiedName(ResourcesPlugin.PI_RESOURCES, "BuildMap");

	// resource info constants
	static final long I_UNKNOWN_SYNC_INFO = -2;
	static final long I_NULL_SYNC_INFO = -1;

	// Useful flag masks for resource info states
	static final int M_OPEN = 0x1;
	static final int M_LOCAL_EXISTS = 0x2;
	static final int M_LOCAL_IS_FILE = 0x4;
	static final int M_PHANTOM = 0x8;
	static final int M_USED = 0x10;
	static final int M_TYPE = 0xF00;
	static final int M_TYPE_START = 8;
	static final int M_MARKERS_SNAP_DIRTY = 0x1000;
	static final int M_SYNCINFO_SNAP_DIRTY = 0x2000;
	static final int NULL_FLAG = -1;

	// Internal status codes	
	// Information Only [00-24]
	public static final int FIRST_INTERNAL_INFO = 10000;
	public static final int OPERATION_FAILED = 10002;
	public static final int LAST_INTERNAL_INFO = 10024;
	// Warnings [25-74]
	public static final int FIRST_INTERNAL_WARNING = 10025;
	public static final int LAST_INTERNAL_WARNING = 10074;
	// Errors [75-99]
	public static final int FIRST_INTERNAL_ERROR = 10075;
	public static final int LAST_INTERNAL_ERROR = 10099;

	// invalid chars for resource names
	public static final char[] invalidResourceNameChars = {':', ';', '*', '?', '"', '\'', '<', '>', '|', '/', '\\'};

	public static final int PROJECT_SEGMENT_LENGTH = 1;
	public static final int MINIMUM_FOLDER_SEGMENT_LENGTH = 2;
	public static final int MINIMUM_FILE_SEGMENT_LENGTH = 2;

	public static final int WORKSPACE_TREE_VERSION_1 = 67305985;
	public static final int WORKSPACE_TREE_VERSION_2 = 67305986;
	
	// helper constants for empty structures
	public static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	public static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
	public static final IFileState[] EMPTY_FILE_STATES = new IFileState[0];
}