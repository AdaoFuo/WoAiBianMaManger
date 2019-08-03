package com.zbf.web;

import com.alibaba.fastjson.JSON;
import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.core.utils.UID;
import com.zbf.enmu.MyRedisKey;
import com.zbf.mapper.TiKuMapper;
import com.zbf.zhongjian.webSocket.WebSocketServer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作者：LCG
 * 创建时间：2019/2/18 20:12
 * 描述：
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private TiKuMapper tiKuMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Excel批量导入试题
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("importExcelData")
    public ResponseResult importExcelData(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {

        ResponseResult responseResult=ResponseResult.getResponseResult ();
        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );
        //得到表格的输入流
        InputStream inputStream = file.getInputStream ();
        //创建一个工作薄
        Workbook wb = null;
        //获取文件的名字
        String originalFilename = file.getOriginalFilename ();

        if (originalFilename.endsWith ( ".xls" )) {//兼容2003
            wb = new HSSFWorkbook (inputStream);
        } else {//xlsx
            wb = new XSSFWorkbook (inputStream);
        }
        int numberOfSheets = wb.getNumberOfSheets ();//获取工作表的个数
        List<List<Map<String,Object>>> listSheet=new ArrayList (  );
        for(int i=0;i<numberOfSheets;i++){//处理多个工作页
            Sheet sheetAt = wb.getSheetAt ( i );//获取下标对应的工作表
            //处理工作表
            int physicalNumberOfRows = sheetAt.getPhysicalNumberOfRows ();//获取物理行数
            //获取行数据
            List<Map<String,Object>> listcell=new ArrayList<> (  );
            for(int hang=2;hang<physicalNumberOfRows;hang++){//从第三行开始读取 设置跳过表头
                Map<String,Object> mapcell=new HashMap<String,Object> ();
                mapcell.putAll ( parameterMap );
                //设置题目的公共数据

                //获取一行数据
                Row row = sheetAt.getRow ( hang );
                //读取列中的数据
                Cell cell0 = row.getCell ( 0 );
                mapcell.put ( "tigan",cell0.getStringCellValue ());cell0=null;
                Cell cell1 = row.getCell ( 1 );
                mapcell.put ( "xuanxiangbianhao",cell1.getStringCellValue ());cell1=null;
                //选项start
                List<String> listxuanxiang=new ArrayList<> (  );
                Cell cell2 = row.getCell ( 2 );
                listxuanxiang.add ( cell2.getStringCellValue () );cell2=null;
                Cell cell3 = row.getCell ( 3 );
                listxuanxiang.add ( cell3.getStringCellValue () );cell3=null;
                Cell cell4 = row.getCell ( 4 );
                listxuanxiang.add ( cell4.getStringCellValue () );cell4=null;
                Cell cell5 = row.getCell ( 5 );
                listxuanxiang.add ( cell5.getStringCellValue () );cell5=null;
                mapcell.put ( "xuanxiangmiaoshu",JSON.toJSONString ( listxuanxiang ));
                listxuanxiang=null;
                //选项end
                Cell cell6 = row.getCell ( 6 );
                mapcell.put ( "daan",cell6.getStringCellValue ());cell6=null;
                Cell cell7 = row.getCell ( 7 );
                if(cell7!=null){
                    mapcell.put ( "timujiexi",cell7.getStringCellValue ()==null?"":cell7.getStringCellValue ());cell7=null;
                }
                //获取表格中的数据end
                mapcell.put ( "id", UID.getUUIDOrder ());
                listcell.add ( mapcell );
            }
            listSheet.add ( listcell );
        }

        return responseResult;
    }


    /**
     * excel  导出功能
     * @param request
     * @return
     */
    @RequestMapping("daochuexcel")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );
        Page<Map<String,Object>> page=new Page<> ();
        parameterMap.put ( "tikuid","1000000468136044" );
        page.setParams ( parameterMap );
        page.setPageSize ( 20 );

        List<Map<String, Object>> list = tiKuMapper.getShitiDataListByTiKu ( page );

        XSSFWorkbook xwk=new XSSFWorkbook (  );

        XSSFSheet sheet = xwk.createSheet ( "导出数据" );
        XSSFRow row = sheet.createRow ( 0 );
        XSSFCell cell0 = row.createCell ( 0 );
        cell0.setCellValue ( "题目id" );
        XSSFCell cell1 = row.createCell ( 1 );
        cell1.setCellValue ( "题干" );
        XSSFCell cell2 = row.createCell ( 2 );
        cell2.setCellValue ( "选项描述" );

        for(int i=0;i<list.size ();i++){
            XSSFRow roww = sheet.createRow ( i+1 );
            Map<String, Object> map = list.get ( i );
            List<String> collect = map.keySet ().stream ().collect ( Collectors.toList () );
            for(int j=0;j<collect.size ();j++){
                if(collect.get ( j ).equals ( "id" )||collect.get ( j ).equals ( "tigan" )||collect.get ( j ).equals ( "xuanxiangmiaoshu" )){
                    XSSFCell cell01 = roww.createCell ( 0 );
                    cell01.setCellValue ( map.get ( collect.get ( j ) ).toString () );
                }
            }
        }

        //写出工作薄
        String filename=new String("信息表.xlsx".getBytes (),"ISO8859-1");

        response.setContentType ( "application/octet-stream;charset=ISO8859-1" );
        response.setHeader("Content-Disposition", "attachment;filename="+filename);

        ServletOutputStream outputStream = response.getOutputStream ();
        xwk.write ( outputStream);
        xwk.close ();
        outputStream.close ();


    }


    /**
     * Excel文件上传，导入数据
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("toImportExcelData")
    public ResponseResult toImportExcelData(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws IOException {

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        String canshu = request.getParameter ( "canshu" );

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );

        //得到表格的输入流
        InputStream inputStream = file.getInputStream ();

        XSSFWorkbook xssfWorkbook=new XSSFWorkbook ( inputStream );

        XSSFSheet sheetAt = xssfWorkbook.getSheetAt ( 0 );

        int physicalNumberOfRows = sheetAt.getPhysicalNumberOfRows ();//获取数据的行数

        XSSFRow row1 = sheetAt.getRow ( 0 );

        XSSFCell cell = row1.getCell ( 0 );
        cell.getStringCellValue ();//获取字符数据
        List<Map<String,Object>> listdata=new ArrayList<> (  );
        for(int i=1;i<physicalNumberOfRows;i++){
            XSSFRow row = sheetAt.getRow ( i );
            row.getPhysicalNumberOfCells ();
            Map<String,Object> maprow=new HashMap<String,Object>();
            maprow.put ("tigan",row.getCell ( 0 ).getStringCellValue ());
            maprow.put ("xuanxiangbianhao",row.getCell ( 1 ).getStringCellValue ());
            List<String> xuanxiangmiaoshu=new ArrayList<> (  );
            xuanxiangmiaoshu.add ( row.getCell ( 2 ).getStringCellValue () );
            xuanxiangmiaoshu.add ( row.getCell ( 3 ).getStringCellValue () );
            xuanxiangmiaoshu.add ( row.getCell ( 4 ).getStringCellValue () );
            xuanxiangmiaoshu.add ( row.getCell ( 5 ).getStringCellValue () );

            maprow.put ( "xuanxiangmiaoshu",JSON.toJSONString ( xuanxiangmiaoshu ) );
            //真确答案
            maprow.put ( "daan",row.getCell ( 6 ).getStringCellValue ());

            if(row.getCell ( 7 )!=null){
                maprow.put ( "timujiexi",row.getCell ( 7 ).getStringCellValue ());
            }

            listdata.add ( maprow );
        }


        System.out.println (JSON.toJSONString ( listdata ));


        return responseResult;

    }


    /**
     * Excel数据的导出 案例
     * @param request
     * @param response
     */
    @RequestMapping("exportExcelData")
    public void exportExcelData(HttpServletRequest request,HttpServletResponse response) throws IOException {

           //获取数据

           Page<Map<String,Object>> page=new Page<> ();

           Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );

           page.setPageSize ( 30 );
           page.setParams ( parameterMap );

           List<Map<String, Object>> resultList = tiKuMapper.getShitiDataListByTiKu ( page );

           //POI的api的操作
           XSSFWorkbook xssfWorkbook=new XSSFWorkbook (  );

           XSSFSheet sheet = xssfWorkbook.createSheet ( "真好听" );
           XSSFRow row1 = sheet.createRow ( 0 );
           row1.createCell ( 0 ).setCellValue ( "ID" );
           row1.createCell ( 1 ).setCellValue ( "答案" );
           row1.createCell ( 2 ).setCellValue ( "答案解析" );
           row1.createCell ( 3 ).setCellValue ( "题干描述" );
           row1.createCell ( 4 ).setCellValue ( "试题类型" );

        for(int i=0;i<resultList.size ();i++){

               Map<String, Object> map = resultList.get ( i );
               XSSFRow row = sheet.createRow ( i+1 );

               List<String> collect = map.keySet ().stream ().collect ( Collectors.toList () );
               for(int j=0;j<collect.size ();j++){
                   XSSFCell cell =row.createCell ( j );
                   cell.setCellValue (map.get ( collect.get ( j ) )!=null?map.get ( collect.get ( j ) ).toString ():"");
               }

           }


           //输出工作簿
           String filename=new String("【实例】信息表.xlsx".getBytes (),"ISO8859-1");
           response.setContentType ( "application/octet-stream;charset=ISO8859-1" );
           response.setHeader("Content-Disposition", "attachment;filename="+filename);

           xssfWorkbook.write ( response.getOutputStream () );


    }

    /**
     * 添加成绩到redis
     * @param request
     */
    @RequestMapping("addScoreOfUserShiJuan")
    public void addScoreOfUserShiJuan(HttpServletRequest request){
        Map<String,Object> map1=new HashMap<> (  );
        map1.put ( "id","100" );
        map1.put ( "value","试卷1" );
        Map<String,Object> map2=new HashMap<> (  );
        map2.put ( "id","200" );
        map2.put ( "value","试卷2" );
        Map<String,Object> map3=new HashMap<> (  );
        map3.put ( "id","300" );
        map3.put ( "value","试卷3" );
        Map<String,Object> map4=new HashMap<> (  );
        map4.put ( "id","400" );
        map4.put ( "value","试卷4" );
        redisTemplate.opsForHash ().put ( MyRedisKey.SHI_JUAN.getKey (),"100",map1 );
        redisTemplate.opsForHash ().put ( MyRedisKey.SHI_JUAN.getKey (),"200",map2 );
        redisTemplate.opsForHash ().put ( MyRedisKey.SHI_JUAN.getKey (),"300",map3 );
        redisTemplate.opsForHash ().put ( MyRedisKey.SHI_JUAN.getKey (),"400",map4 );

        //参数一试卷的id
        //参数2 考生的id
        //参数3 考生的成绩
        DefaultTypedTuple defaultTypedTuple1=new DefaultTypedTuple ( "1",80.0 );
        DefaultTypedTuple defaultTypedTuple2=new DefaultTypedTuple ( "1",81.0 );
        DefaultTypedTuple defaultTypedTuple3=new DefaultTypedTuple ( "1",82.0 );
        DefaultTypedTuple defaultTypedTuple4=new DefaultTypedTuple ( "1",83.0 );
        DefaultTypedTuple defaultTypedTuple5=new DefaultTypedTuple ( "1",84.0);
        DefaultTypedTuple defaultTypedTuple6=new DefaultTypedTuple ( "1",85.0 );
        DefaultTypedTuple defaultTypedTuple7=new DefaultTypedTuple ( "1",86.0 );
        DefaultTypedTuple defaultTypedTuple8=new DefaultTypedTuple ( "1",87.0 );
        DefaultTypedTuple defaultTypedTuple9=new DefaultTypedTuple ( "1",88.0 );
        DefaultTypedTuple defaultTypedTuple10=new DefaultTypedTuple ( "1",89.0 );

        redisTemplate.opsForZSet ().add ( "100","1",80 );
        redisTemplate.opsForZSet ().add ( "100","2",81 );
        redisTemplate.opsForZSet ().add ( "100","3",82 );
        redisTemplate.opsForZSet ().add ( "100","4",83 );
        redisTemplate.opsForZSet ().add ( "100","5",84 );
        redisTemplate.opsForZSet ().add ( "100","6",85 );
        redisTemplate.opsForZSet ().add ( "100","7",86 );
        redisTemplate.opsForZSet ().add ( "100","8",87 );
        redisTemplate.opsForZSet ().add ( "100","9",88 );
        redisTemplate.opsForZSet ().add ( "100","10",89 );

        //查询分数的区间
        Set set = redisTemplate.opsForZSet ().rangeByScore ( "100", 80, 85 );
        for(Object typ:set){
            System.out.println (typ.toString ());
        }
        System.out.println (JSON.toJSONString ( set ));
    }


    @RequestMapping("sendmessage")
    public void sendmessag(){

        System.out.println ("===========发送消息==============");
        System.out.println (WebSocketServer.sessionMap.size ());
        Session session =(Session)WebSocketServer.sessionMap.get ( "1" );

        WebSocketServer.sendMessage ( session,"服务端反馈信息！","1" );

    }

}
