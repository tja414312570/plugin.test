package com.yanan.utils.beans.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个工具用于提供字符串相关操作
 * @author yanan
 *
 */
public class StringUtil {
	public static int maxTimes = 10;
	//资源缓存
	private static Map<Integer, Boolean> resourceCache = new HashMap<Integer, Boolean>();
	//token缓存，用于存储token性质的变量
	private static Map<String, List<Token>> tokenCache = new HashMap<String, List<Token>>();

	/**
	 * 将集合中的变量填充到字符串中的表达式中
	 * 例如 'hello ${name}, my age is ${age};' + {age=15,name="plu"}  ==》 'hello plu, my age is 15';
	 * @param str express
	 * @param obj value
	 * @return 替换后的内容
	 */
	public static String decodeVar(String str, Map<?,?> obj) {
		Pattern var = Pattern.compile("\\$\\{(\\w|_)+\\}");
		Pattern reVar = Pattern.compile("\\$\\{|\\}");
		Matcher m = var.matcher(str);
		int i = 0;
		while (m.find()) {
			i++;
			String result = m.group();
			Matcher mA = reVar.matcher(result);
			String field = mA.replaceAll("");
			result = (String) obj.get(field);
			str = str.substring(0, m.start()) + result + str.substring(m.end());
			m = var.matcher(str);
			if (i > maxTimes)
				break;
		}
		return str;
	}

	public static int getMaxTimes() {
		return maxTimes;
	}

