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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;

/**
 * Concrete implementation of a build configuration
 */
public class BuildConfiguration implements IBuildConfiguration, Cloneable {

	private IProject project;
	/** Configuration id is mandatory */
	private final String id;
	/** Human readable name; options */
	private String name;

	public BuildConfiguration(IProject project, String name) {
		Assert.isNotNull(name);
		this.project = project;
		this.id = name;
	}

	public BuildConfiguration(String name) {
		this(null, name);
	}

	public BuildConfiguration() {
		this(null, DEFAULT_CONFIG_ID);
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
	 * @see IBuildConfiguration#getProject()
	 */
	public IProject getProject() {
		Assert.isNotNull(project);
		return project;
	}

	IProject internalGetProject() {
		return project;
	}

	void setProject(IProject project) {
		Assert.isNotNull(project);
		Assert.isTrue(this.project == null);
		this.project = project;
	}

	void clearProject() {
		project = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// won't happen
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = prime + id.hashCode();
		result = prime * result + (project == null ? 0 : project.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildConfiguration config = (BuildConfiguration) obj;
		if (!id.equals(config.id))
			return false;
		if ((project == null) != (config.project == null))
			return false;
		if (project != null && !project.equals(config.project))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see Object#toString()
	 */
	/** For debugging purposes only. */
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

	/**
	 * @return boolean indicating if this configuration is a default auto-generated one.
	 */
	public boolean isDefault() {
		if (!DEFAULT_CONFIG_ID.equals(id))
			return false;
		if (name != null)
			return false;
		// FIXME Check the references
		return true;
	}
}
