## TinkerDemo

[Walle多渠道打包](Walle.md)


2016年6月微信团队开源了热修复组件tinker,易用且功能强大，能够让应用在不需重新下载安装的情况下实现BUG修复更新。

尝试者写了一个demo，记录下过程和出现的问题。

### Tip
    bakApk下的3个文件需要单独保存好，以后出现问题需要打补丁包，需要用到。
    
    最好每次发布版本之后，都在git上打个tag。

### 支持的功能
    支持类、so、资源替换
    支持gradle
    
### 不支持的功能
    
    不支持修改Androidmanifest.xml文件
    不支持新增四大组件
    对于资源替换不支持remoteview，比如：transition动画、notification icon、桌面图标
    
    
### 代码集成   
   TinkerDemo\build.gradle
   
```
dependencies {
         classpath 'com.android.tools.build:gradle:3.0.0'
         classpath ('com.tencent.tinker:tinker-patch-gradle-plugin:1.9.1')
     }
 ```
 
 app\build.gradle
```
    dependencies {
            implementation("com.tencent.tinker:tinker-android-lib:${TINKER_VERSION}") { changing = true }
            annotationProcessor("com.tencent.tinker:tinker-android-anno:${TINKER_VERSION}") { changing = true }
            compileOnly("com.tencent.tinker:tinker-android-anno:${TINKER_VERSION}") { changing = true }
            implementation "com.android.support:multidex:1.0.1"
    }
```

BaseApplicationLike
```
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.ilyzs.tinkerdemo.BaseApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class BaseApplicationLike extends DefaultApplicationLike {
    public BaseApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }
    
   @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        BaseApplicationContext.application = getApplication();
        BaseApplicationContext.context = getApplication();

        MultiDex.install(base);
        TinkerManager.setTinkerApplicationLike(this);
        TinkerManager.setUpgradeRetryEnable(true);
        TinkerManager.sampleInstallTinker(this);
        Tinker tinker = Tinker.with(getApplication());
    }
}    

```

TinkerManager

```
   /**
     * all use default class, simply Tinker install method
     */
    public static void sampleInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        TinkerInstaller.install(appLike);
        isInstalled = true;

    }
```    
AndroidManifest.xml

```
 android:name=".BaseApplication"
 
```

补丁包安装
```
 TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/tinker/patch_signed_7zip.apk");

```

### 使用

#### 1 生成基础包

在build.gradle中配置好打包方式,将BuildVariants-Build Variant 修改为release，打包。包的默认路径：app\build\bakApk.

生成三个文件：app-release-1201-00-49-24.apk、app-release-1201-00-49-24-mapping.txt、app-release-1201-00-49-24-R.txt

apk即为基础包
```
   signingConfigs {
        release {
            try {
                storeFile file("./keystore/release.keystore")
                storePassword "testres"
                keyAlias "testres"
                keyPassword "testres"
            } catch (ex) {
                throw new InvalidUserDataException(ex.toString())
            }
        }

        debug {
            storeFile file("./keystore/debug.keystore")
        }
    }

```

#### 2 修改代码，修复bug。

修改程序中的bug，最后要修改vesioncode

#### 3 补丁包生成的配置

将基础包配在build.gradle中

```/**
  * you can use assembleRelease to build you base apk
  * use tinkerPatchRelease -POLD_APK=  -PAPPLY_MAPPING=  -PAPPLY_RESOURCE= to build patch
  * add apk from the build/bakApk
  */
 ext {
     //for some reason, you may want to ignore tinkerBuild, such as instant run debug build?
     tinkerEnabled = true
 
     //for normal build
     //old apk file to build patch apk
     tinkerOldApkPath = "${bakPath}/app-release-1201-00-41-05.apk"
     //proguard mapping file to build patch apk
     tinkerApplyMappingPath = "${bakPath}/app-release-1201-00-41-05-mapping.txt"
     //resource R.txt to build patch apk, must input if there is resource changed
     tinkerApplyResourcePath = "${bakPath}/app-release-1201-00-41-05-R.txt"
 
     //only use for build all flavor, if not, just ignore this field
     tinkerBuildFlavorDirectory = "${bakPath}/app-1130-22-57-53-R.txt"
 }
```

只修改方法，替换tinkerOldApkPath 为第一步生成的apk文件

修改了资源文件，同时替换tinkerApplyMappingPath和tinkerApplyResourcePath

#### 4 生成补丁包

选择Gradle下，:app-->Tasks-->-->tinker-->tinkerPatchRelease/Debug生成补丁包

补丁包路径：app\outputs\apk\thinkerPatch\release

找到patch_signed_7zip.apk 复制到手机sd卡中。(正式应用中，可通过联网检查版本号，获取补丁包，而且补丁包最好不要以apk命名)

#### 5 安装补丁包

通过TinkerInstaller.onReceiveUpgradePatch(Context context, String patchLocation)安装补丁包



### 遇到问题

问题1

```Error:A problem occurred configuring project ':app'.
   > Tinker does not support instant run mode, please trigger build by assembleDebug or disable instant run in 'File->Settings...'.
```   
解决办法   

File-->Settings-Build、Execution、Deployment -->instant run    Enable Instant Run 前的复选框取消。


问题2

```
Warning:ignoreWarning is false, but resources.arsc is changed, you should use applyResourceMapping mode to build the new apk, otherwise, it may be crash at some times
com.tencent.tinker.build.util.TinkerPatchException: config_xiaomiRelease_1.0_171203_1223.apkignoreWarning is false, but resources.arsc is changed, you should use applyResourceMapping mode to build the new apk, otherwise, it may be crash a
t some times

```


解决办法

```
tinkerPatch { 
….. 
ignoreWarning = true 
….. 
}

```

问题 3

```
tinkerId is not set!!!

```

解决办法
```
def getTinkerIdValue() {
    return hasProperty("TINKER_ID") ? TINKER_ID : gitSha()
}

```
修改为

```
def getTinkerIdValue() {
    return android.defaultConfig.versionName;
}
```

