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
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.resources.BuildConfiguration;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Assert;

/**
 * Concrete implementation of a build context
 */
public class BuildContext implements IBuildContext {

	private static final IBuildConfiguration[] EMPTY_BUILD_CONFIGURATION_ARRAY = new IBuildConfiguration[0];

	/** Configurations requested to be built */
	private final IBuildConfiguration[] requestedBuilt;
	/** The configurations built before this {@link BuildConfiguration} */
	private final IBuildConfiguration[] builtBefore;
	/** The configurations to be build after this {@link BuildConfiguration} */
	private final IBuildConfiguration[] builtAfter;

	/** Cached lists of referenced and referencing projects and project configurations */
	private IProject[] cachedReferencedProjects;
	private IProject[] cachedReferencingProjects;

	/**
	 * Create an empty build context for the given project configuration.
	 * @param buildConfiguration the project configuration being built, that we need the context for
	 */
	public BuildContext(IBuildConfiguration buildConfiguration) {
		builtBefore = builtAfter = EMPTY_BUILD_CONFIGURATION_ARRAY;
		requestedBuilt = new IBuildConfiguration[] {buildConfiguration};
	}

	/**
	 * Create a build context for the given project configuration.
	 * @param buildConfiguration the project configuration being built, that we need the context for
	 * @param buildOrder the build order for the entire build, indicating how cycles etc. have been resolved
	 */
	public BuildContext(IBuildConfiguration buildConfiguration, IBuildConfiguration[] requestedBuilt, IBuildConfiguration[] buildOrder) {
		this.requestedBuilt = requestedBuilt;
		int position = -1;
		for (int i = 0; i < buildOrder.length; i++) {
			if (buildOrder[i].equals(buildConfiguration))
			{
				position = i;
				break;
			}
		}
		Assert.isTrue(0 <= position && position < buildOrder.length);
		builtBefore = new IBuildConfiguration[position];
		builtAfter = new IBuildConfiguration[buildOrder.length - position - 1];
		System.arraycopy(buildOrder, 0, builtBefore, 0, builtBefore.length);
		System.arraycopy(buildOrder, position + 1, builtAfter, 0, builtAfter.length);
	}

	public IBuildConfiguration[] getRequestedConfigs() {
		return requestedBuilt;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjects()
	 */
	public IProject[] getAllReferencedProjects() {
		if (cachedReferencedProjects == null)
			cachedReferencedProjects = projectConfigurationsToProjects(builtBefore);
		return (IProject[]) cachedReferencedProjects.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedBuildConfigurations()
	 */
	public IBuildConfiguration[] getAllReferencedBuildConfigurations() {
		return (IBuildConfiguration[]) builtBefore.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjects()
	 */
	public IProject[] getAllReferencingProjects() {
		if (cachedReferencingProjects == null)
			cachedReferencingProjects = projectConfigurationsToProjects(builtAfter);
		return (IProject[]) cachedReferencingProjects.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingBuildConfigurations()
	 */
	public IBuildConfiguration[] getAllReferencingBuildConfigurations() {
		return (IBuildConfiguration[]) builtAfter.clone();
	}

	/**
	 * Convert a list of project configurations to projects, while removing duplicates.
	 */
	private IProject[] projectConfigurationsToProjects(IBuildConfiguration[] configs) {
		Set set = new LinkedHashSet();
		for (int i = 0; i < configs.length; i++)
			set.add(configs[i].getProject());
		return (IProject[]) set.toArray(new IProject[set.size()]);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(builtAfter);
		result = prime * result + Arrays.hashCode(builtBefore);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildContext other = (BuildContext) obj;
		if (!Arrays.equals(builtAfter, other.builtAfter))
			return false;
		if (!Arrays.equals(builtBefore, other.builtBefore))
			return false;
		return true;
	}

}
