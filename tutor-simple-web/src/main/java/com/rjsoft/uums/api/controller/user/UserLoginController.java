package com.rjsoft.uums.api.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.rjsoft.common.utils.DateUtil;
import com.rjsoft.common.utils.NetworkUtil;
import com.rjsoft.common.vo.ViewerResult;
import com.rjsoft.uums.api.controller.vo.user.LoginUserVO;
import com.rjsoft.uums.facade.jwt.service.UmsJwtFacade;
import com.rjsoft.uums.facade.log.entity.UmsLog;
import com.rjsoft.uums.facade.log.enums.LogLevelEnum;
import com.rjsoft.uums.facade.log.enums.LogTypeEnum;
import com.rjsoft.uums.facade.log.enums.OpResultEnum;
import com.rjsoft.uums.facade.log.service.UmsLogFacade;
import com.rjsoft.uums.facade.user.entity.UmsUser;
import com.rjsoft.uums.facade.user.service.UmsUserFacade;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * user api
 *
 * @author feichongzheng
 */
@RestController
@RequestMapping("/api/user/login")
public class UserLoginController {

    @Resource
    private UmsUserFacade umsUserFacade;

    @Resource
    private UmsJwtFacade umsJwtFacade;

    @Resource
    private UmsLogFacade umsLogFacade;

    /**
     * login
     *
     * @param obj
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ViewerResult login(@RequestBody JSONObject obj, HttpServletRequest request) {
        ViewerResult result = new ViewerResult();
        UmsUser user = null;
        String appSn = null;
        String username = null;
        String password = null;
        boolean remember = false;
        String jwt = null;
        LocalDateTime opTime = DateUtil.utilDateToLocalDateTime(new Date());
        long startTime = System.currentTimeMillis();
        try {
            appSn = obj.getString("appSn");
            username = obj.getString("username");
            password = obj.getString("password");
            remember = obj.getBooleanValue("remember");
            user = umsUserFacade.login(username, password);
            LoginUserVO userVO = new LoginUserVO();
            userVO.convertPOToVO(user);
            jwt = umsJwtFacade.createJwt(user.getUsername(), remember);
            userVO.setToken(jwt);
            result.setSuccess(true);
            result.setData(userVO);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMessage(e.getMessage());
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        try {
            log(appSn, username, remember, endTime - startTime, opTime, jwt, result.isSuccess(), request);
        } catch (Exception e) {
        }
        return result;
    }

    private void log(String appSn, String username, boolean remember,
                     long execTime, LocalDateTime opTime, String jwt, boolean success, HttpServletRequest request) {
        try {
            UmsLog umsLog = new UmsLog();
            umsLog.setAppSn(appSn);
            umsLog.setBackEndAccessPath("/uums/api/user/login");
            umsLog.setBrowser(NetworkUtil.getBrowser(request));
            umsLog.setExecTime(execTime);
            umsLog.setIp(NetworkUtil.getIpAddress(request));
            umsLog.setOpResource("用户登录");
            umsLog.setLogLevel(LogLevelEnum.NORMAL.getValue());
            umsLog.setLogType(LogTypeEnum.LOGIN.getValue());
            String desc = "用户登录：用户名【" + username + "】、记住密码【" + (remember ? "是" : "否") + "】";
            umsLog.setOpDesc(desc);
            umsLog.setOpResult(success ? OpResultEnum.SUCCESS.getValue() : OpResultEnum.FAIl.getValue());
            umsLog.setOpSystem(NetworkUtil.getOS(request));
            umsLog.setOpTime(opTime);
            umsLog.setUsername(username);
            umsLogFacade.save(umsLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}