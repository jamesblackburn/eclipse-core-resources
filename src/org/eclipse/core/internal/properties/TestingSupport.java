/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.properties;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides special internal access to the property store implementation.
 * This class is to be used for testing purposes only.
 * 
 * @since 2.1
 */
public class TestingSupport {

/**
 * Return the property store which is associated with the given resource.
 * @since 2.1
public static PropertyStore getPropertyStore(IResource resource) throws CoreException {
	return ((Resource) resource).getPropertyManager().getPropertyStore(resource);
}

}