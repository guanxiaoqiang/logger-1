## 日志模块集成orhanobut/logger，快速定位代码位置，个性化打印日志json、xml等数据格式
   链接 ：https://github.com/orhanobut/logger

### 使用说明：

#### （1）. 首先在Application中初始化：

    ```
    //初始化基础日志打印
    Logger.addLogAdapter(new AndroidLogAdapter());
    //添加文件打印
    Logger.addLogAdapter(new DiskLogAdapter());
    ```
    
#### （2）. 使用：

    ```
    Logger.d("debug");
    Logger.e("error");
    Logger.w("warning");
    Logger.v("verbose");
    Logger.i("information");
    Logger.wtf("What a Terrible Failure");
    //打印Json
    Logger.json(JSON_CONTENT);
    //打印xml
    Logger.xml(XML_CONTENT);
    ```
    
#### （3）. 扩展：


当前日志打印会有分割线和多行打印，调试时不便于分析日志，添加单行打印：

    ```
    Logger.addLogAdapter(new AndroidLogAdapter(SingLineFormatStrategy.newBuilder()
            .priority(Log.DEBUG) //日志等级控制
            .showMethod(false)  //是否显示方法名
            .showThreadInfo(true) //是否显示线程信息
            .tag(LOG_TAG) //日志TAG
            .build()));
