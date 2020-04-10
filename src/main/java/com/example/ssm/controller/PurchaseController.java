package com.example.ssm.controller;

import com.example.ssm.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PurchaseController {
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    RedisTemplate redisTemplate;

    //定义一个抢购界面
    @RequestMapping("/test")
    public ModelAndView test(){
        return new ModelAndView("/test");
    }

    @PostMapping("/purchase")
    @ResponseBody
    public Map<String,Object> purchase(@RequestParam("userId")Long userId,
                                       @RequestParam("productId")Long productId,
                                       @RequestParam("quantity")Integer quantity){
        boolean result = this.purchaseService.purchaseLua(userId,productId,quantity);
        String msg = result?"成功":"失败";
        Map<String,Object> map = new HashMap<>();
        map.put("msg",msg);
        return map;
    }

    //尝试lua脚本
    @RequestMapping("/lua")
    @ResponseBody
    public Map<String,Object> lua(String key1,String key2,String obj1,String obj2){
        //定义Lua脚本
        String luaString = "redis.call('set',KEYS[1],ARGV[1]) " +
                "redis.call('set',KEYS[2],ARGV[2]) " +
                "local str1 = redis.call('get',KEYS[1]) " +
                "local str2 = redis.call('get',KEYS[2]) " +
                "if str1 == str2 then " +
                " return 1 " +
                " end " +
                " return 0 ";
        System.out.println("脚本:"+luaString);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptText(luaString);
        redisScript.setResultType(Long.class);
        //采用字符串序列化
        RedisSerializer<String> redisSerializer = this.redisTemplate.getStringSerializer();
        //定义Key参数
        List<String> keyList = new ArrayList<>();
        keyList.add(key1);
        keyList.add(key2);
        //传递两个参数值 其中一个序列化 第二个是参数
        Long result = (Long) this.redisTemplate.execute(redisScript,redisSerializer,redisSerializer,keyList,obj1,obj2);
        Map<String,Object> map = new HashMap<>();
        map.put("result",result==1?"相等":"不相等");
        return map;
    }
}
