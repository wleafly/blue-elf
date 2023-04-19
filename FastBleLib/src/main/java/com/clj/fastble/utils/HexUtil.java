package com.clj.fastble.utils;

import java.util.HashMap;
import java.util.Map;

public class HexUtil {
    //位数较低
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    //十六进制编码
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        if (data == null)
            return null;
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }


    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }
    //十六进制编码Str
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    //十六进制编码Str
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    //十六进制格式字符串
    public static String formatHexString(byte[] data) {
        return formatHexString(data, false);
    }

    //十六进制格式字符串
    public static String formatHexString(byte[] data, boolean addSpace) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.equals("31")){
                hex="1";
            }else if (hex.equals("32")){
                hex="2";
            }else if (hex.equals("33")){
                hex="3";
            }else if (hex.equals("34")){
                hex="4";
            }else if (hex.equals("35")){
                hex="5";
            }else if (hex.equals("36")){
                hex="6";
            }else if (hex.equals("37")){
                hex="7";
            }else if (hex.equals("38")){
                hex="8";
            }else if (hex.equals("39")){
                hex="9";
            }else if (hex.equals("2c")){
                hex=",";
            }else if (hex.equals("2e")){
                hex=".";
            }else if (hex.equals("30")){
                hex="0";
            }else if (hex.equals("2d")){
                hex="-";
            }else if (hex.equals("41")){
                hex="A";
            }else if (hex.equals("42")){
                hex="B";
            }else if (hex.equals("43")){
                hex="C";
            }else if (hex.equals("44")){
                hex="D";
            }else if (hex.equals("45")){
                hex="E";
            }else if (hex.equals("46")){
                hex="F";
            }else if (hex.equals("4b")){
                hex="K";
            }else if (hex.equals("4d")){
                hex="M";
            }else if (hex.equals("4e")){
                hex="N";
            }else if (hex.equals("4f")){
                hex="O";
            }else if (hex.equals("50")){
                hex="P";
            }else if (hex.equals("53")){
                hex="S";
            }else if (hex.equals("54")){
                hex="T";
            }else if (hex.equals("57")){
                hex="W";
            }else if (hex.equals("61")){
                hex="a";
            }else if (hex.equals("62")){
                hex="b";
            }else if (hex.equals("63")){
                hex="c";
            }else if (hex.equals("64")){
                hex="d";
            }else if (hex.equals("65")){
                hex="e";
            }else if (hex.equals("66")){
                hex="f";
            }else if (hex.equals("6f")){
                hex="o";
            }else if (hex.equals("72")){
                hex="r";
            }else if (hex.equals("74")){
                hex="t";
            }else if (hex.equals("7b")){
                hex="{";
            }else if (hex.equals("7d")){
                hex="}";
            }else if (hex.equals("2f")){
                hex="/";
            }else if (hex.equals("3c")){
                hex="<";
            }else if (hex.equals("3e")){
                hex=">";
            }else if (hex.equals("5b")){
                hex="[";
            }else if (hex.equals("5d")){
                hex="]";
            }
//            if (hex.length() == 1) {
//                hex = '0' + hex;
//            }
            sb.append(hex);
//            if (addSpace)
//                sb.append(" ");
        }
        return sb.toString().trim();
    }

    //解码十六进制
    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");//奇数字符
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.两个字符组成十六进制值
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    //角度
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch //输入非法的十六进制数
                    + " at index " + index);//索引
        }
        return digit;
    }

    // 十六进制字符串转成字节
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.trim();
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    //char,字节
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    //提取数据
    public static String extractData(byte[] data, int position) {
        return HexUtil.formatHexString(new byte[]{data[position]});
    }

    // 十六进制转换为十进制
    public static int covert(String content){
        int number=0;
        content = content.toUpperCase();
        String [] HighLetter = {"A","B","C","D","E","F"};
        Map<String,Integer> map = new HashMap<>();
        for(int i = 0;i <= 9;i++){
            map.put(i+"",i);
        }
        for(int j= 10;j<HighLetter.length+10;j++){
            map.put(HighLetter[j-10],j);
        }
        String[]str = new String[content.length()];
        for(int i = 0; i < str.length; i++){
            str[i] = content.substring(i,i+1);
        }
        for(int i = 0; i < str.length; i++){
            number += map.get(str[i])*Math.pow(16,str.length-1-i);
        }
        return number;
    }

    public static String byteToString(byte[] bytes) {

        String hex = "";
        String s = "";
        for (int i = 0; i < bytes.length; i++) {
            hex = hex + bytes[i];
            if (hex.equals("44")) {
                hex = ",";
            } else if (hex.equals("45")) {
                hex = "-";
            } else if (hex.equals("46")) {
                hex = ".";
            } else if (hex.equals("47")) {
                hex = "/";
            }  else if (hex.equals("48")) {
                hex = "0";
            } else if (hex.equals("49")) {
                hex = "1";
            } else if (hex.equals("50")) {
                hex = "2";
            } else if (hex.equals("51")) {
                hex = "3";
            } else if (hex.equals("52")) {
                hex = "4";
            } else if (hex.equals("53")) {
                hex = "5";
            } else if (hex.equals("54")) {
                hex = "6";
            } else if (hex.equals("55")) {
                hex = "7";
            } else if (hex.equals("56")) {
                hex = "8";
            } else if (hex.equals("57")) {
                hex = "9";
            } else if (hex.equals("60")) {
                hex = "<";
            } else if (hex.equals("62")) {
                hex = ">";
            } else if (hex.equals("65")) {
                hex = "A";
            } else if (hex.equals("66")) {
                hex = "B";
            } else if (hex.equals("67")) {
                hex = "C";
            } else if (hex.equals("68")) {
                hex = "D";
            } else if (hex.equals("69")) {
                hex = "E";
            } else if (hex.equals("75")) {
                hex = "K";
            } else if (hex.equals("77")) {
                hex = "M";
            } else if (hex.equals("78")) {
                hex = "N";
            } else if (hex.equals("79")) {
                hex = "O";
            } else if (hex.equals("80")) {
                hex = "P";
            } else if (hex.equals("82")) {
                hex = "R";
            } else if (hex.equals("83")) {
                hex = "S";
            } else if (hex.equals("84")) {
                hex = "T";
            } else if (hex.equals("85")) {
                hex = "U";
            } else if (hex.equals("87")) {
                hex = "W";
            } else if (hex.equals("89")) {
                hex = "y";
            } else if (hex.equals("91")) {
                hex = "[";
            } else if (hex.equals("93")) {
                hex = "]";
            } else if (hex.equals("97")) {
                hex = "a";
            } else if (hex.equals("98")) {
                hex = "b";
            } else if (hex.equals("99")) {
                hex = "c";
            } else if (hex.equals("100")) {
                hex = "d";
            } else if (hex.equals("101")) {
                hex = "e";
            } else if (hex.equals("107")) {
                hex = "k";
            } else if (hex.equals("109")) {
                hex = "m";
            } else if (hex.equals("110")) {
                hex = "n";
            } else if (hex.equals("111")) {
                hex = "o";
            } else if (hex.equals("112")) {
                hex = "p";
            } else if (hex.equals("114")) {
                hex = "r";
            } else if (hex.equals("115")) {
                hex = "s";
            } else if (hex.equals("116")) {
                hex = "t";
            } else if (hex.equals("117")) {
                hex = "u";
            } else if (hex.equals("119")) {
                hex = "w";
            } else if (hex.equals("123")) {
                hex = "{";
            } else if (hex.equals("125")) {
                hex = "}";
            }
            s = s + hex;
            hex = "";
        }
        return s;
    }




}
