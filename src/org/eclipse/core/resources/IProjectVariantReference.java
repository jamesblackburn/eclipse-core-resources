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
 * Represents a reference to a project variant.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.7
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

}
