/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Kurtakov <akurtako@redhat.com> - Bug 459343
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.Hashtable;
import java.util.Vector;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A support class for the marker tests.
 */
public class MarkersChangeListener implements IResourceChangeListener {
	protected Hashtable<IPath, Vector<IMarkerDelta>> changes;

	public MarkersChangeListener() {
		reset();
	}

	/**
	 * Returns whether the changes for the given resource (or null for the workspace)
	 * are exactly the added, removed and changed markers given. The arrays may be null.
	 */
	public boolean checkChanges(IResource resource, IMarker[] added, IMarker[] removed, IMarker[] changed) {
		IPath path = resource == null ? Path.ROOT : resource.getFullPath();
		Vector<IMarkerDelta> v = changes.get(path);
		if (v == null) {
			v = new Vector<>();
		}
		int numChanges = (added == null ? 0 : added.length) + (removed == null ? 0 : removed.length) + (changed == null ? 0 : changed.length);
		if (numChanges != v.size()) {
			return false;
		}
		for (int i = 0; i < v.size(); ++i) {
			IMarkerDelta delta = v.elementAt(i);
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
					if (!contains(added, delta.getMarker())) {
						return false;
					}
					break;
				case IResourceDelta.REMOVED :
					if (!contains(removed, delta.getMarker())) {
						return false;
					}
					break;
				case IResourceDelta.CHANGED :
					if (!contains(changed, delta.getMarker())) {
						return false;
					}
					break;
				default :
					throw new Error();
			}
		}
		return true;
	}

	/**
	 * Returns whether the given marker is contained in the given list of markers.
	 */
	protected boolean contains(IMarker[] markers, IMarker marker) {
		if (markers != null) {
			for (IMarker marker2 : markers) {
				if (marker2.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the number of resources (or the workspace) which have had marker changes since last reset.
	 */
	public int numAffectedResources() {
		return changes.size();
	}

	public void reset() {
		changes = new Hashtable<>(11);
	}

	/**
	 * Notification from the workspace.  Extract the marker changes.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		resourceChanged(event.getDelta());
	}

	/**
	 * Recurse over the delta, extracting marker changes.
	 */
	protected void resourceChanged(IResourceDelta delta) {
		if (delta == null) {
			return;
		}
		if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
			IPath path = delta.getFullPath();
			Vector<IMarkerDelta> v = changes.get(path);
			if (v == null) {
				v = new Vector<>();
				changes.put(path, v);
			}
			IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
			for (IMarkerDelta markerDelta : markerDeltas) {
				v.addElement(markerDelta);
			}
		}
		IResourceDelta[] children = delta.getAffectedChildren();
		for (IResourceDelta element : children) {
			resourceChanged(element);
		}
	}
}
