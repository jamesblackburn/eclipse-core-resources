package org.eclipse.core.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.watson.IElementComparator;

/**
 * A resource delta represents changes in the state of a resource tree
 * between two discrete points in time.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Resource deltas implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see IResource
 * @see Platform#getAdapterManager
 */
public interface IResourceDelta extends IAdaptable {

	/*====================================================================
	 * Constants defining resource delta kinds:
	 *====================================================================*/

	/**
	 * Delta kind constant indicating that the resource has not been changed in any way
	 * @see IResourceDelta#getKind
	 */
	public static final int NO_CHANGE = IElementComparator.K_NO_CHANGE;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been added
	 * to its parent. That is, one that appears in the "after" state,
	 * not in the "before" one.
	 * @see IResourceDelta#getKind
	 */
	public static final int ADDED = 0x1;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been removed
	 * from its parent. That is, one that appears in the "before" state,
	 * not in the "after" one. 
	 * @see IResourceDelta#getKind
	 */
	public static final int REMOVED = 0x2;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been changed. 
	 * That is, one that appears in both the "before" and "after" states.
	 * @see IResourceDelta#getKind
	 */
	public static final int CHANGED = 0x4;

	/**
	 * Delta kind constant (bit mask) indicating that a phantom resource has been added at
	 * the location of the delta node. 
	 * @see IResourceDelta#getKind
	 */
	public static final int ADDED_PHANTOM = 0x8;

	/**
	 * Delta kind constant (bit mask) indicating that a phantom resource has been removed from 
	 * the location of the delta node. 
	 * @see IResourceDelta#getKind
	 */
	public static final int REMOVED_PHANTOM = 0x10;

	/**
	 * The bit mask which describes all possible delta kinds,
	 * including ones involving phantoms.
	 * @see IResourceDelta#getKind
	 */
	public static final int ALL_WITH_PHANTOMS = 
		CHANGED | ADDED | REMOVED | ADDED_PHANTOM | REMOVED_PHANTOM;


	/*====================================================================
	 * Constants which describe resource changes:
	 *====================================================================*/

	/**
	 * Change constant (bit mask) indicating that the content of the resource has changed.
	 * @see IResourceDelta#getFlags 
	 */
	public static final int CONTENT = 0x100;

	/**
	 * Change constant (bit mask) indicating that the resource was moved from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.
	 * @see IResourceDelta#getFlags
	 */
	public static final int MOVED_FROM = 0x1000;

	/**
	 * Change constant (bit mask) indicating that the resource was moved to another location.
	 * The location in the new state can be retrieved using <code>getMovedToPath()</code>.
	 * @see IResourceDelta#getFlags
	 */
	public static final int MOVED_TO = 0x2000;

	/**
	 * Change constant (bit mask) indicating that the resource was opened or closed.
	 * For example, if the current state of the resource is open then 
	 * it was previously closed.
	 * @see IResourceDelta#getFlags
	 */
	public static final int OPEN = 0x4000;

	/**
	 * Change constant (bit mask) indicating that the type of the resource has changed
	 * @see IResourceDelta#getFlags
	 */
	public static final int TYPE = 0x8000;

	/**
	 * Change constant (bit mask) indicating that the resource's sync status has changed.
	 * This type of change is not included in build deltas, only in those for resource notification.
	 * @see IResourceDelta#getFlags
	 */
	public static final int SYNC = 0x10000;

	/**
	 * Change constant (bit mask) indicating that the resource's markers have changed.
	 * This type of change is not included in build deltas, only in those for resource notification.
	 * @see IResourceDelta#getFlags
	 */
	public static final int MARKERS = 0x20000;

	/**
	 * Change constant (bit mask) indicating that the resource has been
	 * replaced by another at the same location (i.e., the resource has 
	 * been deleted and then added). 
	 * @see IResourceDelta#getFlags
	 */
	public static final int REPLACED = 0x40000;

