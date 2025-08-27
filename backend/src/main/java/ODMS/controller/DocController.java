package ODMS.controller;

import ODMS.common.Response;
import ODMS.entity.Document;
import ODMS.entity.DocumentList;
import ODMS.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class DocController {
    @Autowired
    private DocService docService;

    //获取文章详情
    @GetMapping("/document/{id}")
    public Response<Document> get(@PathVariable("id") Integer id) {
        Document doc = docService.getDocById(id);
        return Response.success(doc);
    }

    //添加文章
    @PostMapping("/admin/doc")
    public Response<?> addDoc(@RequestBody Document doc){
        docService.add(doc);
        return Response.success();
    }

    //修改文章
    @PutMapping("/admin/doc")
    public Response<?> updateDoc(@RequestParam Map<String, String> req){
        Document doc = docService.getDocById(Integer.parseInt(req.get("id")));
        doc.setTitle(req.get("title"));
        doc.setContent(req.get("content"));
        doc.setExcerpt(req.get("excerpt"));

        docService.updateDocument(doc);
        return Response.success(doc);
    }

    //搜索文章
    @GetMapping("/document")
    public Response<List<DocumentList>> searchDoc(@RequestParam(required = false) String keyword, @RequestParam(required = false) String type, @RequestParam(required = false) String department,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<DocumentList> docList =docService.searchDocuments(keyword, type,department,startDate,endDate);
        return Response.success(docList);
    }

    //删除文章
    @DeleteMapping("/admin/doc")
    public Response<?> deleteDoc(@RequestParam("id") Integer id) {
        if(docService.getDocById(id) == null){
            return Response.error("This document does not exist");
        }
        docService.delteDocument(id);
        return Response.success();
    }

    //将文章转换为docx字节流供前端下载
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocx(@PathVariable Integer id) throws Exception {
        Document doc = docService.getDocById(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = docService.generateDocx(doc);

        String filename = URLEncoder.encode(doc.getTitle(), StandardCharsets.UTF_8) + ".docx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(fileBytes);
    }
}
