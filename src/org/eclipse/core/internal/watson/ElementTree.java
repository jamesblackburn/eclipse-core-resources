package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import java.util.*;

/**
 * An ElementTree can be viewed as a generic rooted tree that stores
 * a hierarchy of elements.  An element in the tree consists of a 
 * (name, data, children) 3-tuple.  The name can be any String, and 
 * the data can be any Object.  The children are a collection of zero 
 * or more elements that logically fall below their parent in the tree.
 * The implementation makes no guarantees about the ordering of children.
 *
 * Elements in the tree are referenced by a key that consists of the names
 * of all elements on the path from the root to that element in the tree.
 * For example, if root node "a" has child "b", which has child "c", element
 * "c" can be referenced in the tree using the key (/a/b/c).  Keys are represented
 * using IPath objects, where the Paths are relative to the root element of the
 * tree.
 * @see IPath
 *
 * Each ElementTree has a single root element that is created implicitly and
 * is always present in any tree.  This root corresponds to the key (/),
 * or the singleton <code>Path.ROOT</code>.  The root element cannot be created
 * or deleted, and its data and name cannot be set.  The root element's children
 * however can be modified (added, deleted, etc).  The root path can be obtained 
 * using the <code>getRoot()</code> method.
 *
 * ElementTrees are modified in generations.  The method <code>newEmptyDelta()</code>
 * returns a new tree generation that can be modified arbitrarily by the user.
 * For the purpose of explanation, we call such a tree "active".
 * When the method <code>immutable()</code> is called, that tree generation is
 * frozen, and can never again be modified.  A tree must be immutable before
 * a new tree generation can start.  Since all ancestor trees are immutable,
 * different active trees can have ancestors in common without fear of 
 * thread corruption problems. 
 * 
 * Internally, any single tree generation is simply stored as the 
 * set of changes between itself and its most recent ancestor (its parent).
 * This compact delta representation allows chains of element trees to
 * be created at relatively low cost.  Clients of the ElementTree can
 * instantaneously "undo" sets of changes by navigating up to the parent 
 * tree using the <code>getParent()</code> method.
 *
 * Although the delta representation is compact, extremely long delta
 * chains make for a large structure that is potentially slow to query.
 * For this reason, the client is encouraged to minimize delta chain
 * lengths using the <code>collapsing(int)</code> and <code>makeComplete()</code>
 * methods.  The <code>getDeltaDepth()</code> method can be used to
 * discover the length of the delta chain.  The entire delta chain can
 * also be re-oriented in terms of the current element tree using the
 * <code>reroot()</code> operation.
 *
 * Users of ElementTrees can also compute deltas between any pair
 * of arbitrary ElementTrees (they need not have any ancestors in common).
 * These deltas are obtained with the <code>computeDeltaWith()</code>
 * method, which returns an instance of <code>ElementTreeDelta</code>.
 * @see computeDeltaWith()
 * @see ElementTreeDelta
 *
 * Classes are also available for tree serialization and navigation.
 * @see ElementTreeReader
 * @see ElementTreeWriter
 * @see ElementTreeIterator
 *
 * Finally, why are ElementTrees in a package called "watson"?
 * 	- "It's ElementTree my dear Watson, ElementTree."
 */
public class ElementTree {
 	protected DeltaDataTree tree;
 	protected IElementTreeData userData;
	private class ChildIDsCache {
		ChildIDsCache(IPath path, IPath[] childPaths) {
			this.path = path;
			this.childPaths = childPaths;
		}
		IPath path;
		IPath[] childPaths;
	}
	ChildIDsCache childIDsCache = null;

	DataTreeLookup lookupCache = null;

