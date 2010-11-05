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
 * Represents a reference to another build configuration
 *<p>
 * Build configuration references are created by {@link IProject#newReference()}
 * on the referenced project, and set on the referencing project with 
 * {@link IProjectDescription#setDynamicConfigReferences(String, IBuildConfigReference[])}
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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

	/**
	 * Set the configuration id to which this reference points. This is only allowed
	 * if the configuration id has not already been set.
	 * @param id the configuration to reference in the project
	 */
	public void setConfigurationId(String id);

}
