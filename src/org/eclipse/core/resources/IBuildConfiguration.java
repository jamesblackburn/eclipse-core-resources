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
 * Build Configurations provide a mechanism for orthogonal configuration specific
 * builds within a single project.  The resources plugin maintains build deltas per
 * interested builder, per configuration, and allow build configurations to reference
 * each other.
 *<p>
 * All projects have at least one default configuration with ID.  By default this
 * is {@link #DEFAULT_CONFIG_ID}.  One configuration in the project is defined 
 * to be 'active'. The active configuration is built by default.  If unset, the
 * active configuration defaults to the first configuration in the project.
 *<p>
 * BuildConfigurations are created on the project description with:
 * {@link IProjectDescription#newBuildConfiguration(String)}, and
 * set using {@link IProjectDescription#setBuildConfigurations(IBuildConfiguration[])}.
 *<p>
 * When a project is built, a specific configuration is built. This config
 * is passed to the builders so they can adapt their behavior
 * appropriately. Builders which don't care about configurations may ignore this,
 * and work as before.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.7
 */
public interface IBuildConfiguration {

	/**
	 * The name of the default config
	 */
	public static final String DEFAULT_CONFIG_ID = ""; //$NON-NLS-1$

	/**
	 * @return the project that the config is for; never null.
	 */
	public IProject getProject();

	/**
	 * @return the id of the configuration; never null.
	 */
	public String getConfigurationId();

	/**
	 * @return the human readable name of this configuration; may be null
	 */
	public String getName();

	/**
	 * Set the human readable name of this configuration
	 * @param name may be null
	 */
	public void setName(String name);

}
