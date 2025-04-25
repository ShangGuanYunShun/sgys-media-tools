package com.zq.media.tools.dto.resp.driver115;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zq.media.tools.enums.BehaviorType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 表示 life_list 接口的响应结构
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-11-12 14:07
 */
@Getter
@Setter
@ToString
public class LifeListRespDTO {

    /**
     * 请求状态
     */
    private Boolean state;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 包含实际事件列表数据的对象
     */
    private LifeListDataDTO data;

    /**
     * LifeListDataDTO 表示 life_list 数据字段的结构。
     */
    @Getter
    @Setter
    @ToString
    public static class LifeListDataDTO {

        /**
         * 事件数量
         */
        private Integer count;

        /**
         * 事件行为列表
         */
        private List<BehaviorDTO> list;

        /**
         * 上一条数据，主要用于分页请求
         */
        @JsonProperty("last_data")
        private String lastData;
    }

    /**
     * BehaviorDTO 表示单个行为事件的结构。
     */
    @Getter
    @Setter
    @ToString
    public static class BehaviorDTO {

        /**
         * 总条目数
         */
        private Integer total;

        /**
         * 更新时间
         */
        @JsonProperty("update_time")
        private Long updateTime;

        /**
         * 行为类型
         */
        @JsonProperty("behavior_type")
        private BehaviorType behaviorType;

        /**
         * 日期
         */
        private String date;

        /**
         * 行为条目列表
         */
        private List<BehaviorDetailRespDTO> items;
    }

}
