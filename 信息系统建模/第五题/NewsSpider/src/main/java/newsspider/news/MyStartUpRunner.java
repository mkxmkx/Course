package newsspider.news;

import newsspider.news.processor.ChinaNewsProcessor;
import newsspider.news.processor.SohuProcessor;
import newsspider.news.processor.qqProcessor;
import newsspider.news.processor.sinaProcessor;
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


    @Override
    public void run(String... args) throws Exception
    {
        //sohuProcessor.creatSpider();
        //myqqProcessor.creatSpider();
        mysinaProcessor.creatSpider();
        //chinaNewsProcessor.creatSpider();
    }
}
