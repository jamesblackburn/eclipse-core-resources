/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Broadcom Corporation - project variants and references
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.internal.events.BuilderPersistentInfo;
import org.eclipse.core.resources.IProject;

/**
 * Reads version 3 of the workspace tree file format. 
 * 
 * This version differs from version 2 in the amount of information persisted
 * for each builder. It reads which project variant a builder is for, along
 * with all the other information read by version 2.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
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
		String projectName = input.readUTF();
		// Use the name of the project handle if available
		if (project != null)
			projectName = project.getName();
		String variantName = input.readUTF();
		String builderName = input.readUTF();
		return new BuilderPersistentInfo(projectName, variantName, builderName, index);
	}
}
