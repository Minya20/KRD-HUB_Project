package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.HYJ_MyInfoDAO;
import kr.krd.dao.HYJ_RESISearch;
import kr.krd.dao.HYJ_ReportDAO;
import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.HYJ_APPLICATIONCheakDAO;

public class RESIUserMain {
	private BufferedReader br;
	private String role;
	private HYJ_KRDRESOUserDAO dao1;
	private CMY_MemberDAO dao2;
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)
	private HYJ_MyInfoDAO dao3;
	private HYJ_APPLICATIONCheakDAO dao4;
	private HYJ_ReportDAO dao5;
	private HYJ_RESISearch dao6;

	public RESIUserMain(String cust_id, String role, String field) {
		this.br = br;
		this.dao1 = new HYJ_KRDRESOUserDAO();
		this.dao2 = new CMY_MemberDAO();
		this.dao3 = new HYJ_MyInfoDAO();
		this.dao4 = new HYJ_APPLICATIONCheakDAO();
		this.dao5 = new HYJ_ReportDAO();
		this.dao6 = new HYJ_RESISearch();
	}

	//메뉴
	public boolean callMenu()throws IOException{
		//로그인시 보여지는 메뉴
		while(login) {
			System.out.print(
					"1.공고 조회,2.내 신청조회,3.보고서 제출,4.내 정보 수정,5.인재 열람,6.종료>");
			try {
				int no = Integer.parseInt(
						br.readLine());
				if(no == 1) {//공고 조회
					dao1.selectAnn();
				}else if(no == 2) {
					dao4.CheckMyApp(cust_id);
				}else if(no == 3) {
					dao5.InsertReport(cust_id);
				}else if(no == 4) {
					dao3.SelectInfo(cust_id);
				}else if(no == 5) {
					dao6.RESISearch();
				}else if(no == 6) {
					System.out.println("프로그램을 종료합니다.");
					return true;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}//end of while
	}

	public static void main(String[] args) {
		new HYJ_KRDRESOUserMain();
	}
}
