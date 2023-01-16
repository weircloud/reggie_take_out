package com.demo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.reggie.common.R;
import com.demo.reggie.entity.User;
import com.demo.reggie.service.UserService;
import com.demo.reggie.utils.SMSUtils;
import com.demo.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 1. 获取手机号
        String phone = user.getPhone();
        
        // 2. 生成随机4位验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code={}", code);
        
        // 3. 调用阿里云提供的短信服务API发送短信
        //SMSUtils.sendMessage("瑞吉外卖", "", phone, code);
        
        // 4. 将验证码保存到 Session
        session.setAttribute(phone, code);
        return R.success("手机验证码发送成功");
    }

    /**
     * 移动用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();

        // 从 session 获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        
        if (codeInSession == null || !(code.equals(codeInSession)))
            return R.error("登陆失败");

        // 验证码比对（页面提交的验证码和 session 中保存的验证码）
        // 如果能够比对成功，说明登陆成功
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);

        User user = userService.getOne(queryWrapper);
        
        // 判断当前手机号是否在用户表，新用户自动完成注册
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }
        session.setAttribute("user", user.getId());
        return R.success(user);
    }
}
