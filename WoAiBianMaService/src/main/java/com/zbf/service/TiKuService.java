package com.zbf.service;

import com.alibaba.fastjson.JSON;
import com.zbf.common.ResponseResult;
import com.zbf.core.page.Page;
import com.zbf.core.utils.AESUtils;
import com.zbf.core.utils.UID;
import com.zbf.enmu.CodeToName;
import com.zbf.enmu.MyRedisKey;
import com.zbf.enmu.ShiTiLeiXingEnmu;
import com.zbf.mapper.TiKuMapper;
import com.zbf.oauthLogin.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：LCG
 * 创建时间：2019/2/14 11:05
 * 描述：
 */
@Component
public class TiKuService {
    @Autowired
    private TiKuMapper tiKuMapper;

    @Transactional
    public int addTiKuInfo(Map<String,Object> map, RedisTemplate redisTemplate){

        //向redis中写入数据使用 Map数据类型
        redisTemplate.opsForHash ().put ( MyRedisKey.TIKU.getKey (),map.get ( "id" ).toString (),map );
        return tiKuMapper.addTiKuInfo ( map );
    }

    public void getTikuList(Page<Map<String,Object>> page){

         List<Map<String,Object>> list=tiKuMapper.getTikuList(page);

         list.forEach ( (item)->{
             if(item.get ( "tikuzhuangtai" ).toString ().equals ( "1" )){
                 item.put ( "tikuzhuangtai","开放");
             }else{
                 item.put ( "tikuzhuangtai","关闭");
             }
         } );

         page.setResultList ( list );
    }

    /**
     * 更新题库信息
     * @param map
     */
    @Transactional
    public void updateTiKuInfo(Map<String,Object> map,RedisTemplate redisTemplate){
        tiKuMapper.updateTiKuInfo ( map );
        //更新redis中的数据
        redisTemplate.opsForHash ().put ( MyRedisKey.TIKU.getKey (),map.get ( "id" ).toString (),map );
    }

    public void addShitiInfo(RedisTemplate redisTemplate,Map<String,Object> params) throws Exception {
        //判断题目的类型 params.get ( "shitileixing" )!=null&&params.get ( "shitileixing" ).toString ().equals ( "1" )
        //用来存放当前新增的题目信息
        Map<String,Object> timu=new HashMap<String,Object> ();
        //题目ID
        timu.put ( "id",UID.getUUIDOrder () );
        //创建者的ID（来自于当前的用户ID）
        timu.put ( "createuserid",params.get ( "userid" ));
        //题型ID
        timu.put ( "tixingid",params.get ( "shitileixing" ) );
        //题库ID
        timu.put ( "tikuid",params.get ( "tikuid" ) );
        //试题状态
        timu.put ( "shitizhuangtai",params.get ( "shitizhuangtai" ) );
        //试题难度ID
        timu.put ( "nanduid",params.get ( "nanduid" ) );
        //试题来源
        timu.put ( "laiyuan",params.get ( "laiyuan" ));
        //试题题干
        timu.put ( "tigan",AESUtils.desEncrypt ( params.get ( "tigan" ).toString () ) );
        //试题解析
        timu.put ( "timujiexi", AESUtils.desEncrypt (params.get ( "timujiexi" ).toString ()) );

        if(params.get ( "shitileixing" )!=null&&params.get ( "shitileixing" ).toString ().equals ( "1" )){//单选题
            //试题答案
            timu.put ( "daan",params.get ( "danxuancheck" ) );
            //试题的选项编号
            timu.put ( "xuanxiangbianhao",params.get ( "danxuanlist" ));
            //试题的选项描述
            timu.put ( "xuanxiangmiaoshu",params.get ( "danxuanmiaoshu" ) );

        }else if(params.get ( "shitileixing" )!=null&&params.get ( "shitileixing" ).toString ().equals ( "2" )){//多选题
            //试题答案
            timu.put ( "daan",params.get ( "checkList" ) );
            //试题的选项编号
            timu.put ( "xuanxiangbianhao",params.get ( "kuanglist" ));
            //试题的选项描述
            timu.put ( "xuanxiangmiaoshu",params.get ( "inputValues" ) );

        }else if(params.get ( "shitileixing" )!=null&&params.get ( "shitileixing" ).toString ().equals ( "3" )){//判断题
            //试题答案
            timu.put ( "daan",params.get ( "panduancheck" ) );
            //试题的判断描述
            timu.put ( "xuanxiangmiaoshu",params.get ( "panduanxiang" ) );
        }else if(params.get ( "shitileixing" )!=null&&params.get ( "shitileixing" ).toString ().equals ( "5" )){//问答题
            //问答题目的答案
            timu.put ( "daan",params.get ( "wendadaan" ) );
        }
        //存入数据库
        tiKuMapper.addShitiForTIku ( timu );
        //存入solr，以后试题从solr中获取


    }

    /**
     * 获取试题列表
     * @param page
     */
    public void getShitiList(Page<Map<String,Object>> page){
        List<Map<String, Object>> shitiList = tiKuMapper.getShitiList ( page );
        shitiList.forEach ( (item)->{
             item.put ( "shitileixing",CodeToName.getShiTiLeixingName ( item.get ( "tixingid" ).toString () ) );
             item.put ( "nanduid",CodeToName.getShiTiNanDuName ( item.get ( "nanduid" ).toString () ));
             item.put ( "shitizhuangtai",CodeToName.getShiTiZhuangTaiName ( item.get ( "shitizhuangtai" ).toString () ));

        } );
        page.setResultList ( shitiList );
    }

    public Map<String,Object> getShiTiById(Map<String,Object> map){
        Map<String, Object> params = tiKuMapper.getShiTiById ( map );
        if(params.get ( "tixingid" )!=null&&params.get ( "tixingid" ).toString ().equals ( "1" )){//单选题
            //试题的选项编号
            params.put ( "xuanxiangbianhao",JSON.toJSONString ( JSON.parse (params.get ( "xuanxiangbianhao" ).toString () )));
            //试题的选项描述
            params.put ( "xuanxiangmiaoshu",JSON.toJSONString ( JSON.parse (params.get ( "xuanxiangmiaoshu" ).toString () )));

        }else if(params.get ( "tixingid" )!=null&&params.get ( "tixingid" ).toString ().equals ( "2" )){//多选题
            //试题答案
            params.put ( "daan",JSON.toJSONString ( JSON.parse (params.get ( "daan" ).toString () )) );
            //试题的选项编号
            params.put ( "xuanxiangbianhao",JSON.toJSONString ( JSON.parse (params.get ( "xuanxiangbianhao" ).toString () )));
            //试题的选项描述
            params.put ( "xuanxiangmiaoshu",JSON.toJSONString ( JSON.parse (params.get ( "xuanxiangmiaoshu" ).toString () )));

        }else if(params.get ( "tixingid" )!=null&&params.get ( "tixingid" ).toString ().equals ( "3" )){//判断题
            //试题的判断描述
            Object miaoshu=params.get ( "xuanxiangmiaoshu" )!=null?params.get ( "xuanxiangmiaoshu" ):"";
            params.put ( "xuanxiangmiaoshu",JSON.toJSONString ( JSON.parse (miaoshu.toString () )) );

        }

        return params;
    }


    public List<Map<String,Object>> getTiKUFenxi(Map<String,Object> map){
        return tiKuMapper.getTiKUFenXi ( map );
    }

}
