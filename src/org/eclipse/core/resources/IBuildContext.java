/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * Stores information about the context in which a builder was called.
 * This can be interrogated by a builder to determine if it is the top level
 * build (i.e. was explicitly asked to be built by the UI), or the result of
 * building a reference, and if so the portion of the reference graph that led
 * to it being built.
 */
public interface IBuildContext {
	/**
	 * Gets the projects that will be built as a result of building the
	 * project variant that this build context is for.
	 * 
	 * These are the project variants that reference this project variant,
	 * and therefore caused this build to happen.
	 * 
	 * If the list is empty, then this build occur due to a top-level request
	 * (such as a UI action) instead of as a result of building a
	 * projects references.
	 * 
	 * @return a list of referencing projects; never null.
	 */
	public IProject[] getAllReferencingProjects();

	/**
	 * Gets the project variants that will be built as a result of building the
	 * project variant that this build context is for.
	 * 
	 * These are the project variants that reference this project variant,
	 * and therefore caused this build to happen.
	 * 
	 * If the list is empty, then this build occur due to a top-level request
	 * (such as a UI action) instead of as a result of building a
	 * projects references.
	 * 
	 * @return a list of referencing projects; never null.
	 */
	public IProjectVariant[] getAllReferencingProjectVariants();

	/**
	 * Returns a list of project variants, in arbitrary order, that reference
	 * the specified project variant.
	 * 
	 * This can be used to explore the tree of references that make up this context.
	 * 
	 * This only returns project variants that are part of the build. If the
	 * project variants references include cycles, the result may differ to what
	 * you might expect to be returned when considering all of a project variants
	 * references. This is because edges are removed to generate the build order,
	 * so that cycles do not exist when the builders are run.
	 * 
	 * @param projectVariant the project variant to get the references for 
	 * @return a list of referencing project variants. The list will be empty if
	 * the project variant has no referencing project variants, and will be null
	 * if the project variant is not part of this build context.
	 */
	public IProjectVariant[] getReferencingProjectVariants(IProjectVariant projectVariant);

	/**
	 * Returns a list of project variants that are part of the build, in arbitrary
	 * order, that this project variant references.
	 * 
	 * @return a list of referenced project variants; never null.
	 */
	public IProjectVariant[] getAllReferencedProjectVariants();
}
