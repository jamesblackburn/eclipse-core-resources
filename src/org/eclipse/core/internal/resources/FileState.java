/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.internal.localstore.HistoryStore;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class FileState extends PlatformObject implements IFileState {
	protected long lastModified;
	protected UniversalUniqueIdentifier uuid;
	protected HistoryStore store;
	protected IPath fullPath;

	public FileState(HistoryStore store, IPath fullPath, long lastModified, UniversalUniqueIdentifier uuid) {
		this.store = store;
		this.lastModified = lastModified;
		this.uuid = uuid;
		this.fullPath = fullPath;
	}

	/* (non-Javadoc)
	 * @see IFileState#exists()
	 */
	public boolean exists() {
		return store.exists(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IEncodedStorage#getCharset()
	 */
	public String getCharset() throws CoreException {
		// tries to obtain a description for the file contents
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		InputStream contents = new BufferedInputStream(getContents());
		boolean failed = false;
		try {
			IContentDescription description = contentTypeManager.getDescriptionFor(contents, getName(), new QualifiedName[] {IContentDescription.CHARSET});
			return description == null ? null : description.getCharset();
		} catch (IOException e) {
			failed = true;
			String message = Policy.bind("history.errorContentDescription", getFullPath().toString()); //$NON-NLS-1$		
			throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, getFullPath(), message, e);
		} finally {
			if (contents != null)
				try {
					contents.close();
				} catch (IOException e) {
					if (!failed) {
						String message = Policy.bind("history.errorContentDescription", getFullPath().toString()); //$NON-NLS-1$		
						throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, getFullPath(), message, e);
					}
				}
		}
	}

	/* (non-Javadoc)
	 * @see IFileState#getContents()
	 */
	public InputStream getContents() throws CoreException {
		return store.getContents(this);
	}

	/* (non-Javadoc)
	 * @see IFileState#getFullPath()
	 */
	public IPath getFullPath() {
		return fullPath;
	}

	/* (non-Javadoc)
	 * @see IFileState#getModificationTime()
	 */
	public long getModificationTime() {
		return lastModified;
	}

	/* (non-Javadoc)
	 * @see IFileState#getName()
	 */
	public String getName() {
		return fullPath.lastSegment();
	}

	public UniversalUniqueIdentifier getUUID() {
		return uuid;
	}

	/* (non-Javadoc)
	 * @see IFileState#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * Returns a string representation of this object. Used for debug only.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("FileState(uuid: "); //$NON-NLS-1$
		s.append(uuid.toString());
		s.append(", lastModified: "); //$NON-NLS-1$
		s.append(lastModified);
		s.append(')');
		return s.toString();
	}
}