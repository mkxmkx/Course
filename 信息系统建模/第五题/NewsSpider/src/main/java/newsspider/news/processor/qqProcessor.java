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

/**
 * 搜狗搜索，腾讯新闻
 */

@Component
public class qqProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://news\\.sogou\\.com/news\\?query=site:qq\\.com [\\S]+";

    private Site site = Site.me().setRetryTimes(5).setSleepTime(6000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private getDate getdate = new getDate();

    @Override
    public Site getSite()
    {
        return site;
    }

    @Override
    public void process(Page page)
    {
        logger.debug("url: " + page.getUrl().toString());
        boolean flag = page.getUrl().regex(first_url).match();
        logger.error(String.valueOf(flag));

        if(page.getUrl().regex(first_url).match())
        {
            logger.debug("firstUrl: " + page.getUrl().toString());
            List<String> urlList = page.getHtml().xpath("//*[@id='main']//*[@class='vrTitle']")
                    .links().all();
            logger.debug("URL list size : " + urlList.size());
            page.addTargetRequests(urlList);
            List<String> nextURL_a_List = page.getHtml().xpath("//*[@id='pagebar_container']/a/@id").all();
            if (nextURL_a_List.get(nextURL_a_List.size()-1).equals("sogou_next"))
            {
                List<String> nextURLList = page.getHtml().xpath("//*[@id='pagebar_container']/a/@href").all();
                String nextURL = "https://news.sogou.com/news" + nextURLList.get(nextURLList.size()-1);
                page.addTargetRequest(nextURL);
                logger.debug("next url: " + nextURL);
            }
        }
        else
        {
            logger.error("in else");
            String Title = page.getHtml()
                    .xpath("//*[@id='Main-Article-QQ']/div/div[1]/div[1]/div[1]/h1/text()").toString();
            String Time = page.getHtml()
                    .xpath("//*[@id='Main-Article-QQ']/div/div[1]/div[1]/div[1]/div/div[1]/span[3]/text()").toString();
            Date time = new Date();
            if (Time != null)
                time = getdate.convertToDate(Time);
            else
                logger.debug("time : " + Time);
            String Content = page.getHtml()
                    .xpath("//*[@id='Cnt-Main-Article-QQ']/allText()").toString();
            if (Title!=null )
            {
                logger.debug("add a record");

                page.putField("Title",Title);
                page.putField("Time",time);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
                page.putField("Source","腾讯新闻");
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://news.sogou.com/news?query=site:qq.com " + "中美贸易战";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(10000);
        Spider.create(new qqProcessor())
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
