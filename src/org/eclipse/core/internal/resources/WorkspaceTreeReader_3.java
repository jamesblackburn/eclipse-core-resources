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

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.internal.events.BuilderPersistentInfo;

/**
 * Reads version 3 of the workspace tree file format. 
 * 
 * This version differs from version 2 in the amount of information persisted.
 * The first part of the file is identical to version 2. This allows a version 2
 * reader to read a version 3 file.
 * 
 * The extra information includes the names of a projects variants
 * and builder information on a per variant basis.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WorkspaceTreeReader_3 extends WorkspaceTreeReader_2 {
	private int version;
	private List builderInfos;

	public WorkspaceTreeReader_3(Workspace workspace) {
		super(workspace);
	}

	protected int getVersion() {
		return ICoreConstants.WORKSPACE_TREE_VERSION_2;
	}

	/**
	 * Read a workspace tree storing information about multiple projects.
	 */
	public void readTree(DataInputStream input, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			message = Messages.resources_reading;
			monitor.beginTask(message, 10);

			version = ICoreConstants.WORKSPACE_TREE_VERSION_2;
			builderInfos = new ArrayList();

			// Read the version 2 part of the file, but don't set the builder info in
			// the projects. It is stored in builderInfos instead.
			super.readTree(input, monitor);

			// Read the version 3 information if available
			if (input.available() > 0 && input.readInt() == ICoreConstants.WORKSPACE_TREE_VERSION_3) {
				version = ICoreConstants.WORKSPACE_TREE_VERSION_3;

				// Read the variant names and set them in the version 2 builder infos
				int numVariants = input.readInt();
				for (int i = 0; i < numVariants; i++)
					((BuilderPersistentInfo) builderInfos.get(i)).setVariantName(input.readUTF());

				// Read the builder info and trees
				List buildersToBeLinked = new ArrayList(20);
				readBuildersPersistentInfo(null, input, buildersToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				ElementTree[] trees = readTrees(Path.ROOT, input, Policy.subMonitorFor(monitor, Policy.opWork * 40 / 100));
				linkBuildersToTrees(buildersToBeLinked, trees, 0, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
			}

			// Set the builder info on the projects
			setBuilderInfos(builderInfos);

		} catch (IOException e) {
			message = Messages.resources_readProjectTree;
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
		} finally {
			monitor.done();
		}
	}

	public void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			message = Messages.resources_reading;
			monitor.beginTask(message, 10);

			version = ICoreConstants.WORKSPACE_TREE_VERSION_2;
			builderInfos = new ArrayList(2);

			// Read the version 2 part of the file, but don't set the builder info in
			// the projects. It is stored in builderInfos instead.
			super.readTree(project, input, Policy.subMonitorFor(monitor, 8));

			// Read the version 3 information if available
			if (input.available() > 0 && input.readInt() == ICoreConstants.WORKSPACE_TREE_VERSION_3) {
				version = ICoreConstants.WORKSPACE_TREE_VERSION_3;

				// Read the active variant name and set it in the version 2 builder infos
				String activeVariant = input.readUTF();
				for (Iterator it = builderInfos.iterator(); it.hasNext();)
					((BuilderPersistentInfo) it.next()).setVariantName(activeVariant);

				// Read the builder info and trees
				List infos = new ArrayList(5);
				readBuildersPersistentInfo(project, input, infos, Policy.subMonitorFor(monitor, 1));
				ElementTree[] trees = readTrees(project.getFullPath(), input, Policy.subMonitorFor(monitor, 8));
				linkBuildersToTrees(infos, trees, 0, Policy.subMonitorFor(monitor, 1));
			}

			// Set the builder info on the projects
			setBuilderInfos(builderInfos);

		} catch (IOException e) {
			message = Messages.resources_readProjectTree;
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
		} finally {
			monitor.done();
		}
	}

	/*
	 * overrides WorkspaceTreeReader_1#readBuilderInfo
	 * Reads builder info optionally with or without the variant name, depending on the version.
	 */
	protected BuilderPersistentInfo readBuilderInfo(IProject project, DataInputStream input, int index) throws IOException {
		if (version <= ICoreConstants.WORKSPACE_TREE_VERSION_2)
			return super.readBuilderInfo(project, input, index);
		String projectName = input.readUTF();
		// Use the name of the project handle if available
		if (project != null)
			projectName = project.getName();
		String variantName = input.readUTF();
		String builderName = input.readUTF();
		return new BuilderPersistentInfo(projectName, variantName, builderName, index);
	}

	/*
	 * overrides WorkspaceTreeReader_1#linkBuildersToTrees
	 * This implementation allows version 2 and version 3 information to be loaded in separate passes.
	 * Links trees with the given builders, but does not add them to the projects.
	 */
	protected void linkBuildersToTrees(List buildersToBeLinked, ElementTree[] trees, int index, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			for (int i = 0; i < buildersToBeLinked.size(); i++) {
				BuilderPersistentInfo info = (BuilderPersistentInfo) buildersToBeLinked.get(i);
				info.setLastBuildTree(trees[index++]);
				builderInfos.add(info);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Given a list of builder infos, group them by project and set them on the project.
	 */
	private void setBuilderInfos(List infos) {
		Map groupedInfos = new HashMap();
		for (Iterator it = infos.iterator(); it.hasNext();) {
			BuilderPersistentInfo info = (BuilderPersistentInfo) it.next();
			if (!groupedInfos.containsKey(info.getProjectName()))
				groupedInfos.put(info.getProjectName(), new ArrayList());
			((ArrayList) groupedInfos.get(info.getProjectName())).add(info);
		}
		for (Iterator it = groupedInfos.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			IProject proj = workspace.getRoot().getProject((String) entry.getKey());
			workspace.getBuildManager().setBuildersPersistentInfo(proj, (ArrayList) entry.getValue());
		}
	}
}
