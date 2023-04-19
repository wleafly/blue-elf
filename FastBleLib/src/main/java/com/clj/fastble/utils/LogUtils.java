package com.clj.fastble.utils;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.util.Log;

import com.clj.fastble.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 类名: LogUtils
 * 描述: 日志打印类
 *      1.获取调用打印类的类名、方法名及行号 格式:(className.methodName(L:lineNumber))
 *      2.输出日志到指定文件及目录
 * create by yzy
 */
public class LogUtils {
    public static Boolean DEBUG = BuildConfig.DEBUG;
    /** 控制日志输出到文件 */
    public static Boolean isWrite = BuildConfig.DEBUG;
    private static int LOG_MAXLENGTH = 2000;
    private static File file = null;
    private static SimpleDateFormat logSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static SimpleDateFormat logFileDate = new SimpleDateFormat("yyyy-MM-dd");
    public static String TAG;

    public static void d(String TAG, String msg) {
        if (DEBUG) {
            msg = Thread.currentThread().getName() + "---" + msg;
            Log.d(TAG, msg);
        }
        setLog("DEBUG", TAG, msg);
    }

    public static void i(String TAG, String msg) {
         if (DEBUG) {
             //长日志分行显示
//             if(msg == null) {
//                Log.i(TAG, "");
//                return ;
//             }
//            msg = Thread.currentThread().getName() + "---" + msg;
//            int strLength = msg.length();
//            int start = 0;
//            int end = LOG_MAXLENGTH;
//            for (int i = 0; i < 100; i++) {
//                if (strLength > end) {
//                    Log.i(TAG + i, msg.substring(start, end));
//                    start = end;
//                    end = end + LOG_MAXLENGTH;
//                  } else {
//                    Log.i(TAG + i, msg.substring(start, strLength));
//                    break;
//                  }
//            }
             msg = Thread.currentThread().getName() + "---" + msg;
             Log.i(TAG , msg);
        }
        setLog("INFO", TAG, msg);
    }

    public static void w(String TAG, String msg) {
        if (DEBUG) {
            msg = Thread.currentThread().getName() + "---" + msg;
            Log.w(TAG, msg);
        }
        setLog("WARN", TAG, msg);
    }

    public static void e(String TAG, String msg) {
    i(TAG , msg);
  }

    public static void e(String TAG, Throwable ex) {
    d(TAG , getStackElement(ex));
  }

    public static void i(String content) {
        StackTraceElement caller = getCallerStackTraceElement();
        content = generateTag(caller) + " " + content;
        i(TAG, content);
    }

    public static void w(String content) {
        StackTraceElement caller = getCallerStackTraceElement();
        content = generateTag(caller) + " " + content;
        w(TAG, content);
    }

    public static void d(String content) {
        StackTraceElement caller = getCallerStackTraceElement();
        content = generateTag(caller) + " " + content;
        d(TAG, content);
    }

    public static void e(String content) {
        StackTraceElement caller = getCallerStackTraceElement();
        content = generateTag(caller) + " " + content;
        e(TAG, content);
    }

