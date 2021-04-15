# RepoModulePlugin for Android 模块化打包支撑插件

## 简介

 * 方便统一管理一个项目存在多个Git仓库（例如：各个模块独立Git仓库情况），且可能每个git仓库对应的branch不同，为了支持方便快捷的切换分支（解决不同模块对应分支的不一样时）和能自动下载其他git仓库下的module。


## 缺点

 * 需要进行git模块自动管理的模块，需要在repo.xml配置； 一些模块不想被git repo管理模块，按照正常配置在在settings.gradle中的`include`。
 
 * **分支切换尽量在根项目上操作，同步的时候，其他模块会自动跟随切换过去。如果有些模块是使用固定的分支，可以在<module />声明中指定`branch`**


### 如何接入


##### 1. 在根目录的`settings.gradle`文件里添加仓库

```
buildscript {
    repositories {
        ···
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        //repo 插件
        classpath "com.github.mr-woody:RepoModulePlugin:1.0.0"
    }
}


```



###  如何使用

##### 1. 根目录的`settings.gradle`使用插件（plugin）：

```

apply plugin: 'com.woodys.module.repo'

```

##### 2. 项目根目录中创建`repo.xml`文件，并根据项目结构转换成xml格式。

  * repo.xml 示例：
  
  ```
    <?xml version='1.0' encoding='UTF-8'?>
    <repo>
    
        <default fetch="http://git.xxx.cn/stu/" />
    
        <project origin="repomoduleplugin.git">
    
            <module name="business" path="/common-business"  origin="p_common-business.git" />
            <module name="resource" path="/common-resource" local="./repo_modules" origin="http://git.xxx.cn/stu/p_common-resource.git"/>
    
        </project>
    
    </repo>

  ```

  * 关于repo.xml文件格式，如下

  ```

    以下是各元素及其属性的介绍。
    
    repo 元素
    文件的根元素。
    
    default 元素
    最多指定一个default元素。
    
    fetch 属性: Git “fetch” URL的前缀，它将与project元素或module元素的origin拼接成用于“git clone”项目的实际URL地址。
    
    push 属性: Git “push” URL的前缀，它将与project元素或module元素的origin拼接成用于“git push”项目的实际URL地址。该属性为可选项; 如果未指定，则“git push”将使用与fetch属性相同的URL。
    
    project 元素
    最多指定一个project元素。
    
    origin 属性: 远程Git仓库的URL。URL可采用相对地址的方式与default元素拼接成实际URL地址（${fetch}/${origin}.git），也可直接配置完整的URL。
    
        
        module 元素
        可以指定一个或多个module元素。
        
        每一个module元素代表了项目工程中的一个模块，用于描述该模块的远程Git仓库地址、使用的本地分支以及其他相关配置。
        
        name 属性: 模块的名称，且唯一。描述模块的名称，也可以说是别名。

            示例（在settings.gradle中如下,上面的name属性对应：${name}的值）：
    
            include ':${name}'
            project(':${name}').projectDir=new File("../p_common-business/common-business/")

        path 属性: 模块相对当前模块源码目录地址，  所在当前模块git根目录，且唯一。描述模块的名称，也可以说是别名。
    
            示例（在settings.gradle中如下,上面的path属性对应：${path}的值）：
            
            include ':${name}'
            project(':${name}').projectDir = new File(${root_project}/${local}/${git项目默认根文件名称}/${path})
        
        local 属性: 模块相对于根项目的路径。可位于根项目路径之外。如果未指定，则默认位于根项目目录之下。模块对应的路径格式为${root_project}/${local}/${git项目默认根文件名称}
    
        origin 属性: 远程Git仓库的URL。URL可采用相对地址的方式与default元素拼接成实际URL地址（${fetch}/${origin}.git），也可直接配置完整的URL。
        
        branch 属性: Git 分支的名称。如果未指定，则与project分支保持一致。



  ```
 
    



