package com.demo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.reggie.common.CustomException;
import com.demo.reggie.dto.SetmealDto;
import com.demo.reggie.entity.Setmeal;
import com.demo.reggie.entity.SetmealDish;
import com.demo.reggie.mapper.SetmealMapper;
import com.demo.reggie.service.SetmealDishService;
import com.demo.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作 setmeal，执行 insert 操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存套餐和菜品的关联信息，操作 setmeal_dish，执行 insert 操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        // select count(*) from setmeal where id in (1, 2, 3) and status = 1
        // 1.只有停售的套餐才能删除（需要先查询套餐状态，确定是否可以删除）
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if (count > 0)
            // 套餐状态为起售，不能删除，抛出一个业务异常
            throw new CustomException("套餐状态为起售，不能删除该套餐");
        
        // 2.如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        // delete from setmeal_dish where setmeal_id in (1, 2, 3)
        // 3.删除关系表中的数据 ---> setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
