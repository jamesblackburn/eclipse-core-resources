/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.events;

import java.util.EventObject;
import org.eclipse.core.resources.IPathVariableChangeEvent;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.runtime.IPath;

/**
 * Describes a change in path variable. Core's default implementation for the
 * <code>IPathVariableChangeEvent</code> interface.
 */
public class PathVariableChangeEvent extends EventObject implements IPathVariableChangeEvent {
	/**
	 * The name of the changed variable.
	 */
	private String variableName;

	/**
	 * The value of the changed variable (may be null). 
	 */
	private IPath value;

	/** The event type. */
	private int type;

	/**
	 * Constructor for this class.
	 */
	public PathVariableChangeEvent(IPathVariableManager source, String variableName, IPath value, int type) {
		super(source);
		if (type < VARIABLE_CHANGED || type > VARIABLE_DELETED)
			throw new IllegalArgumentException("type = " + type);
		this.variableName = variableName;
		this.value = value;
		this.type = type;
	}

	/**
	 * @see org.eclipse.core.resources.IPathVariableChangeEvent#getValue()
	 */
	public IPath getValue() {
		return value;
	}
	/**
	 * @see org.eclipse.core.resources.IPathVariableChangeEvent#getVariableName()
	 */
	public String getVariableName() {
		return variableName;
	}
	/**
	 * @see org.eclipse.core.resources.IPathVariableChangeEvent#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * Return a string representation of this object.
	 */
	public String toString() {
		String[] typeStrings = { "VARIABLE_CHANGED", "VARIABLE_CREATED", "VARIABLE_DELETED" };
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[variable = ");
		sb.append(variableName);
		sb.append(", type = ");
		sb.append(typeStrings[type - 1]);
		if (type != VARIABLE_DELETED) {
			sb.append(", value = ");
			sb.append(value);
		}
		sb.append("]");
		return sb.toString();
	}

}