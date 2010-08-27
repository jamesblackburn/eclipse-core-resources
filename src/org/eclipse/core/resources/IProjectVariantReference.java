/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

/**
 * Represents a project variant reference.
 * @since 3.2
 */
public interface IProjectVariantReference {
	/**
	 * @return the project that this project variant reference refers to.
	 */
	public IProject getProject();

	/**
	 * @return the name of the variant that this project variant reference refers to
	 * or null if it references the active variant.
	 */
	public String getVariantName();

	/**
	 * Set the variant name to which this reference points. This is only allowed
	 * if the variant name has not already been set.
	 * @param name the variant to reference in the project
	 */
	public void setVariantName(String name);

	/**
	 * @return the variant that this project variant reference refers to.
	 * If this references the active variant of a project, the projects current
	 * active variant is returned.
	 * @throws CoreException if the active variant for the referenced project cannot
	 * be determined. For example if the references project is not accessible.
	 */
	public IProjectVariant getVariant() throws CoreException;
}
