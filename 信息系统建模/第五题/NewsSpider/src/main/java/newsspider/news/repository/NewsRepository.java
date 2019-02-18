package newsspider.news.repository;

import newsspider.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface NewsRepository extends JpaRepository<News,Long> {

    /**
     * 查询所有新闻，按照时间顺序排序
     * @return
     */
    @Transactional(rollbackOn = Exception.class)
    @Query(value = "select * from News n order by n.time",nativeQuery = true)
    List<News> findAll();
}
