package newsspider.news.controller;

import newsspider.news.domain.News;
import newsspider.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/getAllNews")
    public List<News> getNews()
    {
        List<News> list = new ArrayList<News>();
        list = newsService.getAllNews();
        return list;
     }
}
