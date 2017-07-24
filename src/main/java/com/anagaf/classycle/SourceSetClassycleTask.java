package com.anagaf.classycle;

import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.reporting.ReportingExtension;

import java.io.File;

import classycle.ant.DependencyCheckingTask;

/**
 * Source set Classycle analysis task.
 *
 * This task needs a Classycle definition file to perform the analysis. It's possible to specify
 * the definition file path explicitly in build.gradle (e.g. 'classycleMain.definitionFilePath =
 * config/classycle.config'). If the path is not specified explicitly the task checks first 'config/
 * classycle[BuildVariantName].txt' (e.g. 'config/classycleMain.txt') and then 'config/classycle.txt'.
 * If definition file is not found the task does nothing.
 *
 * The analysis result is stored to 'build/reports/classycle/[buildVarianName].txt'.
 */
class SourceSetClassycleTask extends DefaultTask {
    /** Reporting extension. */
    private final ReportingExtension reporting;

    /** Definition file path. */
    private String definitionFilePath;

    /** Directory where class-files are stored. */
    private File classesDir;

    /** Report file. */
    private File reportFile;

    /** Build variant name. */
    private String buildVariantName;

    /**
     * Constructor.
     */
    public SourceSetClassycleTask() {
        final Logger log = getProject().getLogger();

        reporting = getProject().getExtensions().getByType(ReportingExtension.class);

        doLast(task ->
               {
                   final File definitionFile = getDefinitionFile();
                   if (!definitionFile.exists()) {
                       log.info("Cannot find classycle definition file for " + getBuildVariantName() + " build variant");
                       return;
                   }

                   if (!classesDir.exists() || !classesDir.isDirectory()) {
                       throw new RuntimeException("Invalid classycle directory " + classesDir.getAbsolutePath());
                   }

                   reportFile.getParentFile().mkdirs();
                   try {
                       log.debug("Running classycle analysis on: " + classesDir);

                       final DependencyCheckingTask depCheckTask = new DependencyCheckingTask();
                       depCheckTask.setReportFile(reportFile);
                       depCheckTask.setFailOnUnwantedDependencies(true);
                       depCheckTask.setMergeInnerClasses(true);
                       depCheckTask.setDefinitionFile(definitionFile);
                       depCheckTask.setProject(getProject().getAnt().getAntProject());
                       final FileSet fileSet = new FileSet();
                       fileSet.setIncludes("**/*.class");
                       fileSet.setDir(classesDir);
                       fileSet.setProject(depCheckTask.getProject());
                       depCheckTask.add(fileSet);
                       depCheckTask.execute();
                   } catch (Exception e) {
                       throw new RuntimeException("Classycle check failed: " + e.getMessage()
                                                          + ". See report at " + reportFile.getAbsolutePath());

                   }
               });
    }

    /**
     * Returns definition files (may not exist).
     */
    private File getDefinitionFile() {
        File definitionFile;
        if (getDefinitionFilePath() == null) {
            definitionFile = getProject().file("config/classycle"
                                                       + Utils.capitalizeFirstLetter(getBuildVariantName())
                                                       + ".txt");
            if (!definitionFile.exists()) {
                definitionFile = getProject().file("config/classycle.txt");
            }
        } else {
            definitionFile = getProject().file(getDefinitionFilePath());
        }
        return definitionFile;
    }

    void setBuildVariantName(final String buildVariantName) {
        this.buildVariantName = buildVariantName;
        reportFile = reporting.file("classycle/" + buildVariantName + ".txt");
        getOutputs().file(reportFile);
    }

    void setClassesDir(final File classesDir) {
        this.classesDir = classesDir;
        getInputs().dir(classesDir);
    }

    private String getDefinitionFilePath() {
        return definitionFilePath;
    }

    public void setDefinitionFilePath(final String definitionFilePath) {
        this.definitionFilePath = definitionFilePath;
    }

    private String getBuildVariantName() {
        return buildVariantName;
    }
}
