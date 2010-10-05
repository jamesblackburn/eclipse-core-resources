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
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.7
 */
public interface IBuildConfigReference {
	/**
	 * @return the project which contains the build configuration referenced
	 * by this.
	 */
	public IProject getProject();

	/**
	 * @return the id of the configuration this build configuration reference refers to.
	 * May be null if this references the active configuration.
	 */
	public String getConfigurationId();

	/**
	 * Set the configuration id to which this reference points. This is only allowed
	 * if the configuration id has not already been set.
	 * @param id the configuration to reference in the project
	 */
	public void setConfigurationId(String id);

}
