package com.demo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.reggie.dto.DishDto;
import com.demo.reggie.entity.Dish;
import com.demo.reggie.entity.DishFlavor;
import com.demo.reggie.mapper.DishMapper;
import com.demo.reggie.service.DishFlavorService;
import com.demo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishdto
     */
    @Transactional  // 声明事务
    @Override
    public void saveWithFlavor(DishDto dishdto) {
        // 保存菜品基本信息到菜品表 dish
        this.save(dishdto);

        Long dishId = dishdto.getId();

        // 菜品口味
        List<DishFlavor> flavors = dishdto.getFlavors();
        flavors.stream().map((item) -> {    // 遍历
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        // 保存菜品口味数据到口味表 dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id 查询菜品信息和对应的口味信息
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long ids) {
        // 1. 查询菜品基本信息 dish表
        Dish dish = this.getById(ids);

        DishDto dishDto = new DishDto();

        // 对象拷贝
        BeanUtils.copyProperties(dish, dishDto);
        
        // 2. 查询菜品的口味 dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 修改菜品信息
     * @param dishDto
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish 表基本信息
        this.updateById(dishDto);
        
        // 先删除当前菜品对应口味数据  dish_flavor 的delete 操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        
        // 添加当前提交过来的口味数据 dish 表的insert 操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map((item) -> {    // 遍历
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        
        dishFlavorService.saveBatch(flavors);
    }
}
