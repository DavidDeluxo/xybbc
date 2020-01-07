package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.AppVersionApi;
import com.xingyun.bbc.core.operate.api.ConfigApi;
import com.xingyun.bbc.core.operate.dto.AppVersionCondition;
import com.xingyun.bbc.core.operate.enums.VersionPlatformEnum;
import com.xingyun.bbc.core.operate.enums.VersionStatusEnum;
import com.xingyun.bbc.core.operate.enums.VersionUpdateConditionEnum;
import com.xingyun.bbc.core.operate.enums.VersionUpdateTypeEnum;
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
import java.util.Optional;
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
    public VersionVo getAppVersionInfo(Integer fplatform, String version) {
        if (!VersionPlatformEnum.IOS.getCode().equals(fplatform) && !VersionPlatformEnum.ANDROID.getCode().equals(fplatform)) {
            throw new BizException(MallExceptionCode.PARAM_ERROR);
        }
        String iosKey = MallRedisConstant.LASTEST_APP_VERSION_IOS + version;
        String androidKey = MallRedisConstant.LASTEST_APP_VERSION_ANDROID + version;
        if (VersionPlatformEnum.IOS.getCode().equals(fplatform)) {
            //若缓存存在，查询并返回
            if (redisHolder.exists(iosKey)) {
                return (VersionVo) redisHolder.getObject(iosKey);
            }
        } else {
            //若缓存存在，查询并返回
            if (redisHolder.exists(androidKey)) {
                return (VersionVo) redisHolder.getObject(androidKey);
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
                        AppVersion::getFconditionVersions,
                        AppVersion::getFid)
                .sortDesc(AppVersion::getFappVersionId);
        List<AppVersion> appVersions = ResultUtils.getData(appVersionApi.queryByCriteria(appVersionCriteria));
        if (CollectionUtils.isEmpty(appVersions)) {
            log.error("版本控制数据为空");
            return new VersionVo();
        }
        //默认取最新的版本配置
        AppVersion appVersion = appVersions.get(0);
        //将版本号转换成主键id
        Optional<AppVersion> versionOptional = appVersions.stream().filter(item -> item.getFversionNo().equals(version)).findFirst();
        Integer reqId = null;
        if (versionOptional.isPresent()) {
            reqId = versionOptional.get().getFid();
        }
        for (AppVersion item : appVersions) {
            if (VersionUpdateTypeEnum.SILENCE.getCode().equals(item.getFupdateType())) {
                continue;
            }
            if (VersionUpdateConditionEnum.ALL.getCode().equals(item.getFcondition())) {
                appVersion = item;
                break;
            }
            Set<Integer> ids = getRelationIds(fplatform, item.getFconditionVersions());
            boolean isContains = false;
            if (ids.contains(reqId)) {
                isContains = true;
            }
            if (VersionUpdateConditionEnum.APPOINT.getCode().equals(item.getFcondition()) && isContains) {
                appVersion = item;
                break;
            }
            if (VersionUpdateConditionEnum.REMOVE.getCode().equals(item.getFcondition()) && !isContains) {
                appVersion = item;
                break;
            }
        }

        //弹窗展示内容都是展示最新的那条的展示内容
        appVersion.setFcontent(appVersions.get(0).getFcontent());

        VersionVo vo = dozerHolder.convert(appVersion, VersionVo.class);
        if (VersionUpdateConditionEnum.ALL.getCode().equals(appVersion.getFcondition())) {
            vo.setFVersionNos(new ArrayList<>());
        } else {
            Set<Integer> ids = getRelationIds(fplatform, appVersion.getFconditionVersions());
            vo.setFVersionNos(appVersions.stream().filter(item -> ids.contains(item.getFid())).map(item -> item.getFversionNo()).collect(Collectors.toList()));
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
            redisHolder.set(iosKey, vo, TIMEOUT);
        } else {
            redisHolder.set(androidKey, vo, TIMEOUT);
        }
        return vo;
    }

    /**
     * 获取当前配置关联的版本控制id
     *
     * @param fplatform
     * @return
     */
    private Set<Integer> getRelationIds(Integer fplatform, String conditionVersions) {
        Set<Integer> ids;
        try {
            AppVersionCondition appVersionCondition = JacksonUtils.jsonTopojo(conditionVersions, AppVersionCondition.class);
            if (VersionPlatformEnum.IOS.getCode().equals(fplatform)) {
                ids = appVersionCondition.getIos();
            } else {
                ids = appVersionCondition.getAndroid();
            }
        } catch (Exception e) {
            throw new BizException(MallExceptionCode.JSON_PARSE_ERROR);
        }
        return ids;
    }
}
