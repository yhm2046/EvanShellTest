package com.example.evan.evanuploadtxt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.evan.evanuploadtxt.ShellUtils.execCommand;

public class MainActivity extends AppCompatActivity {
private TextView textView;
private String sdcardPath;
//读写权限
    private  static String[]PERMISSIONS_STORAGE={
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.tvShow);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());  //設置滾動條
         if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){   //判断是否android6.0以上
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }   //获取相应权限
                sdcardPath=Environment.getExternalStorageDirectory().getAbsolutePath();//获取相对路径
                Log.i("wp sdcardpath---------->",sdcardPath);
             ;    //执行shell命令
             String shellResutl=doShell("logcat -d");
             textView.setText(shellResutl);
             saveFile(shellResutl,Date2FileName("yyyyMMdd_HHmmss",".txt")); //保存到sd卡
//                    Log.i("wp log---------->",log.toString());
         }
        }

    /**
     *
     * @param nameFormat    日期格式，ex.yyyyMMdd_HHmmss
     * @param fileType  文件类型,比如: .txt
     * @return
     */
    public static String Date2FileName(String nameFormat, String fileType) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(nameFormat);
        String fileName = dateFormat.format(date) + fileType;
        return fileName;
    }

    /**
     * 执行shell命令
     * @param strShell  shell语句
     * @return  shell执行的结果值
     */
        public String doShell(String strShell){
            String result="nothing！！！！";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }   //获取相应权限
            try {
                Process p = Runtime.getRuntime().exec(strShell); //直接调用logcat命令不用adb
                BufferedReader bufferedReader = new BufferedReader( //读取logcat信息
                        new InputStreamReader(p.getInputStream()));
                StringBuilder log=new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    log.append(line+"\n");
                }
                result=log.toString();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    /**
     * 保存文件到sd卡
     * @param stringcontnt  保存的内容
     * @param filename  文件名
     */
    public  void saveFile(String stringcontnt,String filename){
        try{
            FileOutputStream outputStream=new FileOutputStream("/sdcard/"+filename,true);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(outputStream,"UTF-8");
            outputStreamWriter.write(stringcontnt);
            outputStreamWriter.write("/n");
            outputStreamWriter.flush();
            outputStreamWriter.close();
        }
        catch (Exception e){
            Log.i("wp putError------->",e.toString());
        }
    }
}

