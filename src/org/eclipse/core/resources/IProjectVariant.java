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
 * Represents a project variant. A project has one or more variants.
 * When a project is built, a specific variant is built. This variant
 * is passed to the builders so that they can adapt their behavior
 * for different variants of the project.
 * @since 3.6
 */
public interface IProjectVariant {
	/**
	 * @return the project that the variant is for; never null.
	 */
	public IProject getProject();

	/**
	 * @return the name of the variant; never null.
	 */
	public String getVariantName();

	/**
	 * @return a copy of the project variant, of type IProjectVariant
	 */
	public Object clone();
}
