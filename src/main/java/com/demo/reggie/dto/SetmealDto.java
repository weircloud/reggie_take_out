package com.demo.reggie.dto;

import com.demo.reggie.entity.Setmeal;
import com.demo.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
