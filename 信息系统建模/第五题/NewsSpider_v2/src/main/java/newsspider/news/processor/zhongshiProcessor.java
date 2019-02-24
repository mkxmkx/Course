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
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import java.util.Date;
import java.util.List;

/**
 * 中时电子报
 */

@Component
public class zhongshiProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://www\\.chinatimes\\.com/search/result.htm\\?q=[\\S]+";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(6000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        logger.debug("url:" + page.getUrl().toString());

        if(page.getUrl().regex(first_url).match())
        {
            logger.debug("firstUrl: " + page.getUrl().toString());
            List<String> urlList = page.getHtml().xpath("//*[@id='___gcse_0']/div/div/div/div[5]/div[2]/div[2]/div")
                    .links().all();
            logger.debug("URL list size : " + urlList.size());
            page.addTargetRequests(urlList);
            if(flag)
            {
                flag = false;
                int index = 1;
                List<String> indexlist = page.getHtml().xpath("//*[@id='___gcse_0']/div/div/div/div[5]/div[2]/div[2]/div/div[2]/div[13]/div/text()").all();
                page.addTargetRequests(indexlist);
                for (;index <= indexlist.size();index++)
                {
                    index++;
                    String url = "https://www.chinatimes.com/search/result.htm?q=中美贸易战#gsc.tab=0&gsc.q=中美贸易战&gsc.page=" + index;
                    page.addTargetRequest(url);
                }
            }
        }
        else
        {

            String Title = page.getHtml()
                    .xpath("//*[@id='h1']/text()").toString();
            logger.debug("Title : " + Title);
            String Time = page.getHtml()
                    .xpath("//*div[@class='reporter']/time/text()")
                    .toString();
            Date time = new Date();
            if (Time != null)
                time = getdate.convertToDate(Time);
            else
                logger.debug("time : " + Time);
            String Content = page.getHtml()
                    .xpath("//*article[@class='arttext marbotm clear-fix']/allText() ").toString();
            if (Title!=null)
            {
                logger.debug("add a record");

                page.putField("Title",Title);
                page.putField("Time",time);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
                page.putField("Source","中时电子报");
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://www.chinatimes.com/search/result.htm?q=中美贸易战#gsc.tab=0&gsc.q=中美贸易战&gsc.page=1";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(3000);
        Spider.create(new zhongshiProcessor())
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
