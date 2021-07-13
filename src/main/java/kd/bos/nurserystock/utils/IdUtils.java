package kd.bos.nurserystock.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * ID生成器工具类
 * 
 *
 */
public class IdUtils {
	/**
	 * 获取随机UUID
	 * 
	 * @return 随机UUID
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 简化的UUID，去掉了横线
	 * 
	 * @return 简化的UUID，去掉了横线
	 */
	public static String simpleUUID() {
		return UUID.randomUUID().toString(true);
	}

	/**
	 * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
	 * 
	 * @return 随机UUID
	 */
	public static String fastUUID() {
		return UUID.fastUUID().toString();
	}

	/**
	 * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
	 * 
	 * @return 简化的UUID，去掉了横线
	 */
	public static String fastSimpleUUID() {
		return UUID.fastUUID().toString(true);
	}

	/**
	 * 生成24位不重复的随机数，含数字+大小写
	 * 
	 * @return
	 */
	public static String getGUID() {
		StringBuilder uid = new StringBuilder();
		// 产生24位的强随机数
		Random rd = new SecureRandom();
		for (int i = 0; i < 24; i++) {
			// 产生0-2的3位随机数
			int type = rd.nextInt(2);
			switch (type) {
			case 0:
				// 0-9的随机数
				uid.append(rd.nextInt(10));
				/*
				 * int random = ThreadLocalRandom.current().ints(0, 10)
				 * .distinct().limit(1).findFirst().getAsInt();
				 */
				break;
			case 1:
				// ASCII在65-90之间为大写,获取大写随机
				uid.append((char) (rd.nextInt(6) + 65));
				break;
			// case 2:
			// // ASCII在97-122之间为小写，获取小写随机
			// uid.append((char) (rd.nextInt(25) + 97));
			// break;
			default:
				break;
			}
		}
		return uid.toString();
	}

	public static String getRandomValue(int numSize) {
		String str = "";
		for (int i = 0; i < numSize; i++) {
			char temp = 0;
			int key = (int) (Math.random() * 2);
			switch (key) {
			case 0:
				temp = (char) (Math.random() * 10 + 48);// 产生随机数字
				break;
			case 1:
				temp = (char) (Math.random() * 6 + 'A');// 产生a-f
				break;
			default:
				break;
			}
			str = str + temp;
		}
		return str;
	}
}
