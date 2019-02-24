package newsspider.news.processor;


import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import newsspider.news.utils.Relevant;
import newsspider.news.utils.getDate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

/**
 * 搜狗搜索，搜狐新闻爬虫
 */

@Component
public class SohuProcessor implements PageProcessor {
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://news\\.sogou\\.com/news\\?query=site%3Asohu\\.com[\\S]+";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(6000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private getDate getdate = new getDate();

    private Relevant relevant = new Relevant();
    private String lastDate = new String("2018-06-10");

    @Override
    public Site getSite()
    {
        return site;
    }

    @Override
    public void process(Page page)
    {
        //logger.debug("url:" + page.getUrl().toString());

        if(page.getUrl().regex(first_url).match())
        {
            //logger.debug("firstUrl: " + page.getUrl().toString());
            List<String> urlList = page.getHtml().xpath("//*[@id='main']//*[@class='vrTitle']")
                    .links().all();
            //logger.debug("URL list size : " + urlList.size());
            page.addTargetRequests(urlList);
            List<String> nextURL_a_List = page.getHtml().xpath("//*[@id='pagebar_container']/a/@id").all();
            if (nextURL_a_List.get(nextURL_a_List.size()-1).equals("sogou_next"))
            {
                List<String> nextURLList = page.getHtml().xpath("//*[@id='pagebar_container']/a/@href").all();
                String nextURL = "https://news.sogou.com/news" + nextURLList.get(nextURLList.size()-1);
                page.addTargetRequest(nextURL);
                //logger.debug("next url: " + nextURL);
            }
        }
        else
        {

            String Title = page.getHtml()
                    .xpath("//*[@id='article-container']/div[2]/div[1]/div[1]/div[1]/h1/text()").toString();
            //logger.debug("Title : " + Title);
            float score = 0;
            if(Title!=null)
            {
                score = relevant.getRelatGrade(Title,"title");//进行标题主题相关度计算
                if(score>=2)//超过阈值，说明标题与主题相关，判定网页与主题相关
                {
                    String Time = page.getHtml()
                            .xpath("//*[@id='news-time']/text()").toString();
                    Date time = new Date();
                    if (Time != null)
                    {
                        time = getdate.convertToDate(Time);
                        if(lastDate.compareTo(Time)<0)//该新闻发布日期在上次爬虫运行时间之后
                        {
                            logger.debug("add a record");
                            String Content = page.getHtml()
                                    .xpath("//*[@id='mp-editor']/allText() ").toString();
                            page.putField("Title",Title);
                            page.putField("Time",time);
                            page.putField("Content",Content);
                            page.putField("URL",page.getUrl().toString());
                            page.putField("Source","搜狐新闻");
                            page.putField("Score",score);
                        }
                    }
                    else
                        logger.debug("time : " + Time);

                }
                else//标题相关度未超过阈值，则进一步判断文本内容是否相关
                {
                    String Content = page.getHtml()
                            .xpath("//*[@id='mp-editor']/allText() ").toString();
                    score = relevant.getRelatGrade(Content,"content");//文本相关度计算
                    if(score>=4)//文本内容与主题相关
                    {
                        String Time = page.getHtml()
                                .xpath("//*[@id='news-time']/text()").toString();
                        Date time = new Date();
                        if (Time != null)
                        {
                            time = getdate.convertToDate(Time);
                            if(lastDate.compareTo(Time)<0)//该新闻发布日期在上次爬虫运行时间之后
                            {
                                logger.debug("add a record");
                                page.putField("Title",Title);
                                page.putField("Time",time);
                                page.putField("Content",Content);
                                page.putField("URL",page.getUrl().toString());
                                page.putField("Source","搜狐新闻");
                                page.putField("Score",score);
                            }
                        }
                        else
                            logger.debug("time : " + Time);
                    }
                }

            }

        }


    }

    public void creatSpider() throws ParseException {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        String searchURL = "https://news.sogou.com/news?query=site%3Asohu.com" + "中美贸易战";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(3000);
        Spider.create(new SohuProcessor())
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
