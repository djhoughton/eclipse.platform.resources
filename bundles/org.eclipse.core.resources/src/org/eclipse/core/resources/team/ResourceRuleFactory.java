/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources.team;
import java.util.HashSet;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Default implementation of IResourceRuleFactory. The teamHook extension
 * may subclass to provide more specialized scheduling rules for workspace operations that
 * they participate in.
 * 
 * @see IResourceRuleFactory
 * @since 3.0
 */
public class ResourceRuleFactory implements IResourceRuleFactory {
	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	/**
	 * Creates a new default resource rule factory. This constructor must only
	 * be called by subclasses.
	 */
	protected ResourceRuleFactory() {
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#buildRule</code>.
	 * This default implementation always returns the workspace root.
	 * Subclasses may not currently override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#buildRule
	 */
	public final ISchedulingRule buildRule() {
		return workspace.getRoot();
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#copyRule</code>.
	 * This default implementation always returns the parent of the destination
	 * resource.  Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#copyRule
	 */
	public ISchedulingRule copyRule(IResource source, IResource destination) {
		//source is not modified, destination is created
		return parent(destination);
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#createRule</code>.
	 * This default implementation always returns the parent of the resource 
	 * being created. Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#createRule
	 */
	public ISchedulingRule createRule(IResource resource) {
		return parent(resource);
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#deleteRule</code>.
	 * This default implementation always returns the parent of the resource 
	 * being deleted. Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#deleteRule
	 */
	public ISchedulingRule deleteRule(IResource resource) {
		return parent(resource);
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#markerRule</code>.
	 * This default implementation always returns <code>null</code>.
	 * Subclasses may not currently override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#markerRule
	 */
	public final ISchedulingRule markerRule(IResource resource) {
		return null;
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#modifyRule</code>.
	 * This default implementation always returns the resource being modified. Note
	 * that this must encompass any rule required by the <code>validateSave</code> hook.
	 * Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#modifyRule
	 * @see IFileModificationValidator#validateSave
	 */
	public ISchedulingRule modifyRule(IResource resource) {
		return resource;
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#moveRule</code>.
	 * This default implementation returns a rule that combines the parent
	 * of the source resource and the parent of the destination resource.
	 * Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#moveRule
	 */
	public ISchedulingRule moveRule(IResource source, IResource destination) {
		//move needs the parent of both source and destination
		return MultiRule.combine(parent(source), parent(destination));
	}
	/**
	 * Convenience method to return the parent of the given resource, 
	 * or the resource itself for projects and the workspace root.
	 * @param resource the resource to compute the parent of
	 * @return the parent resource for folders and files, and the
	 * resource itself for projects and the workspace root.
	 */
	protected final ISchedulingRule parent(IResource resource) {
		switch (resource.getType()) {
			case IResource.ROOT :
			case IResource.PROJECT :
				return resource;
			default :
				return resource.getParent();
		}
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#refreshRule</code>.
	 * This default implementation always returns the parent of the resource 
	 * being refreshed.  Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#refreshRule
	 */
	public ISchedulingRule refreshRule(IResource resource) {
		return parent(resource);
	}
	/**
	 * Default implementation of <code>IResourceRuleFactory#validateEditRule</code>.
	 * This default implementation returns a rule that combines the parents of
	 * all read-only resources, or <code>null</code> if there are no read-only
	 * resources. Subclasses may override this method.
	 * 
	 * @see org.eclipse.core.resources.IResourceRuleFactory#validateEditRule
	 */
	public ISchedulingRule validateEditRule(IResource[] resources) {
		if (resources.length == 0)
			return null;
		//optimize rule for single file
		if (resources.length == 1)
			return resources[0].isReadOnly() ? parent(resources[0]) : null;
		//need a lock on the parents of all read-only files
		HashSet rules = new HashSet();
		for (int i = 0; i < resources.length; i++)
			if (resources[i].isReadOnly())
				rules.add(parent(resources[i]));
		if (rules.isEmpty())
			return null;
		if (rules.size() == 1)
			return (ISchedulingRule) rules.iterator().next();
		ISchedulingRule[] ruleArray = (ISchedulingRule[]) rules
				.toArray(new ISchedulingRule[rules.size()]);
		return new MultiRule(ruleArray);
	}
}