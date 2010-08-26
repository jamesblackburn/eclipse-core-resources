/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alex Collins (Broadcom) - project variants and references
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.internal.events.BuilderPersistentInfo;
import org.eclipse.core.resources.IProject;

/**
 * Reads version 3 of the workspace tree file format. 
 * 
 * This version differs from version 2 as this reads and writes which project
 * variant a builder is for, and the list of interesting projects is now a list
 * of interesting project variants.
 */
public class WorkspaceTreeReader_3 extends WorkspaceTreeReader_2 {

	public WorkspaceTreeReader_3(Workspace workspace) {
		super(workspace);
	}

	protected int getVersion() {
		return ICoreConstants.WORKSPACE_TREE_VERSION_3;
	}

	/*
	 * overrides WorkspaceTreeReader_1#readBuilderInfo
	 */
	protected BuilderPersistentInfo readBuilderInfo(IProject project, DataInputStream input, int index) throws IOException {
		//read the project name
		String projectName = input.readUTF();
		//use the name of the project handle if available
		if (project != null)
			projectName = project.getName();
		String variantName = input.readUTF();
		String builderName = input.readUTF();
		return new BuilderPersistentInfo(projectName, variantName, builderName, index);
	}
}
