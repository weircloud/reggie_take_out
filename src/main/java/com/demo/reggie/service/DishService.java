package com.demo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.reggie.dto.DishDto;
import com.demo.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表 dish、 dish_flavor
    void saveWithFlavor(DishDto dishdto);
    
    // 根据id 查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long ids);
    
    // 更新菜品信息，同时还要更新对应的口味信息
    void updateWithFlavor(DishDto dishDto);
}
