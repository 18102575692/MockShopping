package com.example.ssm.service.impl;

import com.example.ssm.entity.PurchaseRecord;
import com.example.ssm.service.PurchaseService;
import com.example.ssm.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    PurchaseService purchaseService;

    //redis 购买记录集合前缀
    private static final String PURCHASE_PRODUCT_LIST="purchase_list_";
    //抢购商品集合
    private static final String PRODUCT_SET = "product_schedule_set";
    //每次取1000条，避免一次消耗太多内存
    private static final int ONE_TIME_SIZE = 1000;

    @Override
    //每天凌晨1点执行
//    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(fixedRate = 1000 * 60) //测试使用
    public void purchaseTask() {
        System.out.println("处理采购任务开始 --------");
        Set<String> productIdList = stringRedisTemplate.opsForSet().members(PRODUCT_SET);
        List<PurchaseRecord> recordList = new ArrayList<>();
        for (String str:productIdList){
            Long productId = Long.parseLong(str);
            String purchaseKey = PURCHASE_PRODUCT_LIST+productId;
            BoundListOperations<String,String> ops = stringRedisTemplate.boundListOps(purchaseKey);
            //计算记录数
            long size = stringRedisTemplate.opsForList().size(purchaseKey);
            long times = size % ONE_TIME_SIZE == 0?size/ONE_TIME_SIZE : size/ONE_TIME_SIZE+1;
            for (int i = 0;i<times;i++){
                //获取至多TIME_SIZE个产品信息
                List<String> prList = null;
                if (i==0){
                    prList = ops.range(i*ONE_TIME_SIZE,(i+1)*ONE_TIME_SIZE);
                }else {
                    prList = ops.range(i*ONE_TIME_SIZE+1,(i+1)*ONE_TIME_SIZE);
                }
                for (String prStr: prList){
                    PurchaseRecord record = this.createPurchase(prStr,productId);
                    recordList.add(record);
                }
                try{
                    //该方法采用新事务，不会全局事务回滚
                    this.purchaseService.purchase(recordList);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                //清楚列表为空，等待重新写入数据
                recordList.clear();
            }
            //删除购买列表
            stringRedisTemplate.delete(purchaseKey);
            // 从商品集合中删除商品
            stringRedisTemplate.opsForSet().remove(PRODUCT_SET,str);
        }
        System.out.println("处理采购任务结束 --------");
    }

    /**
     * 处理redis的信息
     * @param string redis 信息题
     * @param productId 产品信息
     * @return
     */
    private PurchaseRecord createPurchase(String string,Long productId){
        String[] arr = string.split(",");
        Long userId = Long.parseLong(arr[0]);
        int quantity = Integer.parseInt(arr[1]);
        double sum = Double.parseDouble(arr[2]);
        double price = Double.parseDouble(arr[3]);
        Timestamp time = new Timestamp(Long.parseLong(arr[4]));
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setProductId(productId);
        purchaseRecord.setUserId(userId);
        purchaseRecord.setSum(sum);
        purchaseRecord.setQuantity(quantity);
        purchaseRecord.setPrice(price);
        purchaseRecord.setPurchaseDate(time);
        purchaseRecord.setNote("购买日志：时间："+purchaseRecord.getPurchaseDate());
        return purchaseRecord;
    }
}
