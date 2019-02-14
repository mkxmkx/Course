package newsspider.news.processor;
import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import newsspider.news.utils.getDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Date;
import java.util.List;

@Component
public class toutiaoProcessor implements PageProcessor{
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://www\\.toutiao\\.com/search/\\?keyword=[\\S]+";

    private Site site = Site.me().setRetryTimes(6).setSleepTime(9000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int start = 0;

    private getDate getdate = new getDate();

    @Override
    public Site getSite()
    {
        return site;
    }

    @Override
    public void process(Page page)
    {
        logger.debug("url:" + page.getUrl().toString());

        if(page.getUrl().regex(first_url).match())
        {
            logger.debug("firstUrl: " + page.getUrl().toString());
            List<String> urlList = page.getHtml().xpath("/html/body/div/div[4]/div[2]/div[3]/div/div")
                    .links().all();
            logger.debug("URL list size : " + urlList.size());
            /*
            for(String s : urlList)
            {
                page.addTargetRequest("https://www.toutiao.com" + s);
            }
            */

            page.addTargetRequests(urlList);

            /*
            List<String> nextURL_a_List = page.getHtml().xpath("//*[@id='pagebar_container']/a/@id").all();
            if (nextURL_a_List.get(nextURL_a_List.size()-1).equals("sogou_next"))
            {
                List<String> nextURLList = page.getHtml().xpath("//*[@id='pagebar_container']/a/@href").all();
                String nextURL = "https://news.sogou.com/news" + nextURLList.get(nextURLList.size()-1);
                page.addTargetRequest(nextURL);
                logger.debug("next url: " + nextURL);
            }
            */
        }
        else
        {
            logger.debug("in else");
            String Title = page.getHtml()
                    .xpath("//*h1[@class='article-title']/text()").toString();
            logger.debug("Title : " + Title);
            List<String> tempTime = page.getHtml().xpath("//*div[@class='article-sub']/span/text()").all();
            logger.debug("temp time :" + tempTime);
            String Time = null;
            if (tempTime.size()>0)
                Time = tempTime.get(tempTime.size()-1);

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
            if (Title!=null)
            {
                logger.debug("add a record");

                page.putField("Title",Title);
                page.putField("Time",time);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
                page.putField("Source","今日头条");
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://www.toutiao.com/search/?keyword=%E4%B8%AD%E7%BE%8E%E8%B4%B8%E6%98%93%E6%88%98&aid=24&offset=200";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(9000);
        Spider.create(new toutiaoProcessor())
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
