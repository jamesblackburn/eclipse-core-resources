/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.resources.IProjectVariant;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.watson.ElementTree;

public class BuilderPersistentInfo {
	protected String builderName;
	/**
	 * Index of this builder in the build spec. A value of -1 indicates
	 * that this index is unknown (it was not serialized in older workspace versions).
	 */
	private int buildSpecIndex = -1;
	protected IProjectVariant[] interestingProjectVariants = ICoreConstants.EMPTY_PROJECT_VARIANT_ARRAY;
	protected ElementTree lastBuildTree;
	protected String projectName;
	protected String variantName;

	public BuilderPersistentInfo(String projectName, String variantName, String builderName, int buildSpecIndex) {
		this.projectName = projectName;
		this.variantName = variantName;
		this.builderName = builderName;
		this.buildSpecIndex = buildSpecIndex;
	}
	public String getBuilderName() {
		return builderName;
	}

	public int getBuildSpecIndex() {
		return buildSpecIndex;
	}

	public IProjectVariant[] getInterestingProjectVariants() {
		return interestingProjectVariants;
	}

	public ElementTree getLastBuiltTree() {
		return lastBuildTree;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getVariantName() {
		return variantName;
	}

	public void setInterestingProjectVariants(IProjectVariant[] projectVariants) {
		interestingProjectVariants = projectVariants;
	}

	public void setLastBuildTree(ElementTree tree) {
		lastBuildTree = tree;
	}
}
