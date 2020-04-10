package com.example.ssm.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.ssm.entity.Product;
import com.example.ssm.entity.PurchaseRecord;
import com.example.ssm.mapper.ProductMapper;
import com.example.ssm.mapper.PurchaseRecordMapper;
import com.example.ssm.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    @Autowired
    PurchaseRecordMapper purchaseRecordMapper;
    @Autowired
    ProductMapper productMapper;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public boolean purchase(Long userId, Long productId, int quantity) {
        //查询产品
        Product product = this.productMapper.getProduct(productId);
        if (product == null || product.getStock()<quantity){
            //商品错误或者库存不足
            return false;
        }
        //减库存
        this.productMapper.decreaseProduct(productId,quantity);
        //添加购买记录
        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setPrice(product.getPrice());
        purchaseRecord.setQuantity(quantity);
        purchaseRecord.setUserId(userId);
        purchaseRecord.setProductId(productId);
        purchaseRecord.setSum(quantity*product.getPrice());
        purchaseRecord.setNote("购买时间："+System.currentTimeMillis());
        this.purchaseRecordMapper.insertSelective(purchaseRecord);
        return true;
    }

    //redis 购买记录集合前缀
    private static final String PURCHASE_PRODUCT_LIST="purchase_list_";
    //抢购商品集合
    private static final String PRODUCT_SET = "product_schedule_set";
    //32位SHA1编码，第一次执行的时候让redis进行缓存脚本返回
    private String sha1=null;

    @Override
    public boolean purchaseLua(Long userId, Long productId, int quantity) {
        //购买时间
        long purchaseDate = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            //获取原始连接
            jedis = (Jedis) Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection().getNativeConnection();
            //检查redis的hash是否存在商品
            Map<String,String> product= jedis.hgetAll("product_"+productId);
            if (product.isEmpty()){
                Product obj = this.productMapper.getProduct(productId);
                if (obj ==null){
                    return false;
                }else {
                    jedis.hset("product_"+productId,"stock",obj.getStock()+"");
                    jedis.hset("product_"+productId,"price",obj.getPrice()+"");
                    jedis.hset("product_"+productId,"note",obj.getNote()+"");
                    jedis.hset("product_"+productId,"productName",obj.getProductName()+"");
                    jedis.hset("product_"+productId,"version",obj.getVersion()+"");
                    jedis.hset("product_"+productId,"id",obj.getId()+"");
                }
            }
            //检查有咩有缓存
            if (sha1 == null){
                String purchaseScript = " redis.call('sadd',KEYS[1],ARGV[2]) " +
                        "local productPurchaseList =KEYS[2] .. ARGV[2] " +  //购买列表
                        "local userId = ARGV[1] " +                        //用户信息
                        "local product = 'product_'..ARGV[2] " +           //产品
                        "local quantity = tonumber(ARGV[3]) " +            //购买数量
                        "local stock = tonumber(redis.call('hget',product,'stock')) " +  //库存
                        "local price = tonumber(redis.call('hget',product,'price')) " +  //价格
                        "local purchase_date = ARGV[4] " +                 //购买时间
                        "if stock <quantity then return 0 end " +
                        "stock = stock - quantity " +                    //减库存
                        "redis.call('hset',product,'stock', tostring(stock)) " +  //存进哈希表
                        "local sum = price * quantity " +                   //计算总价
                        //购买记录
                        "local purchaseRecord = userId..','..quantity..','..sum..','..price..','..purchase_date  " +
                        //把购买记录存进队列
                        "redis.call('rpush',productPurchaseList,purchaseRecord) " +
                        "return 1";
                sha1=jedis.scriptLoad(purchaseScript);
            }
            // 执行脚本，返回结果
            Object res = jedis.evalsha(sha1, 2, PRODUCT_SET,
                    PURCHASE_PRODUCT_LIST, userId + "", productId + "",
                    quantity + "", purchaseDate + "");
            Long result = (Long) res;
            return result == 1;
        }catch (Exception e){
            return false;
        }finally {
            if (jedis != null && jedis.isConnected()){
                jedis.close();
            }
        }
    }

    @Override
    //启用新的独立事务运行
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean purchase(List<PurchaseRecord> recordList) {
        for (PurchaseRecord record:recordList){
            this.purchaseRecordMapper.insertSelective(record);
            this.productMapper.decreaseProduct(record.getProductId(),record.getQuantity());
        }
        return true;
    }

}
