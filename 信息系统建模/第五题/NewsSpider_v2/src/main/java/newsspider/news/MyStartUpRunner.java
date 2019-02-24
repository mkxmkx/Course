package newsspider.news;

import newsspider.news.processor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyStartUpRunner implements CommandLineRunner {

    @Autowired
    SohuProcessor sohuProcessor;

    @Autowired
    qqProcessor myqqProcessor;

    @Autowired
    sinaProcessor mysinaProcessor;

    @Autowired
    ChinaNewsProcessor chinaNewsProcessor;

    @Autowired
    fenghuangProcessor myfenghuangProcessor;

    @Autowired
    wangyiProcessor mywangyiProcessor;

    /*
    @Autowired
    toutiaoProcessor mytoutiaoProcessor;
    */

    @Autowired
    toutiaoAllMessageProcessor mytoutiaoAllMessageProcessor;

    @Autowired
    zhongshiProcessor myzhongshiProcessor;

    @Override
    public void run(String... args) throws Exception
    {
        chinaNewsProcessor.creatSpider();
        //sohuProcessor.creatSpider();
        //myqqProcessor.creatSpider();
        //mysinaProcessor.creatSpider();
        //myfenghuangProcessor.creatSpider();
        //mywangyiProcessor.creatSpider();
        //mytoutiaoAllMessageProcessor.creatSpider();
        //myzhongshiProcessor.creatSpider();
    }
}
