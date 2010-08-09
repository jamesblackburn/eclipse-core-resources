/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alex Collins (Broadcom Corp.) - initial implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectVariant;

public class ProjectVariant implements IProjectVariant {
	private final IProject project;
	private final String variant;

	ProjectVariant(IProject project, String variant) {
		Assert.isLegal(variant != null && !variant.equals("")); //$NON-NLS-1$
		this.project = project;
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
}
