package newsspider.news.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 计算相关度
 */

public class Relevant {
    private static String[] keyWords = {"中美","贸易战","中国","美国","贸易"};
    private static int[] titleWeight = {2,2,1,1,1};
    private static int[] contentWeight = {2,2,1,1,1};

    public float getRelatGrade(String text, String type)
    {
        float score = 0;
        int i = 0;
        int count = 0;
        for(;i<keyWords.length;i++)
        {
            count = 0;//关键词出现的次数
            Pattern p = Pattern.compile(keyWords[i]);
            Matcher m = p.matcher(text);
            while (m.find())
                count++;
            if(type.equals("title"))
                score += titleWeight[i]*count;//相关度
            else
                score += contentWeight[i]*count;
        }
        return score;
    }

}
