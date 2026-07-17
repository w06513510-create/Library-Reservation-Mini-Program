package org.dromara.app.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.app.domain.AppUser;
import org.dromara.app.domain.bo.AppRegisterBody;
import org.dromara.app.domain.vo.AppUserVo;
import org.dromara.app.mapper.AppUserMapper;
import org.dromara.app.service.IAppUserService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * C端用户服务实现
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class AppUserServiceImpl implements IAppUserService {

    private final AppUserMapper baseMapper;

    @Override
    public AppUser getByPhone(String phone) {
        // 登录/注册发生在放行路径上，无租户上下文，须忽略租户过滤按手机号定位
        return TenantHelper.ignore(() ->
            baseMapper.selectOne(Wrappers.<AppUser>lambdaQuery().eq(AppUser::getPhone, phone)));
    }

    @Override
    public boolean existsByPhone(String phone) {
        Long count = TenantHelper.ignore(() ->
            baseMapper.selectCount(Wrappers.<AppUser>lambdaQuery().eq(AppUser::getPhone, phone)));
        return count != null && count > 0;
    }

    @Override
    public Long register(AppRegisterBody body) {
        AppUser u = new AppUser();
        u.setPhone(body.getPhone());
        u.setPassword(BCrypt.hashpw(body.getPassword()));
        String phone = body.getPhone();
        String nickname = StringUtils.isBlank(body.getNickname())
            ? "用户" + phone.substring(Math.max(0, phone.length() - 4))
            : body.getNickname().trim();
        u.setNickname(nickname);
        u.setGender(0);
        u.setStatus(0);
        u.setRegisterTime(new Date());
        // 无租户上下文，忽略租户；tenant_id 由 DDL 默认值 '000000' 兜底
        TenantHelper.ignore(() -> baseMapper.insert(u));
        return u.getId();
    }

    @Override
    public AppUserVo getVoById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public void updateAvatar(Long id, String url) {
        AppUser up = new AppUser();
        up.setId(id);
        up.setAvatar(url);
        baseMapper.updateById(up);
    }

    @Override
    public void updateNickname(Long id, String nickname) {
        AppUser up = new AppUser();
        up.setId(id);
        up.setNickname(nickname);
        baseMapper.updateById(up);
    }

    @Override
    public void touchLoginTime(Long id) {
        AppUser up = new AppUser();
        up.setId(id);
        up.setLastLoginTime(new Date());
        baseMapper.updateById(up);
    }

}
