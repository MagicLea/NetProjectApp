package com.netproject;

import java.io.Serializable;

public class MyProtocol {
    public static final int ENTER_ROOM = 0; // 進入聊天室
    public static final int COMMUNICATION_TEXT = 1; // 傳文字
    public static final int COMMUNICATION_PIC = 2; // 傳圖片
    public static final int COMMUNICATION_WARN = 3; // 傳警告
    public static final int LOCATION = 4 ; //定位訊息

    public String processInput(String theInput) {
        String theOutput = null;

        return theOutput;
    }

    @SuppressWarnings("serial")
    public static class Unit implements Serializable {        
        // 傳訊單元
        public final int type; // 訊息類型
        public final Object[] contents; // 內文(進入聊天室:int, 傳文字:String, 傳圖片:(String,byte[]) )

        public Unit(int type, Object[] contents) { // Unit建構子
            this.type = type;
            this.contents = contents;
        }
    }
}
