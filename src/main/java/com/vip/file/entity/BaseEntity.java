package com.vip.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类：用于自动生成数据库表实体的公共字段
 *
 * @author wgb
 * @date 2020/3/26 11:47
 */
@Getter
@Setter
@Accessors(chain = true)
public class BaseEntity implements Serializable {
    /** 主键 */
    @ApiModelProperty(name = "主键",notes = "")
    private String id ;
    /** 创建人 */
    @ApiModelProperty(name = "创建人",notes = "")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy ;
    /** 更新人 */
    @ApiModelProperty(name = "更新人",notes = "")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy ;
    /**
     * 创建时间，插入数据时自动填充
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 修改时间，插入、更新数据时自动填充
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 删除状态：插入数据时自动填充
     */
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic
    private int isDelete;

}
