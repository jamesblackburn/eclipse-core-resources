/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * Manages a project variant creation.
 * 
 * @see org.eclipse.core.resources.IProjectVariant
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectVariantManager {
	/**
	 * @return a project variant object for the given project and variant name.
	 * The project and variant need not exist. If the variant is null, then this
	 * denotes the given project's active variant.
	 */
	IProjectVariant createProjectVariant(IProject project, String variant);

	/**
	 * Translate the supplied list of project variants, into an equivalent list
	 * with all active project variants resolved to the project's current
	 * active variant. If a project's active variant cannot be determined, for
	 * example if it is inaccessible, the project variant will not appear in the
	 * result.
	 * @param variants The variants to translate
	 * @return A list of variants, with all active variants converted to actual variants.
	 */
	public IProjectVariant[] translateActiveVariants(IProjectVariant[] variants);
}
