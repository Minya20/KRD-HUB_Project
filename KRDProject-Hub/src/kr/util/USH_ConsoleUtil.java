package kr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class USH_ConsoleUtil {
	// ------------------------------------------------
	// ANSI Escape Codes & UI Elements
	// ------------------------------------------------
	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String UNBOLD = "\u001B[22m";

	public static final String CLR_ERR = "\u001B[31m";    // Red (에러 메시지용)
	public static final String CLR_GRY = "\u001B[90m";    // Gray (보조 설명용)

	private static final String INDENT = "      ";

	private final BufferedReader br;
	
	public USH_ConsoleUtil(BufferedReader br) {
		this.br = br;
	}

	// ------------------------------------------------
	// UI Helper Method
	// ------------------------------------------------
	private void printError(String msg) {
		System.out.println(INDENT + CLR_ERR + BOLD + "✘ " + UNBOLD + msg + RESET);
	}
	
	// ------------------------------------------------
	// Input Utility Methods
	// ------------------------------------------------

	// 필수 문자열 입력(빈값이면 계속 재입력)
	public String readRequired(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return ""; // EOF 방어
			s = s.trim();
			
			if(s.isEmpty()) {
				printError("해당 항목은 필수 입력 사항입니다.");
				continue;
			}
			return s;
		}
	}
	
	// 선택 문자열 입력(엔터면 null)
	public String readOptional(String prompt) throws IOException{
		System.out.print(prompt);
		String s = br.readLine();
		if(s == null) return null;
		s = s.trim();
		return s.isEmpty() ? null : s;
	}
	
	// Y/N 확인(올바른 입력까지 반복)
	public boolean confirmYN(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return false;
			s = s.trim();
			
			if(s.equalsIgnoreCase("Y")) return true;
			if(s.equalsIgnoreCase("N")) return false;
			
			printError("잘못된 입력입니다. Y 또는 N만 입력하세요.");
		}
	}
	
	// 허용값 중 하나 선택.(엔터 불가) 입력은 자동으로 trim + 대문자 변환 후 검사
	public String readFromSetRequired(String prompt, Set<String> allowedUpper) throws IOException {
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim().toUpperCase();
			
			if(s.isEmpty()) {
				printError("해당 항목은 필수 입력 사항입니다.");
				continue;
			}
			if(!allowedUpper.contains(s)) {
				printError("허용되지 않은 값입니다. 다음 중 하나를 입력하세요: " + CLR_GRY + allowedUpper + RESET);
				continue;
			}
			return s;
		}
	}
	
	// 날짜 선택 입력.(엔터면 ""반환) - 형식/실존 날짜 검증
	public String readDateOrEmpty(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim();
			
			if(s.isEmpty()) return ""; // 생략
			
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				printError("날짜 형식이 올바르지 않습니다. (예: 2026-03-08)");
				continue;
			}
			
			try {
				LocalDate.parse(s);
				return s;
			} catch(DateTimeParseException e) {
				printError("달력에 존재하지 않는 유효하지 않은 날짜입니다.");
			}
		}
	}
	
	// 날짜 필수 입력.(엔터 불가) - 형식/실존 날짜 검증
	public String readDateRequired(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim();
			
			if(s.isEmpty()) {
				printError("날짜 입력은 필수입니다.");
				continue;
			}
			
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				printError("날짜 형식이 올바르지 않습니다. (예: 2026-03-08)");
				continue;
			}
			
			try {
				LocalDate.parse(s);
				return s;
			} catch(DateTimeParseException e) {
				printError("달력에 존재하지 않는 유효하지 않은 날짜입니다.");
			}
		}
	}
	
	// 정수 입력(숫자 아니면 재입력)
	public int readInt(String prompt) throws IOException {
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return -1; // EOF 방어
			s = s.trim();
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			}
		}
	}
	
	// 범위 제한 정수 입력(min~max)
	public int readIntInRange(String prompt, int min, int max) throws IOException {
		while(true) {
			int n = readInt(prompt);
			if(n >= min && n <= max) return n;
			printError(min + "에서 " + max + " 사이의 숫자를 입력해주세요.");
		}
	}
}