package com.mybank.account.utils;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UtilsDummy {
	static BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

	public static void main(String[] args){

		//jasypt
		/*textEncryptor.setPassword("mybank_password");
		System.out.println(textEncryptor.encrypt("root1234"));*/



		  //bcrypt
		 BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String
		  rawPassword = "user1"; String encodedPassword = encoder.encode(rawPassword);

		  System.out.println(encodedPassword);


	}
}