	/**
	 * Change constant (bit mask) indicating that a project's description has changed. 
	 * @see IResourceDelta#getFlags
	 */
	public static final int DESCRIPTION = 0x80000;
/**
 * Accepts the given visitor.
 * The only kinds of resource deltas visited 
 * are <code>ADDED</code>, <code>REMOVED</code>, 
 * and <code>CHANGED</code>.
 * The visitor's <code>visit</code> method is called with this
 * resource delta if applicable. If the visitor returns <code>true</code>,
 * the resource delta's children are also visited.
 *
 * @param visitor the visitor
 * @exception CoreException if the visitor failed with this exception.
 * @see IResourceDeltaVisitor#visit
 */
public void accept(IResourceDeltaVisitor visitor) throws CoreException;
/** 
 * Accepts the given visitor.
 * The visitor's <code>visit</code> method is called with this
 * resource delta. If the visitor returns <code>true</code>,
 * the resource delta's children are also visited.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only resource deltas involving existing resources will be visited
 * (kinds <code>ADDED</code>, <code>REMOVED</code>, 
 * and <code>CHANGED</code>).
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the result will also include additions and removes of phantom resources
 * (kinds <code>ADDED_PHANTOM</code> and <code>REMOVED_PHANTOM</code>).
 * </p>
 *
 * @param visitor the visitor
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest
 * @exception CoreException if the visitor failed with this exception.
 * @see #accept(IResourceDeltaVisitor)
 * @see IResource#isPhantom
 * @see IResourceDeltaVisitor#visit
 */
public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException;
/**
 * Returns resource deltas for all children of this resource 
 * which were added, removed, or changed. Returns an empty
 * array if there are no affected children.
 * <p>
 * Equivalent to <code>getAffectedChildren(ADDED | REMOVED | CHANGED)</code>.
 * </p>
 *
 * @return the resource deltas for all affected children
 * @see IResourceDelta#ADDED
 * @see IResourceDelta#REMOVED
 * @see IResourceDelta#CHANGED
 * @see #getAffectedChildren(int)
 */
public IResourceDelta[] getAffectedChildren();
/**
 * Returns resource deltas for all children of this resource 
 * whose kind is included in the given mask. Mask are formed
 * by the bitwise or of <code>IResourceDelta</code> kind constants.
 * Returns an empty array if there are no affected children.
 * <p>
 * Specify <code>ALL_WITH_PHANTOMS</code> to see all child
 * resource deltas including phantoms.
 * </p>
 * <p>
 * Use the <code>getAffectedChildren()</code> method to see all
 * resource deltas excluding phantoms.
 * </p>
 *
 * @param mask a mask formed by the bitwise or of <code>IResourceDelta </code> 
 *    delta kind constants
 * @return the resource deltas for all affected children
 * @see IResourceDelta#ADDED
 * @see IResourceDelta#REMOVED
 * @see IResourceDelta#CHANGED
 * @see IResourceDelta#ADDED_PHANTOM
 * @see IResourceDelta#REMOVED_PHANTOM
 * @see IResourceDelta#ALL_WITH_PHANTOMS
 * @see #getAffectedChildren()
 */
public IResourceDelta[] getAffectedChildren(int mask);
/**
 * Returns flags which describe in more detail how a resource has been affected.
 * <p>
 * The following codes (bit masks) are used when kind is <code>CHANGED</code>, and
 * also when the resource is involved in a move:
 * <ul>
 * <li><code>CONTENT</code> - The bytes contained by the resource have been altered.
 * 		This flag is only valid for file resources.</li>
 * <li><code>DESCRIPTION</code> - The description of the project has been altered.
 * 		This flag is only valid for project resources.</li>
 * <li><code>OPEN</code> - The project's open/closed state has changed.
 * 		If it is not open, it was closed, and vice versa.  This flag is only valid for project resources.</li>
 * <li><code>TYPE</code> - The resource (a folder or file) has changed its type.</li>
 * <li><code>SYNC</code> - The resource's sync status has changed.</li>
 * <li><code>MARKERS</code> - The resource's markers have changed.</li>
 * <li><code>REPLACED</code> - The resource (and all its properties)
 *  was deleted (either by a delete or move), and was subsequently re-created
 *  (either by a create, move, or copy).</li>
 * </ul>
 * The following code is only used if kind is <code>REMOVED</code>
 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
 * <ul>
 * <li><code>MOVED_TO</code> - The resource has moved.
 * 	<code>getMovedToPath</code> will return the path of where it was moved to.</li>
 * </ul>
 * The following code is only used if kind is <code>ADDED</code>
 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
 * <ul>
 * <li><code>MOVED_FROM</code> - The resource has moved.
 * 	<code>getMovedFromPath</code> will return the path of where it was moved from.</li>
 * </ul>
 * A simple move operation would result in the following delta information.
 * If a resource is moved from A to B (with no other changes to A or B), 
 * then A will have kind <code>REMOVED</code>, with flag <code>MOVED_TO</code>, 
 * and <code>getMovedToPath</code> on A will return the path for B.  
 * B will have kind <code>ADDED</code>, with flag <code>MOVED_FROM</code>, 
 * and <code>getMovedFromPath</code> on B will return the path for A.
 * B's other flags will describe any other changes to the resource, as compared
 * to its previous location at A.
 * </p>
 * <p>
 * Note that the move flags only describe the changes to a single resource; they
 * don't necessarily imply anything about the parent or children of the resource.  
 * If the children were moved as a consequence of a subtree move operation, 
 * they will have corresponding move flags as well.
 * </p>
 * <p>
 * Note that it is possible for a file resource to be replaced in the workspace
 * by a folder resource (or the other way around).
 * The resource delta, which is actually expressed in terms of
 * paths instead or resources, shows this as a change to either the
 * content or children.
 * </p>
 *
 * @return the flags
 * @see IResourceDelta#CONTENT
 * @see IResourceDelta#DESCRIPTION
 * @see IResourceDelta#OPEN
 * @see IResourceDelta#MOVED_TO
 * @see IResourceDelta#MOVED_FROM
 * @see IResourceDelta#TYPE
 * @see IResourceDelta#SYNC
 * @see IResourceDelta#MARKERS
 * @see IResourceDelta#REPLACED
 * @see #getKind
 * @see #getMovedFromPath
 * @see #getMovedToPath
 * @see IResource#move
 */
public int getFlags();
/**
 * Returns the full, absolute path of this resource delta.
 * <p>
 * Note: the returned path never has a trailing separator.
 * </p>
 * @return the full, absolute path of this resource delta
 * @see IResource#getFullPath
 * @see #getProjectRelativePath
 */
public IPath getFullPath();
/**
 * Returns the kind of this resource delta.
 * Normally, one of <code>ADDED</code>, 
 * <code>REMOVED</code>, <code>CHANGED</code>.
 * When phantom resources have been explicitly requested,
 * there are two additional kinds: <code>ADDED_PHANTOM</code> 
 * and <code>REMOVED_PHANTOM</code>.
 *
 * @return the kind of this resource delta
 * @see IResourceDelta#ADDED
 * @see IResourceDelta#REMOVED
 * @see IResourceDelta#CHANGED
 * @see IResourceDelta#ADDED_PHANTOM
 * @see IResourceDelta#REMOVED_PHANTOM
 */
public int getKind();
/**
 * Returns the changes to markers on the corresponding resource.
 * Returns an empty array if no markers changed.
 *
 * @return the marker deltas
 */
public IMarkerDelta[] getMarkerDeltas();
/**
 * Returns the full path (in the "before" state) from which this resource 
 * (in the "after" state) was moved.  This value is only valid 
 * if the <code>MOVED_FROM</code> change flag is set; otherwise,
 * <code>null</code> is returned.
 * <p>
 * Note: the returned path never has a trailing separator.
 *
 * @return a path, or <code>null</code>
 * @see #getMovedToPath
 * @see #getFullPath
 * @see #getFlags
 */
public IPath getMovedFromPath();
/**
 * Returns the full path (in the "after" state) to which this resource 
 * (in the "before" state) was moved.  This value is only valid if the 
 * <code>MOVED_TO</code> change flag is set; otherwise,
 * <code>null</code> is returned.
 * <p>
 * Note: the returned path never has a trailing separator.
 * 
 * @return a path, or <code>null</code>
 * @see #getMovedFromPath
 * @see #getFullPath
 * @see #getFlags
 */
public IPath getMovedToPath();
/**
 * Returns the project-relative path of this resource delta.
 * Returns the empty path for projects and the workspace root.
 * <p>
 * A resource's project-relative path indicates the route from the project
 * to the resource.  Within a workspace, there is exactly one such path
 * for any given resource. The returned path never has a trailing separator.
 * </p>
 * @return the project-relative path of this resource delta
 * @see IResource#getProjectRelativePath
 * @see #getFullPath
 * @see Path#EMPTY
 */
public IPath getProjectRelativePath();
/**
 * Returns a handle for the affected resource.
 * <p> 
 * For additions (<code>ADDED</code>), this handle describes the newly-added resource; i.e.,
 * the one in the "after" state.
 * <p> 
 * For changes (<code>CHANGED</code>), this handle also describes the resource in the "after"
 * state. When a file or folder resource has changed type, the
 * former type of the handle can be inferred.
 * <p>
 * For removals (<code>REMOVED</code>), this handle describes the resource in the "before" 
 * state. Even though this resource would not normally exist in the
 * current workspace, the type of resource that was removed can be
 * determined from the handle.
 * <p> 
 * For phantom additions and removals (<code>ADDED_PHANTOM</code>
 * and <code>REMOVED_PHANTOM</code>), this is the handle of the phantom resource.
 *
 * @return the affected resource (handle)
 */
public IResource getResource();
}
