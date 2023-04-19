package com.clj.fastble.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyLog {

    private static Boolean MYLOG_SWITCH = true; // 日志文件总开关
    private static Boolean MYLOG_WRITE_TO_FILE = true;// 日志写入文件开关
    private static char MYLOG_TYPE = 'v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息
    private static String MYLOG_PATH_SDCARD_DIR = "/sdcard/蓝牙/";// 日志文件在sdcard中的路径
    private static String MYLOG_PATH_SDCARD_DIR2 = "/sdcard/Android/data/com.clj.blesample/files/蓝牙/";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "ReadLog.txt";// 本类输出的日志文件名称
    private static String MYLOGFILE = "NotifyLog.txt";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    public Context context;

    /**
     * 打开日志文件并写入日志

     * @param text
     */
    public static  void writeLogToReadFile( String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
//        String needWriteMessage = myLogSdf.format(nowtime) + text;
        File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
//        File dirsFile2 = new File(MYLOG_PATH_SDCARD_DIR2);
        if (!dirsFile.exists()){
            dirsFile.mkdirs();
        }

        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile.toString(), needWriteFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }


        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(text);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 打开日志文件并写入日志

     * @param text
     */
    public static void writeLogToNotifyFile( String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
//        String needWriteMessage = myLogSdf.format(nowtime) + text;
        File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
        if (!dirsFile.exists()){
            dirsFile.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile.toString(), needWriteFiel + MYLOGFILE);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(text);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 实时数据下载函数
     * @param data
     * @param fileName
     */
    public static void realDataCreateDownload(String data, String fileName) {

        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime); // yyyy-mm-dd
        String[] nf = needWriteFiel.split("-");
        // 创建蓝牙文件夹
        String dir = MYLOG_PATH_SDCARD_DIR + "实时数据/" + nf[0] + "/" + nf[1] + "/" + nf[2];
        File dirsFile = new File(dir);
        if (!dirsFile.exists()){
            dirsFile.mkdirs();
        }
        String childName = fileName + ".txt";
        File file = new File(dirsFile.toString(), childName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {

            System.out.println("文件不存在");
            try {
                //在指定的文件夹中创建文件
                Boolean b = file.createNewFile();
                System.out.println("创建文件夹:" + b);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("异常问题");
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(data);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 实时数据下载主函数，这里进行数据处理
     * @param num 序号
     * @param type 传感器类型
     * @param data 数据，不带时间
     */
    public static void downloadRealDataMain(int num, int type, String data, int[] mutilSensorSet) {

        String name = getSensorFileName(type);
        // 根据设备号分析数据


        // 初次发送数据
        if(num == 0) {
            // 空行隔开
            realDataCreateDownload("", name);
            // 时间
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = "\ndownload time：" + formatter.format(date);
            realDataCreateDownload(time, name);

            // 列表头          序号，时间，数值，温度
            String head = "序号(num)      时间(time)      " + getSensorName(type, mutilSensorSet);
            // 非ORP
            if(type != 4 && type != 999) {
                head = head + "    温度(temp)";
            }
            // COD
            if(type == 9) {
                head = head + "    浊度(ZS)(NTU)" + "    BOD(mg/L)";
            }
            realDataCreateDownload(head, name);

        } else {
            // 拆开数据
            String[] ds = data.split(",");
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String result = num + blankNumber(num) + formatter.format(date) + "      ";
            for(String d : ds) {
                result = result + d + blankNumberString(d);
            }
            realDataCreateDownload(result, name);
        }

    }


    /**
     * 下载进行分类处理的历史数据
     */
    public static void downloadToMemoryByCategory(String data, String fileName) {
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
//        String needWriteMessage = myLogSdf.format(nowtime) + text;
        String[] nf = needWriteFiel.split("-");
        // 创建蓝牙文件夹
        String dirsName = MYLOG_PATH_SDCARD_DIR + "历史数据/" + nf[0] + "/" + nf[1] + "/" + nf[2];
        // String dirsName = MYLOG_PATH_SDCARD_DIR + needWriteFiel; // 创建文件夹：/sdcards/蓝牙/2021-11-9
        File dirsFile = new File(dirsName);
        if (!dirsFile.exists()){
            dirsFile.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile.toString(), fileName + ".txt");// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(data);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 下载二
//        String dirsName2 = MYLOG_PATH_SDCARD_DIR2 + needWriteFiel; // 创建文件夹：/sdcards/蓝牙/2021-11-9
//        File dirsFile2 = new File(dirsName2);
//        if (!dirsFile2.exists()){
//            dirsFile2.mkdirs();
//        }
//        //Log.i("创建文件","创建文件");
//        File file2 = new File(dirsFile2.toString(), fileName + ".txt");// MYLOG_PATH_SDCARD_DIR
//        if (!file2.exists()) {
//            try {
//                //在指定的文件夹中创建文件
//                file2.createNewFile();
//            } catch (Exception e) {
//            }
//        }
//
//        try {
//            FileWriter filerWriter2 = new FileWriter(file2, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
//            BufferedWriter bufWriter2 = new BufferedWriter(filerWriter2);
//            bufWriter2.write(data);
//            bufWriter2.newLine();
//            bufWriter2.close();
//            filerWriter2.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    
    public static String blankNumber(int n) {
        if(n < 10) {
            return "        ";
        }else if(n < 100) {
            return "       ";
        }else if(n < 1000) {
            return "      ";
        }else if(n < 10000) {
            return "     ";
        }else if(n < 100000) {
            return "    ";
        }else {
            return "   ";
        }
    }

    public static String blankNumberString(String s) {
        if(s.length() < 2) {
            return "        ";
        }else if(s.length() < 3) {
            return "       ";
        }else if(s.length() < 4) {
            return "      ";
        }else if(s.length() < 5) {
            return "     ";
        }else if(s.length() < 6) {
            return "    ";
        }else {
            return "   ";
        }
    }

    /**
     * 历史数据：对下载数据进行分类，并调用下载函数
     */
    public static void categoryToDownload(List<String> s, int[] mutilSensor) {
        int ec1=0,ec2=0,ph=0,orp=0,rdo=0,nhn=0,zs=0,sal=0,cod=0,rc=0,ch=0,cy=0,tss=0,tsm=0,ow=0,mutil=0;

        for(int i = 0; i < s.size(); i ++) {
            String[] data = s.get(i).split(",");
            String type0 = data[0];
            String type = data[1];

            // 判断数据类型
            if(type0.equals("0")) {
                // 单参数
                if(type.equals("1")) {
                    // 电导率
                    
                    if(ec1 == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "电导率μS");

                        String str = "序号   电导率μS   温度";
                        downloadToMemoryByCategory(str,"电导率μS");
                        ec1 ++;
                        str = ec1 + blankNumber(ec1) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "电导率μS");
                    }else{
                        ec1 ++;
                        String str = ec1 + blankNumber(ec1) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "电导率μS");
                    }
                }else if(type.equals("2")) {
                    // 电导率mS
                    if(ec2 == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "电导率mS");
                        String str = "序号   电导率mS   温度";
                        downloadToMemoryByCategory(str,"电导率mS");
                        ec2 ++;
                        str = ec2 + blankNumber(ec2) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "电导率mS");
                    }else{
                        ec2 ++;
                        String str = ec2 + blankNumber(ec2) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "电导率mS");
                    }
                }else if(type.equals("3")) {
                    // PH
                    if(ph == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "PH");
                        String str = "序号   PH值   温度";
                        downloadToMemoryByCategory(str,"PH");
                        ph ++;
                        str = ph + blankNumber(ph) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "PH");
                    }else{
                        ph ++;
                        String str = ph + blankNumber(ph) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "PH");
                    }
                }else if(type.equals("4")) {
                    // ORP
                    if(orp == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "ORP");
                        String str = "序号   ORP值";
                        downloadToMemoryByCategory(str,"ORP");
                        orp ++;
                        str = orp + blankNumber(orp) + data[2];
                        downloadToMemoryByCategory(str, "ORP");
                    }else{
                        orp ++;
                        String str = orp + blankNumber(orp) + data[2];
                        downloadToMemoryByCategory(str, "ORP");
                    }
                }else if(type.equals("5")) {
                    // 溶解氧
                    if(rdo == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "溶解氧");
                        String str = "序号   溶解氧   温度";
                        downloadToMemoryByCategory(str,"溶解氧");
                        rdo ++;
                        str = rdo + blankNumber(rdo) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "溶解氧");
                    }else{
                        rdo ++;
                        String str = rdo + blankNumber(rdo) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "溶解氧");
                    }
                }else if(type.equals("6")) {
                    // 铵氮/离子类
                    if(nhn == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "铵氮/离子类");
                        String str = "序号   铵氮/离子类   温度";
                        downloadToMemoryByCategory(str,"铵氮/离子类");
                        nhn ++;
                        str = nhn + blankNumber(nhn) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "铵氮/离子类");
                    }else{
                        nhn ++;
                        String str = nhn + blankNumber(nhn) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "铵氮/离子类");
                    }
                }else if(type.equals("7")) {
                    // 浊度
                    if(zs == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "浊度");
                        String str = "序号   浊度   温度";
                        downloadToMemoryByCategory(str,"浊度");
                        zs ++;
                        str = zs + blankNumber(zs) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "浊度");
                    }else{
                        zs ++;
                        String str = zs + blankNumber(zs) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "浊度");
                    }
                }else if(type.equals("8")) {
                    // 盐度
                    if(sal == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "盐度");
                        String str = "序号   盐度   温度";
                        downloadToMemoryByCategory(str,"盐度");
                        sal ++;
                        str = sal + blankNumber(sal) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "盐度");
                    }else{
                        sal ++;
                        String str = sal + blankNumber(sal) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "盐度");
                    }
                }else if(type.equals("9")) {
                    // COD
                    if(cod == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "盐度");
                        String str = "序号   COD   温度   浊度   BOD";
                        downloadToMemoryByCategory(str,"盐度");
                        cod ++;
                        str = cod + blankNumber(cod) + data[2] + blankNumberString(data[2]) + data[3] + blankNumberString(data[3]) + data[4] + blankNumberString(data[4]) + data[5];
                        downloadToMemoryByCategory(str, "盐度");
                    }else{
                        cod ++;
                        String str = cod + blankNumber(cod) + data[2] + blankNumberString(data[2]) + data[3] + blankNumberString(data[3]) + data[4] + blankNumberString(data[4]) + data[5];
                        downloadToMemoryByCategory(str, "盐度");
                    }
                }else if(type.equals("10")) {
                    // 余氯
                    if(rc == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "余氯");
                        String str = "序号   余氯   温度";
                        downloadToMemoryByCategory(str,"余氯");
                        rc ++;
                        str = rc + blankNumber(rc) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "余氯");
                    }else{
                        rc ++;
                        String str = rc + blankNumber(rc) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "余氯");
                    }
                }else if(type.equals("11")) {
                    // 叶绿素
                    if(ch == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "叶绿素");
                        String str = "序号   叶绿素   温度";
                        downloadToMemoryByCategory(str,"叶绿素");
                        ch ++;
                        str = ch + blankNumber(ch) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "叶绿素");
                    }else{
                        ch ++;
                        String str = ch + blankNumber(ch) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "叶绿素");
                    }
                }else if(type.equals("12")) {
                    // 蓝绿藻
                    if(cy == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "蓝绿藻");
                        String str = "序号   蓝绿藻   温度";
                        downloadToMemoryByCategory(str,"蓝绿藻");
                        cy ++;
                        str = cy + blankNumber(cy) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "蓝绿藻");
                    }else{
                        cy ++;
                        String str = cy + blankNumber(cy) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "蓝绿藻");
                    }
                }else if(type.equals("13")) {
                    // 透明度
                    if(tss == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "透明度");
                        String str = "序号   透明度   温度";
                        downloadToMemoryByCategory(str,"透明度");
                        tss ++;
                        str = tss + blankNumber(tss) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "透明度");
                    }else{
                        tss ++;
                        String str = tss + blankNumber(tss) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "透明度");
                    }
                }else if(type.equals("14")) {
                    // 悬浮物
                    if(tsm == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "悬浮物");
                        String str = "序号   悬浮物   温度";
                        downloadToMemoryByCategory(str,"悬浮物");
                        tsm ++;
                        str = tsm + blankNumber(tsm) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "悬浮物");
                    }else{
                        tsm ++;
                        String str = tsm + blankNumber(tsm) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "悬浮物");
                    }
                }else if(type.equals("15")) {
                    // 水中油
                    if(ow == 0) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = "\n下载时间：" + formatter.format(date);
                        downloadToMemoryByCategory(time, "水中油");
                        String str = "序号   水中油   温度";
                        downloadToMemoryByCategory(str,"水中油");
                        ow ++;
                        str = ow + blankNumber(ow) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "水中油");
                    }else{
                        ow ++;
                        String str = ow + blankNumber(ow) + data[2] + blankNumberString(data[2]) + data[3];
                        downloadToMemoryByCategory(str, "水中油");
                    }
                }
            }else if(type0.equals("1")) {
                // 多参数
                if(mutil == 0) {Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = "\n下载时间：" + formatter.format(date);
                    downloadToMemoryByCategory(time, "多参数");
//                    String str = "序号    " + mutilSensorNameSet(mutilSensor);
                    String str = "序号    温度    参数一   参数二    参数三    参数四    参数五    参数六    参数七    参数八";
                    downloadToMemoryByCategory(str,"多参数");
                    mutil ++;
                    str = mutil + blankNumber(mutil)
                            + data[2] + blankNumberString(data[2])
                            + data[3] + blankNumberString(data[3])
                            + data[4] + blankNumberString(data[4])
                            + data[5] + blankNumberString(data[5])
                            + data[6] + blankNumberString(data[6])
                            + data[7] + blankNumberString(data[7])
                            + data[8] + blankNumberString(data[8])
                            + data[9] + blankNumberString(data[9])
                            + data[10];
                    downloadToMemoryByCategory(str, "多参数");
                }else{
                    mutil ++;
                    String str = mutil + blankNumber(mutil)
                            + data[2] + blankNumberString(data[2])
                            + data[3] + blankNumberString(data[3])
                            + data[4] + blankNumberString(data[4])
                            + data[5] + blankNumberString(data[5])
                            + data[6] + blankNumberString(data[6])
                            + data[7] + blankNumberString(data[7])
                            + data[8] + blankNumberString(data[8])
                            + data[9] + blankNumberString(data[9])
                            + data[10];
                    downloadToMemoryByCategory(str, "多参数");
                }
            }

        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {// 删除日志文件
        String needDelFiel = logfile.format(getDateBefore());
        File dirPath = Environment.getExternalStorageDirectory();
        File file = new File(dirPath, needDelFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }

    /**
     * 返回传感器类型名称
     * @param type 传感器类型
     * @return
     */
    public static String getSensorFileName(int type) {
        switch (type) {
            case 1: return "电导率us";
            case 2: return "电导率ms";
            case 3: return "PH";
            case 4: return "ORP";
            case 5: return "溶解氧";
            case 6: return "铵氮/离子类";
            case 7: return "浊度";
            case 8: return "盐度";
            case 9: return "COD";
            case 10: return "余氯";
            case 11: return "叶绿素";
            case 12: return "蓝绿藻";
            case 13: return "透明度";
            case 14: return "悬浮物";
            case 15: return "水中油";
            case 999: return "多参数";
        }
        return "";
    }

    /**
     * 返回传感器类型名称
     * @param type 传感器类型
     * @return
     */
    public static String getSensorName(int type, int[] mutilSensorSet) {
        switch (type) {
            case 0: return "未连接";
            case 1: return "电导率(DDM)(us/cm)";
            case 2: return "电导率(DDM)(ms/cm)";
            case 3: return "PH";
            case 4: return "ORP(mV)";
            case 5: return "溶解氧(RDO)(mg/L)";
            case 6: return "铵氮/离子类(NHN)(mg/L)";
            case 7: return "浊度(ZS)(NTU)";
            case 8: return "盐度(DDM-S)(PSU)";
            case 9: return "COD(mg/L)";
            case 10: return "余氯(CL)(mg/L)";
            case 11: return "叶绿素(CHLO)(mg/L)";
            case 12: return "蓝绿藻(BGA)(Kcells/ml)";
            case 13: return "透明度(TPS)(mg/L)";
            case 14: return "悬浮物(TSS)(mg/L)";
            case 15: return "水中油(OIL)(mg/L)";
            case 999: return mutilSensorNameSet(mutilSensorSet);
        }
        return "";
    }

    public static String mutilSensorNameSet(int[] mutilSensor) {
        String[] name = {"未连接" , getSensorName(1, mutilSensor), getSensorName(2, mutilSensor), getSensorName(3, mutilSensor), getSensorName(4, mutilSensor),
                getSensorName(5, mutilSensor), getSensorName(6, mutilSensor), getSensorName(7, mutilSensor), getSensorName(8, mutilSensor),
                getSensorName(9, mutilSensor), getSensorName(10, mutilSensor), getSensorName(11, mutilSensor), getSensorName(12, mutilSensor),
                getSensorName(13, mutilSensor), getSensorName(14, mutilSensor), getSensorName(15, mutilSensor), getSensorName(16, mutilSensor)};
        String temp = "    ";
        String zsCod = "浊度(COD)";
        if(mutilSensor[0] == 0) {
            zsCod = "未连接";
        }
        String result = "温度(Temp)(℃)" + temp + name[mutilSensor[0]] + temp + zsCod + temp +
                name[mutilSensor[2]] + temp + name[mutilSensor[3]] + temp + name[mutilSensor[4]] + temp +
                name[mutilSensor[5]] + temp  + name[mutilSensor[6]] + temp + name[mutilSensor[7]];
        return result;
    }
}