	private static int treeCounter = 0;
	private int treeStamp;
/**
 * Creates a new empty element tree.
 */
public ElementTree() {
	initialize(new DeltaDataTree());
}
/**
 * Creates a new element tree having as root elements the root
 * elements of the given trees.
 * This constructor allows element trees to be built bottom-up.
 * Note that this is a relatively expensive operation.
 */
public ElementTree(ElementTree[] children) {
	DataTreeNode node = nodeForElement(null, null, children);
	initialize(new DeltaDataTree(node));
}
/**
 * Creates a new element tree having a single (root) element with the given 
 * name and data.
 */
public ElementTree(String name, Object data) {
	DataTreeNode node = nodeForElement(name, data, null);
	initialize(node);
}
/**
 * Creates a new element tree having a root with the given name and 
 * element, and with the given subtrees as children.
 * This constructor allows element trees to be built bottom-up.
 * Note that this is a relatively expensive operation.
 */
public ElementTree(String name, Object data, ElementTree[] children) {
	DataTreeNode node = nodeForElement(name, data, children);
	initialize(node);
}
/**
 * Creates an element tree given its internal node representation.
 */
protected ElementTree(DataTreeNode rootNode) {
	initialize(rootNode);
}
/**
 * Creates a new element tree with the given data tree as its representation.
 */
protected ElementTree(DeltaDataTree tree) {
	initialize(tree);
}
/**
 * Creates an <code>ElementTree</code> given an ElementSubtree data structure.
 */
ElementTree(ElementSubtree subtree) {
	DataTreeNode rootNode = nodeFor(null, null, subtree.getChildren());
	DeltaDataTree tree = new DeltaDataTree(rootNode);
	initialize(tree);
}
/**
 * Creates a new empty delta element tree having the
 * given tree as its parent.
 */
protected ElementTree(ElementTree parent) {
	if (!parent.isImmutable()) {
		parent.immutable();
	}

	/* copy the user data forward */
	IElementTreeData data = parent.getTreeData();
	if (data != null) {
		userData = (IElementTreeData)data.clone();
	}
	
	initialize(parent.tree.newEmptyDeltaTree());
}
/**
 * Collapses this tree so that the given ancestor becomes its
 * immediate parent.  Afterwards, this tree will still have exactly the
 * same contents, but its internal stucture will be compressed.
 *
 * <p> This operation should be used to collapse chains of
 * element trees created by newEmptyDelta()/immutable().
 *
 * <p>This element tree must be immutable at the start of this operation,
 * and will be immutable afterwards.
 * @return this tree.
 */
public ElementTree collapseTo(ElementTree parent) {
	Assert.isTrue(tree.isImmutable());
	if (this == parent || getParent() == parent) {
		//already collapsed
		return this;
	}
	//collapse my tree to be a forward delta of the parent's tree.
	DeltaDataTree c = parent.tree.forwardDeltaWith(tree, DefaultElementComparator.getComparator());

	//update my internal tree
	this.tree = c;
	return this;
}
/**
 * Returns an immutable element tree with the same content as this element
 * tree, but expressed as a delta from an element tree a
 * given number of stages back.
 * <p> n=2 is the first interesting case; the most recent 2 sets
 * of changes are collapsed.
 * <p> n=deltaDepth()-1; collapses all changes over the original base.
 * <p> n=deltaDepth(); everything folded in a single element tree.
 *
 * <p> This operation should be used to collapse chains of
 * element trees created by newEmptyDelta()/immutable().
 *
 * <p> This element tree must be immutable.
 */
private ElementTree collapsing(int depth) {
	//this method is not currently used
	//it is based on assumptions that may no longer be valid.
	return this;

	/*
	if (depth <= 0) return this;
	
	Assert.isTrue(tree.isImmutable());
	
	DeltaDataTree c= tree;
	DeltaDataTree a= tree;
	//find the "depth"th parent of c
	for (int i=1; i<=depth; i++) {
		DeltaDataTree parent= a.getParent();
		if (parent==null) break;
		a= parent;
	}
	DeltaDataTree d= a.assembleWithForwardDelta(a.forwardDeltaWith(c, DefaultElementComparator.getComparator()));
	ElementTree result= new ElementTree(d);
	result.immutable();
	Assert.isTrue(depth<=2 || result.deltaDepth()==this.deltaDepth()-depth);
	return result;*/
}
/**
 * Computes a delta between this element tree and the given one,
 * using the given comparator to compare elements.
 * The result describes the changes applied to the given element tree
 * to yield this one.
 * <p>
 * For each element, if it is added or deleted it shows up as such in the delta.
 * Otherwise, the element in the given tree is compared to the one in this tree
 * using the comparator.
 * If it returns non-zero, the element is included in the delta as a change and the
 * return value of the comparator is remembered in the delta.
 * <p>
 * The result will be created for this tree, with its parent set to the given tree.
 * The trees need not be related.
 * The root element IDs of both trees must be equal.
 *
 * @see ElementDelta#getComparison()
 */

public ElementTreeDelta computeDeltaWith(ElementTree olderTree, IElementComparator comparator) {
	if (olderTree == null || comparator == null) {
		throw new IllegalArgumentException(Policy.bind("watson.nullArg", "ElementTree.computeDeltaWith"));
	}
	
	return new ElementTreeDelta((ElementTree) olderTree, this, comparator);
}
/**
 * Computes a delta between this element tree and the given one,
 * using the given comparator to compare elements.  The delta will
 * begin at the given path.  The result describes the changes applied 
 * to the subtree of the given element tree to yield this one.
 * <p>
 * For each element, if it is added or deleted it shows up as such in the delta.
 * Otherwise, the element in the given tree is compared to the one in this tree
 * using the comparator.
 * If it returns non-zero, the element is included in the delta as a change and the
 * return value of the comparator is remembered in the delta.
 * <p>
 * The result will be created for this tree, with its parent set to the given tree.
 * The trees need not be related.
 * The root element IDs of both trees must be equal.
 *
 * @see ElementDelta#getComparison()
 */

public ElementTreeDelta computeDeltaWith(ElementTree olderTree, IElementComparator comparator, IPath path) {
	if (olderTree == null || comparator == null) {
		throw new IllegalArgumentException(Policy.bind("watson.nullArg", "ElementTree.computeDeltaWith"));
	}
	if (path.isRoot()) {
		/* can optimize certain cases when computing deltas on the whole tree */
		return new ElementTreeDelta(olderTree, this, comparator);
	} else {
		return new ElementTreeDelta(olderTree, this, comparator, path);
	}
}
/** 
 * Creates the indicated element and sets its element info. 
 * The parent element must be present, otherwise an IllegalArgumentException
 * is thrown. If the indicated element is already present in the tree, 
 * its element info is replaced and any existing children are 
 * deleted.
 *
 * @param key element key
 * @param data element data, or <code>null</code>
 */
public void createElement(IPath key, Object data) {
	/* don't allow modification of the implicit root */
	if (key.isRoot()) return;
	
	/**
	 * Clear the child IDs cache in case it's referring to this parent.
	 * This is conservative.
	 */
	childIDsCache = null;
	
	/**
	 * Clear the lookup cache, in case the element being created is the same
	 * as for the last lookup.
	 */
	lookupCache = null;
	
	IPath parent = key.removeLastSegments(1);
	try {
		tree.createChild(parent, key.lastSegment(), data);
	} catch (ObjectNotFoundException e) {
		elementNotFound(parent);
	}
}
/**
 * Creates or replaces the subtree below the given path with 
 * the given tree. The subtree can only have one child below 
 * the root, which will become the node specified by the given
 * key in this tree.
 *
 * @param key The path of the new subtree in this tree.
 * @see getSubtree(IPath)
 */
public void createSubtree(IPath key, ElementTree subtree) {
	/* don't allow creating subtrees at the root */
	if (key.isRoot()) {
		throw new IllegalArgumentException(Policy.bind("watson.noModify"));
	}
	
	// Clear the child IDs cache in case it's referring to this parent.
	// This is conservative.
	childIDsCache = null;
	// Clear the lookup cache, in case the element being created is the same
	// as for the last lookup.
	lookupCache = null;
	try {
		/* don't copy the implicit root node of the subtree */
		IPath[] children = subtree.getChildren(subtree.getRoot());
		if (children.length != 1) {
			throw new IllegalArgumentException(Policy.bind("watson.illegalSubtree"));
		}
		
		/* get the subtree for the specified key */
		DataTreeNode node = (DataTreeNode) subtree.tree.copyCompleteSubtree(children[0]);

		/* insert the subtree in this tree */
		tree.createSubtree(key, node);

	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
	}
}
/** 
 * Deletes the indicated element and its descendents. 
 * The element must be present.
 */
public void deleteElement(IPath key) {
	/* don't allow modification of the implicit root */
	if (key.isRoot()) return;
		
	// Clear the child IDs cache in case it's referring to this parent.
	// This is conservative.
	childIDsCache = null;
	// Clear the lookup cache, in case the element being deleted is the same
	// as for the last lookup.
	lookupCache = null;
	try {
		tree.deleteChild(key.removeLastSegments(1), key.lastSegment());
	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
	}
}
/**
 * Returns the "delta depth" of this element tree.
 * A brand new element tree has delta depth 0;
 * <code>newEmptyDelta()</code> on an element tree
 * of delta depth n returns one of depth n+1.
 *
 * <p> The delta depth of an element tree should be managed.
 * If the element tree is allowed to get deep, the speed
 * of accesses slows (it searches the delta layers sequentially),
 * and the memory footprint increases (it is still hanging on
 * to all the information in the previous immutable trees).
 */
public int deltaDepth() {
	int d = 0;
	for (DeltaDataTree t = tree; t.getParent() != null; t = t.getParent()) {
		d++;
	}
	return d;
}
/**
 * Complains that an element was not found
 */
protected void elementNotFound(IPath key) {
	throw new IllegalArgumentException(Policy.bind("watson.elementNotFound", key.toString()));
}
/**
 * Given an array of element trees, returns the index of the 
 * oldest tree.  The oldest tree is the tree such that no
 * other tree in the array is a descendent of that tree.
 * Note that this counter-intuitive concept of oldest is based on the
 * ElementTree orientation such that the complete tree is always the
 * newest tree.
 */
public static int findOldest(ElementTree[] trees) {

	/* first put all the trees in a hashtable */
	Hashtable candidates = new Hashtable((int) (trees.length * 1.5 + 1));
	for (int i = 0; i < trees.length; i++) {
		candidates.put(trees[i], trees[i]);
	}

	/* keep removing parents until only one tree remains */
	ElementTree oldestSoFar = null;
	while (candidates.size() > 0) {
		/* get a new candidate */
		ElementTree current = (ElementTree) candidates.elements().nextElement();

		/* remove this candidate from the table */
		candidates.remove(current);

		/* remove all of this element's parents from the list of candidates*/
		ElementTree parent = current.getParent();
		int count = 0;

		/* walk up chain until we hit the root or a tree we have already tested */
		while (parent != null && parent != oldestSoFar) {
			candidates.remove(parent);
			parent = parent.getParent();
		}

		/* the current candidate is the oldest tree seen so far */
		oldestSoFar = current;

		/* if the table is now empty, we have a winner */
	}
	Assert.isNotNull(oldestSoFar);

	/* return the appropriate index */
	for (int i = 0; i < trees.length; i++) {
		if (trees[i] == oldestSoFar) {
			return i;
		}
	}
	Assert.isTrue(false, "Should not get here");
	return -1;
}
/**
 * Returns the path of the N'th child of the element
 * specified by the given path.
 * The given element must be present in this tree, and
 * have such a child.
 */
public IPath getChild(IPath key, int childIndex) {
	Assert.isNotNull(key);
	return getChildIDs(key)[childIndex];
}
/**
 * Returns the number of children of the element
 * specified by the given path.
 * The given element must be present in this tree.
 */
public int getChildCount(IPath key) {
	Assert.isNotNull(key);
	return getChildIDs(key).length;
}
/**
 * Returns the IDs of the children of the specified element.
 * If the specified element is null, returns the root element path.
 */
protected IPath[] getChildIDs(IPath key) {
	ChildIDsCache cache = childIDsCache; // Grab it in case it's replaced concurrently.
	if (cache != null && cache.path == key) {
		return cache.childPaths;
	}
	try {
		if (key == null) {
			return new IPath[] {tree.rootKey()};
		} else {
			IPath[] children = tree.getChildren(key);
			childIDsCache = new ChildIDsCache(key, children); // Cache the result
			return children;
		}
	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
		return null; // can't get here
	}
}
/**
 * Returns the paths of the children of the element
 * specified by the given path.
 * The given element must be present in this tree.
 */
public IPath[] getChildren(IPath key) {
	Assert.isNotNull(key);
	return getChildIDs(key);
}
/**
 * Returns the internal data tree.
 */
public DeltaDataTree getDataTree() {
	return tree;
}
/**
 * Returns the delta representation for this tree.
 * Returns <code>null</code> if this tree does not have
 * a delta representation.
 */
public ElementTreeDelta getDelta() {
	ElementTree parent = getParent();
	if (parent == null) {
		return null;
	}
	return new ElementTreeDelta(parent, this, DefaultElementComparator.getComparator());
}
/**
 * Returns the element data for the given element identifier.
 * The given element must be present in this tree.
 */
public Object getElementData(IPath key) {
	/* don't allow modification of the implicit root */
	if (key.isRoot()) return null;

	DataTreeLookup lookup = lookupCache;  // Grab it in case it's replaced concurrently.
	if (lookup == null || lookup.key != key) {
		lookupCache = lookup = tree.lookup(key);
	}
	if (lookup.isPresent) {
		return lookup.data;
	}
	else {
		elementNotFound(key);
		return null; // can't get here
	}
}
/**
 * Returns the entire tree structure as an ElementSubtree.
 *
 * @see ElementSubtree
 */
/*package */ElementSubtree getElementSubtree() {
	DataTreeNode elementNode = (DataTreeNode) tree.copyCompleteSubtree(tree.rootKey());
	return new ElementSubtree(elementNode);
}
/**
 * Returns the names of the children of the specified element.
 * The specified element must exist in the tree.
 * If the specified element is null, returns the root element path.
 */
public String[] getNamesOfChildren(IPath key) {
	try {
		if (key == null) {
			return new String[] {""};
		} else {
			return tree.getNamesOfChildren(key);
		}
	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
		return null; // can't get here
	}
}
/**
 * Returns the parent tree, or <code>null</code> if there is no parent.
 */
public ElementTree getParent() {
	DeltaDataTree parentTree = tree.getParent();
	if (parentTree == null) {
		return null;
	}
	// The parent ElementTree is stored as the node data of the parent DeltaDataTree,
	// to simplify canonicalization in the presence of rerooting.
	return (ElementTree) parentTree.getData(tree.rootKey());
}
/**
 * Returns the root node of this tree.
 */
public IPath getRoot() {
	return getChildIDs(null)[0];
}
/**
 * Returns the subtree rooted at the given key. In the resulting tree, 
 * the implicit root node (designated by Path.ROOT), has a single child, 
 * which is the node specified by the given key in this tree.
 * 
 * The subtree must be present in this tree.
 *
 * @see createSubtree(ElementSubtree)
 */
public ElementTree getSubtree(IPath key) {
	/* the subtree of the root of this tree is just this tree */
	if (key.isRoot()) {
		return this;
	}
	try {
		DataTreeNode elementNode = (DataTreeNode) tree.copyCompleteSubtree(key);
		return new ElementTree(elementNode);
	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
		return null;
	}
}
/**
 * Returns the user data associated with this tree.
 */
public IElementTreeData getTreeData() {
	return userData;
}
/**
 * Returns whether this tree has the given tree as a direct
 * or indirect ancestor.  Returns false if they are the same tree.
 */
public boolean hasAncestor(ElementTree oldTree) {
	if (this == oldTree) return false;
	/**
	 * If this tree has been closed, the ancestor chain is flipped
	 */
	if (this.tree.isImmutable()) {
		for (ElementTree tree = oldTree.getParent(); tree != null; tree = tree.getParent()) {
			if (tree == this) {
				return true;
			}
		}
	} else {
		for (ElementTree tree = this.getParent(); tree != null; tree = tree.getParent()) {
			if (tree == oldTree) {
				return true;
			}
		}
	}
		
	return false;
}
/**
 * Makes this tree immutable (read-only); ignored if it is already
 * immutable.
 */
public void immutable() {
	if (!tree.isImmutable()) {
 		tree.immutable();
 		/* need to clear the lookup cache since it reports whether results were found
 		   in the topmost delta, and the order of deltas is changing */
 		lookupCache = null;
		/* reroot the delta chain at this tree */
 		tree.reroot();
	}
 } 
/**
 * Returns true if this element tree includes an element with the given
 * key, false otherwise.
 */
public boolean includes(IPath key) {
	DataTreeLookup lookup = lookupCache;  // Grab it in case it's replaced concurrently.
	if (lookup == null || lookup.key != key) {
		lookupCache = lookup = tree.lookup(key);
	}
	return lookup.isPresent;
}
protected void initialize(DataTreeNode rootNode) {
	/* create the implicit root node */
	initialize(new DeltaDataTree(
		new DataTreeNode(null, null, new AbstractDataTreeNode[] {rootNode})));
}
protected void initialize(DeltaDataTree tree) {
	// Keep this element tree as the data of the root node.
	// Useful for canonical results for ElementTree.getParent().
	// see getParent().
	treeStamp = treeCounter++;
	tree.setData(tree.rootKey(), this);
	this.tree = tree;
}
/**
 * Returns whether this tree is immutable.
 */
public boolean isImmutable() {
	return tree.isImmutable();
}
/**
 * Converts this tree's representation to be a complete tree, not a delta.
 * This disconnects this tree from its parents.
 * The parent trees are unaffected.
 */
public void makeComplete() {
	/* need to clear the lookup cache since it reports whether results were found
	   in the topmost delta, and the order of deltas is changing */
	lookupCache = null;
	tree.makeComplete();
}
/**
 * Merges a chain of deltas for a certain subtree to this tree.
 * If this tree has any data in the specified subtree, it will
 * be overwritten.  The receiver tree must be open, and it will
 * be made immutable during the merge operation.  The trees in the
 * provided array will be replaced by new trees that have been
 * merged into the receiver's delta chain.
 *
 * @param path The path of the subtree chain to merge
 * @param trees The chain of trees to merge.  The trees can be
 *  in any order, but they must all form a simple ancestral chain.
 * @return A new open tree with the delta chain merged in.
 */
public ElementTree mergeDeltaChain(IPath path, ElementTree[] trees) {
	if (path == null || trees == null) {
		throw new IllegalArgumentException(Policy.bind("watson.nullArg", "ElementTree.mergeDeltaChain"));
	}

	/* The tree has to be open */
	if (isImmutable()) {
		throw new IllegalArgumentException(Policy.bind("watson.immutable"));
	}
	ElementTree current = this;
	if (trees.length > 0) {
		/* find the oldest tree to be merged */
		ElementTree toMerge = trees[findOldest(trees)];

		/* merge the trees from oldest to newest */
		while (toMerge != null) {
			if (path.isRoot()) {
				//copy all the children
				IPath[] children = toMerge.getChildren(Path.ROOT);
				for (int i = 0; i < children.length; i++) {
					current.createSubtree(children[i], toMerge.getSubtree(children[i]));
				}
			} else {
				//just copy the specified node
				current.createSubtree(path, toMerge.getSubtree(path));
			}
			current.immutable();

			/* replace the tree in the array */
			for (int i = 0; i < trees.length; i++) {
				if (trees[i] == toMerge) {
					trees[i] = current;
					break;
				}
			}
			current = current.newEmptyDelta();
			toMerge = toMerge.getParent();
		}
	}
	return current;
}
/**
 * Creates a new element tree which is represented as a delta on this one.
 * Initially they have the same content.  Subsequent changes to the new
 * tree will not affect this one.
 */
public ElementTree newEmptyDelta() {
	lookupCache = null; // Don't want old trees hanging onto cached infos.
	return new ElementTree(this);
}
/** 
 * Computes the node for an element given its element name, element info and a list
 * of ElementTypeSubtrees.
 */
private DataTreeNode nodeFor(String elementName, Object elementInfo, ElementSubtree[] subtrees) {
	if (subtrees == null || subtrees.length == 0) {
		return new DataTreeNode(elementName, elementInfo);
	}
	DataTreeNode[] childNodes = new DataTreeNode[subtrees.length];
	for (int i = subtrees.length; --i >= 0;) {
		childNodes[i] = nodeFor(subtrees[i]);
	}
	AbstractDataTreeNode.sort(childNodes);
	return new DataTreeNode(elementName, elementInfo, childNodes);
}
private DataTreeNode nodeFor(ElementSubtree subtree) {
	return nodeFor(subtree.elementName, subtree.elementData, subtree.getChildren());
}
/**
 * Returns the node hierarchy corresponding to the given element and any children (optional).
 */
protected DataTreeNode nodeForElement(String elementName, Object element, ElementTree[] children) {

	/* if children is null or empty there is nothing to do here */
	if (children == null || children.length == 0)
		return new DataTreeNode(elementName, element, null);

	/**
	 * Each of the given child trees has an implicit root node
	 * specified by Path.ROOT.  What we are really interested in
	 * are the children of these roots.
	 */

	/* find out the number of real children */
	int childCount = 0;
	for (int i = 0; i < children.length; i++)
		childCount += children[i].getChildCount(children[i].getRoot());

	/* if there is no child, just go on */
	if (childCount == 0)
		return new DataTreeNode(elementName, element, null);

	/* grab children of roots to create child array */
	AbstractDataTreeNode[] childNodes = new AbstractDataTreeNode[childCount];
	int next = 0;
	for (int i = 0; i < children.length; i++) {
		IPath[] rootChildren = children[i].getChildren(children[i].getRoot());
		for (int j = 0; j < rootChildren.length; j++) {
			childNodes[next++] = children[i].tree.copyCompleteSubtree(rootChildren[j]);
		}
	}
	Assert.isTrue(next == childCount);
	return new DataTreeNode(elementName, element, childNodes);
}
/**
 * Returns a mutable copy of the element data for the given path.
 * This copy will be held onto in the most recent delta.
 * ElementTree data MUST implement the IElementTreeData interface
 * for this method to work.  If the data does not define that interface
 * this method will fail.
 */
public Object openElementData(IPath key) {
	Assert.isTrue(!isImmutable());
	
	/* don't allow modification of the implicit root */
	if (key.isRoot())
		return null;
	DataTreeLookup lookup = lookupCache; // Grab it in case it's replaced concurrently.
	if (lookup == null || lookup.key != key) {
		lookupCache = lookup = tree.lookup(key);
	}
	if (lookup.isPresent) {
		if (lookup.foundInFirstDelta)
			return lookup.data;
		/**
		 * The node has no data in the most recent delta.
		 * Pull it up to the present delta by setting its data with a clone.
		 */
		IElementTreeData oldData = (IElementTreeData) lookup.data;
		if (oldData != null) {
			try {
				Object newData = oldData.clone();
				tree.setData(key, newData);
				lookupCache = null;
				return newData;
			} catch (ObjectNotFoundException e) {
				elementNotFound(key);
			}
		}
	} else {
		elementNotFound(key);
	}
	return null;
}
/**
 * Sets the element for the given element identifier.
 * The given element must be present in this tree.
 * @param key element identifier
 * @param data element info, or <code>null</code>
 * @see #setElementInfo
 */
public void setElementData(IPath key, Object data) {
	/* don't allow modification of the implicit root */
	if (key.isRoot()) return;
		
	Assert.isNotNull(key);
	// Clear the lookup cache, in case the element being modified is the same
	// as for the last lookup.
	lookupCache = null;
	try {
		tree.setData(key, data);
	} catch (ObjectNotFoundException e) {
		elementNotFound(key);
	}
}
/**
 * Sets the user data associated with this tree.
 */
public void setTreeData(IElementTreeData data) {
	userData = data;
}
/** 
 * Returns a string representation of this element tree's
 * structure suitable for debug puposes.
 */
public String toDebugString() {
	final StringBuffer buffer = new StringBuffer("\n");
	ElementTreeIterator iterator = new ElementTreeIterator();
	IElementContentVisitor visitor = new IElementContentVisitor() {
		public void visitElement(ElementTree tree, IPath elementID, Object elementContents) {
			buffer.append(elementID + " " + elementContents + "\n");
		}
	};
	iterator.iterate(this, visitor);
	return buffer.toString();
}
public String toString() {
	return "ElementTree(" + treeStamp + ")";
}
}
