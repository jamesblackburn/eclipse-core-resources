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
 * Represents a reference to an {@link IBuildConfiguration}. {@link IBuildConfigReference}s
 * are pointers to build configurations.  Neither the
 * referenced project, nor configuration in the referenced project, need exist.
 *<p>
 * IBuildConfigReferences are created with {@link IProject#newBuildConfigurationReference(String configId)}
 * on the project to be referenced. They are then added to the referencing project
 * with {@link IProjectDescription#setDynamicConfigReferences(String, IBuildConfigReference[])}
 *</p>
 * <p>
 * At build time, the Platform dereferences the {@link IBuildConfigReference}. If the
 * referenced project is accessible, and the build configuration exists on that project,
 * the referenced build configuration is built before the referencing build configuration.
 *</p>
 * <p>
 * A <code>null</code> configuration Id can be specified when creating the {@link IBuildConfigReference}.
 * This resolves to the currently active configuration in the referenced project, as seen when
 * dereferencing the {@link IBuildConfigReference}.
 *</p>
 *
 * @see IBuildConfiguration
 * @see IProject#newBuildConfigurationReference(String)
 * @see IProjectDescription#getDynamicConfigReferences(String)
 * @see IProjectDescription#setDynamicConfigReferences(String, IBuildConfigReference[])
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.7
 */
public interface IBuildConfigReference {

	/**
	 * @return the project container referenced by this build configuration reference ; never null
	 */
	public IProject getProject();

	/**
	 * @return the id of the configuration this build configuration reference refers to.
	 * May be null if this references the active configuration in the project.
	 */
	public String getConfigurationId();

}
