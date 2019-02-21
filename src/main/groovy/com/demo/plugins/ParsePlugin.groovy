package com.demo.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by chrc on 2019/1/24.
 * Desc :
 */

public class ParsePlugin implements Plugin<Project>{

//    String compileModule = "app"
    String compileModule = "listen"

    @Override
    void apply(Project project) {
        project.task("testParsePlugin") {
            System.out.println("hello gradle plugin! This is testParsePlugin")
        }
        project.task("printTask") {
            System.out.println("========================")
            System.out.println("hello gradle plugin!")
            System.out.println("========================")
        }
        System.out.println("========================")
        System.out.println("hello gradle plugin!")
        System.out.println("========================")

        String module = project.path.replace(":", "")
        System.out.println("============current module is "+module)
        AssembleTask assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)
        System.out.println("============assembleTask.isAssemble "+assembleTask.isAssemble)
        System.out.println("============module is equals "+(module.equals(compileModule)))
//        String flavor  = project.rootProject.extensions.ext.configname
        if (assembleTask.isAssemble && module.equals(compileModule)) {
            compileComponents(assembleTask, project)
//            project.android.registerTransform(new ComCodeTransform(project))
        }
    }

    private AssembleTask getTaskInfo(List<String> taskNames) {
        AssembleTask assembleTask = new AssembleTask()
        for (String task : taskNames) {
            if (task.toUpperCase().contains("ASSEMBLE")
                    || task.contains("aR")
                    || task.contains("asR")
                    || task.contains("asD")
                    || task.toUpperCase().contains("TINKER")
                    || task.toUpperCase().contains("INSTALL")
                    || task.toUpperCase().contains("RESGUARD")) {
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true
                }
                assembleTask.isAssemble = true
                System.out.println("debug assembleTask info:"+task)
                String[] strs = task.split(":")
                assembleTask.modules.add(strs.length > 1 ? strs[strs.length - 2] : "all")
                break
            }
        }
        return assembleTask
    }

    /**
     * 自动添加依赖，只在运行assemble任务的才会添加依赖，因此在开发期间组件之间是完全感知不到的，这是做到完全隔离的关键
     * 支持两种语法：module或者groupId:artifactId:version(@aar),前者之间引用module工程，后者使用maven中已经发布的aar
     * @param assembleTask
     * @param project
     */
    private void compileComponents(AssembleTask assembleTask, Project project) {
//        System.out.println("configExt="+project.rootProject.extensions.toString())
        System.out.println("compileComponent=" + project.rootProject.extensions.ext.compileComponent)
        String components
        if (assembleTask.isDebug) {
//            components = (String) project.rootProject.properties.get("debugComponent")
            components = (String) project.rootProject.extensions.ext.compileComponent
        } else {
//            components = (String) project.rootProject.properties.get("compileComponent")
            components = (String) project.rootProject.extensions.ext.compileComponent
        }

        if (components == null || components.length() == 0) {
            System.out.println("there is no add dependencies ")
            return
        }
        String[] compileComponents = components.split(",")
        if (compileComponents == null || compileComponents.length == 0) {
            System.out.println("there is no add dependencies ")
            return
        }
        for (String str : compileComponents) {
            System.out.println("comp is " + str)
            str = str.trim()
            if (str.startsWith(":")) {
                str = str.substring(1)
            }
            // 是否是maven 坐标
            if (StringUtil.isMavenArtifact(str)) {
                /**
                 * 示例语法:groupId:artifactId:version(@aar)
                 * compileComponent=com.luojilab.reader:readercomponent:1.0.0
                 * 注意，前提是已经将组件aar文件发布到maven上，并配置了相应的repositories
                 */
                project.dependencies.add("compile", str)
                System.out.println("add dependencies lib  : " + str)
            } else {
                /**
                 * 示例语法:module
                 * compileComponent=readercomponent,sharecomponent
                 */
                project.dependencies.add("compile", project.project(':' + str))
                System.out.println("add dependencies project : " + str)
            }
        }
    }

    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false
        List<String> modules = new ArrayList<>()
    }
}
