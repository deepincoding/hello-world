package com.example.demo.BO;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;


/**
 * @Description
 * @Author changda
 * @Date 2021-02-07
 */

@Data
public class HHHConf {


    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Timestamp updateDate;

    /**
     * 业务状态(0-待生效；1-生效中；2-已失效)
     */
    private Integer status;

    /**
     * 状态（1-正常；0-删除）
     */
    private Integer dataStatus;


}
