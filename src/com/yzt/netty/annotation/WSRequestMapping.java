package com.yzt.netty.annotation;

import java.lang.annotation.*;

/**
 * 用来映射 handlerAdapter
 * @Title:  WSRequestMapping     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:42:51   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WSRequestMapping {

    String uri() ;
}
