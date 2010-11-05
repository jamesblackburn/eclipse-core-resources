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
 * referenced project nor configuration in the referenced project need exist.
 * <p>
 * {@link IBuildConfigReference}s are created with {@link IProject#newBuildConfigurationReference(String configId)}
 * on the project to be referenced. It is then added to the referencing project
 * with {@link IProjectDescription#setDynamicConfigReferences(String, IBuildConfigReference[])}
 * <p>
 * At build time, the Platform de-references the {@link IBuildConfigReference}. If the
 * referenced project is accessible, and the build configuration exists on that project,
 * the referenced build configuration is built before the referencing build configuration.
 * <p>
 * A <code>null</code> configuration Id can be specified when creating the {@link IBuildConfigReference}.
 * This resolves to the current active configuration in the referenced project as seen when
 * the {@link IBuildConfigReference} is dereferenced.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @see IBuildConfiguration
 * @since 3.7
 */
public interface IBuildConfigReference {
	/**
	 * @return the project container referenced by this build configuration reference
	 */
	public IProject getProject();

	/**
	 * @return the id of the configuration this build configuration reference refers to.
	 * May be null if this references the active configuration in the project.
	 */
	public String getConfigurationId();
//
//	/**
//	 * Set the configuration id to which this reference points. This is only allowed
//	 * if the configuration id has not already been set.
//	 * @param id the configuration to reference in the project
//	 */
//	public void setConfigurationId(String id);

}
