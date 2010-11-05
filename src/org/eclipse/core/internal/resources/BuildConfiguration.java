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
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Assert;

/**
 * Concrete implementation of a build configuration
 */
public class BuildConfiguration implements IBuildConfiguration, Cloneable {

	/** Project on which this build configuration is set */
	private final IProject project;
	/** Configuration id is mandatory; never null */
	private final String id;
	/** Human readable name; optional */
	private String name;
	

	/** Ensure we don't expose internal BuildConfigurations to clients */
	boolean readOnly = false;

	public BuildConfiguration() {
		this(null, DEFAULT_CONFIG_ID);
	}

	public BuildConfiguration(String id) {
		this(null, id);
	}
	
	public BuildConfiguration(IProject project) {
		this(project, DEFAULT_CONFIG_ID);
	}

	public BuildConfiguration(IBuildConfiguration config, IProject project) {
		this(project, config.getConfigurationId());
		this.name = config.getName();
	}

	public BuildConfiguration(IProject project, String configurationId) {
		Assert.isNotNull(configurationId);
		this.project = project;
		this.id = configurationId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see IBuildConfiguration#getConfigurationId()
	 */
	public String getConfigurationId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildConfiguration#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see IBuildConfiguration#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Helper method used to work out if the project's build configurations
	 * need to be persisted in the .project.
	 * If the user isn't using build configurations then no need to clutter the project XML.
	 * @return boolean indicating if this configuration is a default auto-generated one.
	 */
	public boolean isDefault() {
		if (!DEFAULT_CONFIG_ID.equals(id))
			return false;
		if (name != null)
			return false;
		return true;
//		// If any of the build configuration references don't track the active configuration,
//		// then this build configuration isn't default
//		IProjectDescription desc = ((Project)project).internalGetDescription();
//		if (desc == null)
//			return true;
//		IBuildConfigReference[] refs = desc.getReferencedProjectConfigs(id);
//		for (int i = 0; i < refs.length; i++)
//			if (refs[i].getConfigurationId() != null)
//				return false;
//		return true;
	}

	public void setName(String name) {
		Assert.isLegal(!readOnly, "BuildConfiguration is read-only."); //$NON-NLS-1$
		this.name = name;
	}

	/**
	 * Helper method which marks the configuration readonly to ensure
	 * we don't expose internal BuildConfigurations to clients.
	 *
	 * Currently only affects the name attribute via {@link #setName(String)}
	 */
	void setReadOnly() {
		readOnly = true;
	}

	/*
	 * (non-Javadoc)
	 * @see Object#clone()
	 */
	public Object clone() {
		try {
			BuildConfiguration bc = ((BuildConfiguration)super.clone());
			bc.readOnly = false;
			return bc;
		} catch (CloneNotSupportedException e) {
			// won't happen
			Assert.isTrue(false);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildConfiguration other = (BuildConfiguration) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (project != null)
			result.append(project.getName());
		else
			result.append("?"); //$NON-NLS-1$
		result.append(";"); //$NON-NLS-1$
		if (name != null)
			result.append(name);
		result.append(" [").append(id).append(']'); //$NON-NLS-1$
		return result.toString();
	}

}
