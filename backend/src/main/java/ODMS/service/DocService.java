package ODMS.service;

import ODMS.entity.Document;
import ODMS.entity.DocumentList;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


public interface DocService {
    //添加文章
    void add(Document doc);

    //通过id获取文章
    Document getDocById(Integer id);

    //搜索文章
    List<DocumentList> searchDocuments(String keyword, String type, String department, LocalDate startDate, LocalDate endDate);

    //删除文章
    void delteDocument(Integer id);

    //修改文章
    void updateDocument(Document doc);

    byte[] generateDocx(Document document) throws IOException;
}
