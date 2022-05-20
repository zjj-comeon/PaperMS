package com.database.paperms.controller;

import cn.hutool.core.util.RandomUtil;
import com.database.paperms.entity.User;
import com.database.paperms.response.ResultData;
import com.database.paperms.response.ReturnCode;
import com.database.paperms.service.UserService;
import com.database.paperms.utils.MailUtil;
import com.database.paperms.utils.RedisUtil;
import com.database.paperms.utils.RegexUtil;
import io.swagger.annotations.*;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * ClassName: com.database.paperms.controller.UserController
 * Created by zjj
 * Date: 2022-05-05 15:24
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MailUtil mailUtil;
    @Resource
    private Pbkdf2PasswordEncoder passwordEncoder;

    private static final int EXPIRE_TIME = 300;
    private static final int CODE_LENGTH = 6;

    /**
     * 注册模块：
     *                                                                      --超时-> 验证码失效，redis内保存的信息消失
     *                                                                     |
     * 填写名字、邮箱（账号）、密码、重复密码 --> 发送验证码（前端传递给后端User信息） --
     *                                                                     |
     *                                                                     --未超时-> 允许多次验证，将验证码传递给后端，若成功，那么信息会录入数据库，失败则否
     */
    @PostMapping("/register")
    public ResultData register(@RequestBody User user){
        if(userService.accountExist(user.getUserAccount())){
            return ResultData.fail(ReturnCode.USED_EMAIL);
        }
        String code = RandomUtil.randomString(CODE_LENGTH);
        while(!redisUtil.setNx(code,user,EXPIRE_TIME)){
            code = RandomUtil.randomString(CODE_LENGTH);
        }
        String mail = user.getUserAccount();
        if(RegexUtil.checkEmail(mail)) {
            mailUtil.sendSimpleMail(user.getUserAccount(), code);
            return ResultData.success();
        }
        else{
            return ResultData.fail(ReturnCode.INVALID_EMAIL);
        }
    }

    @PutMapping("/verify/{code}") // verify/sdcvsa
    public ResultData verify(@PathVariable String code){
        User user = null;
        ResultData resultData = null;
        try {
            user = (User) redisUtil.get(code);
            if(user == null){
                resultData =  ResultData.fail(ReturnCode.CLIENT_AUTHENTICATION_FAILED);
            }
            else{
                userService.save(user);
                resultData =  ResultData.success(ReturnCode.RC100);
            }
        }catch(ClassCastException e){
            e.printStackTrace();
            resultData = ResultData.fail(ReturnCode.CLASS_CAST_ERROR);
        }
        finally {
            redisUtil.del(code);
        }
        return resultData;
    }

    @PostMapping ("/login") // login?a=xxx&b=xx.....
    public ResultData login(@RequestParam String account, @RequestParam String password){
        User user = userService.login(account);
        if(user != null){
            if(passwordEncoder.matches(password,user.getUserPassword())){
                return ResultData.success(user);
            }else {
                return ResultData.fail(ReturnCode.USERNAME_OR_PASSWORD_ERROR);
            }
        }else{
            return ResultData.fail(ReturnCode.USERNAME_OR_PASSWORD_ERROR);
        }
    }

}
