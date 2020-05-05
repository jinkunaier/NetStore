package com.kingsoft.netstore.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月19日
 */
@Controller
public class MainControl {

    @GetMapping("/")
    public String main() {
        return "redirect:/index.html";
    }

}
