package com.wander.manifold.controller;

import com.wander.core.utils.JwtTokenUtil;
import com.wander.manifold.pojo.User;
import com.wander.manifold.service.IUserService;
import com.wander.core.utils.KemingCodeUtil;
import com.wander.core.utils.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/manifold")
public class UserController {

    //Redis操作Template
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //keming加密服务-密钥
    @Value("${keming.secretKey}")
    private String kmSecretKey;

    //用户服务层
    @Resource(name = "userService")
    private IUserService userService;

    //token服务
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    //日志
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 登录
     *
     * @param email
     * @param password
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<?> login(String email, String password) {
        User user = userService.queryByEmailPsw(email, password);
        if (user != null && user.getStatus() == 1) {
            String token = jwtTokenUtil.createJwt(user);
            log.info("用户" + email + "生成的token信息:{}", token);
            return new ResponseEntity<String>(token, HttpStatus.OK);
        } else if(user != null && user.getStatus() == 0) {
            return new ResponseEntity<Integer>(0, HttpStatus.OK);
        }else{
            return new ResponseEntity<Integer>(1, HttpStatus.OK);
        }
    }

    /**
     * 注册
     *
     * @param email
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/user")
    public ResponseEntity<?> register(String email, String username, String password) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setJoinTime(new Date());

        //生成随机数
        String userCode = String.valueOf((int) (Math.random() * 100000));
        //保存激活码到Redis，有效时间30分钟--测试阶段设置为3分钟
        ValueOperations<String, Object> value = redisTemplate.opsForValue();
        value.set(user.getEmail(), userCode, 3, TimeUnit.MINUTES);
        //user对象暂时保存到Redis，失效时间30分钟--测试阶段设置为3分钟
        value.set(user.getEmail() + "_info", user, 3, TimeUnit.MINUTES);

        String activationCode = KemingCodeUtil.encode(user.getEmail() + "#div#" + userCode, kmSecretKey);

        try {
            MailUtil.sendMail(user.getEmail(), activationCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<String>(user.getEmail(), HttpStatus.OK);
    }

}
