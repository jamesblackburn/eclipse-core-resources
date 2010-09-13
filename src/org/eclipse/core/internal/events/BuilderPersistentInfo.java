/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Broadcom Corporation - project variants
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.IProject;

public class BuilderPersistentInfo {
	protected String projectName;
	protected String variantName;
	protected String builderName;
	/**
	 * Index of this builder in the build spec. A value of -1 indicates
	 * that this index is unknown (it was not serialized in older workspace versions).
	 */
	private int buildSpecIndex = -1;
	protected IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
	protected ElementTree lastBuildTree;

	public BuilderPersistentInfo(String projectName, String builderName, int buildSpecIndex) {
		this(projectName, null, builderName, buildSpecIndex);
	}

	public void setVariantName(String variantName) {
		this.variantName = variantName;
	}

	public BuilderPersistentInfo(String projectName, String variantName, String builderName, int buildSpecIndex) {
		this.projectName = projectName;
		this.variantName = variantName;
		this.builderName = builderName;
		this.buildSpecIndex = buildSpecIndex;
	}

	public String getBuilderName() {
		return builderName;
	}

	/**
	 * @return the name of the variant for which this information refers. May return
	 * null if reading persistent data from an old workspace tree.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public String getVariantName() {
		return variantName;
	}

	public int getBuildSpecIndex() {
		return buildSpecIndex;
	}

	public IProject[] getInterestingProjects() {
		return interestingProjects;
	}

	public ElementTree getLastBuiltTree() {
		return lastBuildTree;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setInterestingProjects(IProject[] projects) {
		interestingProjects = projects;
	}

	public void setLastBuildTree(ElementTree tree) {
		lastBuildTree = tree;
	}
}
