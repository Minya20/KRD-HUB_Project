package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;

import kr.krd.dao.HYJ_KRDRESOUserDAO;
import kr.krd.dao.HYJ_MyInfoDAO;
import kr.krd.dao.HYJ_RESISearch;
import kr.krd.dao.HYJ_ReportDAO;
import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.HYJ_APPLICATIONCheakDAO;

public class RESIUserMain {
	private BufferedReader br;
	private String role;
	private String cust_id;
	private String field;

	private HYJ_KRDRESOUserDAO dao1;
	private CMY_MemberDAO dao2;
	private HYJ_MyInfoDAO dao3;
	private HYJ_APPLICATIONCheakDAO dao4;
	private HYJ_ReportDAO dao5;
	private HYJ_RESISearch dao6;

	public RESIUserMain(BufferedReader br, String cust_id, String role, String field) {
		this.br = br;
		this.cust_id = cust_id;
		this.role = role;
		this.field = field;

		this.dao1 = new HYJ_KRDRESOUserDAO();
		this.dao2 = new CMY_MemberDAO();
		this.dao3 = new HYJ_MyInfoDAO();
		this.dao4 = new HYJ_APPLICATIONCheakDAO();
		this.dao5 = new HYJ_ReportDAO(br); // 여기 수정
		this.dao6 = new HYJ_RESISearch();
	}

	// 메뉴
	public boolean callMenu() throws IOException {
		while(true) {
			System.out.print("1.공고 조회, 2.내 신청조회, 3.보고서 제출, 4.내 정보 수정, 5.시스템 로그아웃, 6.프로그램 종료 > ");
			try {
				int no = Integer.parseInt(br.readLine());

				if(no == 1) {
					dao1.selectAnn(cust_id);
				} else if(no == 2) {
					dao4.CheckMyApp(cust_id);
				} else if(no == 3) {
					dao5.InsertReport(cust_id);
				} else if(no == 4) {
					dao3.SelectInfo(cust_id);
				} else if(no == 5) {
					System.out.println("로그아웃 하시겠습니까? (Y/N)");
					String input = br.readLine();

					if("Y".equalsIgnoreCase(input)) {
						System.out.println("계정에서 안전하게 로그아웃 되었습니다.");
						return true;
					} else if("N".equalsIgnoreCase(input)) {
						continue;
					} else {
						System.out.println("Y 또는 N만 입력해주세요.");
					}
				} else if(no == 6) {
					System.out.println("프로그램을 종료합니다.");
					System.exit(0);
				} else {
					System.out.println("잘못 입력했습니다.");
				}
			} catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
}