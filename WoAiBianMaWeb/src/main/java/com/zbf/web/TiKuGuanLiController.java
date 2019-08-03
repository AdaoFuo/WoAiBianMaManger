package com.zbf.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.core.utils.FileUploadDownUtils;
import com.zbf.core.utils.UID;
import com.zbf.enmu.MyRedisKey;
import com.zbf.enmu.ShiTiLeiXingEnmu;
import com.zbf.service.TiKuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：LCG
 * 创建时间：2019/2/14 11:00
 * 描述：
 */
@RequestMapping("tiku")
@RestController
public class TiKuGuanLiController {
    @Autowired
    private TiKuService tiKuService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加题库信息
     * @param request
     * @return
     */
    @RequestMapping("toaddTiKuInfo")
    public ResponseResult toaddTiKuInfo(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        //获取数据
        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );
        //存入数据
        try {
            parameterMap.put ( "id",UID.getUUIDOrder () );
            tiKuService.addTiKuInfo ( parameterMap,redisTemplate);
            responseResult.setSuccess ( "ok" );
        }catch (Exception e){
            e.printStackTrace ();
            responseResult.setError ( "error" );
            //删除上一步骤中写入的数据
            redisTemplate.opsForHash ().delete ( MyRedisKey.TIKU.getKey (),parameterMap.get ( "id" ).toString ());
        }

        return responseResult;

    }

    /**
     * 题库列表
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("getTikuList")
    public ResponseResult getTikuList(HttpServletRequest httpServletRequest){

        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap ( httpServletRequest );

        Page<Map<String,Object>> page=new Page<> ();

        ResponseResult responseResult=ResponseResult.getResponseResult ();
        //设置查询参数
        page.setParams ( paramsJsonMap );

        Page.setPageInfo ( page, paramsJsonMap);

        //
        tiKuService.getTikuList ( page );
        //

        responseResult.setResult ( page );

        return responseResult;

    }

    /**
     * 更新题库信息
     * @param request
     * @return
     */
    @RequestMapping("updateTiKuInfo")
    public ResponseResult updateTiKuInfo(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap ( request );

        tiKuService.updateTiKuInfo ( paramsJsonMap,redisTemplate );

        responseResult.setSuccess ( "ok" );

        return responseResult;
    }

    /**
     * 从redis中获取题库列表信息
     * @param request
     * @return
     */
    @RequestMapping("getTikuListFromRedis")
    public ResponseResult getTikuListFromRedis(HttpServletRequest request){

        List<Map<String,Object>> values = redisTemplate.opsForHash ().values ( MyRedisKey.TIKU.getKey () );

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        responseResult.setResult ( values );

        return responseResult;

    }

    /**
     * 手动添加试题
     * @return
     */
    @RequestMapping("toAddShiTi")
    public ResponseResult toAddShiTi(HttpServletRequest request) throws Exception {

        ResponseResult responseResult=ResponseResult.getResponseResult ();
        //获取请求数据
        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );
        //写入数据
        tiKuService.addShitiInfo ( redisTemplate,parameterMap );

        responseResult.setSuccess ( "ok" );

        return responseResult;
    }

    /**
     *
     *  试题管理 试题列表
     * @param request
     * @return
     */
    @RequestMapping("togetShitiList")
    public ResponseResult togetShitiList(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );

        Page<Map<String,Object>> page=new Page<> ();

        Page.setPageInfo ( page,parameterMap );

        tiKuService.getShitiList ( page );

        responseResult.setResult ( page );

         return responseResult;
    }

    @RequestMapping("getExceltemplate")
    public void getExceltemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

        File excelTemplate = FileUploadDownUtils.getExcelTemplate ( "exceltemplate/timu.xlsx" );

        FileUploadDownUtils.responseFileBuilder ( response,excelTemplate,"数据模板【题目】.xlsx" );

    }


    /**
     * 根据ID获取试题信息
     * @param request
     * @return
     */
    @RequestMapping("getShitiById")
    public ResponseResult getShitiById(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );

        Map<String, Object> shiTiById = tiKuService.getShiTiById ( parameterMap );

        responseResult.setResult ( shiTiById );

        return responseResult;

    }

    /**
     * 分析题库
     * @param request
     * @return
     */
    @RequestMapping("toFenxiTiku")
    public ResponseResult toFenxiTiku(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );

        List<Map<String, Object>> tiKUFenxi = tiKuService.getTiKUFenxi ( parameterMap );
        List<String> listName =new ArrayList<> (  );
        int countnum=0;
        Map<String,Object> mmp=new HashMap<> (  );
        if(tiKUFenxi.size ()>0){
            for(Map<String,Object> map:tiKUFenxi){
                String tixigid=map.get ( "name" ).toString ();
                map.put ( "name",ShiTiLeiXingEnmu.getNameFromId ( tixigid ) );
                   listName.add ( ShiTiLeiXingEnmu.getNameFromId ( tixigid ) );
                countnum=countnum+Integer.valueOf ( map.get ( "value" ).toString () );
            }
            mmp.put ( "tubiaoData",tiKUFenxi );
            mmp.put ( "data",listName);
            mmp.put ( "countnum",countnum);
        }else{
            mmp.put ( "tubiaoData",tiKUFenxi );
            mmp.put ( "data",listName);
            mmp.put ( "countnum",0);
        }

        responseResult.setResult ( mmp );

        mmp=null;
        tiKUFenxi=null;
        listName=null;

        return responseResult;
    }

}
