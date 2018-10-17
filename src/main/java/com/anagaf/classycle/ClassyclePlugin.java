package com.anagaf.classycle;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Gradle plugin that creates classycle tasks for project build variants. Task names are built as
 * "classycle" + build variant name (e.g. "classycleRelease", "classycleMain" etc).
 *
 * Resulting task graph is:
 * -- check
 * ---- classycle
 * ------ classycleMain
 * ------ classycleTest
 * ...
 */
public class ClassyclePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final Logger logger = project.getLogger();

        final Task classycleTask = project.task("classycle");

        final JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        for (SourceSet sourceSet : javaPlugin.getSourceSets()) {
            logger.debug("Creating classycle tasks for Java source sets");

            createSourceSetClassycleTask(logger,
                                         classycleTask,
                                         sourceSet.getName(),
                                         sourceSet.getOutput().getClassesDir(),
                                         sourceSet.getClassesTaskName());
        }

        final AppExtension androidExtension = project.getExtensions().findByType(AppExtension.class);
        if (androidExtension != null) {
            logger.debug("Creating classycle tasks for Android build variants");

            androidExtension.getApplicationVariants().all(variant -> {
                final Task compileTask = variant.getJavaCompiler();
                if (compileTask instanceof JavaCompile) {
                    final JavaCompile javaCompileTask = (JavaCompile) compileTask;
                    final File classesDir = javaCompileTask.getDestinationDir();
                    createSourceSetClassycleTask(
                        logger,
                        classycleTask,
                        variant.getName(),
                        classesDir,
                        "assemble");
                } else {
                    logger.warn("Unexpected Java compiler task type");
                }
            });
        }

        project.getTasks().getByName("check").dependsOn(classycleTask);
    }

    /**
     * Creates source set classycle task. Adds dependency of the created task to general "classycle"
     * task.
     *
     * @param logger           logger
     * @param classycleTask    general classycle task
     * @param buildVariantName source set name
     * @param classesDir       source set classes directory
     * @param classesTaskName  task that builds class-files
     */
    private void createSourceSetClassycleTask(final Logger logger,
                                              final Task classycleTask,
                                              final String buildVariantName,
                                              final File classesDir,
                                              final String classesTaskName) {
        final Project project = classycleTask.getProject();
        final String taskName = "classycle" + Utils.capitalizeFirstLetter(buildVariantName);
        if (project.getTasks().findByName(taskName) == null) {
            final SourceSetClassycleTask task = project.getTasks().create(taskName, SourceSetClassycleTask.class);
            task.setBuildVariantName(buildVariantName);
            task.setClassesDir(classesDir);
            task.dependsOn(project.getTasks().getByName(classesTaskName));
            classycleTask.dependsOn(task);

            logger.debug("Created task " + taskName
                                 + " for source set " + buildVariantName
                                 + " (classes dir " + classesDir.getAbsolutePath()
                                 + ", classes task name " + classesTaskName
                                 + ")");
        }
    }
}
