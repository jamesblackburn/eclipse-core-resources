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
 * Project Variants are a mechanism to provide orthogonal variant specific
 * builds.  The core maintains build deltas per builder, per variant, 
 * and allow variants to reference other variants.
 *
 * All projects have a default variant.
 *
 * When a project is built, a specific variant is built. This variant
 * is passed to the builders so they can adapt their behavior
 * appropriately. Builders which don't care about variants may ignore this.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.7
 */
public interface IProjectVariant {

	/**
	 * The name of the default variant
	 */
	public static final String DEFAULT_VARIANT = ""; //$NON-NLS-1$
	
	// TODO
	// Add a variant ID, as distinct from a variant name?
	// Add a variant Comment ?

	/**
	 * @return the project that the variant is for; never null.
	 */
	public IProject getProject();

	/**
	 * @return the name of the variant; never null.
	 */
	public String getVariantName();

}
