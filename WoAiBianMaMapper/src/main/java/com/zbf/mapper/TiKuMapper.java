package com.zbf.mapper;

import com.zbf.core.page.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 作者：LCG
 * 创建时间：2019/2/14 11:01
 * 描述：
 */
@Mapper
public interface TiKuMapper {

    public int addTiKuInfo(Map<String,Object> map);

    public List<Map<String,Object>> getTikuList(Page<Map<String,Object>> page);

    public int updateTiKuInfo(Map<String,Object> map);
    //向题库中添加试题
    public int addShitiForTIku(Map<String,Object> map);

    public List<Map<String,Object>> getShitiList(Page<Map<String,Object>> page);

    public Map<String,Object> getShiTiById(Map<String,Object> map);

    public int deleteByShiTiId(Map<String,Object> map);

    /**
     * 按数题库导出数据
     * @param page
     * @return
     */
    public List<Map<String,Object>> getShitiDataListByTiKu(Page<Map<String,Object>> page);

    /**
     * 题库分析
     * @param map
     * @return
     */
    public List<Map<String,Object>> getTiKUFenXi(Map<String,Object> map);

}
