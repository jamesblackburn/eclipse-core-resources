package org.eclipse.core.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.internal.utils.Policy;

/**
 * A <code>DeletedNode</code> represents a node that has been deleted in a 
 * <code>DeltaDataTree</code>.  It is a node that existed in the parent tree, 
 * but no longer exists in the current delta tree.  It has no children or data.
 */
public class DeletedNode extends AbstractDataTreeNode {

/**
 * Creates a new tree with the given name
 */
DeletedNode(String localName) {
	super(localName, emptyChildArray);
}
/**
 * @see AbstractDataTreeNode#asBackwardDelta
 */
AbstractDataTreeNode asBackwardDelta (DeltaDataTree myTree, 
	DeltaDataTree parentTree, IPath key) {
	
	if (parentTree.includes(key)) {
		return parentTree.copyCompleteSubtree(key);
	} else {
		return this;
	}
}
/**
 * Returns the child with the given local name
 */
 AbstractDataTreeNode childAt (String localName) {
 	
 	/* deleted nodes do not have children */
 	throw new ObjectNotFoundException(Policy.bind("dtree.missingChild", localName));
}
/**
 * Returns the child with the given local name
 */
 AbstractDataTreeNode childAtOrNull (String localName) {
 	
 	/* deleted nodes do not have children */
 	return null;
}
/**
 * Replaces the child with the given local name.
 */
 AbstractDataTreeNode childAtPut (String localName) {
 	
 	/* deleted nodes do not have children */
 	return null;
}
AbstractDataTreeNode compareWithParent(IPath key, DeltaDataTree parent, IComparator comparator) {
	/**
	 * Just because there is a deleted node, it doesn't mean there must
	 * be a corresponding node in the parent.  Deleted nodes can live
	 * in isolation.
	 */
	if (parent.includes(key)) {
		return convertToRemovedComparisonNode(parent.copyCompleteSubtree(key), NodeComparison.K_REMOVED);
	} else {
		// Node doesn't exist in either tree.  Return an empty comparison.
		// Empty comparisons are omitted from the delta.
		return new DataTreeNode(key.lastSegment(), new NodeComparison(null, null, 0, 0));
	}
}
/**
 * Creates and returns a new copy of the receiver.  Makes a deep copy of 
 * children, but a shallow copy of name and data.
 */
AbstractDataTreeNode copy () {
	return new DeletedNode(name);
}
/**
 * Returns true if the receiver represents a deleted node, false otherwise.
 */
boolean isDeleted() {
	return true;
}
/** 
 * Simplifies the given node, and returns its replacement.
 */
AbstractDataTreeNode simplifyWithParent(IPath key, DeltaDataTree parent, IComparator comparer) {
	
	if (parent.includes(key)) {
		return this;
	} else {
		return new NoDataDeltaNode(name);
	}
}
/**
 * Returns the number of children of the receiver
 */
int size() {
	
	/* deleted nodes have no children */
	return 0;
}
/**
 * Return a unicode representation of the node.  This method
 * is used for debugging purposes only (no NLS please)
 */
public String toString () {
	return "a DeletedNode(" + this.getName() + ")";
}
/**
 * Returns a string describing the type of node.
 */
int type() {
	return DeletedNodeType;
}
}