    @SuppressLint("DefaultLocale")
    private static String generateTag(StackTraceElement caller) {
        String TAG = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        TAG = String.format(TAG, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return TAG;
    }

    @Deprecated
    /** 以下方法是通过自行利用写入流进行日志输出,已废弃 **/
    private static void setLog(String level, String TAG, String msg) {
        if (isWrite)
            writeLog(level, TAG, msg);
    }

    private static File creatSDFile(String path, String fileName) throws Exception {
        File file = new File(path + fileName);
        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) {
                file.createNewFile();
            }
        }
        return file;
    }

    public static String logPath;
    private static LinkedList<List> linkedList;
    @Deprecated
    private static void writeLog(String level, String TAG, String msg) {
      if(null == logPath){
          return;
      }
      if(null == linkedList){
          linkedList = new LinkedList<>();
      }
      List<String> list = new ArrayList<>();
      list.add(level);
      list.add(TAG);
      list.add(msg);
      linkedList.addLast(list);
      enqueue();
  }
    private static ExecutorService fixedThreadPool;
    @Deprecated
    private static void enqueue(){
        if(null == fixedThreadPool){
            fixedThreadPool = Executors.newFixedThreadPool(1);
        }else{
            return;
        }
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (isStart){
                    try {
                        List<String> list = linkedList.size() > 0 ?linkedList.poll() : null;
                        if (null == list) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                        saveLog(list.get(0), list.get(1), list.get(2));
                    }catch (Exception ex){
                        ex.printStackTrace();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
        });
    }

    private static ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private static boolean isStart = BuildConfig.DEBUG;
    private static FileOutputStream fout;
    @Deprecated
    private static void saveLog(String level, String TAG, String msg) {
        try {
          rwlock.writeLock().lock();
          Calendar nowTime = Calendar.getInstance();
          if(null == file) {
              file = creatSDFile(logPath, logFileDate.format(nowTime.getTime()) + "_client.txt");
              fout = new FileOutputStream(file, true);
          }

          String WriteMessage = "\r\n";

          WriteMessage = logSDF.format(nowTime.getTime());
          WriteMessage = WriteMessage + "  " + Thread.currentThread().getName();
          WriteMessage = WriteMessage + "  " + level;
          WriteMessage = WriteMessage + "  " + TAG;
          WriteMessage = WriteMessage + "  " + msg + "\r\n";
          byte[] bytes = WriteMessage.getBytes();
          while (fout == null){
              //等待输出文件流初始化
          }
          fout.write(bytes);
          fout.flush();
        } catch (Exception e) {
          e.printStackTrace();
        }finally {
            rwlock.writeLock().unlock();
        }
    }
    @Deprecated
    public static void release(){
        try {
            if(fout != null) {
                fout.close();
            }
            file = null;
            fout = null;
            isStart = false;
            if(null != fixedThreadPool){
                fixedThreadPool.shutdown();
            }
            fixedThreadPool = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 以下方法是通过自行利用写入流进行日志输出,已废弃 **/



    /**
     * 获取代码位置
     * @param ex
     * @return
     */
    private static String getStackElement(Throwable ex) {
        StackTraceElement[] ste = ex.getStackTrace();

        StringBuffer sb = new StringBuffer();

        sb.append(" message = " + ex.getMessage());

        sb.append("\r\n");

        for (int i = 0; i < ste.length; i++) {
          sb.append(ste[i]);

          sb.append("\r\n");
        }
        return sb.toString();
  }

    /**
     * 开始收集日志信息
     */
    public static void createLogCollector(final String path) {
        if (path == null) {
            Log.d("LogUtils", "未设置path");
            return;
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    String cmdCollect = path + logFileDate.format(Calendar.getInstance().getTime()) + ".txt";
                    try {
                        List<String> commandList = new ArrayList<>();
                        commandList.add("logcat");
                        commandList.add("-f");
                        commandList.add(cmdCollect);
                        commandList.add("-v");
                        commandList.add("time");
                        commandList.add(TAG + ":I");

                        commandList.add("System.err:W");// 过滤所有的错误信息
                        commandList.add("System.out:I");// 过滤所有的错误信息
                        commandList.add("AndroidRuntime:E"); //运行报错

                        // 过滤指定TAG的信息
                        commandList.add(TAG + ":V");
                        commandList.add(TAG + ":D");
                        commandList.add("*:S");
                        try {
                            Process process = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.e(TAG, "CollectorThread == >" + e.getMessage(), e);
                        }
                    } catch (Exception ex) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception ex){
                    Log.d(TAG , ex.getMessage());
                }
                Log.d(TAG ,"收集日志循环已完全启动!!!");
            }
        }.start();
    }

    public static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }
}
