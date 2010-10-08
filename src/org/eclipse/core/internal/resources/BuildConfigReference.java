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

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IBuildConfigReference;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

/**
 * Represents a reference to a specific project configuration. The project is
 * stored as an IProject handle, and the configuration as the configurations unique name.
 * This allows references to project configurations that do not exist in the workspace,
 * or configurations that do not exist in the project.
 */
public class BuildConfigReference implements IBuildConfigReference {
	private final IProject project;
	private String configId;

	/**
	 * Create a reference to a project's active configuration.
	 */
	public BuildConfigReference(IProject project) {
		Assert.isLegal(project != null);
		this.project = project;
		this.configId = null;
	}

	/**
	 * Create a reference to a specific configuration of a project.
	 */
	public BuildConfigReference(IProject project, String configurationId) {
		Assert.isLegal(project != null);
		this.project = project;
		this.configId = configurationId;
	}

	/**
	 * Create a reference to a specific configuration of a project.
	 */
	public BuildConfigReference(IBuildConfiguration buildConfiguration) {
		Assert.isLegal(buildConfiguration != null);
		this.project = buildConfiguration.getProject();
		this.configId = buildConfiguration.getConfigurationId();
	}

	/* (non-Javadoc)
	 * @see IBuildConfigReference#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see IBuildConfigReference#getConfigurationName()
	 */
	public String getConfigurationId() {
		return configId;
	}

	/*
	 * (non-Javadoc)
	 * @see IBuildConfigReference#setConfigurationName(String)
	 */
	public void setConfigurationId(String name) {
		Assert.isLegal(name != null);
		Assert.isTrue(configId == null);
		configId = name;
	}

	/**
	 * @return the configuration that this project configuration reference refers to.
	 * If this references the active configuration of a project, the projects current
	 * active configuration is returned.
	 * @throws CoreException if the active configuration for the referenced project cannot
	 * be determined. For example if the references project is not accessible.
	 */
	public IBuildConfiguration getConfiguration() throws CoreException {
		return project.getBuildConfiguration(configId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = prime + project.hashCode();
		result = prime * result + (configId == null ? 0 : configId.hashCode());
		return result;
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
		BuildConfigReference ref = (BuildConfigReference) obj;
		if (!project.equals(ref.project))
			return false;
		if ((configId == null) != (ref.configId == null))
			return false;
		if (configId != null && !configId.equals(ref.configId))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append(project.getName());
		result.append(";"); //$NON-NLS-1$
		if (configId != null)
			result.append(configId);
		else
			result.append("<active>"); //$NON-NLS-1$
		return result.toString();
	}
 }
