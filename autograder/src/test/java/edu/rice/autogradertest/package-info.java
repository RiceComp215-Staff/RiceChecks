/*
 * AnnoAutoGrader
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

@GradeProject(
    name = "TP2",
    description = "Another fairly basic project",
    maxPoints = 10.0,
    warningPoints = 1.0)
@GradeTopic(project = "TP2", topic = "Group1", maxPoints = 5.0)
@GradeTopic(project = "TP2", topic = "Group2", maxPoints = 4.0)
@GradeCoverage(project = "TP2")
package edu.rice.autogradertest;

import edu.rice.autograder.annotations.GradeCoverage;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
