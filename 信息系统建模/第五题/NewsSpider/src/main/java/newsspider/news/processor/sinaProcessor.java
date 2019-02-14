package newsspider.news.processor;

import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import newsspider.news.utils.DateConverterConfig;
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
 * 搜狗搜索，新浪新闻
 */

@Component
public class sinaProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://news\\.sogou\\.com/news\\?query=site:sina\\.com.cn [\\S]+";

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
        logger.error("String.valueOf(flag): "+String.valueOf(flag));

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
                    .xpath("//*h1[@class='main-title']/text()").toString();
            //String Title = page.getHtml()
             //       .xpath("/html/body/div[4]/h1/text()").toString();
            logger.debug("Title : " + Title);
            String Time = page.getHtml()
                    .xpath("//*[@id='top_bar']/div/div[2]/span/text()").toString();
            Date time = new Date();
            if (Time != null)
                time = getdate.convertToDate(Time);
            else
                logger.debug("time : " + Time);
            String Content = page.getHtml()
                    .xpath("//*[@id='artibody']/allText() | //*[@id='article']/allText()").toString();
            logger.debug("content : " + Content);
            if (Title!=null)
            {
                logger.debug("add a record");

                page.putField("Title",Title);
                page.putField("Time",time);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
                page.putField("Source","新浪新闻");
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://news.sogou.com/news?query=site:sina.com.cn " + "中美贸易战";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(3000);
        Spider.create(new sinaProcessor())
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
