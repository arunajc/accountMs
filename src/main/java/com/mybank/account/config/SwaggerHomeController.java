package com.mybank.account.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SwaggerHomeController {
	
	@RequestMapping("/")
	public String index() {
		return "redirect:swagger-ui.html";
	}

}
