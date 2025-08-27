package ODMS.mapper;

import ODMS.entity.Document;
import ODMS.entity.DocumentList;
import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface DocMapper {
    //新增
    @Insert("insert into documents(title,type,content,excerpt,creator,created_at,updated_at) " +
            "values(#{title},#{type},#{content},#{excerpt},#{creator},#{createdAt},#{updatedAt})")
    void add(Document doc);

    //查找
    @Select("select * from documents where id=#{id}")
    Document findById(Integer id);

    //搜索
    List<DocumentList> searchDocuments(@Param("keyword") String keyword, 
                                      @Param("type") String type, 
                                      @Param("department") String department,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);

    //删除
    @Delete("delete from documents where id=#{id}")
    void delteDoc(Integer id);

    //修改
    @Update("update documents set title=#{title},type=#{type},content=#{content},excerpt=#{excerpt},updated_at=now() where id=#{id}")
    void updateDocument(Document doc);
}
