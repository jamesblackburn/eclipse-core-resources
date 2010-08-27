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

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ProjectVariant implements IProjectVariant {
	private static final String DEFAULT_NAME = ""; //$NON-NLS-1$

	private final String name;
	private IProject project;

	public ProjectVariant(IProject project, String name) {
		this.project = project;
		this.name = name;
	}

	public ProjectVariant(String name) {
		this(null, name);
	}

	public ProjectVariant() {
		this(null, DEFAULT_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see IProjectVariant#getVariantName()
	 */
	public String getVariantName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see IProjectVariant#getProject()
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

	/*
	 * (non-Javadoc)
	 * @see Object#clone()
	 */
	public Object clone() {
		return new ProjectVariant(project, name);
	}

	/*
	 * (non-Javadoc)
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
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
		ProjectVariant other = (ProjectVariant) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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
		result.append(name);
		return result.toString();
	}
}
