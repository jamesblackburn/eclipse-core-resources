/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.refresh;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.RefreshProvider;
import org.eclipse.core.runtime.*;

/**
 * Manages monitors by creating new monitors when projects are added and
 * removing monitors when projects are removed. Also handles the polling
 * mechanism when contributed native monitors cannot handle a project.
 */
class MonitorManager implements IResourceChangeListener, IResourceDeltaVisitor, IPathVariableChangeListener {
	/**
	 * The list of registered monitor factories.
	 */
	private RefreshProvider[] providers;
	/**
	 * The PollingMonitor in charge of doing filesystem polls.
	 */
	protected PollingMonitor pollMonitor;
	/**
	 * Reference to the refresh manager
	 */
	protected RefreshManager refreshManager;
	/**
	 * A mapping of monitors to a list of resources each monitor is responsible
	 * for
	 */
	protected Map registeredMonitors;
	/**
	 * Reference to the worskpace
	 */
	protected IWorkspace workspace;

	public MonitorManager(IWorkspace workspace, RefreshManager refreshManager) {
		this.workspace = workspace;
		this.refreshManager = refreshManager;
		registeredMonitors = Collections.synchronizedMap(new HashMap(10));
		pollMonitor = new PollingMonitor(refreshManager);
	}

	/**
	 * Queries extensions of the refreshProviders extension point, and
	 * creates the provider classes. Will never return <code>null</code>.
	 * 
	 * @return RefreshProvider[] The array of registered <code>RefreshProvider</code>
	 *             objects or an empty array.
	 */
	private RefreshProvider[] getRefreshProviders() {
		if (providers != null)
			return providers;
		IPluginDescriptor descriptor = ResourcesPlugin.getPlugin().getDescriptor();
		IExtensionPoint extensionPoint = descriptor.getExtensionPoint(ResourcesPlugin.PT_REFRESH_PROVIDERS);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		List providers = new ArrayList(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			RefreshProvider provider = null;
			try {
				provider = (RefreshProvider) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
			}
			if (provider != null)
				providers.add(provider);
		}
		return (RefreshProvider[]) providers.toArray(new RefreshProvider[providers.size()]);
	}
	/**
	 * Collects the set of root resources that required monitoring. This
	 * includes projects and all linked resources.
	 */
	private List getResourcesToMonitor() {
		final List resourcesToMonitor = new ArrayList(10);
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			resourcesToMonitor.add(projects[i]);
			try {
				IResource[] members = projects[i].members();
				for (int j = 0; j < members.length; j++)
					if (members[i].isLinked())
						resourcesToMonitor.add(members[i]);
			} catch (CoreException e) {
				ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
			}
		}
		return resourcesToMonitor;
	}
	private boolean isMonitoring(IResource resource) {
		synchronized (registeredMonitors) {
			for (Iterator i = registeredMonitors.keySet().iterator(); i.hasNext();) {
				List resources = (List) registeredMonitors.get(i.next());
				if ((resources != null) && (resources.contains(resource)))
					return true;
			}
		}
		return false;
	}
	private void monitor(IResource resource) {
		if (isMonitoring(resource))
			return;
		boolean pollingMonitorNeeded = true;
		RefreshProvider[] providers = getRefreshProviders();
		for (int i = 0; i < providers.length; i++) {
			IRefreshMonitor monitor = providers[i].installMonitor(resource, refreshManager);
			if (monitor != null) {
				registerMonitor(monitor, resource);
				pollingMonitorNeeded = false;
			}
		}
		if (pollingMonitorNeeded) {
			pollMonitor.monitor(resource);
			registerMonitor(pollMonitor, resource);
		}
	}
	/* (non-Javadoc)
	 * @see IRefreshResult#monitorFailed
	 */
	public void monitorFailed(IRefreshMonitor monitor, IResource resource) {
		if (RefreshManager.DEBUG)
			System.err.println(RefreshManager.DEBUG_PREFIX + " monitor (" + monitor + ") failed to monitor resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
		if (registeredMonitors == null || monitor == null)
			return;
		if (resource == null) {
			List resources = (List) registeredMonitors.get(monitor);
			if (resources == null || resources.isEmpty()) {
				registeredMonitors.remove(monitor);
				return;
			}
			// synchronized: protect the collection during iteration
			synchronized (registeredMonitors) {
				for (Iterator i = resources.iterator(); i.hasNext();) {
					resource = (IResource) i.next();
					pollMonitor.monitor(resource);
					registerMonitor(pollMonitor, resource);
				}
				registeredMonitors.remove(monitor);
			}
		} else {
			removeMonitor(monitor, resource);
			pollMonitor.monitor(resource);
			registerMonitor(pollMonitor, resource);
		}
	}
	/**
	 * @see org.eclipse.core.resources.IPathVariableChangeListener#pathVariableChanged(org.eclipse.core.resources.IPathVariableChangeEvent)
	 */
	public void pathVariableChanged(IPathVariableChangeEvent event) {
		if (registeredMonitors.isEmpty())
			return;
		String variableName = event.getVariableName();
		Set invalidResources = new HashSet();
		for (Iterator i = registeredMonitors.values().iterator(); i.hasNext();) {
			for (Iterator j = ((List) i.next()).iterator(); j.hasNext();) {
				IResource resource = (IResource) j.next();
				IPath rawLocation = resource.getRawLocation();
				if (rawLocation != null) {
					if (rawLocation.segmentCount() > 0 && variableName.equals(rawLocation.segment(0)) && !invalidResources.contains(resource)) {
						invalidResources.add(resource);
					}
				}
			}
		}
		if (!invalidResources.isEmpty()) {
			for (Iterator i = invalidResources.iterator(); i.hasNext();) {
				IResource resource = (IResource) i.next();
				unmonitor(resource);
				monitor(resource);
			}
		}
	}
	private void registerMonitor(IRefreshMonitor monitor, IResource resource) {
		// synchronized: protect the collection during add
		synchronized (registeredMonitors) {
			List resources = (List) registeredMonitors.get(monitor);
			if (resources == null) {
				resources = new ArrayList(1);
				registeredMonitors.put(monitor, resources);
			}
			if (!resources.contains(resource))
				resources.add(resource);
		}
		if (RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " added monitor (" + monitor + ") on resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
	}
	private void removeMonitor(IRefreshMonitor monitor, IResource resource) {
		// synchronized: protect the collection during remove
		synchronized (registeredMonitors) {
			List resources = (List) registeredMonitors.get(monitor);
			if (resources != null && !resources.isEmpty()) 
				resources.remove(resource);
			else
				registeredMonitors.remove(monitor);
		}
		if (RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " removing monitor (" + monitor + ") on resource: " + resource); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
			case IResourceChangeEvent.PRE_DELETE :
			case IResourceChangeEvent.PRE_CLOSE :
				/*
				 * Additions and project open handled in visitor.
				 */
				IProject project = (IProject) event.getResource();
				unmonitor(project);
				unmonitorLinkedContents(project);
				break;
			default :
				try {
					event.getDelta().accept(this);
				} catch (CoreException e) {
					ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
				}
		}
	}
	public void setPollingDelay(long delay) {
		pollMonitor.setPollingDelay(delay);
	}
	/**
	 * Start the monitoring of resources by all monitors.
	 */
	public void start() {
		for (Iterator i = getResourcesToMonitor().iterator(); i.hasNext();)
			monitor((IResource) i.next());
		workspace.addResourceChangeListener(this);
		workspace.getPathVariableManager().addChangeListener(this);
		if (RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " starting monitor manager."); //$NON-NLS-1$
	}
	/**
	 * Stop the monitoring of resources by all monitors.
	 */
	public void stop() {
		workspace.removeResourceChangeListener(this);
		workspace.getPathVariableManager().removeChangeListener(this);
		// synchronized: protect the collection during iteration
		synchronized (registeredMonitors) {
			for (Iterator i = registeredMonitors.keySet().iterator(); i.hasNext();) {
				IRefreshMonitor monitor = (IRefreshMonitor) i.next();
				monitor.unmonitor(null);
			}
		}
		registeredMonitors.clear();
		if (RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " stopping monitor manager."); //$NON-NLS-1$
	}
	private void unmonitor(IResource resource) {
		if (resource == null) {
			return;
		}
		if (!isMonitoring(resource)) {
			return;
		}
		synchronized (registeredMonitors) {
			for (Iterator i = registeredMonitors.entrySet().iterator(); i.hasNext();) {
				Map.Entry current = (Map.Entry) i.next();
				List resources = (List) current.getValue();
				if ((resources != null) && !resources.isEmpty() && resources.contains(resource)) {
					((IRefreshMonitor) current.getKey()).unmonitor(resource);
					resources.remove(resource);
				}
			}
		}
	}
	private void unmonitorLinkedContents(IProject project) {
		IResource[] children = null;
		try {
			children = project.members();
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		if (children != null && children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				if (child.isLinked()) {
					unmonitor(child);
				}
			}
		}
	}
	public boolean visit(IResourceDelta delta) {
		IResource resource = delta.getResource();
		switch (resource.getType()) {
			case IResource.FILE :
			case IResource.FOLDER :
				if (resource.isLinked()) {
					switch (delta.getKind()) {
						case IResourceDelta.ADDED :
							monitor(resource);
							break;
						case IResourceDelta.REMOVED :
							unmonitor(resource);
							break;
						default :
							break;
					}
				}
				return false;
			case IResource.ROOT :
				return true;
			case IResource.PROJECT :
				IProject project = (IProject) resource;
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
						/*
						 * Project deletion is handled in
						 * resourceChanged(IResourceEvent)
						 */
						if (project.isOpen()) {
							monitor(project);
						}
						break;
					case IResourceDelta.CHANGED :
						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
							/*
							 * Project closure is handled in
							 * resourceChanged(IResourceEvent)
							 */
							if (project.isOpen()) {
								monitor(project);
							}
						}
						break;
					default :
						break;
				}
				return true;
			default :
				break;
		}
		return false;
	}

}