/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

public abstract class ModelObject implements Cloneable {
	protected String name;
public ModelObject() {
}
public ModelObject(String name) {
	setName(name);
}
public Object clone() {
	try {
		return super.clone();
	} catch (CloneNotSupportedException e) {
		return null; // won't happen
	}
}
public String getName() {
	return name;
}
public void setName(String value) {
	name = value;
}
}
