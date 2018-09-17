package com.spring.nahong.swagger2.swaggerdoc;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    @ApiOperation(value = "查询用户名称", notes = "查询用户名")
    @ApiImplicitParam(name = "name", value = "用户名", dataType = "String", paramType = "path")
    @RequestMapping(value = "name", method = RequestMethod.GET)
    public String getName(@RequestParam("name") String name) {
        return "hi, my name is " + name;
    }
}
