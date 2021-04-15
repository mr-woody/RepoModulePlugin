package com.woodys.module.repo

import com.woodys.module.repo.model.ModuleInfo
import com.woodys.module.repo.model.RemoteInfo
import com.woodys.module.repo.model.RepoInfo
import com.woodys.module.repo.utils.GitUtils
import com.woodys.module.repo.utils.RepoUtils
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class RepoSettingsPlugin implements Plugin<Settings> {

    Settings settings
    File projectDir

    void apply(Settings settings) {
        this.settings = settings
        projectDir = settings.rootProject.projectDir

        RepoInfo repoInfo = RepoUtils.getRepoInfo(projectDir)

        boolean initialized = GitUtils.isGitDir(projectDir)
        if (!initialized) {
            GitUtils.init(projectDir)
        }

        RemoteInfo projectRemoteInfo = repoInfo.projectInfo.remoteInfo
        setRemote(projectDir, projectRemoteInfo)

        def currentProjectBranch = GitUtils.getBranchName(projectDir)
        repoInfo.projectInfo.branch = currentProjectBranch

        repoInfo.projectInfo.moduleInfoMap.each {
            ModuleInfo moduleInfo = it.value
            if (moduleInfo.branch == null) {
                moduleInfo.branch = currentProjectBranch
            }

            def moduleGitRootDir = RepoUtils.getModuleGitRootDir(projectDir, moduleInfo)

            def modulePathDir = RepoUtils.getModulePathDir(moduleGitRootDir,moduleInfo.path)

            def moduleName = RepoUtils.getModuleName(moduleInfo.name)

            // include
            settings.include moduleName
            settings.project(moduleName).projectDir = modulePathDir


            // module
            if (moduleGitRootDir.exists()) {
                GitUtils.removeCachedDir(projectDir, moduleGitRootDir.canonicalPath)

                boolean moduleInitialized = GitUtils.isGitDir(moduleGitRootDir)
                if (moduleInitialized) {
                    setRemote(moduleGitRootDir, moduleInfo.remoteInfo)

                    String branch = moduleInfo.branch
                    def currentBranchName = GitUtils.getBranchName(moduleGitRootDir)
                    if (currentBranchName != branch) {
                        boolean isClean = GitUtils.isClean(moduleGitRootDir)
                        if (isClean) {
                            if (moduleInfo.remoteInfo == null) {
                                if (GitUtils.isLocalBranch(moduleGitRootDir, branch)) {
                                    GitUtils.checkoutBranch(moduleGitRootDir, branch)
                                    println "[repo] - module '$moduleName': git checkout $branch"
                                } else {
                                    GitUtils.checkoutNewBranch(moduleGitRootDir, branch)
                                    println "[repo] - module '$moduleName': git checkout -b $branch"
                                }
                            } else {
                                if (GitUtils.isLocalBranch(moduleGitRootDir, branch)) {
                                    GitUtils.checkoutBranch(moduleGitRootDir, branch)
                                    println "[repo] - module '$moduleName': git checkout $branch"
                                } else {
                                    if (GitUtils.isRemoteBranch(moduleGitRootDir, branch)) {
                                        GitUtils.checkoutRemoteBranch(moduleGitRootDir, branch)
                                        println "[repo] - module '$moduleName': git checkout -b $branch origin/$branch"
                                    } else {
                                        GitUtils.checkoutNewBranch(moduleGitRootDir, branch)
                                        println "[repo] - module '$moduleName': git checkout -b $branch"
                                    }
                                }
                            }
                        } else {
                            throw new RuntimeException("[repo] - module '$moduleName': please commit or revert changes before checkout branch '$branch', current branch is '$currentBranchName'.")
                        }
                    }
                } else {
                    RemoteInfo remoteInfo = moduleInfo.remoteInfo
                    if (moduleGitRootDir.list().size() > 0) {
                        GitUtils.init(moduleGitRootDir)
                        if (remoteInfo != null) {
                            GitUtils.addRemote(moduleGitRootDir, remoteInfo.fetchUrl)
                        }
                    } else {
                        if (remoteInfo != null) {
                            String originUrl = remoteInfo.fetchUrl
                            println "[repo] - module '$moduleName': git clone $originUrl --branch $moduleInfo.branch"
                            GitUtils.clone(moduleGitRootDir, originUrl, moduleInfo.branch)
                            if (remoteInfo.pushUrl != remoteInfo.fetchUrl) {
                                GitUtils.setOriginRemotePushUrl(moduleGitRootDir, remoteInfo.pushUrl)
                            }
                        }
                    }
                    GitUtils.addExclude(moduleGitRootDir)
                }
            } else {
                moduleGitRootDir.mkdirs()
                RemoteInfo remoteInfo = moduleInfo.remoteInfo
                if (remoteInfo == null) return

                String originUrl = remoteInfo.fetchUrl
                println "[repo] - module '$moduleName': git clone $originUrl --branch $moduleInfo.branch"
                GitUtils.clone(moduleGitRootDir, originUrl, moduleInfo.branch)
                if (remoteInfo.pushUrl != remoteInfo.fetchUrl) {
                    GitUtils.setOriginRemotePushUrl(moduleGitRootDir, remoteInfo.pushUrl)
                }
                GitUtils.addExclude(moduleGitRootDir)
            }
        }

        if (initialized) {
            GitUtils.updateExclude(projectDir, repoInfo)
        }
    }

    void setRemote(File dir, RemoteInfo remoteInfo) {
        if (remoteInfo == null) {
            GitUtils.removeRemote(dir)
            return
        }

        String fetchUrl = GitUtils.getOriginRemoteFetchUrl(dir)
        if (fetchUrl == null) {
            GitUtils.addRemote(dir, remoteInfo.fetchUrl)
        } else if (remoteInfo.fetchUrl != null && remoteInfo.fetchUrl != fetchUrl) {
            GitUtils.setOriginRemoteUrl(dir, remoteInfo.fetchUrl)
        }

        String pushUrl = GitUtils.getOriginRemotePushUrl(dir)
        if (pushUrl != remoteInfo.pushUrl) {
            GitUtils.setOriginRemotePushUrl(dir, remoteInfo.pushUrl)
        }
    }

}