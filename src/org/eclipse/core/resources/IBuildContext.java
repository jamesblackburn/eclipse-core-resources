/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;


/**
 * Stores information about the context in which a builder was called.
 * This can be interrogated by a builder to determine if it is the top level
 * build (i.e. was explicitly asked to be built by the UI), or the result of
 * building a reference, and if so the portion of the reference graph that led
 * to it being built.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.7
 */
public interface IBuildContext {
	/**
	 * Gets a list of projects that were built before this build configuration, and
	 * which this build configuration required to be built.
	 *
	 * This includes indirect dependencies as well as direct dependencies. For example
	 * if we have three projects P1, P2 and P3, where P1 references P2 and P2 references P3,
	 * the list of all referenced projects for P1 (as part of building P1 and all its
	 * references) will include _both_ P2 and P3, not just P2.
	 *
	 * @return the list of all referenced projects that have been built
	 * in the current build; never null.
	 */
	public IProject[] getAllReferencedProjects();

	/**
	 * Gets a list of build configuration that were built before this build configuration, and
	 * which this build configuration required to be built.
	 *
	 * This includes indirect dependencies as well as direct dependencies.
	 * @see #getAllReferencedProjects
	 *
	 * @return the list of all referenced build configurations that have been built
	 * in the current build; never null.
	 */
	public IBuildConfiguration[] getAllReferencedBuildConfigurations();

	/**
	 * Gets a list of projects that will be built after this build configuration,
	 * and which reference this build configuration.
	 *
	 * This includes indirect dependencies as well as direct dependencies.
	 * @see #getAllReferencedProjects
	 *
	 * If the list is empty, then this build occurred due to a top-level request
	 * (such as a UI action); not as a result of building another build configuration's
	 * references.
	 *
	 * @return a list of all referencing projects that will be built
	 * in the current build; never null.
	 */
	public IProject[] getAllReferencingProjects();

	/**
	 * Gets a list of build configurations that will be built after this build configuration,
	 * and which reference this build configuration.
	 *
	 * This includes indirect dependencies as well as direct dependencies.
	 * @see #getAllReferencedProjects
	 *
	 * If the list is empty, then this build occurred due to a top-level request
	 * (such as a UI action); not as a result of building another build configuration's
	 * references.
	 *
	 * @return a list of all referencing build configurations that will be built
	 * in the current build; never null.
	 */
	public IBuildConfiguration[] getAllReferencingBuildConfigurations();
}
