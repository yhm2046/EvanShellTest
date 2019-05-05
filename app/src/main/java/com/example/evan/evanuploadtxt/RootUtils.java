package com.example.evan.evanuploadtxt;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * 检查是否root
 */
public class RootUtils {
    private static String LOG_TAG = "wp--------->";

    /**
     * 访问/data目录，查看读写权限
     * 在Android系统中，有些目录是普通用户不能访问的，例如 /data、/system、/etc 等。
     * 我们就已/data为例，来进行读写访问。本着谨慎的态度，我是先写入一个文件，然后读出，查看内容是否匹配，若匹配，才认为系统已经root了。
     * @return
     */
    public static synchronized boolean checkAccessRootData()
    {
        try
        {
            Log.i(LOG_TAG,"to write /data");
            String fileContent = "test_ok";
            Boolean writeFlag = writeFile("/data/su_test",fileContent);
            if (writeFlag){
                Log.i(LOG_TAG,"write ok");
            }else{
                Log.i(LOG_TAG,"write failed");
            }

            Log.i(LOG_TAG,"to read /data");
            String strRead = readFile("/data/su_test");
            Log.i(LOG_TAG,"strRead="+strRead);
            if(fileContent.equals(strRead)){
                return true;
            }else {
                return false;
            }
        } catch (Exception e)
        {
            Log.i(LOG_TAG, "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        }
    }
    //写文件
    public static Boolean writeFile(String fileName,String message){
        try{
            FileOutputStream fout = new FileOutputStream(fileName);
            byte [] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    //读文件
    public static String readFile(String fileName){
        File file = new File(fileName);
        try {
            FileInputStream fis= new FileInputStream(file);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while((len=fis.read(bytes))>0){
                bos.write(bytes, 0, len);
            }
            String result = new String(bos.toByteArray());
            Log.i(LOG_TAG, result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 方法四：Android是基于Linux系统的，可是在终端Terminal中操作，会发现一些基本的命令都找不到。这是由于Android系统为了安全，将可能带来风险的命令都去掉了，最典型的，例如su，还有find、mount等。对于一个已经获取了超级权限的人来讲，这是很不爽的事情，所以，便要想办法加上自己需要的命令了。一个个添加命令也麻烦，有一个很方便的方法，就是使用被称为“嵌入式Linux中的瑞士军刀”的Busybox。简单的说BusyBox就好像是个大工具箱，它集成压缩了 Linux 的许多工具和命令。
     * 所以若设备root了，很可能Busybox也被安装上了。这样我们运行busybox测试也是一个好的检测方法。
     * @return
     */
    public static synchronized boolean checkBusybox()
    {
        try
        {
            Log.i(LOG_TAG,"to exec busybox df");
            String[] strCmd = new String[] {"busybox","df"};
            ArrayList<String> execResult = executeCommand(strCmd);
            if (execResult != null){
                Log.i(LOG_TAG,"execResult="+execResult.toString());
                return true;
            }else{
                Log.i(LOG_TAG,"execResult=null");
                return false;
            }
        } catch (Exception e)
        {
            Log.i(LOG_TAG, "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        }
    }
    /**
     * 方法3c：常用目录下是否存在su
     * @return
     */
    public static boolean checkRootPathSU()
    {
        File f=null;
        final String kSuSearchPaths[]={"/system/bin/","/system/xbin/","/system/sbin/","/sbin/","/vendor/bin/"};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++)
            {
                f=new File(kSuSearchPaths[i]+"su");
                if(f!=null&&f.exists())
                {
                    Log.i(LOG_TAG,"find su in : "+kSuSearchPaths[i]);
                    return true;
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**方法3b：which是linux下的一个命令，可以在系统PATH变量指定的路径中搜索某个系统命令的位置并且返回第一个搜索结果。
     这里，我们就用它来查找su
     *
     * @return
     */
    public static boolean checkRootWhichSU() {
        String[] strCmd = new String[] {"/system/xbin/which","su"};
        ArrayList<String> execResult = executeCommand(strCmd);
        if (execResult != null){
            Log.i(LOG_TAG,"execResult="+execResult.toString());
            return true;
        }else{
            Log.i(LOG_TAG,"execResult=null");
            return false;
        }
    }
    /**
     * 执行linux下的shell命令
     * @param shellCmd
     * @return
     */
    public static ArrayList<String> executeCommand(String[] shellCmd){
        String line = null;
        ArrayList<String> fullResponse = new ArrayList<String>();
        Process localProcess = null;
        try {
            Log.i("wp--------->","to shell exec which for find su :");
            localProcess = Runtime.getRuntime().exec(shellCmd);
        } catch (Exception e) {
            return null;
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(localProcess.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
        try {
            while ((line = in.readLine()) != null) {
                Log.i("wp--------->","–> Line received: " + line);
                fullResponse.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("wp--------->","–> Full response was: " + fullResponse);
        return fullResponse;
    }
    /**
     * 方法三a：执行这个命令su。这样，系统就会在PATH路径中搜索su，如果找到，就会执行，执行成功后，就是获取到真正的超级权限了。
     * @return
     */
    public static synchronized boolean checkGetRootAuth()
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            Log.i("wp show--------->","to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            Log.i("wp show--------->", "exitValue="+exitValue);
            if (exitValue == 0)
            {
                return true;
            } else
            {
                return false;
            }
        } catch (Exception e)
        {
            Log.i("wp show--------->", "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                process.destroy();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    /**方法二
     * Superuser.apk是一个被广泛使用的用来root安卓设备的软件，所以可以检查这个app是否存在
     * @return
     */
    public static boolean checkSuperuserApk(){
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                Log.i("wp way2:","/system/app/Superuser.apk exist");
                return true;
            }
        } catch (Exception e) { }
        return false;
    }
    /**
     * 方法一:    查看发布的系统版本，是test-keys（测试版），还是release-keys（发布版）
     * @return
     */
    public static boolean checkDeviceDebuggable(){
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            Log.i("wp way1:","buildTags="+buildTags);
            return true;
        }
        return false;
    }
}
