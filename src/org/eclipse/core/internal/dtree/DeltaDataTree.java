package org.eclipse.core.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import java.util.Enumeration;

/**
 * Externally, a <code>DeltaDataTree</code> appears to have the same content as
 * a standard data tree.  Internally, the delta tree may be complete, or it may
 * just indicate the changes between itself and its parent.  
 *
 * <p>Nodes that exist in the parent but do not exist in the delta, are represented 
 * as instances of <code>DeletedNode</code>.  Nodes that are identical in the parent
 * and the delta, but have differences in their subtrees, are represented as
 * instances of <code>NoDataDeltaNode</code> in the delta tree.  Nodes that differ
 * between parent and delta are instances of <code>DataDeltaNode</code>.  However,
 * the <code>DataDeltaNode</code> only contains the children whose subtrees differ
 * between parent and delta.
 *
 * A delta tree algebra is used to manipulate sets of delta trees.  Given two trees,
 * one can obtain the delta between the two using the method 
 * <code>forwardDeltaWith(aTree)</code>.  Given a tree and a delta, one can assemble
 * the complete tree that the delta represents using the method <code>
 * assembleWithForwardDelta</code>.  Refer to the public API methods of this class
 * for further details.
 */

public class DeltaDataTree extends AbstractDataTree {
	private AbstractDataTreeNode rootNode;
	private DeltaDataTree parent;

/**
 * Creates a new empty tree.
 */
public DeltaDataTree() {
	this.empty();
}
/**
 * Creates a new tree.
 * @param rootNode
 *	root node of new tree.
 */
public DeltaDataTree(AbstractDataTreeNode rootNode) {
	this.rootNode = rootNode;
	this.parent = null;
}
protected DeltaDataTree(AbstractDataTreeNode rootNode, DeltaDataTree parent) {
	this.rootNode = rootNode;
	this.parent = parent;
}
/**
 * Adds a child to the tree.
 *
 * @param parentKey parent for new child.
 * @param localName name of child.
 * @param childNode child node.
 */
protected void addChild (IPath parentKey, String localName, 
	AbstractDataTreeNode childNode) {
	
	if (!includes(parentKey)) {
		handleNotFound(parentKey);
	}
	
	childNode.setName(localName);
	this.assembleNode(
		parentKey, 
		new NoDataDeltaNode(parentKey.lastSegment(), childNode));
}
/**
 * Returns the tree as a backward delta.  If the delta is applied to the tree it
 * will produce its parent. The receiver must have a forward 
 * delta representation. I.e.:  Call the receiver's parent A, 
 * and the receiver B.  The receiver's representation is A->B.
 * Returns the delta A<-B.  The result is equivalent to A, but has B as its parent.
 */
DeltaDataTree asBackwardDelta() {
	
	if (getParent() == null) {
		return newEmptyDeltaTree();
	}

	return new DeltaDataTree(
		getRootNode().asBackwardDelta (this, getParent(), rootKey()),
		this);
}
/**
 * This method can only be called on a comparison tree created
 * using DeltaDataTree.compareWith().  This method flips the orientation
 * of the given comparison tree, so that additions become removals,
 * and vice-versa.  This method destructively changes the tree
 * as opposed to making a copy.
 */
public DeltaDataTree asReverseComparisonTree(IComparator comparator) {
	/* don't reverse the root node if it's the absolute root (name==null) */
	if (rootNode.getName() == null) {
		AbstractDataTreeNode[] children = rootNode.getChildren();
		int nextChild = 0;
		for (int i = 0; i < children.length; i++) {
			AbstractDataTreeNode newChild = children[i].asReverseComparisonNode(comparator); 
			if (newChild != null) {
				children[nextChild++] = newChild;
			}
		}

		if (nextChild < children.length) {
			AbstractDataTreeNode[] newChildren = new AbstractDataTreeNode[nextChild];
			System.arraycopy(children, 0, newChildren, 0, nextChild);
			rootNode.setChildren(newChildren);
		}
	} else {
		rootNode.asReverseComparisonNode(comparator);
	}
	return this;
}
/**
 * Replaces a node in the tree with the result of assembling the node
 * with the given delta node (which represents a forward delta on 
 * the existing node).
 *
 * @param key
 *	key of the node to replace.
 * @param deltaNode
 *	delta node to use to assemble the new node.
 */
protected void assembleNode(IPath key, AbstractDataTreeNode deltaNode) {
	rootNode = rootNode.assembleWith(deltaNode, key, 0);
}
/**
 * Assembles the receiver with the given delta tree and answer 
 * the resulting, mutable source tree.  The given delta tree must be a 
 * forward delta based on the receiver (i.e. missing information is taken from 
 * the receiver).  This operation is used to coalesce delta trees.
 *
 * <p>In detail, suppose that c is a forward delta over source tree a.
 * Let d := a assembleWithForwardDelta: c.
 * d has the same content as c, and is represented as a delta tree
 * whose parent is the same as a's parent.
 *
 * <p>In general, if c is represented as a chain of deltas of length n,
 * then d is represented as a chain of length n-1.
 *
 * <p>So if a is a complete tree (i.e., has no parent, length=0), then d
 	 * will be a complete tree too.
 *
 * <p>Corollary: (a assembleWithForwardDelta: (a forwardDeltaWith: b)) = b
 */
public DeltaDataTree assembleWithForwardDelta (DeltaDataTree deltaTree) {
	return new DeltaDataTree(
		getRootNode().assembleWith(deltaTree.getRootNode()),
		this);
}
/**
 * Compares this tree with another tree, starting from the given path.  The
 * given path will be the root node of the returned tree.  Both this
 * tree and other tree must contain the given path.
 */
protected DeltaDataTree basicCompare(DeltaDataTree other, IComparator comparator, IPath path) {
	DeltaDataTree newTree;
	if (this == other) {
		newTree = new DeltaDataTree();
		newTree.setData(Path.ROOT, new NodeComparison(null, null, 0, 0));
	} else
		if (other.hasAncestor(this)) {
			AbstractDataTreeNode assembled = other.searchNodeAt(path);
			DeltaDataTree tree = other;

			/* Iterate through the receiver's ancestors until the receiver is reached */
			while ((tree = tree.getParent()) != this) {
				//ancestor may not contain the given path
				AbstractDataTreeNode treeNode = tree.searchNodeAt(path);
				if (treeNode != null) {
					assembled = treeNode.assembleWith(assembled);
				}
			}
			AbstractDataTreeNode comparedRoot = assembled.compareWithParent(path, this, comparator);
			newTree = new DeltaDataTree(comparedRoot);
		} else
			if (this.hasAncestor(other)) {
				AbstractDataTreeNode assembled = this.asBackwardDelta().searchNodeAt(path);
				DeltaDataTree tree = this;

				/* Iterate through the receiver's ancestors until the other tree is reached */
				while ((tree = tree.getParent()) != other) {
					assembled = assembled.assembleWith(tree.asBackwardDelta().searchNodeAt(path));
				}
				AbstractDataTreeNode comparedRoot = assembled.compareWithParent(path, this, comparator);
				newTree = new DeltaDataTree(comparedRoot);
			} else {
				//revert to naive comparison
				DataTreeNode thisCompleteRoot = (DataTreeNode) this.copyCompleteSubtree(path);
				DataTreeNode otherCompleteRoot = (DataTreeNode) other.copyCompleteSubtree(path);
				AbstractDataTreeNode comparedRoot = thisCompleteRoot.compareWith(otherCompleteRoot, comparator);
				newTree = new DeltaDataTree(comparedRoot);
			}
	newTree.immutable();
	return newTree;
}
/**
 * Returns a DeltaDataTree that describes the differences between
 * this tree and "other" tree.  Each node of the returned tree
 * will contain a NodeComparison object that describes the differences
 * between the two trees.
 */
public DeltaDataTree compareWith(DeltaDataTree other, IComparator comparator) {

	DeltaDataTree newTree;
	if (this == other) {
		newTree = new DeltaDataTree();
		newTree.setData(Path.ROOT, new NodeComparison(null, null, 0, 0));
	} else if (other.hasAncestor(this)) {
		
		AbstractDataTreeNode assembled = other.getRootNode();
		DeltaDataTree tree = other;
		
		/* Iterate through the receiver's ancestors until the receiver is reached */
		while ((tree = tree.getParent()) != this) {
			assembled = tree.getRootNode().assembleWith(assembled);
		}
		AbstractDataTreeNode comparedRoot = assembled.compareWithParent(rootKey(), this, comparator);
		newTree = new DeltaDataTree(comparedRoot);
	} else if (this.hasAncestor(other)) {
			AbstractDataTreeNode assembled = this.asBackwardDelta().getRootNode();
			DeltaDataTree tree = this;

			/* Iterate through the receiver's ancestors until the other tree is reached */
			while ((tree = tree.getParent()) != other) {
				assembled = assembled.assembleWith(tree.asBackwardDelta().getRootNode());
			}
			AbstractDataTreeNode comparedRoot = assembled.compareWithParent(rootKey(), this, comparator);
			newTree = new DeltaDataTree(comparedRoot);
	} else {
		//revert to naive comparison if trees have no common ancestry
		DataTreeNode thisCompleteRoot = (DataTreeNode) this.copyCompleteSubtree(rootKey());
		DataTreeNode otherCompleteRoot = (DataTreeNode) other.copyCompleteSubtree(rootKey());
		AbstractDataTreeNode comparedRoot = thisCompleteRoot.compareWith(otherCompleteRoot, comparator);
		newTree = new DeltaDataTree(comparedRoot);
	}
	newTree.immutable();
	return newTree;
}
/**
 * Compares this tree with another tree, starting from the given path.  The
 * given path will be the root node of the returned tree.
 */
public DeltaDataTree compareWith(DeltaDataTree other, IComparator comparator, IPath path) {
	/* need to figure out if trees really contain the given path */
	if (this.includes(path)) {
		if (other.includes(path)) {
			return basicCompare(other, comparator, path);
		} else {
			/* only exists in this tree */
			return new DeltaDataTree(AbstractDataTreeNode.convertToRemovedComparisonNode(
				this.copyCompleteSubtree(path), comparator.compare(this.getData(path), null)));
		}
	} else {
		if (other.includes(path)) {
			/* only exists in other tree */
			return new DeltaDataTree(AbstractDataTreeNode.convertToAddedComparisonNode(
				other.copyCompleteSubtree(path), comparator.compare(null, other.getData(path))));
		} else {
			/* doesn't exist in either tree */
			return DeltaDataTree.createEmptyDelta();
		}
	}
}
/**
 * Returns a copy of the tree which shares its instance variables.
 */
protected AbstractDataTree copy () {
	return new DeltaDataTree(rootNode, parent);
}
/**
 * Returns a complete node containing the contents of a subtree of the tree.
 *
 * @param key
 *	key of subtree to copy
 */
public AbstractDataTreeNode copyCompleteSubtree (IPath key) {

	AbstractDataTreeNode node = searchNodeAt(key);
	if (node == null) {
		// not found
		handleNotFound(key);
	}
	if (node.isDelta()) {
		return naiveCopyCompleteSubtree(key);
	} else {
		//copy the node in case the user wants to hammer the subtree name
		return node.copy();
	}
}
/**
 * @see AbstractDataTree#createChild
 */
public void createChild(IPath parentKey, String localName) {
	createChild(parentKey, localName, null);
}
/**
 * @see AbstractDataTree#createChild
 */
public void createChild (IPath parentKey, String localName, Object data) {

	if (isImmutable()) {
		handleImmutableTree();
	}		
	addChild(
		parentKey, 
		localName, 
		new DataTreeNode(localName, data));
}
/**
 * Returns a delta data tree that represents an empty delta.
 * (i.e. it represents a delta on another (unspecified) tree, 
 * but introduces no changes).
 */
static DeltaDataTree createEmptyDelta () {

	DeltaDataTree newTree = new DeltaDataTree();
	newTree.emptyDelta();
	return newTree;
}
/**
 * Creates and returns an instance of the receiver.
 * @see AbstractDataTree#createInstance
 */
protected AbstractDataTree createInstance() {
	return new DeltaDataTree();
}
/**
 * @see AbstractDataTree#createSubtree
 */
public void createSubtree (IPath key, AbstractDataTreeNode node) {

	if (isImmutable()) {
		handleImmutableTree();
	}
			
	if (key.isRoot()) {
		setParent(null);
		setRootNode(node);
	} else {
		addChild (key.removeLastSegments(1), key.lastSegment(), node);
	}
}
/**
 * @see AbstractDataTree#deleteChild
 */
public void deleteChild (IPath parentKey, String localName) {

	if (isImmutable()) {
		handleImmutableTree();
	}
	
	/* If the child does not exist */
	IPath childKey = parentKey.append(localName);
	if (!includes(childKey)) {
		handleNotFound(childKey);
	}

	assembleNode(
		parentKey,
		new NoDataDeltaNode(parentKey.lastSegment(), new DeletedNode(localName)));
}
/**
 * Initializes the receiver so that it is a complete, empty tree. 
 * @see AbstractDataTree#empty
 */
public void empty() {
	rootNode = new DataTreeNode(null, null);
	parent = null;
}
/**
 * Initializes the receiver so that it represents an empty delta.
 * (i.e. it represents a delta on another (unspecified) tree, 
 * ut introduces no changes).  The parent is left unchanged.
 */
void emptyDelta() {
	rootNode = new NoDataDeltaNode(null);
}

public boolean isEmptyDelta() {
	return rootNode.getChildren().length == 0;
}
/**
 * Returns a node of the tree if it is present, otherwise returns null
 *
 * @param key
 *	key of node to find
 */
public AbstractDataTreeNode findNodeAt (IPath key) {
	
	AbstractDataTreeNode node = rootNode;
	int segmentCount = key.segmentCount();
	for (int i = 0; i < segmentCount; i++) {
		node = node.childAtOrNull(key.segment(i));
		if (node == null)
			return null;
	}
	return node;
}
/**
 * Returns a forward delta between the receiver and the given source tree,
 * using the given comparer to compare data objects.  
 * The result describes the changes which, if assembled with the receiver,
 * will produce the given source tree.
 * In more detail, let c = a.forwardDeltaWith(b).
 * c has the same content as b, but is represented as a delta tree with a as the parent.
 * Also, c is immutable.
 *
 * There is no requirement that a and b be related, although it is usually more
 * efficient if they are. The node keys are used as the basis of correlation
 * between trees.
 * 
 * Note that if b is already represented as a delta over a, 
 * then c will have the same internal structure as b.
 * Thus the very common case of previous forwardDeltaWith: current
 * is actually very fast when current is a modification of previous.
 *
 * @param sourceTree second delta tree to create a delta between
 * @param comparer the comparer used to compare data objects
 * @return the new delta
 */
public DeltaDataTree forwardDeltaWith(DeltaDataTree sourceTree, IComparator comparer) {
	DeltaDataTree newTree;
	if (this == sourceTree) {
		newTree = this.newEmptyDeltaTree();
	} else
		if (sourceTree.hasAncestor(this)) {
			AbstractDataTreeNode assembled = sourceTree.getRootNode();
			DeltaDataTree treeParent = sourceTree;

			/* Iterate through the sourceTree's ancestors until the receiver is reached */
			while ((treeParent = treeParent.getParent()) != this) {
				assembled = treeParent.getRootNode().assembleWith(assembled);
			}
			newTree = new DeltaDataTree(assembled, this);
			newTree.simplify(comparer);
		} else
			if (this.hasAncestor(sourceTree)) {
				//create the delta backwards and then reverse it
				newTree = sourceTree.forwardDeltaWith(this, comparer);
				newTree = newTree.asBackwardDelta();
			} else {
				DataTreeNode thisCompleteRoot = (DataTreeNode) this.copyCompleteSubtree(rootKey());
				DataTreeNode sourceTreeCompleteRoot = (DataTreeNode) sourceTree.copyCompleteSubtree(rootKey());
				AbstractDataTreeNode deltaRoot = thisCompleteRoot.forwardDeltaWith(sourceTreeCompleteRoot, comparer);
				newTree = new DeltaDataTree(deltaRoot, this);
			}
	newTree.immutable();
	return newTree;
}
/**
 * @see AbstractDataTree#getChildCount
 */
 public int getChildCount(IPath parentKey) {
	return getChildNodes(parentKey).length;
}
/**
 * Returns the child nodes of a node in the tree.
 */
protected AbstractDataTreeNode[] getChildNodes(IPath parentKey) {

	/* Algorithm:
	 *   for each delta in chain (going backwards),
	 *     get list of child nodes, if any in delta
	 *     assemble with previously seen list, if any
	 *     break when complete tree found, 
	 *   report error if parent is missing or has been deleted
	 */
	 
	AbstractDataTreeNode[] childNodes = null;
	int keyLength = parentKey.segmentCount();
	for (DeltaDataTree tree = this; tree != null; tree = tree.parent) {
		AbstractDataTreeNode node = tree.rootNode;
		boolean complete = !node.isDelta();
		for (int i = 0; i < keyLength; i++) {
			node = node.childAtOrNull(parentKey.segment(i));
			if (node == null) {
				break;
			}
			if (!node.isDelta()) {
				complete = true;
			}
		}
		if (node != null) {
			if (node.isDeleted()) {
				break;
			}
			if (childNodes == null) {
				childNodes = node.children;
			} else {
				// Be sure to assemble(old, new) rather than (new, old).
				// Keep deleted nodes if we haven't encountered the complete node yet.
				childNodes = AbstractDataTreeNode.assembleWith(node.children, childNodes, !complete);
			}
		}
		if (complete) {
			if (childNodes != null) {
				return childNodes;
			}
			// Not found, but complete node encountered, so should not check parent tree.
			break;
		}
	}
	if (childNodes != null) {
		// Some deltas carry info about children, but there is
		// no complete node against which they describe deltas.
		Assert.isTrue(false, Policy.bind("dtree.malformedTree"));
	}

	// Node is missing or has been deleted.
	handleNotFound(parentKey);
	return null;//should not get here
}
/**
 * @see AbstractDataTree#getChildren
 */
public IPath[] getChildren(IPath parentKey) {

	AbstractDataTreeNode[] childNodes = getChildNodes(parentKey);
	int len = childNodes.length;
	IPath[] answer = new IPath[len];
	for (int i = 0; i < len; ++i) {
		answer[i] = parentKey.append(childNodes[i].name);
	}
	return answer;
}
/**
 * Returns the data at a node of the tree.
 *
 * @param key
 *	key of node for which to return data.
 */
public Object getData (IPath key) {

	/* Algorithm:
	 *   for each delta in chain (going backwards),
	 *     get node, if any in delta
	 *	   if it carries data, return it
	 *     break when complete tree found
	 *   report error if node is missing or has been deleted
	 */
	
	int keyLength = key.segmentCount();
	for (DeltaDataTree tree = this; tree != null; tree = tree.parent) {
		AbstractDataTreeNode node = tree.rootNode;
		boolean complete = !node.isDelta();
		for (int i = 0; i < keyLength; i++) {
			node = node.childAtOrNull(key.segment(i));
			if (node == null) {
				break;
			}
			if (!node.isDelta()) {
				complete = true;
			}
		}
		if (node != null) {
			if (node.hasData()) {
				return node.getData();
			} else if (node.isDeleted()) {
				break;
			}
		}
		if (complete) {
			// Not found, but complete node encountered, so should not check parent tree.
			break;
		}
	}
	handleNotFound(key);
	return null; //can't get here
}
/**
 * @see AbstractDataTree#getNameOfChild
 */
public String getNameOfChild(IPath parentKey, int index) {

	AbstractDataTreeNode[] childNodes = getChildNodes(parentKey);
	return childNodes[index].name;
}
/**
 * Returns the local names for the children of a node of the tree.
 *
 * @see AbstractDataTree#getNamesOfChildren
 */
public String[] getNamesOfChildren (IPath parentKey) {

	AbstractDataTreeNode[] childNodes = getChildNodes(parentKey);
	int len = childNodes.length;
	String[] namesOfChildren = new String[len];
	for (int i = 0; i < len; ++i) {
		namesOfChildren[i] = childNodes[i].name;
	}
	return namesOfChildren;
}
/**
 * Returns a node info object describing the specified node
 * of the receiver.  Only the receiver's representation is accessed.  If the
 * receiver is a delta representation, and the specified node has not been
 * modified in the delta, the node info describes the node as missing (the parent
 * tree is not consulted).
 */
public NodeInfo getNodeInfo (IPath key) {
	
	AbstractDataTreeNode found = findNodeAt (key);
	if (found == null) {
		return NodeInfo.missing();
	}
	
	return found.nodeInfoAt(this);
}
/** 
 * Returns the parent of the tree.
 */
public DeltaDataTree getParent() {
	return parent;
}
/**
 * Returns the root node of the tree.
 */
protected AbstractDataTreeNode getRootNode() {
	return rootNode;
}
/**
 * Returns true if the receiver's parent has the specified ancestor
 *
 * @param ancestor the ancestor in question
 */
protected boolean hasAncestor (DeltaDataTree ancestor) {
	
	DeltaDataTree parent = this;
	while ((parent = parent.getParent()) != null) {
		if (parent == ancestor) {
			return true;
		}
	}

	return false;
}
/**
 * Returns true if the receiver includes a node with
 * the given key, false otherwise.
 */
public boolean includes (IPath key) {
	return searchNodeAt(key) != null;
}
/**
 * Returns an object containing:
 *  - the node key
 * 	- a flag indicating whether the specified node was found
 *  - the data for the node, if it was found
 *
 * @param key  key of node for which we want to retrieve data.
 */
public DataTreeLookup lookup(IPath key) {
	int keyLength = key.segmentCount();
	for (DeltaDataTree tree = this; tree != null; tree = tree.parent) {
		AbstractDataTreeNode node = tree.rootNode;
		boolean complete = !node.isDelta();
		for (int i = 0; i < keyLength; i++) {
			node = node.childAtOrNull(key.segment(i));
			if (node == null) {
				break;
			}
			complete |= !node.isDelta();
		}
		if (node != null) {
			if (node.hasData()) {
				return DataTreeLookup.newLookup(key, true, node.getData(), tree==this);
			} else if (node.isDeleted()) {
				break;
			}
		}
		if (complete) {
			// Not found, but complete node encountered, so should not check parent tree.
			break;
		}
	}
	return DataTreeLookup.newLookup(key, false, null);
}
/**
 * Converts this tree's representation to be a complete tree, not a delta.
 * This disconnects this tree from its parents.
 * The parent trees are unaffected.
 */
public void makeComplete() {

	AbstractDataTreeNode assembled = getRootNode();
	DeltaDataTree parent = getParent();
	while (parent != null) {
		assembled = parent.getRootNode().assembleWith(assembled);
		parent = parent.getParent();
	}
	setRootNode(assembled);
	setParent(null);
}
/**
 * Returns a complete node containing the contents of the subtree 
 * rooted at @key in the receiver.  Uses the public API.
 *
 * @param key
 *	key of subtree whose contents we want to copy.
 */
protected AbstractDataTreeNode naiveCopyCompleteSubtree (IPath key) {

	String[] childNames = getNamesOfChildren (key);
	AbstractDataTreeNode[] childNodes = new AbstractDataTreeNode[childNames.length];

	/* do for each child */
	for (int i = childNames.length; --i >= 0;) {
		childNodes[i] = copyCompleteSubtree(key.append(childNames[i]));
	}
	
	return new DataTreeNode(key.lastSegment(), getData(key), childNodes);
}
/**
 * Returns a new tree which represents an empty, mutable delta on the
 * receiver.  It is not possible to obtain a new delta tree if the receiver is
 * not immutable, as subsequent changes to the receiver would affect the
 * resulting delta.
 */
public DeltaDataTree newEmptyDeltaTree() {
	
	if (!isImmutable()) {
		throw new IllegalArgumentException(Policy.bind("dtree.notImmutable"));
	}
	
	DeltaDataTree newTree = (DeltaDataTree) this.copy();
	newTree.setParent(this);
	newTree.emptyDelta();
	return newTree;
}
/**
 * Makes the receiver the root tree in the list of trees on which it is based.
 * The receiver's representation becomes a complete tree, while its parents'
 * representations become backward deltas based on the receiver.
 * It is not possible to reroot a source tree that is not immutable, as this
 * would require that its parents be expressed as deltas on a source tree
 * which could still change.
 *
 * @exception InvalidParameterException
 *	receiver is not immutable
 */
public DeltaDataTree reroot() {
	
	/* self mutex critical region */
	reroot(this);
	return this;
}
/**
 * Makes the given source tree the root tree in the list of trees on which it is based.
 * The source tree's representation becomes a complete tree, while its parents'
 * representations become backward deltas based on the source tree.
 * It is not possible to reroot a source tree that is not immutable, as this
 * would require that its parents be expressed as deltas on a source tree
 * which could still change.
 *
 * @param sourceTree
 *	source tree to set as the new root
 * @exception InvalidParameterException
 *	sourceTree is not immutable
 */
protected void reroot(DeltaDataTree sourceTree) {
	if (!sourceTree.isImmutable()) {
		throw new IllegalArgumentException(Policy.bind("dtree.parentsNotImmutable"));
	}
	DeltaDataTree parent = sourceTree.getParent();
	if (parent == null) {
		return;
	}
	this.reroot(parent);
	DeltaDataTree backwardDelta = sourceTree.asBackwardDelta();
	DeltaDataTree complete = parent.assembleWithForwardDelta(sourceTree);
	parent.setRootNode(backwardDelta.getRootNode());
	parent.setParent(sourceTree);
	sourceTree.setRootNode(complete.getRootNode());
	sourceTree.setParent(null);
}
/**
 * Returns the specified node.  Search in the parent if necessary.  Return null
 * if the node is not found or if it has been deleted
 */
protected AbstractDataTreeNode searchNodeAt (IPath key) {
	int keyLength = key.segmentCount();
	for (DeltaDataTree tree = this; tree != null; tree = tree.parent) {
		AbstractDataTreeNode node = tree.rootNode;
		boolean complete = !node.isDelta();
		for (int i = 0; i < keyLength; i++) {
			node = node.childAtOrNull(key.segment(i));
			if (node == null) {
				break;
			}
			if (!node.isDelta()) {
				complete = true;
			}
		}
		if (node != null) {
			if (node.isDeleted()) {
				break;
			} else {
				return node;
			}
		}
		if (complete) {
			// Not found, but complete node encountered, so should not check parent tree.
			break;
		}
	}
	return null;
	}
/**
 * @see AbstractDataTree#setData 
 */
public void setData (IPath key, Object data) {
	
	if (isImmutable()) {
		handleImmutableTree();
	}		

	if (!includes(key)) {
		handleNotFound(key);
	}
	assembleNode(key, new DataDeltaNode(key.lastSegment(), data));
}
/**
 * Sets the parent of the tree.
 */
public void setParent (DeltaDataTree aTree) {
	parent = aTree;
}
/**
 * Sets the root node of the tree
 */
void setRootNode (AbstractDataTreeNode aNode) {
	rootNode = aNode;
}
/**
 * Simplifies the receiver:
 *	- replaces any DataDelta nodes with the same data as the parent 
 *	  with a NoDataDelta node
 *	- removes any empty (leaf NoDataDelta) nodes
 */
protected void simplify(IComparator comparer) {
	
	if (parent == null) {
		return;
	}
	setRootNode(rootNode.simplifyWithParent(rootKey(), parent, comparer));
}
}
