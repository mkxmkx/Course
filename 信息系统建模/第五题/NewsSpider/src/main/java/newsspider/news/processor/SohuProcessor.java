package newsspider.news.processor;


import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜狐新闻爬虫
 */

@Component
public class SogouProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://news\\.sogou\\.com/news\\?query=site%3Asohu\\.com[\\S]+";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(6000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

            String Title = page.getHtml()
                    .xpath("//*[@id='article-container']/div[2]/div[1]/div[1]/div[1]/h1/text()").toString();
            String Time = page.getHtml()
                    .xpath("//*[@id='news-time']/text()").toString();
            String Content = page.getHtml()
                    .xpath("//*[@id='mp-editor']/allText()").toString();
            if (Title!=null && Content!=null )
            {
                logger.debug("add a record");
                logger.debug(Title);
                page.putField("Title",Title);
                page.putField("Time",Time);
                logger.debug("Content: " + Content);
                page.putField("Content",Content);
                page.putField("URL",page.getUrl().toString());
            }

        }


    }

    public void creatSpider()
    {
        String searchURL = "https://news.sogou.com/news?query=site%3Asohu.com" + "中美贸易战";
        Spider.create(new SogouProcessor())
                .addUrl(searchURL)
                .addPipeline(new MySQLPipeline(newsRepository))
                .thread(5)
                .run();
    }

    public static void main(String[] args)
    {

    }

}
