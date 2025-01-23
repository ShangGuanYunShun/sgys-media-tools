package com.zq.media.tools.dto.req.ttm;

import com.zq.media.tools.enums.TtmAction;
import com.zq.media.tools.enums.TtmScopeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * ttm 请求
 *
 * @author zhaoqiang
 * @version 1.0
 * @date 2024-12-23 17:17
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class TtmReqDTO {

    /**
     * The name of the action to trigger
     */
    private TtmAction action;

    /**
     * The scope for the action. This defines on which entries the action should be applied.
     * The optional parameter args can be used to fine-tune the scope (not available on all scope values).
     * Valid scope values depend on the action you trigger
     */
    private Scope scope;

    /**
     * Any extra arguments you may pass to the actions (optional - used by some actions)
     */
    private Map<String, String> args;

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class Scope {

        private TtmScopeName name;

        private List<String> args;
    }
}
