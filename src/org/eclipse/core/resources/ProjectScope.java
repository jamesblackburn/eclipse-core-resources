/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.Preferences;

/**
 * Object representing the project scope in the Eclipse preferences
 * hierarchy. Can be used as a context for searching for preference
 * values (in the IPreferencesService APIs) or for determining the 
 * correct preference node to set values in the store.
 * <p>
 * Project preferences are stored on a per project basis in the
 * project's content area as specified by <code>IProject#getLocation</code>.
 * </p>
 * <p>
 * The path for preferences defined in the project scope hierarchy
 * is as follows: <code>/project/&lt;projectName&gt;/&lt;qualifier&gt;</code>
 * </p>
 * @see IProject#getLocation()
 * @since 3.0
 */
public final class ProjectScope implements IScopeContext {

	/**
	 * String constant (value of <code>"project"</code>) used for the 
	 * scope name for this preference scope.
	 */
	public static final String SCOPE = "project"; //$NON-NLS-1$

	private IProject context;

	/**
	 * Create and return a new project scope for the given project.
	 * 
	 * @param context the project
	 * @throws IllegalArgumentException if the project is <code>null</code>
	 */
	public ProjectScope(IProject context) {
		super();
		if (context == null)
			throw new IllegalArgumentException();
		this.context = context;
	}

	/*
	 * @see org.eclipse.core.runtime.IScopeContext#getNode(java.lang.String)
	 */
	public Preferences getNode(String qualifier) {
		if (qualifier == null)
			throw new IllegalArgumentException();
		if (context == null)
			return null;
		return Platform.getPreferencesService().getRootNode().node(SCOPE).node(context.getName()).node(qualifier);
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IScopeContext#getParentLocation()
	 */
	public IPath getLocation() {
		if ((context == null) || !(context instanceof IResource))
			return null;
		IProject project = ((IResource) context).getProject();
		return project.getLocation();
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IScopeContext#getName()
	 */
	public String getName() {
		return SCOPE;
	}
}
