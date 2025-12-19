package com.eggmoney.payv.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.eggmoney.payv.infrastructure.mybatis.mapper.TimeMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final TimeMapper timeMapper;
	
	@GetMapping("/")
    public String home(Model model) {
        System.out.println("hello");
        model.addAttribute("msg", "Payv 가동 OK");
        model.addAttribute("time", timeMapper.now());
        return "home";
    }
}
