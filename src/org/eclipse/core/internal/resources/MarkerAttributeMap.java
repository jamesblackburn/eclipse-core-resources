package org.eclipse.core.internal.resources;
	protected Object[] elements = null;
	protected int count = 0;

	// 8 attribute keys, 8 attribute values
	protected static final int DEFAULT_SIZE = 16;

	this(map.size());
	putAll(map);
/**
public boolean containsKey(Object key) {
		return false;
	for (int i = 0; i < elements.length; i = i + 2)
		if (elements[i] == key)
			return true;
	return false;
}
/**
	if (elements == null || count == 0)
		return false;
	for (int i = 1; i < elements.length; i = i + 2)
		if (elements[i] != null && elements[i].equals(value))
			return true;
	return false;
}
/**
	Object[] expanded = new Object[elements.length + GROW_SIZE];
	System.arraycopy(elements, 0, expanded, 0, elements.length);
	elements = expanded;
}
/**
	key = ((String)key).intern();
		return null;
	for (int i = 0; i < elements.length; i = i + 2) {
		if (elements[i] == key) {
			elements[i] = null;
			Object result = elements[i + 1];
			elements[i + 1] = null;
			count--;
			return result;
		}
	}
	return null;
}
/**