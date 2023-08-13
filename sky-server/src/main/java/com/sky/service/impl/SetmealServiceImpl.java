package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;



    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //复制套餐属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //插入套餐数据
        setmealMapper.insert(setmeal);

        //回显套餐id
        Long id = setmeal.getId();

        //插入套餐菜品关联表
        //给所有套餐菜品属性的套餐id赋值
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()){
            setmealDish.setSetmealId(id);
        }
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //保存套餐菜品表关联信息
        setmealDishMapper.insertBatch(setmealDishes);



    }
}
