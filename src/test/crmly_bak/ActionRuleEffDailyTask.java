package com.asiainfo.crmly.schedule.config.batch.task;

import com.alibaba.fastjson.JSONObject;
import com.asiainfo.common.base.constant.ScoreConsts;
import com.asiainfo.common.base.util.DateStyle;
import com.asiainfo.common.base.util.DateUtil;
import com.asiainfo.common.base.util.StringUtil;
import com.asiainfo.crmly.schedule.mapper.LyActRuleMapper;
import com.asiainfo.crmly.schedule.model.LyActRule;
import com.asiainfo.crmly.system.redis.RedisClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Auther: changda
 * @Date: 2021/03/04/16:18
 * @Description:
 */

@Component
@JobHandler("actionRuleEffDailyTask")
public class ActionRuleEffDailyTask extends IJobHandler {
    Logger logger = LoggerFactory.getLogger(ActionRuleEffDailyTask.class);

    @Autowired
    LyActRuleMapper lyActRuleMapper;

    @Resource(name = "redisClient")
    private RedisClient redisClient;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        List<Long> errActRuleId = new ArrayList<Long>();
        String errorMsg;
        int failCount = 0;
        Date endDay;
        if (!StringUtil.isEmpty(s)) {
            endDay = DateUtil.parseDate(s, DateStyle.YYYYMMDD.getValue());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            endDay = calendar.getTime();
        }

        Date beginDay = new Date(endDay.getTime() - 24 * 60 * 60 * 1000);

        logger.info(" ------------------------------  EffConfChangeMemberLevelTask JOB START !  -----------------------------------------");
        logger.info("-------  每日会员行为规则调度  --------" + "输入参数为" + s);
        long beginTime = System.currentTimeMillis();
        try {
            //先进行失效调度任务
            Date currentDate = new Date();

            LyActRule expLyA = new LyActRule();
            expLyA.setStatus(5);
            expLyA.setUpdateDate(currentDate);
            logger.info("开始时间"+beginDay+"结束时间"+endDay+"行为规则失效");
            Example ex2 = new Example(LyActRule.class);
            ex2.createCriteria().andBetween("expDate", beginDay, endDay).andEqualTo("status", 4).andEqualTo("dataStatus", 1);
            int i = lyActRuleMapper.updateByExampleSelective(expLyA, ex2);


            
            //根据生效时间和状态查询今日需要生效的规则
            Example example = new Example(LyActRule.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andBetween("effDate", beginDay, endDay).andEqualTo("status", 2).andEqualTo("dataStatus", 1);
            List<LyActRule> lyActRuleList = lyActRuleMapper.selectByExample(example);
            logger.info("今日需要生效的数据"+lyActRuleList);
            for (LyActRule lyActRule : lyActRuleList) {
                //失效同一渠道同一行为ID的原有规则
                LyActRule lyA = new LyActRule();
                lyA.setStatus(5);
                lyA.setUpdateDate(currentDate);

                logger.info("更新数据"+lyA);

                Example ex = new Example(LyActRule.class);
                Example.Criteria cr = ex.createCriteria();
                cr.andEqualTo("fsysId", lyActRule.getFsysId()).andEqualTo("actId", lyActRule.getActId()).andEqualTo("status", 4).andEqualTo("dataStatus", 1);
                List<LyActRule> lyActRules = lyActRuleMapper.selectByExample(ex);
                if (!lyActRules.isEmpty()) {
                    //失效原有规则
                    int a = lyActRuleMapper.updateByExampleSelective(lyA, ex);
                    if (a == 0) {
                        logger.error("未能失效原有规则，规则id=" + lyActRule.getActRuleId() + "渠道id=" + lyActRule.getFsysId() + "行为id=" + lyActRule.getActId());
                        errActRuleId.add(lyActRule.getActRuleId());
                        failCount++;
                    } else {
                        //生效新规则
                        LyActRule lyEff = new LyActRule();
                        lyEff.setStatus(4);
                        lyEff.setUpdateDate(currentDate);
                        Example ex1 = new Example(LyActRule.class);
                        Example.Criteria cr1 = ex1.createCriteria();
                        cr1.andEqualTo("actRuleId", lyActRule.getActRuleId());
                        int b = lyActRuleMapper.updateByExampleSelective(lyEff, ex1);
                        if (b == 0) {
                            logger.error("未能生效新规则，规则id=" + lyActRule.getActRuleId() + "渠道id=" + lyActRule.getFsysId() + "行为id=" + lyActRule.getActId());
                            errActRuleId.add(lyActRule.getActRuleId());
                            failCount++;
                        }
                    }
                } else {
                    //生效新规则
                    LyActRule lyEff = new LyActRule();
                    lyEff.setStatus(4);
                    lyEff.setUpdateDate(currentDate);
                    Example ex1 = new Example(LyActRule.class);
                    Example.Criteria cr1 = ex1.createCriteria();
                    cr1.andEqualTo("actRuleId", lyActRule.getActRuleId());
                    int b = lyActRuleMapper.updateByExampleSelective(lyEff, ex1);
                    if (b == 0) {
                        logger.error("未能生效新规则，规则id=" + lyActRule.getActRuleId() + "渠道id=" + lyActRule.getFsysId() + "行为id=" + lyActRule.getActId());
                        errActRuleId.add(lyActRule.getActRuleId());
                        failCount++;
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return FAIL;
        }

        long endTime = System.currentTimeMillis();
        double costTime = (double) (endTime - beginTime) / 1000;
        logger.info(" ---  ActionRuleEffDailyTask 耗时【" + costTime + "】秒 ---");

        if (failCount > 0) {
            logger.error("失败条数" + failCount);
            redisClient.set(endDay + "", JSONObject.toJSONString(errActRuleId), ScoreConsts.RedisScene.DAILY_FAIL_ACT_RULE);
            return FAIL;
        } else {
            return SUCCESS;
        }

    }
}
