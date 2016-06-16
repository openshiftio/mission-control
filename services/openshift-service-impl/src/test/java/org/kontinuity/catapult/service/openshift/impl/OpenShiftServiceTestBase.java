package org.kontinuity.catapult.service.openshift.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public abstract class OpenShiftServiceTestBase implements OpenShiftServiceContainer {

    private static final Logger log = Logger.getLogger(OpenShiftServiceTestBase.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    @Rule
    public DeleteOpenShiftProjectRule deleteOpenShiftProjectRule = new DeleteOpenShiftProjectRule(this);
    
    @Test
    public void createProjectOnly() {
    	// given
        final String projectName = getUniqueProjectName();
        // when (just) creating the project
        final OpenShiftProject project = triggerCreateProject(projectName);
        // then
        final String actualName = project.getName();
        assertEquals("returned project did not have expected name", projectName, actualName);
    }

    @Test
    public void createProjectAndApplyTemplate() throws URISyntaxException {
    	// given
    	final String projectName = getUniqueProjectName();
    	// when creating the project and then applying the template
    	final OpenShiftProject project = triggerCreateProject(projectName);
    	log.log(Level.INFO, "Created project: \'" + projectName + "\'");
    	final URI projectGitHubRepoUri = new URI("https://github.com/redhat-kontinuity/kitchensink-html5-mobile.git");
    	getOpenShiftService().configureProject(project, projectGitHubRepoUri);
    	// then
    	final String actualName = project.getName();
    	assertEquals("returned project did not have expected name", projectName, actualName);
		// checking that all 1 Build Config was created.
		assertThat(project.getResources()).isNotNull().hasSize(1)
		        .contains(new OpenShiftResourceImpl("pipeline", "BuildConfig", project));
    }
    
    @Test(expected = DuplicateProjectException.class)
    public void duplicateProjectNameShouldFail() {
    	// given
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        // when
        final String name = project.getName();
        getOpenShiftService().createProject(name); 
        // then using same name should fail with DPE here
    }

    private String getUniqueProjectName() {
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }

    private OpenShiftProject triggerCreateProject(final String projectName) {
        final OpenShiftProject project = getOpenShiftService().createProject(projectName);
        log.log(Level.INFO, "Created project: \'" + projectName + "\'");
        deleteOpenShiftProjectRule.add(project);
        return project;
    }
}
