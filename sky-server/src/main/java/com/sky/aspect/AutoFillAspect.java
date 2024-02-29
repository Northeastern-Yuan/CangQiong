package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.sky.constant.AutoFillConstant.*;

/*
* 自定义切面类，实现公共字段自动填充处理逻辑
* */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /*
    * 切入点
    * */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
        public  void autoFillPointCut(){}

    /*
     * 前置通知，在通知中进行公共字段的赋值
     * */
    @Before("autoFillPointCut()")
        public void  autoFill(JoinPoint joinPoint)  {
        log.info("开始进行公共字段的填充");

        //获得到当前被拦截的方法上的数据库操作类型
        MethodSignature signature= (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得注解对象
        OperationType operationType=autoFill.value();//获得数据库操作类型

        //获得当前被拦截的方法的参数——实体对象
        Object[] args= joinPoint.getArgs();
        if(args == null || args.length ==0){
            return;
        }
        Object entity=args[0];

        //根据不同的操作类型，为对应的属性通过反射来赋值
        if(operationType ==OperationType.INSERT){
            try {
                //为四个公共字段赋值
                Method setCreateTime =entity.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser =entity.getClass().getDeclaredMethod(SET_CREATE_USER, Long.class);
                Method setUpdateTime =entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser =entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,LocalDateTime.now());
                setCreateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType ==OperationType.UPDATE) {
            try {
                //为两个公共字段赋值
                Method setUpdateTime =entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser =entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }
}
