package newsspider.news.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * 实体类
 * 存储新闻的内容
 * 标题，时间，正文，URL
 */

@Entity
public class News {

    @Id
    @GeneratedValue
    private Long newsId;


    private String title;
    @Column(columnDefinition = "Date")
    private Date time;
    private String source;

    @Column(columnDefinition = "longtext")
    private String content;

    private String URL;

    private float score;


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                ", URL='" + URL + '\'' +
                '}';
    }
}
