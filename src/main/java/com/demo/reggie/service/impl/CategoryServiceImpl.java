package com.demo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.reggie.common.CustomException;
import com.demo.reggie.entity.Category;
import com.demo.reggie.entity.Dish;
import com.demo.reggie.entity.Setmeal;
import com.demo.reggie.mapper.CategoryMapper;
import com.demo.reggie.service.CategoryService;
import com.demo.reggie.service.DishService;
import com.demo.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id 删除分类，删除之前需要进行判断
     *
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类 id 进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        // 查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if (dishCount > 0)
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类项关联了菜品，不能删除");

        // 查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件，根据分类 id 进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);

        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);

        // 查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        if (setmealCount > 0)
            // 已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类项关联了套餐，不能删除");

        // 正常删除分类
        super.removeById(ids);
    }
}
