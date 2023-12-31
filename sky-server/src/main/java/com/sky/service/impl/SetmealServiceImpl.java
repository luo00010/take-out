package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.controller.admin.SetmealController;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
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

    @Autowired
    private DishMapper dishMapper;



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


    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }


    /**
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断是否启售
        for (Long id : ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE)  throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        for (Long setmealId :ids) {
            //删除套餐表的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品关联表的数据
            setmealDishMapper.deletById(setmealId);
        }
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByid(Long id) {
        //根据id查找套餐数据
        Setmeal setmeal = setmealMapper.getById(id);

        //根据套餐id查找套餐菜品关联数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        //组合
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return  setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    @AutoFill(OperationType.UPDATE)
    public void update(SetmealDTO setmealDTO) {
        //修改套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        //获取套餐id
        Long id = setmeal.getId();

        //修改套餐菜品关联表
        //根据套餐id删除原本套餐菜品关联表信息
        setmealDishMapper.deletById(id);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) setmealDish.setSetmealId(id);

        //插入套餐菜品表信息
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐启售或停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //启售套餐时，查看套餐内是否有菜品停售
        if (status == StatusConstant.ENABLE){
            List<Dish> dishes = dishMapper.getByStatusId(id);
            if (dishes!=null && dishes.size()>0){
                for (Dish dish : dishes) if (dish.getStatus()==StatusConstant.DISABLE) throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
