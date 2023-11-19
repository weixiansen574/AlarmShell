package top.weixiansen574.alarmshell.xposed;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedMain implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        if (loadPackageParam.packageName.equals("com.android.deskclock")) {
            XposedHelpers.findAndHookMethod("com.android.deskclock.alarm.alert.AlarmAlertFullScreenActivity", classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Activity activity = (Activity) param.thisObject;
                    Parcelable alarm = activity.getIntent().getParcelableExtra("intent.extra.alarm");
                    if (alarm != null) {
                        String label = (String) XposedHelpers.getObjectField(alarm, "label");
                        File shellDir = new File("/data/user/0/com.android.deskclock/files/shell/");
                        if (!shellDir.exists()) {
                            shellDir.mkdirs();
                        }
                        if (label == null) {
                            return;
                        }
                        
                        File[] shFiles = shellDir.listFiles();
                        if (shFiles == null) {
                            return;
                        }
                        for (File shFile : shFiles) {
                            String shFileName = shFile.getName();
                            if (shFileName.equals(label)) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String shFilePath = shFile.getAbsolutePath();
                                XposedBridge.log(sdf.format(System.currentTimeMillis()) + " 正在执行shell脚本:" + shFilePath);
                                Thread execThread = new ShellExecThread(shFilePath);
                                execThread.setName("AlarmShellThread");
                                execThread.start();
                                break;
                            } else {
                                XposedBridge.log("闹钟已响，但是没有对应的sh文件："+label);
                            }
                        }
                    }
                }
            });
        }
    }
}
