package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.internal.properties.PropertyStore;
import java.util.HashMap;
import java.util.Hashtable;

public class ProjectInfo extends ResourceInfo {
	/** The list of builders for this project */
	protected Hashtable builders = null;

	/** The property store for this resource */
	protected PropertyStore propertyStore = null;

	/** The description of this object */
	protected ProjectDescription description = null;

	/** The list of natures for this project */
	protected HashMap natures = null;
public synchronized void clearNatures() {
	natures = null;
}
public Hashtable getBuilders() {
	return builders;
}
/**
 * Returns the description associated with this info.  The return value may be null.
 */
public ProjectDescription getDescription() {
	return description;
}
public IProjectNature getNature(String natureId) {
	// thread safety: (Concurrency001)
	HashMap temp = natures;
	if (temp == null)
		return null;
	return (IProjectNature) temp.get(natureId);
}
/**
 * Returns the property store associated with this info.  The return value may be null.
 */
public PropertyStore getPropertyStore() {
	return propertyStore;
}
public void setBuilders(Hashtable value) {
	builders = value;
}
/**
 * Sets the description associated with this info.  The value may be null.
 */
public void setDescription(ProjectDescription value) {
	description = value;
}
public synchronized void setNature(String natureId, IProjectNature value) {
	// thread safety: (Concurrency001)
	if (value == null) {
		if (natures == null)
			return;
		HashMap temp = (HashMap) natures.clone();
		temp.remove(natureId);
		if (temp.isEmpty())
			natures = null;
		else
			natures = temp;
	} else {
		HashMap temp = natures;
		if (temp == null)
			temp = new HashMap(5);
		else
			temp = (HashMap) natures.clone();
		temp.put(natureId, value);
		natures = temp;
	}
}
/**
 * Sets the property store associated with this info.  The value may be null.
 */
public void setPropertyStore(PropertyStore value) {
	propertyStore = value;
}
}
