package sto.common.util;

import java.util.Random;

public class VerifyCodeUtil2 {
	
	public static String getRandomVerifyCode(){
		String sRand = "";
		Random random = new Random();
		for (int i=0;i<6;i++){    
			String rand=String.valueOf(random.nextInt(10));    
			sRand+=rand;
		}
		return sRand;
	}
	
	public static String getRandomVerifyCodeBySize(int size){
		String sRand = "";
		Random random = new Random();
		for (int i=0;i<size;i++){    
			String rand=String.valueOf(random.nextInt(10));    
			sRand+=rand;
		}
		return sRand;
	}
	
}
