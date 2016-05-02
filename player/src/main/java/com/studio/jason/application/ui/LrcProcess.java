package com.studio.jason.application.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class LrcProcess {
    private List<LrcContent> lrcList;    //List集合存放歌词内容对象
    private LrcContent mLrcContent;        //声明一个歌词内容对象
    private String rootPath;

    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess(String rootPath) {
        mLrcContent = new LrcContent();
        lrcList = new ArrayList<LrcContent>();
        this.rootPath = rootPath;
    }

    /**
     * 读取歌词
     *
     * @param path
     * @return
     */
    public boolean readLRC(String path) {
        boolean flag = false;
        File f = new File(rootPath,path);
        try {
            if(!f.exists()){
                return false;
            }
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while ((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");
                s = s.replaceAll(",","\n");

                //分离“@”字符
                String splitLrcData[] = s.split("@");
                if (splitLrcData.length > 1) {
                    mLrcContent.setLrcStr(splitLrcData[1]);
                    //处理歌词取得歌曲的时间
                    int lrcTime = time2Str(splitLrcData[0]);
                    mLrcContent.setLrcTime(lrcTime);
                    //添加进列表数组
                    lrcList.add(mLrcContent);
                    //新创建歌词内容对象
                    mLrcContent = new LrcContent();
                }
            }
            fis.close();
            isr.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@");    //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int minute = Integer.parseInt(timeData[0]);    /////有异常
        int second = Integer.parseInt(timeData[1]);
        int millisecond = Integer.parseInt(timeData[2]);

        //计算上一行与下一行的时间转换为毫秒数
        int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }

    public List<LrcContent> getLrcList() {
        return lrcList;
    }
}

