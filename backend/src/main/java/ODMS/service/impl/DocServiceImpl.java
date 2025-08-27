package ODMS.service.impl;

import ODMS.entity.Document;
import ODMS.entity.DocumentList;
import ODMS.mapper.DocMapper;
import ODMS.service.DocService;
import ODMS.utils.ThreadLocalUtil;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class DocServiceImpl implements DocService {
    @Autowired
    private DocMapper docMapper;

    @Override
    public void add(Document doc) {
        Map<String, Object> map = ThreadLocalUtil.getThreadLocal();
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        Integer creator = (Integer) map.get("id");
        doc.setCreator(creator);

        docMapper.add(doc);
    }

    @Override
    public Document getDocById(Integer id) {
        return docMapper.findById(id);
    }

    @Override
    public List<DocumentList> searchDocuments(String keyword, String type, String department,
                                              LocalDate startDate, LocalDate endDate) {
        return docMapper.searchDocuments(keyword, type,department,startDate,endDate);
    }

    @Override
    public void delteDocument(Integer id) {
        docMapper.delteDoc(id);
    }

    @Override
    public void updateDocument(Document doc) {
        doc.setUpdatedAt(LocalDateTime.now());
        docMapper.updateDocument(doc);
    }

    @Override
    public byte[] generateDocx(Document document) throws IOException{
        XWPFDocument doc = new XWPFDocument();

        // 标题
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(document.getTitle());
        titleRun.setBold(true);
        titleRun.setFontSize(18);

        // 元信息（类型、部门、更新时间）
        XWPFParagraph metaPara = doc.createParagraph();
        XWPFRun metaRun = metaPara.createRun();
        metaRun.setFontSize(11);
        metaRun.setText("类型: " + (document.getType() != null ? document.getType() : "无"));
        metaRun.addBreak();
        metaRun.setText("部门: " + (document.getDepartment() != null ? document.getDepartment() : "无"));
        metaRun.addBreak();
        if (document.getUpdatedAt() != null) {
            String updateTime = document.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            metaRun.setText("更新时间: " + updateTime);
        }
        metaRun.addBreak();

        // 摘要
        if (document.getExcerpt() != null && !document.getExcerpt().isEmpty()) {
            XWPFParagraph excerptPara = doc.createParagraph();
            XWPFRun excerptRun = excerptPara.createRun();
            excerptRun.setText("摘要: " + document.getExcerpt());
            excerptRun.setItalic(true);
            excerptRun.addBreak();
        }

        // 正文（自动分段）
        if (document.getContent() != null) {
            String[] paragraphs = document.getContent().split("\\r?\\n");
            for (String para : paragraphs) {
                if (!para.trim().isEmpty()) {
                    XWPFParagraph contentPara = doc.createParagraph();
                    contentPara.setAlignment(ParagraphAlignment.LEFT);
                    XWPFRun contentRun = contentPara.createRun();
                    contentRun.setText(para.trim());
                    contentRun.addBreak();
                }
            }
        }

        // 输出为 byte[]
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        doc.close();
        return out.toByteArray();
    }
}