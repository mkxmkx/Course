package newsspider.news.pipeline;

import newsspider.news.domain.News;
import newsspider.news.repository.NewsRepository;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class MySQLPipeline implements Pipeline {

    NewsRepository newsRepository;

    public MySQLPipeline(NewsRepository newsRepository)
    {
        this.newsRepository = newsRepository;
    }

    @Override

    public void process(ResultItems resultItems, Task task)
    {
        if(resultItems.get("Title")!=null){
            News news = new News();
            news.setTitle(resultItems.get("Title"));
            news.setTime(resultItems.get("Time"));
            news.setURL(resultItems.get("URL"));
            news.setContent(resultItems.get("Content"));
            news.setSource(resultItems.get("Source"));
            news.setScore(resultItems.get("Score"));
            newsRepository.save(news);
        }
    }

}
