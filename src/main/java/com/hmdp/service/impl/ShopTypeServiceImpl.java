package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryShopTypeList() {
        // // 1. 从redis中查询商铺类型列表
        // String jsonArray = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE);
        // // json转list
        // List<ShopType> typeList = JSONUtil.toList(jsonArray,ShopType.class);
        //
        // // 2. 命中，返回redis中商铺类型信息
        // if (!CollectionUtils.isEmpty(typeList)) {
        //     return Result.ok(typeList);
        // }
        // // 3. 未命中，从数据库中查询商铺类型,并根据sort排序
        // List<ShopType> shopTypesByMysql = query().orderByAsc("sort").list();
        // //不存在，返回错误
        // if (shopTypesByMysql == null) {
        //     return Result.fail("分类不存在");
        // }
        // // 4. 将商铺类型存入到redis中
        // stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE,JSONUtil.toJsonStr(shopTypesByMysql));
        // // 5. 返回数据库中商铺类型信息
        // return Result.ok(shopTypesByMysql);

        // 1.从 Redis 中查询商铺缓存
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE);

        // 2.判断 Redis 中是否存在数据
        if (StrUtil.isNotBlank(shopTypeJson)) {
            // 2.1.存在，则返回
            List<ShopType> shopTypes = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(shopTypes);
        }
        // 2.2.Redis 中不存在，则从数据库中查询
        List<ShopType> shopTypes = query().orderByAsc("sort").list();

        // 3.判断数据库中是否存在
        if (shopTypes == null) {
            // 3.1.数据库中也不存在，则返回 false
            return Result.fail("分类不存在！");
        }
        // 3.2.数据库中存在，则将查询到的信息存入 Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE, JSONUtil.toJsonStr(shopTypes));
        // 3.3返回
        return Result.ok(shopTypes);
    }
}
