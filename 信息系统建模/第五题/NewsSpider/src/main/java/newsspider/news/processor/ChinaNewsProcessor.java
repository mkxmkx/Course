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
public class ChinaNewsProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "http://sou\\.chinanews\\.com/search\\.do\\?q=[\\S]+";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(6000).setCharset("UTF-8");

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
            List<String> urlList = page.getHtml().xpath("//*[@id='news_list']")
                    .links().all();
            logger.debug("URL list size : " + urlList.size());
            page.addTargetRequests(urlList);
            List<String> nextURL_a_List = page.getHtml().xpath("//*[@id='pagediv']/a/text()").all();
            logger.error("next url text list : " + nextURL_a_List);
            if(nextURL_a_List.size()>0)
            {
                if (nextURL_a_List.get(nextURL_a_List.size()-2).equals("下一页"))
                {
                    start += 10;
                    String nextURL = "http://sou.chinanews.com/search.do?q=" + "中美贸易战" + "&start=" + start;
                    page.addTargetRequest(nextURL);
                    logger.debug("next url: " + nextURL);
                }
            }

        }
        else
        {

            String Title = page.getHtml()
                    .xpath("//*[@id='cont_1_1_2']/h1/text()").toString();
            String Time = page.getHtml()
                    .xpath("//*[@id='cont_1_1_2']/div[@class='left-time']/div[@class='left-t']/text()").toString();
            Date time = new Date();
            if (Time != null)
                time = getdate.convertToDate(Time);
            else
                logger.debug("time : " + Time);
            String Content = page.getHtml()
                    .xpath("//*[@id='cont_1_1_2']/div[@class='left_zw']/allText()").toString();
            if (Title!=null)
            {
                logger.debug("add a record");

                page.putField("Title",Title);
                page.putField("Time",time);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
                page.putField("Source","中国新闻网");
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "http://sou.chinanews.com/search.do?q=" + "中美贸易战";
        //http://sou.chinanews.com/search.do?q=%E4%B8%AD%E7%BE%8E%E8%B4%B8%E6%98%93%E6%88%98
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(3000);
        Spider.create(new ChinaNewsProcessor())
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
