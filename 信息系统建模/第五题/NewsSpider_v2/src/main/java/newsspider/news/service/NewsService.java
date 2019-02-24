package newsspider.news.service;

import newsspider.news.domain.News;
import newsspider.news.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NewsRepository newsRepository;

    /**
     * 查询所有新闻
     */
    public List<News> getAllNews()
    {
        try {
            List<News> newsList = new ArrayList<News>();
            newsList = newsRepository.findAll();
            return newsList;
        }catch (Exception err)
        {
            logger.error("查询信息失败");
        }
        return null;
    }
}
