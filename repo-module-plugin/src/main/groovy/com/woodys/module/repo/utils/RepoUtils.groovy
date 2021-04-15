package com.woodys.module.repo.utils


import com.woodys.module.repo.model.ModuleInfo
import com.woodys.module.repo.model.ProjectInfo
import com.woodys.module.repo.model.RemoteInfo
import com.woodys.module.repo.model.RepoInfo
import org.gradle.api.GradleException
import org.w3c.dom.Document
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class RepoUtils {

    static RepoInfo getRepoInfo(File projectDir) {
        File repoFile = new File(projectDir, 'repo.xml')
        if (!repoFile.exists()) {
            throw new GradleException("[repo] - repo.xml not found under " + projectDir.absolutePath)
        }

        RepoInfo repoInfo = parseRepo(repoFile)
        return repoInfo
    }

    private static RepoInfo parseRepo(File repoFile) {
        RepoInfo repoInfo = new RepoInfo()
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        DocumentBuilder builder = factory.newDocumentBuilder()
        FileInputStream inputStream = new FileInputStream(repoFile)
        Document doc = builder.parse(inputStream)
        Element rootElement = doc.getDocumentElement()

        RemoteInfo defaultInfo = null
        NodeList defaultNodeList = rootElement.getElementsByTagName("default")
        if (defaultNodeList.getLength() > 1) {
            throw new RuntimeException("[repo] - Make sure there is only one '<default />' element in repo.xml")
        } else if (defaultNodeList.getLength() == 1) {
            defaultInfo = new RemoteInfo()
            Element defaultElement = (Element) defaultNodeList.item(0)
            defaultInfo.fetchUrl = defaultElement.getAttribute('fetch')
            defaultInfo.pushUrl = defaultElement.getAttribute('push')
            if (defaultInfo.pushUrl.trim().isEmpty()) {
                defaultInfo.pushUrl = defaultInfo.fetchUrl
            }
        }

        repoInfo.projectInfo = new ProjectInfo()
        // project remoteInfo info
        NodeList projectNodeList = rootElement.getElementsByTagName("project")
        if (projectNodeList.getLength() > 1) {
            throw new RuntimeException("[repo] - Make sure there is only one '<project />' element in repo.xml")
        } else if (projectNodeList.getLength() == 1) {
            Element projectElement = (Element) projectNodeList.item(0)
            repoInfo.projectInfo.remoteInfo = getProjectRemoteInfo(defaultInfo, projectElement)
            // project module
            NodeList moduleNodeList = projectElement.getElementsByTagName("module")
            repoInfo.projectInfo.moduleInfoMap = new HashMap<>()
            for (int i = 0; i < moduleNodeList.getLength(); i++) {
                Element moduleElement = (Element) moduleNodeList.item(i)
                ModuleInfo moduleInfo = getModuleInfo(defaultInfo, moduleElement)
                repoInfo.projectInfo.moduleInfoMap.put(moduleInfo.name, moduleInfo)
            }
        }

        if (defaultInfo == null) {
            defaultInfo = new RemoteInfo()
            RemoteInfo projectRemoteInfo = repoInfo.projectInfo.remoteInfo
            if (projectRemoteInfo != null) {
                String fetchUrl = projectRemoteInfo.fetchUrl
                if (fetchUrl.startsWith("git@")) {
                    String[] temp = fetchUrl.split(":")
                    defaultInfo.fetchUrl = temp[0] + ':' + temp[1].substring(0, temp[1].lastIndexOf('/'))
                } else {
                    URI uri = new URI(fetchUrl)
                    String path = uri.getPath();
                    String parent = path.substring(0, path.lastIndexOf('/'))
                    defaultInfo.fetchUrl = fetchUrl.replace(uri.getPath(), "") + parent
                }
                defaultInfo.pushUrl = defaultInfo.fetchUrl
            }
        }

        repoInfo.defaultInfo = defaultInfo

        return repoInfo
    }


    static RemoteInfo getProjectRemoteInfo(RemoteInfo defaultInfo, Element element) {
        String origin = element.getAttribute('origin')
        if (origin.trim().isEmpty()) {
            return null
        }

        RemoteInfo remoteInfo
        if (origin.startsWith('http') || origin.startsWith('git@')) {
            remoteInfo = filterOrigin(origin)
        } else {
            if (defaultInfo != null && defaultInfo.fetchUrl != null) {
                remoteInfo = filterOrigin(defaultInfo, origin)
            } else {
                throw new RuntimeException("[repo] - The 'origin' attribute value of the '<project />' element is invalid.")
            }
        }

        return remoteInfo
    }

    static ModuleInfo getModuleInfo(RemoteInfo defaultInfo, Element element) {
        ModuleInfo moduleInfo = new ModuleInfo()
        String name = element.getAttribute("name")
        if (name.trim().isEmpty()) {
            throw new RuntimeException("[repo] - The 'name' attribute value of the '<module />' element is not configured.")
        }
        moduleInfo.name = name

        String local = element.getAttribute("local")
        if (local.trim().isEmpty()) {
            local = "./"
        }
        moduleInfo.local = local


        String path = element.getAttribute("path")
        if (path.trim().isEmpty()) {
            path = "./"
        }
        moduleInfo.path = path


        String branch = element.getAttribute("branch")
        if (branch.trim().isEmpty()) {
            branch = null
        }
        moduleInfo.branch = branch

        String origin = element.getAttribute("origin")
        if (!origin.trim().isEmpty()) {
            RemoteInfo remoteInfo
            if (origin.startsWith("http") || origin.startsWith("git@")) {
                remoteInfo = filterOrigin(origin)
            } else {
                if (defaultInfo != null && defaultInfo.fetchUrl != null) {
                    remoteInfo = filterOrigin(defaultInfo, origin)
                } else {
                    throw new RuntimeException("[repo] - The 'origin' attribute value of the '<module />' element is invalid.")
                }
            }
            moduleInfo.remoteInfo = remoteInfo
        }

        return moduleInfo
    }


    static RemoteInfo filterOrigin(RemoteInfo defaultInfo, String origin) {
        RemoteInfo remoteInfo = new RemoteInfo()
        if (defaultInfo.fetchUrl == defaultInfo.pushUrl) {
            String fetchUrl = defaultInfo.fetchUrl + '/./' + origin
            if (fetchUrl.startsWith("git@")) {
                String[] temp = fetchUrl.split(":")
                remoteInfo.fetchUrl = temp[0] + ':' + PathUtils.normalize(temp[1], true)
                remoteInfo.pushUrl = remoteInfo.fetchUrl
            } else {
                URI uri = new URI(fetchUrl)
                remoteInfo.fetchUrl = fetchUrl.replace(uri.getPath(), "") + PathUtils.normalize(uri.getPath(), true)
                remoteInfo.pushUrl = remoteInfo.fetchUrl
            }
        } else {
            String fetchUrl = defaultInfo.fetchUrl + '/./' + origin
            if (fetchUrl.startsWith("git@")) {
                String[] temp = fetchUrl.split(":")
                remoteInfo.fetchUrl = temp[0] + ':' + PathUtils.normalize(temp[1], true)
            } else {
                URI uri = new URI(fetchUrl)
                remoteInfo.fetchUrl = fetchUrl.replace(uri.getPath(), "") + PathUtils.normalize(uri.getPath(), true)
            }

            String pushUrl = defaultInfo.pushUrl + '/./' + origin
            if (pushUrl.startsWith("git@")) {
                String[] temp = pushUrl.split(":")
                remoteInfo.pushUrl = temp[0] + ':' + PathUtils.normalize(temp[1], true)
            } else {
                URI uri = new URI(pushUrl)
                remoteInfo.pushUrl = pushUrl.replace(uri.getPath(), "") + PathUtils.normalize(uri.getPath(), true)
            }
        }

        if (!remoteInfo.fetchUrl.endsWith('.git')) {
            remoteInfo.fetchUrl += '.git'
        }

        if (!remoteInfo.pushUrl.endsWith('.git')) {
            remoteInfo.pushUrl += '.git'
        }

        return remoteInfo
    }

    static RemoteInfo filterOrigin(String origin) {
        RemoteInfo remoteInfo = new RemoteInfo()
        if (origin.startsWith("git@")) {
            String[] temp = origin.split(":")
            remoteInfo.fetchUrl = temp[0] + ':' + PathUtils.normalize(temp[1], true)
            remoteInfo.pushUrl = remoteInfo.fetchUrl
        } else {
            URI uri = new URI(origin)
            remoteInfo.fetchUrl = origin.replace(uri.getPath(), "") + PathUtils.normalize(uri.getPath(), true)
            remoteInfo.pushUrl = remoteInfo.fetchUrl
        }
        return remoteInfo
    }

    static File getModuleGitRootDir(File projectDir, ModuleInfo moduleInfo) {
        def moduleGitFileName = getModuleGitFileName(moduleInfo)
        if( moduleGitFileName == null){
            throw new RuntimeException("[repo] - The 'origin' attribute value of the '<module />' element is invalid.")
        }
        def moduleParentDir = new File(projectDir, moduleInfo.local)
        return new File(moduleParentDir,moduleGitFileName)
    }

    static File getModulePathDir(File moduleGitRootDir, String path) {
        return new File(moduleGitRootDir, path)
    }

    static String getModuleGitFileName( ModuleInfo moduleInfo) {
        String moduleGitFileName = null
        RemoteInfo remoteInfo = moduleInfo.remoteInfo
        String origin = remoteInfo.fetchUrl
        if (origin.startsWith("git@")) {
            String[] temp = origin.split(":")
            moduleGitFileName = temp[1].substring(temp[1].lastIndexOf('/')).replace(".git","")
        } else {
            URI uri = new URI(origin)
            String path = uri.getPath();
            moduleGitFileName = path.substring(path.lastIndexOf('/')).replace(".git","")
        }
        return moduleGitFileName
    }

    static String getModuleName(String moduleName) {
        String name = moduleName
                .replaceAll("\\\\", "/")
                .replaceAll("./", "")
                .replaceAll("/", ":")

        if(!name.startsWith(":")){
            name = ":" + name
        }
        return name
    }

    static String getModuleName(File projectDir, File moduleDir) {
        String rootPath = projectDir.absolutePath
        String modulePath = moduleDir.absolutePath
        if (modulePath.contains(rootPath)) {
            return modulePath
                    .replace(rootPath, "")
                    .replace("\\", "/")
                    .replace("./", "")
                    .replace("/", ":")
        }
        return ""
    }

}