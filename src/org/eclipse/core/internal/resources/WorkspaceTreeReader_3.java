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
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WorkspaceTreeReader_3 extends WorkspaceTreeReader_2 {
	private List builderInfos;

	public WorkspaceTreeReader_3(Workspace workspace) {
		super(workspace);
	}

	protected int getVersion() {
		return ICoreConstants.WORKSPACE_TREE_VERSION_2;
	}

	/**
	 * Read a workspace tree storing information about multiple projects.
	 * Overrides {@link WorkspaceTreeReader_1#readTree(DataInputStream, IProgressMonitor)}
	 */
	public void readTree(DataInputStream input, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			message = Messages.resources_reading;
			monitor.beginTask(message, Policy.totalWork);

			builderInfos = new ArrayList(20);

			// Read the version 2 part of the file, but don't set the builder info in
			// the projects. Store it in builderInfos instead.
			readWorkspaceFields(input, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			HashMap savedStates = new HashMap(20);
			List pluginsToBeLinked = new ArrayList(20);
			readPluginsSavedStates(input, savedStates, pluginsToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
			workspace.getSaveManager().setPluginsSavedState(savedStates);

			List buildersToBeLinked = new ArrayList(20);
			readBuildersPersistentInfo(null, input, buildersToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

			final ElementTree[] trees = readTrees(Path.ROOT, input, Policy.subMonitorFor(monitor, Policy.opWork * 40 / 100));
			linkPluginsSavedStateToTrees(pluginsToBeLinked, trees, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
			linkBuildersToTrees(buildersToBeLinked, trees, pluginsToBeLinked.size(), Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

			// Read the version 3 information if available
			if (input.available() > 0 && input.readInt() == ICoreConstants.WORKSPACE_TREE_VERSION_3) {

				buildersToBeLinked.clear();
				readBuildersPersistentInfo(null, input, buildersToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				linkBuildersToTrees(buildersToBeLinked, trees, 0, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

				for (Iterator it = builderInfos.iterator(); it.hasNext();)
					((BuilderPersistentInfo) it.next()).setVariantName(input.readUTF());
			}

			// Set the builder infos on the projects
			setBuilderInfos(builderInfos);

		} catch (IOException e) {
			message = Messages.resources_readProjectTree;
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Read a workspace tree storing information about a single project.
	 * Overrides {@link WorkspaceTreeReader_2#readTree(IProject, DataInputStream, IProgressMonitor)}
	 */
	public void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			message = Messages.resources_reading;
			monitor.beginTask(message, 10);

			builderInfos = new ArrayList(20);

			// Read the version 2 part of the file, but don't set the builder info in
			// the projects. It is stored in builderInfos instead.

			List buildersToBeLinked = new ArrayList(20);
			readBuildersPersistentInfo(project, input, buildersToBeLinked, Policy.subMonitorFor(monitor, 1));

			ElementTree[] trees = readTrees(project.getFullPath(), input, Policy.subMonitorFor(monitor, 8));
			linkBuildersToTrees(buildersToBeLinked, trees, 0, Policy.subMonitorFor(monitor, 1));

			// Read the version 3 information if available
			if (input.available() > 0 && input.readInt() == ICoreConstants.WORKSPACE_TREE_VERSION_3) {

				List infos = new ArrayList(5);
				readBuildersPersistentInfo(project, input, infos, Policy.subMonitorFor(monitor, 1));
				linkBuildersToTrees(infos, trees, 0, Policy.subMonitorFor(monitor, 1));

				for (Iterator it = builderInfos.iterator(); it.hasNext();)
					((BuilderPersistentInfo) it.next()).setVariantName(input.readUTF());
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

	/**
	 * This implementation allows version 2 and version 3 information to be loaded in separate passes.
	 * Links trees with the given builders, but does not add them to the projects.
	 * Overrides {@link WorkspaceTreeReader_1#linkBuildersToTrees(List, ElementTree[], int, IProgressMonitor)}
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
