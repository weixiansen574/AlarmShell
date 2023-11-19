package top.weixiansen574.alarmshell.xposed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ShellExecThread extends Thread {
    private final String shFilePath;

    public ShellExecThread(String shFilePath) {
        this.shFilePath = shFilePath;
    }

    @Override
    public void run() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            DataInputStream is = new DataInputStream(process.getInputStream());
            //命令转换成UTF-8避免中文乱码，然后执行脚本
            os.write(("sh " + shFilePath + "\n").getBytes(StandardCharsets.UTF_8));
            os.writeBytes("exit\n");
            os.flush();
            String line = null;
            while ((line = is.readLine()) != null) {
                XposedBridge.log("AlarmShell:"+line);
            }
            process.waitFor();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            XposedBridge.log(sdf.format(System.currentTimeMillis()) + " shell脚本执行结束：" + shFilePath);
        } catch (IOException | InterruptedException e) {
            XposedBridge.log(e);
            throw new RuntimeException(e);
        }
    }
}
