/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Alex Collins (Broadcom) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class BuildContext implements IBuildContext {
	/** The project variant for which this context applies. */
	private final IProjectVariant projectVariant;
	/** The build order for the build that this context is part of. */
	private final IProjectVariant[] buildOrder;
	/** The position in the build order array that this project variant is. */
	private final int buildOrderPosition;

	static final IProjectVariant[] EMPTY_PROJECT_VARIANT_ARRAY = new IProjectVariant[0];

	/** Cached lists of referenced and referencing projects and project variants */
	private IProject[] cachedReferencedProjects = null;
	private IProjectVariant[] cachedReferencedProjectVariants = null;
	private IProject[] cachedReferencingProjects = null;
	private IProjectVariant[] cachedReferencingProjectVariants = null;

	/**
	 * Create an empty build context for the given project variant.
	 * @param projectVariant the project variant being built, that we need the context for
	 */
	public BuildContext(IProjectVariant projectVariant) {
		this.projectVariant = projectVariant;
		buildOrder = EMPTY_PROJECT_VARIANT_ARRAY;
		buildOrderPosition = -1;
	}

	/**
	 * Create a build context for the given project variant.
	 * @param projectVariant the project variant being built, that we need the context for
	 * @param buildOrder the build order for the entire build, indicating how cycles etc. have been resolved
	 */
	public BuildContext(IProjectVariant projectVariant, IProjectVariant[] buildOrder) {
		this.projectVariant = projectVariant;
		this.buildOrder = buildOrder;
		int position = -1;
		for (int i = 0; i < buildOrder.length; i++) {
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
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjects()
	 */
	public IProject[] getAllReferencedProjects() {
		if (cachedReferencedProjectVariants == null)
			cachedReferencedProjectVariants = computeReferenced();
		if (cachedReferencedProjects == null)
			cachedReferencedProjects = projectVariantsToProjects(cachedReferencedProjectVariants);
		return (IProject[]) cachedReferencedProjects.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjectVariants()
	 */
	public IProjectVariant[] getAllReferencedProjectVariants() {
		if (cachedReferencedProjectVariants == null)
			cachedReferencedProjectVariants = computeReferenced();
		return (IProjectVariant[]) cachedReferencedProjectVariants.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjects()
	 */
	public IProject[] getAllReferencingProjects() {
		if (cachedReferencingProjectVariants == null)
			cachedReferencingProjectVariants = computeReferencing();
		if (cachedReferencingProjects == null)
			cachedReferencingProjects = projectVariantsToProjects(cachedReferencingProjectVariants);
		return (IProject[]) cachedReferencingProjects.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjectVariants()
	 */
	public IProjectVariant[] getAllReferencingProjectVariants() {
		if (cachedReferencingProjectVariants == null)
			cachedReferencingProjectVariants = computeReferencing();
		return (IProjectVariant[]) cachedReferencingProjectVariants.clone();
	}

	private IProjectVariant[] computeReferenced() {
		Set previousVariants = new HashSet();
		for (int i = 0; i < buildOrderPosition; i++)
			previousVariants.add(buildOrder[i]);

		// Do a depth first search of the project variant's references to construct
		// the references graph.
		return computeReachable(projectVariant, previousVariants, new GetChildrenFunctor() {
			IProjectVariantManager mngr = ResourcesPlugin.getWorkspace().getProjectVariantManager();
			public IProjectVariant[] run(IProjectVariant variant) {
				try {
					return mngr.translateActiveVariants(variant.getProject().getReferencedProjectVariants(variant.getVariant()));
				} catch (CoreException e) {
					return EMPTY_PROJECT_VARIANT_ARRAY;
				}
			}
		});
	}

	private IProjectVariant[] computeReferencing() {
		Set subsequentVariants = new HashSet();
		for (int i = buildOrderPosition+1; i < buildOrder.length; i++)
			subsequentVariants.add(buildOrder[i]);

		// Do a depth first search of the project variant's referencing variants
		// to construct the referencing graph.
		return computeReachable(projectVariant, subsequentVariants, new GetChildrenFunctor() {
			IProjectVariantManager mngr = ResourcesPlugin.getWorkspace().getProjectVariantManager();
			public IProjectVariant[] run(IProjectVariant variant) {
				return mngr.translateActiveVariants(variant.getProject().getReferencingProjectVariants(variant.getVariant()));
			}
		});
	}

	private static interface GetChildrenFunctor {
		IProjectVariant[] run(IProjectVariant variant);
	}

	/**
	 * Perform a depth first search from the given root using the a functor to
	 * get the children for each item.
	 * @returns A set containing all the reachable project variants from the given root.
	 */
	private IProjectVariant[] computeReachable(IProjectVariant root, Set filter, GetChildrenFunctor getChildren) {
		Set result = new HashSet();
		Stack stack = new Stack();
		stack.push(root);
		Set visited = new HashSet();

		if (filter.contains(root))
			result.add(root);

		while (!stack.isEmpty()) {
			IProjectVariant variant = (IProjectVariant) stack.pop();
			visited.add(variant);
			IProjectVariant[] refs = getChildren.run(variant);
			for (int i = 0; i < refs.length; i++) {
				IProjectVariant ref = refs[i];

				if (!filter.contains(ref))
					continue;

				// Avoid exploring cycles
				if (visited.contains(ref))
					continue;

				result.add(ref);
				stack.push(ref);
			}
		}

		return (IProjectVariant[]) result.toArray(new IProjectVariant[result.size()]);
	}

	/**
	 * Convert a list of project variants to projects, while removing duplicates.
	 */
	private IProject[] projectVariantsToProjects(IProjectVariant[] projectVariants) {
		Set set = new HashSet();
		for (int i = 0; i < projectVariants.length; i++)
			set.add(projectVariants[i].getProject());
		return (IProject[]) set.toArray(new IProject[set.size()]);
	}
}
