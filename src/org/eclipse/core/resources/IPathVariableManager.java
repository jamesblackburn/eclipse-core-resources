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

package org.eclipse.core.resources;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * Manages a collection of path variables and resolves paths containing a
 * variable reference.
 *  
 * <p>A path variable is a pair of non-null elements (name,value) where name is 
 * a case-sensitive string (containing only letters, digits and the underscore
 * character, and not starting with a digit), and value is an <code>IPath</code>
 * object.
 * </p>
 * 
 * <p>Path variables allow for the creation of relative paths whose exact
 * location in the file system depends on the value of a variable. A variable
 * reference may only appear as the first segment of a relative path.
 * 
 * <p>This interface is not intended to be implemented by clients.</p>
 * 
 * @see org.eclipse.core.runtime.IPath
 * 
 * @since 2.1
 */
public interface IPathVariableManager {
    
/**
 * Sets the path variable with the given name to be the specified value.
 * Depending on the value given and if the variable is currently defined
 * or not, there are several possible outcomes for this operation:
 * <p>
 * <ol>
 * <li><p>A new variable will be created, if there is no variable defined with
 * the given name, and the given value is not <code>null</code>.</p></li>
 * 
 * <li><p>The referred variable's value will be changed, if it already exists
 * and the given value is not <code>null</code>.</p></li>
 * 
 * <li><p>The referred variable will be removed, if a variable with the given
 * name is currently defined and the given value is <code>null</code> or
 * an empty path (equivalent to <code>Path.EMPTY</code>). Note if the path
 * variable is removed by using <code>Path.EMPTY</code>, the value for the
 * variable in the subsequent <code>IPathVariableChangeEvent</code> will
 * be <code>null</code>.</p></li>
 *  
 * <li><p>The  call will be ignored, if a variable with the given name is
 * not currently defined and the given value is <code>null</code>, or if it
 * is defined but the given value is equal to its current value.
 * </p></li>
 * 
 * <li><p>an <code>IllegalArgumentException</code> will be thrown, if the
 * variable name is invalid.</p></li>
 * </ol>
 * </p>
 * <p>If a variable is effectively changed, created or removed by a call to this
 * method, a proper notification will be sent to all registered listeners.</p>
 * 
 * @param name the name of the variable 
 * @throws IllegalArgumentException if the variable name is invalid
 */
public void setValue(String name,IPath value);

/**
 * Returns the value of the path variable with the given name. If there is no
 * variable defined with the given name, returns <code>null</code>.
 * 
 * @param name the name of the variable to return the value for  
 * @return the value for the variable, or <code>null</code> if there is no
 *    variable  defined with the given name
 */
public IPath getValue(String name);

/**
 * Returns an array containing all defined path variable names.
 *  
 * @return an array containing all defined path variable names
public String[] getPathVariableNames();

/**
 * Registers the given listener to receive notification of changes to path
 * variables. The listener will be notified whenever a variable has been added,
 * removed or had its value changed. Has no effect if an identical path variable
 * changes listener is already registered.
 * 
 * @param listener the listener
 * @see IPathVariableChangeListener
 */
public void addChangeListener(IPathVariableChangeListener listener);

/**
 * Removes the given path variable change listener from the listeners list. Has
 * no effect if an identical listener is not registered.
 * 
 * @param listener the listener 
 * @see IPathVariableChangeListener
 */
public void removeChangeListener(IPathVariableChangeListener listener);

/**
 * Resolves a relative <code>IPath</code> object potentially containing a
 * variable reference as its first segment, replacing the variable reference (if
 * any) with the variable's value (which is a concrete path). If the given path
 * is absolute or has a non-<code>null</code> device then no variable
 * substitution is done and that path is returned as is.
 * <p>
 * If the given path is <code>null</code> then <code>null</code> will be
 * returned.
 * </p>
 * 
 * <p>
 * For example, consider the following collection of path variables:
 * </p>
 * <ul>
 * <li>TEMP = c:/temp</li>
 * <li>BACKUP = /tmp/backup</li>
 * </ul>
 * <p>The following paths would be resolved as:
 * <p>c:/bin => c:/bin</p>
 * <p>c:TEMP => c:TEMP</p>
 * <p>/TEMP => /TEMP</p>
 * <p>TEMP  => c:/temp</p>
 * <p>TEMP/foo  => c:/temp/foo</p>
 * <p>BACKUP  => /tmp/backup</p>
 * <p>BACKUP/bar.txt  => /tmp/backup/bar.txt</p></p>
 * 
 * @param path the path to be resolved
 */
public IPath resolvePath(IPath path);

/**
 * Returns <code>true</code> if the given variable is defined and
 * <code>false</code> otherwise. Returns <code>false</code> if the given name is
 * not a valid path variable name.
 * 
 * @param name the variable's name
 *    otherwise
 */
public boolean isDefined(String name);

/**
 * Validates the given name as the name for a path variable. A valid path
 * variable name is made exclusively of letters, digits and the underscore
 * character, and does not start with a digit.
 * 
 * @param name a possibly valid path variable name
 *    the given name is a valid path variable name, otherwise a status
 *    object indicating what is wrong with the string
 * @see IStatus#OK
 */
public IStatus validateName(String name);

}