package com.simple

import android.app.Application
import com.okay.sampletamplate.configurtion.TemplateConfiguration
import com.simple.ui.SampleActivity


public class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        TemplateConfiguration.init(this){

            item {
                title = "支持模块化配置"
                desc = "支持创建多个RetrofitWrapper，并且可以自定义ConfigBuilder"
                clazz =  SampleActivity::class.java
            }

        }
    }
}
