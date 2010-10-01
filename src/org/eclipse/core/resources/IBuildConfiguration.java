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
 * Build Configurations are a mechanism to provide orthogonal config specific
 * builds.  The core maintains build deltas per interested builder, per configuration, 
 * and allow configs to reference each other.
 *<p>
 * All projects have a default configuration with ID {@link #DEFAULT_CONFIG_ID}
 *<p>
 * When a project is built, a specific configuration is built. This config
 * is passed to the builders so they can adapt their behavior
 * appropriately. Builders which don't care about configurations may ignore this,
 * and work as before.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.7
 */
public interface IBuildConfiguration {

	/**
	 * The name of the default config
	 */
	public static final String DEFAULT_CONFIG_ID = ""; //$NON-NLS-1$
	
	// FIXME
	// Add a config ID, as distinct from a config name?
	// Add a config Comment ?

	/**
	 * @return the project that the config is for; never null.
	 */
	public IProject getProject();

	/**
	 * @return the id of the configuration; never null.
	 */
	public String getConfigurationId();

}
