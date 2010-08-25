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
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class ProjectVariantManager implements IProjectVariantManager, IManager {
	public void startup(IProgressMonitor monitor) {
		//stub
	}

	public void shutdown(IProgressMonitor monitor) {
		//stub
	}

	/*
	 * (non-Javadoc)
	 * @see IProjectVariantManager#createProjectVariant(IProject, String)
	 */
	public IProjectVariant createProjectVariant(IProject project, String variant) {
		if (variant == null)
			return new ProjectVariant(project);
		return new ProjectVariant(project, variant);
	}

	/*
	 * (non-Javadoc)
	 * @see IProjectVariantManager#translateActiveVariants(IProjectVariant[])
	 */
	public IProjectVariant[] translateActiveVariants(IProjectVariant[] variants) {
		List result = new ArrayList(variants.length);
		for (int i = 0; i < variants.length; i++) {
			IProjectVariant variant = variants[i];
			IProject project = variant.getProject();
			if (!project.isAccessible())
				continue;
			if (variant.isActiveVariant()) {
				try {
					variant = new ProjectVariant(project, project.getActiveVariant().getVariant());
				} catch (CoreException e) {
					// Ignore the variant
				}
			}
			result.add(variant);
		}
		return (IProjectVariant[]) result.toArray(new IProjectVariant[result.size()]);
	}
}
