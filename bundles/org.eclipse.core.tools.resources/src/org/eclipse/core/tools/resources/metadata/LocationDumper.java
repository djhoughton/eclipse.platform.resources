/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.resources.metadata;

import java.io.*;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import org.eclipse.core.tools.metadata.IStringDumpingStrategy;
import org.eclipse.core.tools.metadata.MultiStrategyDumper;

/**
 * A dumper for .location files.
 *  
 * @see org.eclipse.core.tools.metadata.AbstractDumper
 * @see org.eclipse.core.tools.resources.metadata.LocationStrategy  
 */
public class LocationDumper extends MultiStrategyDumper {

	/**
	 * @see org.eclipse.core.tools.metadata.MultiStrategyDumper#getStringDumpingStrategy(java.io.DataInputStream)
	 */
	@Override
	protected IStringDumpingStrategy getStringDumpingStrategy(DataInputStream input) throws Exception {
		return new LocationStrategy();
	}

	/**
	 * @see org.eclipse.core.tools.metadata.AbstractDumper#openInputStream(java.io.File)
	 */
	@Override
	protected InputStream openInputStream(File file) throws IOException {
		return new SafeChunkyInputStream(file);
	}

}
