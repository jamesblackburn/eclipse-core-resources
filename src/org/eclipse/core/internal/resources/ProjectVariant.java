/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alex Collins (Broadcom) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

public class ProjectVariant implements IProjectVariant {
	private IProject project;
	/** if null, the variant is the projects active variant */
	private String variant;

	/**
	 * Create a project variant that represents a reference
	 * to a projects active variant.
	 */
	public ProjectVariant(IProject project) {
		this.project = project;
		this.variant = null;
	}

	/**
	 * Create a project variant that represents a reference
	 * to a specific variant of a project.
	 */
	public ProjectVariant(IProject project, String variant) {
		Assert.isLegal(project != null);
		this.project = project;
		this.variant = variant;
	}

	ProjectVariant() {
		project = null;
		variant = null;
	}

	void setProject(IProject project) {
		Assert.isLegal(project != null);
		this.project = project;
	}

	void setVariant(String variant) {
		Assert.isLegal(variant != null);
		this.variant = variant;
	}

	/* (non-Javadoc)
	 * @see IProjectVariant#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see IProjectVariant#getVariant()
	 */
	public String getVariant() {
		return variant;
	}

	/* (non-Javadoc)
	 * @see IProjectVariant#isActiveVariant()
	 */
	public boolean isActiveVariant() {
		return variant == null;
	}

	/* (non-Javadoc)
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((variant == null) ? 0 : variant.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (variant == null) {
			if (other.variant != null)
				return false;
		} else if (!variant.equals(other.variant))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * For debugging purposes only.
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		if (project != null)
			result.append(project.getName());
		else
			result.append("<active>"); //$NON-NLS-1$
		result.append(";"); //$NON-NLS-1$
		if (variant != null)
			result.append(variant);
		else
			result.append("?"); //$NON-NLS-1$
		return result.toString();
	}
 }