	public static void setMaxTimes(int maxTimes) {
		StringUtil.maxTimes = maxTimes;
	}
	/**
	 * 将字符串复制n份
	 * @param size 复制的份数
	 * @param str 要复制的内容
	 * @return 处理后的字符
	 */
	public static String multi(int size, String str) {
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < size; i++) {
			temp.append(str);
		}
		return temp.toString();
	}

	// 问号匹配
	private static boolean QTMark(String src, String regex) {
		if (src.length() != regex.length())
			return false;
		for (int i = 0; i < regex.length(); i++)
			if (src.charAt(i) != regex.charAt(i) && regex.charAt(i) != '?' && src.charAt(i) != '/')
				return false;
		return true;
	}

	/**
	 * 任意字符匹配,使用缓存，建议当匹配内容很少变化 与 表达式 很少变化时使用，否则一定要使用match
	 * 
	 * @param src 要匹配的字符
	 * @param regex 要匹配的表达式
	 * @return 是否匹配
	 */
	public static boolean matchAndCache(String src, String regex) {
		int hash = hash(src, regex);
		Boolean b = resourceCache.get(hash);
		if (b != null)
			return b;
		b = match(src, regex);
		resourceCache.put(hash, b);
		return b;
	}

	/**
	 * 任意字符匹配,不使用缓存
	 * 
	 * @param src 要匹配的字符
	 * @param regex 要匹配的表达式
	 * @return 是否匹配
	 */
	public static boolean match(String src, String regex) {
		// 对*? 或 *? 进行处理，默认替换为*
		// 使用while循环处理 ***???或???***格式等错误格式 该格式导致程序异常
		while (regex.contains("*?"))
			regex = regex.replace("*?", "?");
		while (regex.contains("?*"))
			regex = regex.replace("?*", "*");
		int SMI = regex.indexOf("*");
		int QMI = regex.indexOf("?");
		// 如果没有星号和问号 则为精确匹配
		if (SMI == -1 && QMI == -1) {
			if (regex.length() != src.length())
				return false;
			return regex.equals(src);
		}
		// 如果有问号没有星号 交给星号处理部分
		if (SMI == -1 && QMI != -1)
			return QTMark(src, regex);
		// 如果只有星号没有问号 交给问号处理部分
		if (SMI != -1 && QMI == -1)
			return STMark(src, regex);
		// 特殊位置处理 格式 *.*.?.?格式
		// 如果?在前面
		if (SMI < QMI) {
			// 星号特殊位置处理
			// 星号的位置不为00
			if (SMI != 0) {
				String REGTemp = regex.substring(0, SMI);
				String SRCTemp = src.substring(0, SMI);
				if (!REGTemp.equals(SRCTemp))
					return false;
			}
			// 连着多个星号*.*x*.?格式
			String stmp = regex.substring(0, QMI);
			if (stmp.indexOf("*", SMI + 1) != -1) {
				int lastSMI = stmp.lastIndexOf("*");
				String stmpREG = stmp.substring(0, lastSMI + 1);
				String spmpREG = stmp.substring(lastSMI + 1, QMI);
				if (!src.contains(spmpREG))
					return false;
				int srcSpIndex = src.indexOf(spmpREG);
				int regSpIndex = regex.indexOf(spmpREG);
				String stmpSRC = src.substring(0, srcSpIndex);
				if (!STMark(stmpSRC, stmpREG))
					return false;
				String rstREG = regex.substring(regSpIndex + spmpREG.length());
				String rstSRC = src.substring(srcSpIndex + spmpREG.length());
				return match(rstSRC, rstREG);
			}
			String temp = regex.substring(SMI + 1, QMI);
			int RTIndex = regex.indexOf(temp);
			int STIndex = src.indexOf(temp);
			if (src.contains(temp)) {
				return match(src.substring(STIndex + temp.length(), src.length()),
						regex.substring(RTIndex + temp.length(), regex.length()));
			}
			return false;
		} else {
			String qtmp = regex.substring(0, SMI);
			// 连着的问号处理
			if (qtmp.indexOf("?", QMI + 1) != -1) {
				int lastQMI = qtmp.lastIndexOf("?");
				String qtmpREG = qtmp.substring(0, lastQMI + 1);
				String qpmpREG = qtmp.substring(lastQMI + 1, SMI);
				if (!src.contains(qpmpREG))
					return false;
				int srcSpIndex = src.indexOf(qpmpREG);
				int regSpIndex = regex.indexOf(qpmpREG);
				String qtmpSRC = src.substring(0, srcSpIndex);
				if (!QTMark(qtmpSRC, qtmpREG))
					return false;
				String rstREG = regex.substring(regSpIndex + qpmpREG.length());
				String rstSRC = src.substring(srcSpIndex + qpmpREG.length());
				return match(rstSRC, rstREG);
			}
			String temp = regex.substring(QMI + 1, SMI);
			int RTIndex = regex.indexOf(temp);
			int STIndex = src.indexOf(temp);
			// 问号特殊处理,需要判断索引位置是否相同
			if (RTIndex != STIndex)
				return false;
			if (src.contains(temp)) {
				return match(src.substring(STIndex + temp.length(), src.length()),
						regex.substring(RTIndex + temp.length(), regex.length()));
			}
			return false;
		}
	}

	/**
	 * 任意字符匹配,不使用缓存
	 * 
	 * @param src 要匹配的字符
	 * @param regex 要匹配的表达式
	 * @return 是否匹配
	 */
	public static boolean matchURI(String src, String regex) {
		// 对*? 或 *? 进行处理，默认替换为*
		// 使用while循环处理 ***???或???***格式等错误格式 该格式导致程序异常
		while (regex.contains("*?"))
			regex = regex.replace("*?", "?");
		while (regex.contains("?*"))
			regex = regex.replace("?*", "*");
		int SMI = regex.indexOf("*");
		int QMI = regex.indexOf("?");
		// 如果没有星号和问号 则为精确匹配
		if (SMI == -1 && QMI == -1) {
			if (regex.length() != src.length())
				return false;
			return regex.equals(src);
		}
		// 如果有问号没有星号 交给星号处理部分
		if (SMI == -1 && QMI != -1)
			return QTMark(src, regex);
		// 如果只有星号没有问号 交给问号处理部分
		if (SMI != -1 && QMI == -1)
			return STMarkURI(src, regex);
		// 特殊位置处理 格式 *.*.?.?格式
		// 如果?在前面
		if (SMI < QMI) {
			// 星号特殊位置处理
			// 星号的位置不为00
			if (SMI != 0) {
				String REGTemp = regex.substring(0, SMI);
				String SRCTemp = src.substring(0, SMI);
				if (!REGTemp.equals(SRCTemp))
					return false;
			}
			// 连着多个星号*.*x*.?格式
			String stmp = regex.substring(0, QMI);
			if (stmp.indexOf("*", SMI + 1) != -1) {
				int lastSMI = stmp.lastIndexOf("*");
				String stmpREG = stmp.substring(0, lastSMI + 1);
				String spmpREG = stmp.substring(lastSMI + 1, QMI);
				if (!src.contains(spmpREG))
					return false;
				int srcSpIndex = src.indexOf(spmpREG);
				int regSpIndex = regex.indexOf(spmpREG);
				String stmpSRC = src.substring(0, srcSpIndex);
				if (!STMarkURI(stmpSRC, stmpREG))
					return false;
				String rstREG = regex.substring(regSpIndex + spmpREG.length());
				String rstSRC = src.substring(srcSpIndex + spmpREG.length());
				return match(rstSRC, rstREG);
			}
			String temp = regex.substring(SMI + 1, QMI);
			int RTIndex = regex.indexOf(temp);
			int STIndex = src.indexOf(temp);
			if (src.contains(temp))
				return match(src.substring(STIndex + temp.length(), src.length()),
						regex.substring(RTIndex + temp.length(), regex.length()));
			return false;
		} else {
			String qtmp = regex.substring(0, SMI);
			// 连着的问号处理
			if (qtmp.indexOf("?", QMI + 1) != -1) {
				int lastQMI = qtmp.lastIndexOf("?");
				String qtmpREG = qtmp.substring(0, lastQMI + 1);
				String qpmpREG = qtmp.substring(lastQMI + 1, SMI);
				if (!src.contains(qpmpREG))
					return false;
				int srcSpIndex = src.indexOf(qpmpREG);
				int regSpIndex = regex.indexOf(qpmpREG);
				String qtmpSRC = src.substring(0, srcSpIndex);
				if (!QTMark(qtmpSRC, qtmpREG))
					return false;
				String rstREG = regex.substring(regSpIndex + qpmpREG.length());
				String rstSRC = src.substring(srcSpIndex + qpmpREG.length());
				return match(rstSRC, rstREG);
			}
			String temp = regex.substring(QMI + 1, SMI);
			int RTIndex = regex.indexOf(temp);
			int STIndex = src.indexOf(temp);
			// 问号特殊处理,需要判断索引位置是否相同
			if (RTIndex != STIndex)
				return false;
			if (src.contains(temp)) {
				return match(src.substring(STIndex + temp.length(), src.length()),
						regex.substring(RTIndex + temp.length(), regex.length()));
			}
			return false;
		}
	}

	// 星号匹配
	private static boolean STMark(String Src, String regex) { // *号匹配
		int Lstr = Src.length();
		int Lreg = regex.length();
		// 第一次星号的位置
		int SIndex = regex.indexOf("*");
		// 第二次星号的位置
		int SNIndex = regex.indexOf("*", SIndex + 1);
		// 如果第一个星号的位置=-1，则说明没有星号，则为精确匹配
		switch (SIndex) {
		case -1: {
			// 如果没有星号，则为精确匹配
			return Src.equals(regex);
		}
		case 0: {// SIndex=0 regex 中 * 号在首位
			if (Lreg == 1)
				return true;// 只有一个星号，自然是匹配的，如 regex="*"
			// 如果只有一个星号，且表达式的长度不为1 如 *abc 截取abc
			if (SNIndex == -1) {
				String rtemp = regex.substring(1, regex.length());
				// 如果字符串的长度与截取表达式片段长度小，匹配失败
				if (Src.length() < rtemp.length())
					return false;
				// 字符串中从右往左截取表达式片段长度的字符片段
				if (!Src.substring(Src.length() - rtemp.length(), Src.length()).equals(rtemp))
					return false;
				else
					return true;
			}
			// 多个星号，则按片段截取
			String temp = regex.substring(SIndex + 1, SNIndex);
			// 如果源字符串不包含该片段，匹配失败
			if (!Src.contains(temp))
				return false;
			// 如果第二星号的位置在最后以为 匹配成功
			if (SNIndex == regex.length() - 1)
				return true;
			// 截取匹配以后剩下的片段
			int STIndex = Src.indexOf(temp) + temp.length();
			int RTIndex = regex.indexOf(temp) + temp.length();
			String subSRC = Src.substring(STIndex, Src.length());
			String subREG = regex.substring(RTIndex, regex.length());
			return STMark(subSRC, subREG);
		}
		default: { // SIndex>0
			String temp = regex.substring(0, SIndex);
			// 如果元字符串总长度小于片段
			if (Src.length() < temp.length())
				return false;
			for (int i = 0; i < SIndex; i++)
				if (Src.charAt(i) != regex.charAt(i))
					return false;
			return STMark(Src.substring(SIndex, Lstr), regex.substring(SIndex, Lreg));
		}
		}
	}

	private static boolean STMarkURI(String Src, String regex) {
		// *号匹配
		int Lstr = Src.length();
		int Lreg = regex.length();
		// 第一次星号的位置
		int SIndex = regex.indexOf("*");
		// 第二次星号的位置
		int SNIndex = regex.indexOf("*", SIndex + 1);
		// 如果第一个星号的位置=-1，则说明没有星号，则为精确匹配
		switch (SIndex) {
		case -1: {
			// 如果没有星号，则为精确匹配
			return Src.equals(regex);
		}
		case 0: {
			// SIndex=0 regex 中 * 号在首位
			if (Lreg == 1)
				return Src.indexOf("/") < 0;// 只有一个星号，自然是匹配的，如 regex="*"
			// 如果只有一个星号，且表达式的长度不为1 如 *abc 截取abc
			if (SNIndex == -1) {
				if (Src.indexOf("/") >= 0)
					return false;
				String rtemp = regex.substring(1, regex.length());
				// 如果字符串的长度与截取表达式片段长度小，匹配失败
				if (Src.length() < rtemp.length())
					return false;
				// 字符串中从右往左截取表达式片段长度的字符片段
				if (!Src.substring(Src.length() - rtemp.length(), Src.length()).equals(rtemp))
					return false;
				else
					return true;
			}
			boolean dbST = false;
			if (SNIndex == SIndex + 1) {
				if (SNIndex + 1 == regex.length())
					return true;
				dbST = true;
				while (SNIndex == SIndex + 1) {
					SIndex = SNIndex;
					SNIndex = regex.indexOf("*", SIndex + 1);
				}
			}
			// 多个星号，则按片段截取
			String temp = SNIndex > 0 ? regex.substring(SIndex + 1, SNIndex) : regex.substring(SIndex + 1);
			// 如果源字符串不包含该片段，匹配失败
			if (Src.indexOf(temp) < 0)
				return false;
			// 如果第二星号的位置在最后以为 匹配成功
			if (!dbST && Src.substring(0, Src.indexOf(temp)).indexOf("/") >= 0)
				return false;
			// 截取匹配以后剩下的片段
			int STIndex = Src.indexOf(temp) + temp.length();
			int RTIndex = regex.indexOf(temp) + temp.length();
			String subSRC = Src.substring(STIndex, Src.length());
			String subREG = regex.substring(RTIndex, regex.length());
			return STMarkURI(subSRC, subREG);
		}
		default: { // SIndex>0
			String temp = regex.substring(0, SIndex);
			// 如果元字符串总长度小于片段
			if (Src.length() < temp.length())
				return false;
			for (int i = 0; i < SIndex; i++)
				if (Src.charAt(i) != regex.charAt(i))
					return false;
			return STMarkURI(Src.substring(SIndex, Lstr), regex.substring(SIndex, Lreg));
		}
		}
	}
	/**
	 * 获取字符数组的hash值
	 * @param objects 字符数组
	 * @return 计算后的hash值
	 */
	public static int hash(String... objects) {
		int hash = 0;String now;
		for (int i = 0; i < objects.length; i++) {
			 now = objects[i];
			 hash += now == null ? 1 << i : objects[i].hashCode();
		}
		return hash;
	}
	/**
	 * 判断字符和表达式是否匹配
	 * @param src 源字符串
	 * @param regs 表达式数组
	 * @return 是否有匹配
	 */
	public static boolean match(String src, String[] regs) {
		for (String reg : regs)
			if (match(src, reg))
				return true;
		return false;
	}

	/**
	 * 重组基础变量，后面依次为参数位置
	 * @param src 字符表达式
	 * @param arguments 参数数组
	 * @return 解析后的字符
	 */
	public static String decodeBaseVar(String src, Object... arguments) {
		StringBuilder sb = new StringBuilder(src);
		int index, v = 0;
		while ((index = sb.indexOf("${")) >= 0 && v < arguments.length)
			sb = new StringBuilder(sb.substring(0, index)).append(arguments[v++])
					.append(sb.substring(sb.indexOf("}", index + 1) + 1));
		return sb.toString();
	}
	/**
	 * 找到字符表达式中的变量名称，并将原始位置替换为目标字符
	 * 其中数组最后一条为替换后的内容
	 * 'select * from table where id = ${var1}','${','}','?' ==》 [var1 , select * from table where id = ?]
	 * @param regex 字符串表达式
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @param replace 代替的类容
	 * @return 数据的变量的集合
	 */
	public static List<String> find(String regex, String prefix, String suffix, String replace) {
		List<String> result = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer("");
		if (regex != null) {
			int index = regex.indexOf(prefix);
			int endex = regex.indexOf(suffix, index + prefix.length());
			int lastIndex = 0;
			while (index > -1 && endex > index) {
				String stmp = regex.substring(index + prefix.length(), endex);
				result.add(stmp);
				buffer.append(regex.substring(lastIndex, index)).append(replace);
				lastIndex = endex + suffix.length();
				index = regex.indexOf(prefix, endex);
				endex = regex.indexOf(suffix, index + prefix.length());
			}
			buffer.append(regex.substring(lastIndex));
			result.add(buffer.toString());
		}
		return result;
	}
	/**
	 * 找到字符串表达式中的表达式
	 * @param regex 表达式
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return 变量的集合
	 */
	public static List<String> find(String regex, String prefix, String suffix) {
		List<String> result = new ArrayList<String>();
		if (regex != null) {
			int index = regex.indexOf(prefix);
			int endex = regex.indexOf(suffix, index + prefix.length());
			while (index > -1 && endex > index) {
				String stmp = regex.substring(index + prefix.length(), endex);
				result.add(stmp);
				index = regex.indexOf(prefix, endex);
				endex = regex.indexOf(suffix, index + prefix.length());
			}
		}
		return result;
	}
	/**
	 * 找出表达式中指定类型的占位符的变量名称，比如,'abc${def}ghi#{jkl}','#{ }','${ }','{{ }}' ==》 'def','jkl'
	 * @param str 表达式
	 * @param fixArrays 占位符的数组，比如'#{ }','${ }','{{ }}'
	 * @return 变量的集合
	 */
	public static List<String> findAllVars(String str, String... fixArrays) {
		List<String> result = new ArrayList<String>();
		if (str != null && fixArrays != null && fixArrays.length > 0) {
			String suffix = "";
			String prefix = "";
			int index = Integer.MAX_VALUE;
			for (String reg : fixArrays) {
				String[] fixs = reg.split(" ");
				if (fixs.length < 2)
					throw new RuntimeException("express \"" + reg + "\" could not found suffix");
				int tIndex = str.indexOf(fixs[0]);
				if (tIndex > -1 && tIndex < index) {
					index = tIndex;
					suffix = fixs[1];
					prefix = fixs[0];
				}
			}
			int endex = str.indexOf(suffix, index + prefix.length());
			while (index != Integer.MAX_VALUE && endex > index) {
				String stmp = str.substring(index + prefix.length(), endex);
				if (!result.contains(stmp))
					result.add(stmp);
				index = Integer.MAX_VALUE;
				for (String reg : fixArrays) {
					String[] fixs = reg.split(" ");
					if (fixs.length < 2)
						throw new RuntimeException("express \"" + reg + "\" could not found suffix");
					int tIndex = str.indexOf(fixs[0], endex + suffix.length());
					if (tIndex > -1 && tIndex < index) {
						index = tIndex;
						suffix = fixs[1];
						prefix = fixs[0];
					}
				}
				endex = str.indexOf(suffix, index + prefix.length());
			}

		}
		return result;
	}
	/**
	 * 匹配表达式，并将占位符填入表达式
	 * @param res 表达式
	 * @param tokens 令牌集合
	 * @param variable 变量列表
	 * @return  是否匹配
	 */
	public static boolean match(String res, List<Token> tokens, Map<Integer, Token> variable) {
		Iterator<Token> iterator = tokens.iterator();
		Token nT;
		int p;
		while (iterator.hasNext()) {
			Token token = iterator.next();
			if (token.getType() == 0) {
				if (!res.startsWith(token.getToken())) {
					return false;
				} else {
					res = res.substring(token.getToken().length());
				}
			} else {
				switch (token.getType()) {
				case 1:// *
						// 获取下一个token
					if (iterator.hasNext()) {
						nT = iterator.next();
						p = res.indexOf(nT.getToken());
						if (p < 0)
							return false;
						String ma = res.substring(0, p);
						if (ma.indexOf("/") != -1) {
							return false;
						}
						token.setValue(ma);
						variable.put(token.getIndex(), token);
						res = res.substring(p + nT.getToken().length());
					} else {// 最后一个位置
						String ma = res;
						if (ma.indexOf("/") != -1) {
							return false;
						}
						token.setValue(ma);
						variable.put(token.getIndex(), token);
					}
					break;
				case 2:// **
					String var;
					if (iterator.hasNext()) {
						nT = iterator.next();
						int n = 0;
						String temp;
						while ((p = res.indexOf(nT.getToken(), n + 1)) > -1) {
							var = res.substring(0, p);
							temp = res.substring(p + nT.getToken().length());
							if (match(temp, tokens.subList(tokens.indexOf(nT) + 1, tokens.size()), variable)) {
								token.setValue(var);
								variable.put(token.getIndex(), token);
								return true;
							}
							n = p;
						}
						return false;
					} else {
						var = res;
					}
					token.setValue(var);
					variable.put(token.getIndex(), token);
					break;
				case 3:// ?
					if (token.getToken().length() > res.length()) {
						return false;
					}
					int i = 0;
					int len = token.getToken().length();
					char[] chars = new char[len];
					while (i < len) {
						char ch = res.charAt(i);
						if (ch == '/') {
							return false;
						}
						chars[i++] = ch;
					}
					token.setValue(new String(chars));
					variable.put(token.getIndex(), token);
					res = res.substring(token.getToken().length());
				}
			}
		}
		return true;
	}
	/**
	 * 将表达式生成token
	 * @param express string express
	 * @return token list
	 */
	public static List<Token> getToken(String express) {
		List<Token> tokens = tokenCache.get(express);
		if (tokens != null)
			return tokens;
		int index = 0;
		int last = 0;
		int point = 0;
		tokens = new ArrayList<Token>();
		while (index < express.length()) {
			char ch = express.charAt(index++);
			switch (ch) {
			case '*':
				Token token = new Token();
				token.setToken(express.substring(last, index - 1));
				tokens.add(token);
				token.setIndex(point++);
				if (index == express.length() || express.charAt(index) != '*') {
					token = new Token();
					token.setToken("*");
					token.setIndex(point++);
					token.setType(1);
					tokens.add(token);
				} else {
					token = new Token();
					token.setToken("**");
					token.setIndex(point++);
					token.setType(2);
					tokens.add(token);
					index++;
				}
				last = index;
				break;
			case '?':
				token = new Token();
				token.setToken(express.substring(last, index - 1));
				tokens.add(token);
				token.setIndex(point++);
				StringBuffer sb = new StringBuffer('?');
				--index;
				while (index < express.length() && express.charAt(index++) == '?')
					sb.append('?');
				token = new Token();
				token.setToken(sb.toString());
				tokens.add(token);
				token.setType(3);
				token.setIndex(point++);
				last = index - 1;
			}
		}
		if (last < express.length()) {
			Token token = new Token();
			token.setToken(express.substring(last));
			token.setIndex(point++);
			tokens.add(token);
		}
		return tokens;
	}
	/**
	 * 该类用来标记匹配的字符的信息，该信息包含位置信息，匹配变量名称，匹配后的值等
	 * @author yanan
	 */
	public static class Token {
		private int index;
		private String token;
		private String name;
		private String value;
		private int type;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public String toString() {
			return "Token [index=" + index + ", token=" + token + ", name=" + name + ", value=" + value + ", type="
					+ type + "]";
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}
	}
	public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
	public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }
	public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
	public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
	 /**
     * <p>Compares two CharSequences, returning {@code true} if they represent
     * equal sequences of characters.</p>
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is <strong>case sensitive</strong>.</p>
     *
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @param cs1  the first CharSequence
     * @param cs2  the second CharSequence
     * @return if the CharSequences are equal (case-sensitive), or both null
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 查找字符串位置
     * @param src 原字符
     * @param target 目标字符
     * @return 位置
     */
	public static int indexOf(String src, String target) {
		return src == null ? -1 : src.indexOf(target);
	}
	/**
	 * 判断字符串是否包含某字符串
	 * @param src 原字符
	 * @param target 目标字符
	 * @return 是否包含
	 */
	public static boolean contains(String src, String target) {
		return indexOf(src, target) != -1;
	}
	/**
	 * 获取字符串中目标字符的位置
	 * @param src 原字符串
	 * @param start 开始位置
	 * @param end 结束位置
	 * @param targets 要查找的字符数组
	 * @return 位置
	 */
	public static int indexOf(String src,int start,int end, char... targets) {
		char[] chars = src.toCharArray();
		for(int i = start;i<end;i++) {
			for(char ch : targets) {
				if(chars[i] == ch)
					return i;
			}
		}
		return -1; 
	}
	/**
	 * 获取字符串中目标字符的位置
	 * @param src 原字符串
	 * @param start 开始位置
	 * @param targets 要查找的字符数组
	 * @return 位置
	 */
	public static int indexOf(String src,int start,char... targets) {
		return indexOf(src,start,src.length(),targets); 
	}
	/**
	 * 获取字符串中目标字符的位置
	 * @param src 原字符串
	 * @param targets 要查找的字符数组
	 * @return 位置
	 */
	public static int indexOf(String src,char... targets) {
		return indexOf(src,0,src.length(),targets); 
	}
	/**
	 * 获取字符串中某个字符最后出现的位置
	 * @param src 字符串
	 * @param targets 目标字符
	 * @return 位置
	 */
	public static int lastIndexOf(String src, char... targets) {
		char[] chars = src.toCharArray();
		for(int i = src.length()-1;i>-1;i--) {
			for(char ch : targets) {
				if(chars[i] == ch)
					return i;
			}
		}
		return -1;
	}
	public static boolean endsWith(String src, char c) {
		return src.charAt(src.length()-1) == c;
	}
}
