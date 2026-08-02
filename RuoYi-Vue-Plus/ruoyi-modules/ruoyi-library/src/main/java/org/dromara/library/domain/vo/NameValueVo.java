package org.dromara.library.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 名称-数值 通用对象（大屏图表用：饼/柱的一项）
 *
 * @author library
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private Long value;

}
