/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * Tests protocol of IProjectDescription and other specified behavior
 * that relates to the project description.
 */
public class IProjectDescriptionTest extends EclipseWorkspaceTest {
	public static Test suite() {
		return new TestSuite(IProjectDescriptionTest.class);
	}
	public IProjectDescriptionTest() {
		super();
	}
	public IProjectDescriptionTest(String name) {
		super(name);
	}
	public void testDescriptionConstant() {
		assertEquals("1.0", ".project", IProjectDescription.DESCRIPTION_FILE_NAME);
	}
	/**
	 * Tests that the description file is not dirtied if the description has not actually
	 * changed.
	 */
	public void testDirtyDescription() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IProject target1 = getWorkspace().getRoot().getProject("target1");
		IProject target2 = getWorkspace().getRoot().getProject("target2");
		ensureExistsInWorkspace(project, true);
		IFile descriptionFile = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		assertTrue("1.0", descriptionFile.exists());
		
		long timestamp = descriptionFile.getLocalTimeStamp();
		try {
			//wait a bit to ensure that timestamp granularity does not
			//spoil our test
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			fail("1.99", e1);
		}
		try {
			IProjectDescription description = project.getDescription();
			description.setBuildSpec(description.getBuildSpec());
			description.setComment(description.getComment());
			description.setDynamicReferences(description.getDynamicReferences());
			description.setLocation(description.getLocation());
			description.setName(description.getName());
			description.setNatureIds(description.getNatureIds());
			description.setReferencedProjects(description.getReferencedProjects());
			project.setDescription(description, IResource.NONE, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//the timestamp should be the same
		assertEquals("2.0", timestamp, descriptionFile.getLocalTimeStamp());
		
		//adding a dynamic reference should not dirty the file
		try {
			IProjectDescription description = project.getDescription();
			description.setDynamicReferences( new IProject[] {target1, target2});
			project.setDescription(description, IResource.NONE, null);
		} catch (CoreException e) {
			fail("3.99", e);
		}
		assertEquals("2.1", timestamp, descriptionFile.getLocalTimeStamp());
	}
	public void testDynamicProjectReferences() {
		IProject target1 = getWorkspace().getRoot().getProject("target1");
		IProject target2 = getWorkspace().getRoot().getProject("target2");
		ensureExistsInWorkspace(target1, true);
		ensureExistsInWorkspace(target2, true);

		IProject project = getWorkspace().getRoot().getProject("project");
		ensureExistsInWorkspace(project, true);

		IProjectDescription description = null;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			fail("1.0", e);
		}
		description.setReferencedProjects(new IProject[] { target1 });
		description.setDynamicReferences(new IProject[] { target2 });
		try {
			project.setDescription(description, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		IProject[] refs = null;
		try {
			refs = project.getReferencedProjects();
		} catch (CoreException e2) {
			fail("2.99", e2);
		}
		assertEquals("2.1", 2, refs.length);
		assertEquals("2.2", target1, refs[0]);
		assertEquals("2.3", target2, refs[1]);
		assertEquals("2.4", 1, target1.getReferencingProjects().length);
		assertEquals("2.5", 1, target2.getReferencingProjects().length);

		//get references for a non-existent project
		try {
			getWorkspace().getRoot().getProject("DoesNotExist").getReferencedProjects();
			fail("3.0");
		} catch (CoreException e1) {
			//should fail
		}
	}
	/**
	 * Tests IProjectDescription project references
	 */
	public void testProjectReferences() {
		IProject target = getWorkspace().getRoot().getProject("Project1");
		ensureExistsInWorkspace(target, true);

		IProject project = getWorkspace().getRoot().getProject("Project2");
		ensureExistsInWorkspace(project, true);

		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("0.0", e);
		}

		IProjectDescription description = null;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			fail("1.0", e);
		}
		description.setReferencedProjects(new IProject[] { target });
		try {
			project.setDescription(description, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertEquals("2.1", 1, target.getReferencingProjects().length);

		//get references for a non-existent project
		try {
			getWorkspace().getRoot().getProject("DoesNotExist").getReferencedProjects();
			fail("3.0");
		} catch (CoreException e1) {
			//should fail
		}
		//get referencing projects for a non-existent project
		IProject[] refs = getWorkspace().getRoot().getProject("DoesNotExist2").getReferencingProjects();
		assertEquals("4.0", 0, refs.length);
	}
}