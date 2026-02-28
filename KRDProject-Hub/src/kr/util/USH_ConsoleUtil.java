package kr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class USH_ConsoleUtil {
	private final BufferedReader br;
	
	public USH_ConsoleUtil(BufferedReader br) {
		this.br = br;
	}
	
	//필수 문자열 입력(빈값이면 계속 재입력)
	public String readRequired(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return ""; // EOF 방어
			s = s.trim();
			
			if(s.isEmpty()) {
				System.out.println("입력은 필수입니다.");
				continue;
			}
			return s;
		}
	}
	
	//선택 문자열 입력(엔터면 null)
	public String readOptional(String prompt) throws IOException{
		System.out.print(prompt);
		String s = br.readLine();
		if(s == null) return null;
		s = s.trim();
		return s.isEmpty() ? null : s;
	}
	
	//Y/N 확인(올바른 입력까지 반복)
	public boolean confirmYN(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return false;
			s = s.trim();
			
			if(s.equalsIgnoreCase("Y")) return true;
			if(s.equalsIgnoreCase("N")) return false;
			
			System.out.println("잘못된 입력입니다. Y 또는 N만 입력하세요.");
		}
	}
	
	//허용값 중 하나 선택.(엔터 불가) 입력은 자동으로 trim + 대문자 변환 후 검사
	public String readFromSetRequired(String prompt, Set<String> allowedUpper)throws IOException {
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim().toUpperCase();
			
			if(s.isEmpty()) {
				System.out.println("입력은 필수입니다.");
				continue;
			}
			if(!allowedUpper.contains(s)) {
				System.out.println("허용값 중 하나로 입력하세요: " + allowedUpper);
				continue;
			}
			return s;
		}
	}
	
	//날짜 선택 입력.(엔터면 ""반환) - 형식/실존 날짜 검증
	public String readDateOrEmpty(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim();
			
			if(s.isEmpty()) return ""; //생략
			
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				System.out.println("[날짜 형식 오류] YYYY-MM-DD로 입력하세요. (또는 엔터로 생략)");
				continue;
			}
			
			try {
				LocalDate.parse(s);
				return s;
			}catch(DateTimeParseException e) {
				System.out.println("[존재하지 않는 날짜] 다시 입력하세요. (또는 엔터로 생략)");
			}
		}
	}
	
	//날짜 필수 입력.(엔터 불가) - 형식/실존 날짜 검증
	public String readDateRequired(String prompt) throws IOException{
		while(true) {
			System.out.print(prompt);
			String s = br.readLine();
			if(s == null) return "";
			s = s.trim();
			
			if(s.isEmpty()) {
				System.out.println("날짜 입력은 필수입니다.");
				continue;
			}
			
			if(!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
				System.out.println("[날짜 형식 오류] YYYY-MM-DD로 입력하세요.");
				continue;
			}
			
			try {
				LocalDate.parse(s);
				return s;
			}catch(DateTimeParseException e) {
				System.out.println("[존재하지 않는 날짜] 다시 입력하세요.");
			}
		}
	}
	
}
