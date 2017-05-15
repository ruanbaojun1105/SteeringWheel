package com.hwx.wheel.steeringwheel;

/**
 * Created by Administrator on 2016/8/23.
 */
public class Constants {
    public static final String server_url="120.76.118.136:8080";
    public static String Deviceid="1";
    public static String keyPwd="11111111";
    public static final String SD_KEY_PATH= "http://oa8iajf2m.bkt.clouddn.com/KEY_DATA.xls";

    public static final String MILK_DO_OK="MILK_DO_OK";
    public static final String MILK_DO_FAIL="MILK_DO_FAIL";

    public static final String SERIAL_PORT_COMMAND="SERIAL_PORT_COMMAND";//接收
    public static final String SERIAL_PORT_SEND="SERIAL_PORT_SEND";//发送的数据
    public static final String SERIAL_PORT_CONNECT_FAIL="SERIAL_PORT_CONNECT_FAIL";//蓝牙连接失败
    public static final String SERIAL_PORT_CONNECT_LOST="SERIAL_PORT_CONNECT_LOST";//蓝牙连接丢失
    public static final String SERIAL_PORT_CONNECT_STATE="SERIAL_PORT_CONNECT_STATE";//蓝牙连接状态
    public static final String SERIAL_PORT_CONNECT_NAME="SERIAL_PORT_CONNECT_NAME";//蓝牙连接设备的名字
    public static final String arg_isManagerEdti="arg_isManagerEdti";

    public static final int PAGE_IMAGE_COUNT=17;//固定一页总产品数量
    public static final int PAGE_COUNT=4;//固定总共支持的页数
    public static final int SCREEN_TIME_COUNT=300;//屏幕不动现实视频的时间

    public static int isLoadData=0;//1本地加载或2加载服务器 0不同步

    public static final String REFRESH_IAMGE="REFRESH_IAMGE";

    public static String VIDEO_PATH="";
}
