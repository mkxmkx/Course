package newsspider.news.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import newsspider.news.utils.getDate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import java.util.*;

@Component
public class toutiaoAllMessageProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://www\\.toutiao\\.com/a[\\S]+";

    private Site site = Site.me().setRetryTimes(6).setSleepTime(9000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int start = 0;

    private getDate getdate = new getDate();

    private boolean flag = true;

    @Override
    public Site getSite()
    {
        return site;
    }

    @Override
    public void process(Page page)
    {
        logger.debug("in process");
        logger.debug("url:" + page.getUrl().toString());

        /**
         * 因为我随便添加了一个初始url，所以第一次只让把所有url都添加到队列里，不对第一个url进行判断，写成了if-else语句
         */
        if(flag)
        {
            flag = false;
            List<String> urls = getURL();
            page.addTargetRequests(urls);
        }
        else {
            if (page.getUrl().regex(first_url).match()) {
                logger.debug("Url: " + page.getUrl().toString());

                String Title = page.getHtml()
                        .xpath("//*h1[@class='article-title']/text()").toString();
                logger.debug("Title : " + Title);
                List<String> tempTime = page.getHtml().xpath("//*div[@class='article-sub']/span/text()").all();
                logger.debug("temp time :" + tempTime);
                String Time = null;
                if (tempTime.size() > 0)
                    Time = tempTime.get(tempTime.size() - 1);

            /*
            String Time = page.getHtml()
                    .xpath("//*div[@class='article-sub']/span[2]/text()").toString();
                    */
                Date time = new Date();
                if (Time != null)
                    time = getdate.convertToDate(Time);
                else
                    logger.debug("time : " + Time);
                String Content = page.getHtml()
                        .xpath("//*div[@class='article-content']/allText() ").toString();
                if (Title != null) {
                    logger.debug("add a record");

                    page.putField("Title", Title);
                    page.putField("Time", time);
                    page.putField("Content", Content);
                    page.putField("URL", page.getUrl().toString());
                    page.putField("Source", "今日头条");
                }
            }
        }

    }

    public List<String> getURL()
    {
        List<String> urls = new ArrayList<String>();
        int index = 0;
        while (true)
        {
            String url = "https://www.toutiao.com/api/search/content/?aid=24&offset="
            + index +"&format=json&keyword=" + "中美贸易战"
            + "&autoload=true&count=20&cur_tab=1&from=search_tab&pd=synthesi";
            List<String> list = getDetailUrls(url);
            if(list==null)
            {
                break;
            }
            else {
                urls.addAll(list);
                logger.debug("in getURL()");
            }
            index += 20;
        }

        int urlNumber = urls.size();
        logger.debug("url number: " + urlNumber);
        return urls;
    }

    public List<String> getDetailUrls(String url)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response;
        List<String> urls = new ArrayList<String>();
        try{
            response = httpClient.execute(httpGet);
            String temp;
            HttpEntity entity = response.getEntity();
            temp = EntityUtils.toString(entity,"UTF-8");
            JSONObject jsStr = JSONObject.parseObject(temp);
            if(jsStr.getIntValue("return_count") < 1){
                return null;
            }else{
                JSONArray data = jsStr.getJSONArray("data");
                for(Object item : data){
                    JSONObject jsonObject = JSONObject.parseObject(item.toString());
                    //System.out.println(jsonObject.getString("group_id"));
                    if(jsonObject.getString("group_id") != null){
                        String init_url = "https://www.toutiao.com/a" + jsonObject.getString("group_id") + "/";
                        String writer = jsonObject.getString("source");
                        urls.add(init_url);
                        logger.debug("get a detail url : " + init_url);
                    }
                }
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return urls;
    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://www.toutiao.com/search/?keyword=%E4%B8%AD%E7%BE%8E%E8%B4%B8%E6%98%93%E6%88%98&aid=24&offset=200";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(9000);

        List<String> urls = getURL();

        Spider.create(new toutiaoAllMessageProcessor())
                .setScheduler(new QueueScheduler()
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(1000)))//添加布隆过滤器，进行URL去重
                .setDownloader(seleniumDownloader)
                .addUrl(searchURL)
                .addPipeline(new MySQLPipeline(newsRepository))
                .thread(5)
                .run();
    }

    public static void main(String[] args)
    {

    }
}
