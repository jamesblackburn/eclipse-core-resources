/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alex Collins (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.runtime.Assert;

import java.util.HashSet;

import org.eclipse.core.resources.IProjectVariant;

import java.util.*;
import org.eclipse.core.resources.*;

public class BuildContext implements IBuildContext {
	/** The project variant for which this context applies. */
	private final IProjectVariant projectVariant;
	/** The build order for the build that this context is part of. */
	private final IProjectVariant[] buildOrder;
	/** The position in the build order array that this project variant is. */
	private final int buildOrderPosition;

	private static final IProjectVariant[] EMPTY_PROJECT_VARIANT_ARRAY = new IProjectVariant[0];
	/**
	 * A directed acyclic graph of the project variants representing the project
	 * references that caused this build to happen. The tree is rooted at the project
	 * variant that this build context represents.
	 * 
	 * This is computed on demand, when the client requests the information, and is
	 * subsequently cached.
	 */
	private DAG graph = null;

	/**
	 * Set of all project variants that reference this project reference as
	 * part of the build.
	 * 
	 * This is computed on demand, when the client requests the information, and is
	 * subsequently cached.
	 */
	private Set/*<IProjectVariant>*/ allReferences = null;

	/**
	 * List of project variants that this project references, and that preceed it
	 * in the build order.
	 * 
	 * This is computed on demand, when the client requests the information, and is
	 * subsequently cached.
	 */
	private IProjectVariant[] referencedVariants = null;

	/** Cached lists of referencing projects and project variants */
	private IProject[] cachedProjects = null;
	private IProjectVariant[] cachedProjectVariants = null;

	/**
	 * A lightweight implementation of a directed acyclic graph
	 * This implementation does not check for the acyclic nature of the
	 * graph, it relies on the caller to supply a set of edges that does not
	 * create a cycle.
	 */
	private static class DAG {
		private static class Vertex {
			List children;
			Vertex() {
				// Empty ctor
			}
		}

		Map vertexMap = new HashMap();
		Set vertices = new HashSet();

		DAG() {
			// Empty ctor
		}

		public boolean hasVertex(Object id) {
			return vertexMap.containsKey(id);
		}

		public List getChildren(Object id) {
			if (vertexMap.containsKey(id))
				return ((Vertex) vertexMap.get(id)).children;
			return null;
		}

		void addVertex(Object id) {
			Vertex vertex = new Vertex();
			if (!vertices.contains(vertex)) {
				vertices.add(vertex);
				vertexMap.put(id, vertex);
			}
		}

		void addEdge(Object from, Object to) {
			Vertex fromVertex = (Vertex) vertexMap.get(from);
			Vertex toVertex = (Vertex) vertexMap.get(to);
			if (!vertices.contains(fromVertex))
				return;
			if (!vertices.contains(toVertex))
				return;
			fromVertex.children.add(toVertex);
		}
	}

	/**
	 * Create an empty build context for the given project variant.
	 * @param projectVariant the project variant being built, that we need the context for
	 */
	BuildContext(IProjectVariant projectVariant) {
		this.projectVariant = projectVariant;
		buildOrder = EMPTY_PROJECT_VARIANT_ARRAY;
		buildOrderPosition = -1;

		graph = new DAG();
		graph.addVertex(projectVariant);
		allReferences = new HashSet();
		referencedVariants = EMPTY_PROJECT_VARIANT_ARRAY;
	}

	/**
	 * Create a build context for the given project variant.
	 * @param projectVariant the project variant being built, that we need the context for
	 * @param buildOrder the build order for the entire build, indicating how cycles etc. have been resolved
	 */
	BuildContext(IProjectVariant projectVariant, IProjectVariant[] buildOrder) {
		this.projectVariant = projectVariant;
		this.buildOrder = buildOrder;
		int position = -1;
		for (int i = 0; i < position; i++) {
			if (buildOrder[i].equals(projectVariant))
			{
				position = i;
				break;
			}
		}
		buildOrderPosition = position;
		Assert.isTrue(0 <= buildOrderPosition && buildOrderPosition < buildOrder.length);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getReferencingProjects()
	 */
	public IProject[] getAllReferencingProjects() {
		if (allReferences == null)
			computeGraphAndReferencingVariants();
		if (cachedProjects == null) {
			Set set = new HashSet();
			for (Iterator it = allReferences.iterator(); it.hasNext();) {
				set.add(((IProjectVariant) it.next()).getProject());
			}
			cachedProjects = new IProject[set.size()];
			set.toArray(cachedProjects);
		}
		return (IProject[]) cachedProjects.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getReferencingProjectVariants()
	 */
	public IProjectVariant[] getAllReferencingProjectVariants() {
		if (cachedProjectVariants == null) {
			cachedProjectVariants = new IProjectVariant[allReferences.size()];
			allReferences.toArray(cachedProjectVariants);
		}
		return (IProjectVariant[]) cachedProjectVariants.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getReferencingProjectVariants(org.eclipse.core.resources.IProjectVariant)
	 */
	public IProjectVariant[] getReferencingProjectVariants(IProjectVariant variant) {
		if (graph == null)
			computeGraphAndReferencingVariants();
		if (!graph.hasVertex(variant))
			return null;
		List children = graph.getChildren(variant);
		IProjectVariant[] result = new IProjectVariant[children.size()];
		children.toArray(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjectVariants()
	 */
	public IProjectVariant[] getAllReferencedProjectVariants() {
		if (referencedVariants == null)
			computeReferencedVariants();
		return referencedVariants;
	}

	/** Compute the reference graph and set of all variants that reference this project variant. */
	private void computeGraphAndReferencingVariants() {
		graph.addVertex(projectVariant);

		// Get the sets subsequent variants in the build order
		Set subsequentVariants = new HashSet();
		for (int i = buildOrderPosition+1; i < buildOrder.length; i++)
			subsequentVariants.add(buildOrder[i]);

		// Do a depth first search of the project variants references
		// to construct the build context tree
		Stack stack = new Stack();
		stack.push(projectVariant);

		// Set to track the number of unique referencing project variants
		allReferences = new HashSet();

		while (!stack.isEmpty()) {
			IProjectVariant variant = (IProjectVariant) stack.pop();

			IProjectVariant[] refs = variant.getProject().getReferencingProjectVariants(variant.getVariant());
			for (int i = 0; i < refs.length; i++) {
				IProjectVariant ref = refs[i];

				// Don't explore nodes not included in the build order
				if (!subsequentVariants.contains(ref))
					continue;

				// Avoid exploring cycles
				if (stack.contains(ref))
					continue;

				graph.addVertex(ref);
				graph.addEdge(variant, ref);
				stack.push(ref);
				allReferences.add(ref);
			}
		}
	}

	/** Compute the set of all variants that this project variant references. */
	private void computeReferencedVariants() {
		// Get the sets previous variants in the build order
		Set previousVariants = new HashSet();
		for (int i = 0; i < buildOrderPosition; i++)
			previousVariants.add(buildOrder[i]);

		// Intersect the set of previous variants and the set of all referencing project variants
		Set allReferencing = new HashSet();
		allReferencing.addAll(Arrays.asList(projectVariant.getProject().getReferencingProjectVariants(projectVariant.getVariant())));
		allReferencing.retainAll(previousVariants);
		referencedVariants = new IProjectVariant[allReferencing.size()];
		allReferencing.toArray(referencedVariants);
	}
}
