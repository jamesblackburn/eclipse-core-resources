/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
/**
 * Given a target and a temporary locations, it tries to read the contents
 * from the target. If a file does not exist at the target location, it tries
 * to read the contents from the temporary location.
 *
 * @see SafeFileOutputStream
 */
public class SafeFileInputStream extends FilterInputStream {
	protected static final String EXTENSION = ".bak"; //$NON-NLS-1$
public SafeFileInputStream(File file) throws IOException {
	this(file.getAbsolutePath(), null);
}
public SafeFileInputStream(String targetName) throws IOException {
	this(targetName, null);
}
/**
 * If targetPath is null, the file will be created in the default-temporary directory.
 */
public SafeFileInputStream(String targetPath, String tempPath) throws IOException {
	super(getInputStream(targetPath, tempPath));
}
private static InputStream getInputStream(String targetPath, String tempPath) throws IOException {
	File target = new File(targetPath);
	if (!target.exists()) {
		if (tempPath == null)
			tempPath = target.getAbsolutePath() + EXTENSION;
		target = new File(tempPath);
	}
	return new BufferedInputStream(new FileInputStream(target));
}
}
