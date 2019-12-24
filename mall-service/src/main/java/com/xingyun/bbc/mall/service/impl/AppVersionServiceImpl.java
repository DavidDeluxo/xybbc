package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.AppVersionApi;
import com.xingyun.bbc.core.operate.api.ConfigApi;
import com.xingyun.bbc.core.operate.dto.AppVersionCondition;
import com.xingyun.bbc.core.operate.enums.VersionPlatformEnum;
import com.xingyun.bbc.core.operate.enums.VersionStatusEnum;
import com.xingyun.bbc.core.operate.enums.VersionUpdateConditionEnum;
import com.xingyun.bbc.core.operate.po.AppVersion;
import com.xingyun.bbc.core.operate.po.Config;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.JacksonUtils;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.RedisHolder;
import com.xingyun.bbc.mall.common.constans.MallRedisConstant;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.vo.VersionConfigVo;
import com.xingyun.bbc.mall.model.vo.VersionVo;
import com.xingyun.bbc.mall.service.AppVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppVersionServiceImpl implements AppVersionService {

    /**
     * 一天 86400秒
     */
    private static final Long TIMEOUT = 86400L;

    /**
     * APP版本更新配置
     */
    private static final String APP_UPDATE_CONFIG = "APP_UPDATE_CONFIG";

    @Resource
    private AppVersionApi appVersionApi;

    @Resource
    private ConfigApi configApi;

    @Autowired
    private DozerHolder dozerHolder;

    @Autowired
    private RedisHolder redisHolder;

    @Override
    public VersionVo getAppVersionInfo(Integer fplatform) {
        if (!VersionPlatformEnum.IOS.getCode().equals(fplatform) && !VersionPlatformEnum.ANDROID.getCode().equals(fplatform)) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        if (VersionPlatformEnum.IOS.getCode().equals(fplatform)) {
            //若缓存存在，查询并返回
            if (redisHolder.exists(MallRedisConstant.LASTEST_APP_VERSION_IOS)) {
                return (VersionVo) redisHolder.getObject(MallRedisConstant.LASTEST_APP_VERSION_IOS);
            }
        } else {
            //若缓存存在，查询并返回
            if (redisHolder.exists(MallRedisConstant.LASTEST_APP_VERSION_ANDROID)) {
                return (VersionVo) redisHolder.getObject(MallRedisConstant.LASTEST_APP_VERSION_ANDROID);
            }
        }
        log.info("APP版本信息缓存失效，查询数据库");
        List<Integer> platforms = new ArrayList<>();
        platforms.add(fplatform);
        platforms.add(VersionPlatformEnum.ALL.getCode());
        Criteria<AppVersion, Object> appVersionCriteria = Criteria.of(AppVersion.class)
                .andIn(AppVersion::getFplatform, platforms)
                .andEqualTo(AppVersion::getFstatus, VersionStatusEnum.AVAILABLE.getCode())
                .fields(AppVersion::getFappVersionId,
                        AppVersion::getFplatform,
                        AppVersion::getFversionNo,
                        AppVersion::getFupdateType,
                        AppVersion::getFcontent,
                        AppVersion::getFcondition,
                        AppVersion::getFconditionVersions)
                .sortDesc(AppVersion::getFappVersionId);
        AppVersion appVersion = ResultUtils.getData(appVersionApi.queryOneByCriteria(appVersionCriteria));
        VersionVo vo = dozerHolder.convert(appVersion, VersionVo.class);
        if (VersionUpdateConditionEnum.ALL.getCode().equals(appVersion.getFcondition())) {
            vo.setFVersionNos(new ArrayList<>());
        } else {
            Set<Integer> ids;
            try {
                AppVersionCondition appVersionCondition = JacksonUtils.jsonTopojo(appVersion.getFconditionVersions(), AppVersionCondition.class);
                if (VersionPlatformEnum.IOS.getCode().equals(fplatform)) {
                    ids = appVersionCondition.getIos();
                } else {
                    ids = appVersionCondition.getAndroid();
                }
            } catch (Exception e) {
                throw new BizException(MallExceptionCode.BALANCE_NOT_ENOUGH);
            }
            Criteria<AppVersion, Object> condition = Criteria.of(AppVersion.class)
                    .fields(AppVersion::getFversionNo)
                    .andIn(AppVersion::getFid, ids)
                    .sortDesc(AppVersion::getFmodifyTime);
            List<AppVersion> appVersions = ResultUtils.getData(appVersionApi.queryByCriteria(condition));
            if (CollectionUtils.isEmpty(appVersions)) {
                throw new BizException(MallExceptionCode.RELATION_VERSION_IS_EMPTY);
            }
            vo.setFVersionNos(appVersions.stream().map(item -> item.getFversionNo()).collect(Collectors.toList()));
        }
        Criteria<Config, Object> criteria = Criteria.of(Config.class)
                .andEqualTo(Config::getFkey, APP_UPDATE_CONFIG)
                .fields(Config::getFvalue);
        Config config = ResultUtils.getDataNotNull(configApi.queryOneByCriteria(criteria));
        try {
            VersionConfigVo versionConfigVo = JacksonUtils.jsonTopojo(config.getFvalue(), VersionConfigVo.class);
            vo.setMinVersion(versionConfigVo.getMinVersion());
            vo.setMinVersionUpdateType(versionConfigVo.getType());
        } catch (Exception e) {
            log.error("APP版本更新配置查询json转换出错", e);
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        if (VersionPlatformEnum.IOS.getCode().equals(fplatform)) {
            redisHolder.set(MallRedisConstant.LASTEST_APP_VERSION_IOS, vo, TIMEOUT);
        } else {
            redisHolder.set(MallRedisConstant.LASTEST_APP_VERSION_ANDROID, vo, TIMEOUT);
        }
        return vo;
    }
}
