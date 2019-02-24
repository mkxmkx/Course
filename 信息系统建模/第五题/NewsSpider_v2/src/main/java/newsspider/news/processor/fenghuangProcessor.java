package newsspider.news.processor;

import newsspider.news.pipeline.MySQLPipeline;
import newsspider.news.repository.NewsRepository;
import newsspider.news.utils.Relevant;
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
 * 搜狐搜索，凤凰网
 */
@Component
public class fenghuangProcessor implements PageProcessor{
    @Autowired
    NewsRepository newsRepository;


    public static final String first_url = "https://news\\.sogou\\.com/news\\?query=site%3Aifeng\\.com[\\S]+";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(6000).setCharset("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int start = 0;

    private getDate getdate = new getDate();

    private Relevant relevant = new Relevant();
    private String lastDate = new String("2018-10-10");

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
            logger.debug("in else");
            String Title = page.getHtml()
                    .xpath("//*[@id='artical_topic']/text()").toString();
            logger.debug("Title : " + Title);
            float score = 0;
            if(Title!=null) {
                score = relevant.getRelatGrade(Title, "title");//进行标题主题相关度计算
                if (score >= 2)//超过阈值，说明标题与主题相关，判定网页与主题相关
                {
                    String Time = page.getHtml()
                            .xpath("//*[@id='artical_sth']/p/span[1]/text()").toString();
                    Date time = new Date();
                    if (Time != null) {
                        time = getdate.convertToDate(Time);
                        String year = Time.trim().substring(0,4);
                        String month = Time.trim().substring(5,7);
                        String day = Time.trim().substring(8,10);

                        String timetem = year + "-" + month + "-" + day;
                        if (lastDate.compareTo(timetem) < 0)//该新闻发布日期在上次爬虫运行时间之后
                        {
                            logger.debug("add a record");
                            String Content = page.getHtml()
                                    .xpath("//*[@id='main_content']/allText() ").toString();
                            page.putField("Title", Title);
                            page.putField("Time", time);
                            page.putField("Content", Content);
                            page.putField("URL", page.getUrl().toString());
                            page.putField("Source", "凤凰网");
                            page.putField("Score", score);
                        }
                    } else
                        logger.debug("time : " + Time);

                } else//标题相关度未超过阈值，则进一步判断文本内容是否相关
                {
                    String Content = page.getHtml()
                            .xpath("//*[@id='main_content']/allText() ").toString();
                    score = relevant.getRelatGrade(Content, "content");//文本相关度计算
                    if (score >= 4)//文本内容与主题相关
                    {
                        String Time = page.getHtml()
                                .xpath("//*[@id='artical_sth']/p/span[1]/text()").toString();
                        Date time = new Date();
                        if (Time != null) {
                            time = getdate.convertToDate(Time);
                            String year = Time.trim().substring(0,4);
                            String month = Time.trim().substring(5,7);
                            String day = Time.trim().substring(8,10);

                            String timetem = year + "-" + month + "-" + day;
                            if (lastDate.compareTo(timetem) < 0)//该新闻发布日期在上次爬虫运行时间之后
                            {
                                logger.debug("add a record");
                                page.putField("Title", Title);
                                page.putField("Time", time);
                                page.putField("Content", Content);
                                page.putField("URL", page.getUrl().toString());
                                page.putField("Source", "凤凰网");
                                page.putField("Score", score);
                            }
                        } else
                            logger.debug("time : " + Time);
                    }
                }
            }

        }


    }

    public void creatSpider()
    {
        System.setProperty("selenuim_config", "D://spiderProject/webMagicProject/chromedriver/config.ini");
        //String searchURL = "https://news.sogou.com/news?query=site%3Aifeng.com" + "中美贸易战";
        String searchURL = "https://news.sogou.com/news?query=site%3Aifeng.com" + "+中美贸易战";
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader("D://spiderProject/webMagicProject/chromedriver/chromedriver.exe");
        seleniumDownloader.setSleepTime(3000);
        Spider.create(new fenghuangProcessor())
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
