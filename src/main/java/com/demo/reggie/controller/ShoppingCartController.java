package com.demo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.reggie.common.BaseContext;
import com.demo.reggie.common.R;
import com.demo.reggie.entity.ShoppingCart;
import com.demo.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据：{}", shoppingCart);
        
        // 设置用户 id ，指定当前购物车数据是哪个用户的
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();
        
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        // 判断添加到购物车中的是菜品还是套餐
        if(dishId != null) 
            // 添加到购物车当中的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        else
            // 添加到购物车当中的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        // 查询当前菜品或者套餐是否在购物车中
        // select * from shopping_cart where user_id = ? and dish_id/setmeal_id == ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        // 如果已经存在，就在原来数量基础上加一
        if (cartServiceOne != null) {
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }
    
    /**
     * 删除购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("删除购物车数据：{}", shoppingCart);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        // 判断要删除的购物车中的是菜品还是套餐
        if(dishId != null)
            // 要删除的购物车当中的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        else
            // 要删除的购物车当中的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        // 查询当前菜品或者套餐在购物车中的数量
        // select * from shopping_cart where user_id = ? and dish_id/setmeal_id == ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        
        // 获得数量
        Integer number = cartServiceOne.getNumber();

        // 将数量-1
        cartServiceOne.setNumber(number - 1);
        shoppingCartService.updateById(cartServiceOne);
        
        // 判断当前购物车中菜品/套餐数量是否小于1
        if (number <= 1) {
            shoppingCartService.remove(queryWrapper);
        }
        
        return R.success("删除成功！");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        // delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功！");
    }
}
