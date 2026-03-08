package kr.krd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import kr.krd.dao.CMY_MemberDAO;
import kr.krd.dao.MemberDAO;
import kr.krd.dao.USH_AnnouncementDAO;
import kr.krd.vo.USH_AnnSummaryVO;


public class UserMain {
	// ------------------------------------------------
	// ANSI Escape Codes & UI Elements
	// ------------------------------------------------
	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String UNBOLD = "\u001B[22m";

	public static final String CLR_PRIMARY = "\u001B[36m"; // Cyan
	public static final String CLR_WHT = "\u001B[37m";    // White
	public static final String CLR_GRY = "\u001B[90m";    // Gray
	public static final String CLR_ERR = "\u001B[31m";    // Red
	public static final String CLR_SUC = "\u001B[32m";    // Green

	private static final String LN_SINGLE = "─";
	private static final String LN_DOUBLE = "═";
	private static final String INDENT = "      ";

	private BufferedReader br;
	private MemberDAO dao;	//공통클래스
	private CMY_MemberDAO revdao; //평가위원클래스
	private USH_AnnouncementDAO annDao;
	private String cust_id;
	private String role; 
	private String field;
	private boolean login;

	public UserMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new MemberDAO(br);
			revdao = new CMY_MemberDAO();
			annDao = new USH_AnnouncementDAO();
			// 콘솔 화면 화면 초기화 (지원하는 터미널에서만 작동)
			callMenu();

		} catch(Exception e) {
			printError("시스템 초기화 중 예상치 못한 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			if(br!=null) try { br.close(); } catch(IOException e) {}
		}
	}

	// ------------------------------------------------
	// UI Helper Methods (깔끔한 출력을 위한 유틸리티)
	// ------------------------------------------------
	private void printDrawLine(String start, String middle, String end, String lineType, int width) {
		StringBuilder sb = new StringBuilder();
		sb.append(INDENT).append(CLR_GRY).append(start);
		for(int i=0; i<width; i++) sb.append(lineType);
		sb.append(end).append(RESET);
		System.out.println(sb.toString());
	}
	
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CLR_GRY + "────────────────────────────────────────" + RESET);
	}

	private void printMenuOption(String key, String description) {
		System.out.print(INDENT + "  " + CLR_PRIMARY + BOLD + "[" + key + "]" + RESET + " ");
		System.out.println(CLR_WHT + description + RESET);
	}

	private void printInputTag(String tag) {
		System.out.print("\n" + INDENT + CLR_PRIMARY + BOLD + "▶ " + tag + RESET + " : ");
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + UNBOLD + msg + RESET);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + UNBOLD + msg + RESET);
	}

	// ------------------------------------------------
	// Screen Outputs (실제 화면 출력)
	// ------------------------------------------------

	// 1. 메인 로고 (ASCII Art - 화려함 담당)
	private void printLogo() {
		System.out.println(CLR_PRIMARY + BOLD);
		System.out.println(INDENT + " ██╗  ██╗██████╗ ██████╗     ██╗  ██╗██╗   ██╗██████╗ ███████╗");
		System.out.println(INDENT + " ██║ ██╔╝██╔══██╗██╔══██╗    ██║  ██║██║   ██║██╔══██╗██╔════╝");
		System.out.println(INDENT + " █████╔╝ ██████╔╝██║  ██║    ███████║██║   ██║██████╔╝███████╗");
		System.out.println(INDENT + " ██╔═██╗ ██╔══██╗██║  ██║    ██╔══██║██║   ██║██╔══██╗╚════██║");
		System.out.println(INDENT + " ██║  ██╗██║  ██║██████╔╝    ██║  ██║╚██████╔╝██████╔╝███████║");
		System.out.println(INDENT + " ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝     ╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝");
		System.out.println(RESET);
	}

	// 2. 메인 메뉴화면
	private void callMenu() throws IOException {
		while(true) {
			System.out.println("\n\n"); // Upper padding
			printLogo();

			System.out.println(INDENT + CLR_WHT + BOLD + "  국가 연구과제 관리 플랫폼" + RESET + CLR_GRY + " | Version 1.0" + RESET);
			printDrawLine("┏", LN_SINGLE, "┓", LN_SINGLE, 50);
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);

			printMenuOption("1", "시스템 로그인 (Login)");
			printMenuOption("2", "신규 회원가입 (Sign Up)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("3", "프로그램 종료 (Exit)");

			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printDrawLine("┗", LN_SINGLE, "┛", LN_SINGLE, 50);

			printInputTag("메뉴 선택");

			try {
				String input = br.readLine();
				if(input == null) break;
				int MenuNo = Integer.parseInt(input);

				if(MenuNo == 1) {
					// 로그인 화면 로직
					if(handleLoginScreen()) {
						// 로그인 성공 시 권한별 메뉴 진입
						processRoleMenu();
					}
				} else if(MenuNo == 2) {
					System.out.println("\n" + INDENT + "[ 회원가입 모듈로 이동합니다 ]");
					dao.insertMember(); 
				} else if(MenuNo == 3) {
					System.out.println("\n" + INDENT + CLR_WHT + "KRD Hubs를 종료합니다. 이용해 주셔서 감사합니다." + RESET);
					break;
				} else {
					printError("잘못된 입력입니다. 메뉴 번호를 확인하세요.");
				}
			} catch(NumberFormatException e) {
				printError("숫자만 입력 가능합니다.");
			}
		}
	}

	// 3. 로그인 및 ID/PW 찾기 선택 화면
	private boolean handleLoginScreen() throws IOException {
		while(true) {
			System.out.println("\n");
			printDrawLine("╔", LN_DOUBLE, "╗", LN_DOUBLE, 40);
			System.out.println(INDENT + CLR_GRY + "║" + RESET + "  " + CLR_WHT + BOLD + "접속 인증" + RESET);
			printDrawLine("╠", LN_SINGLE, "╣", LN_SINGLE, 40);
			System.out.println(INDENT + CLR_GRY + "║" + RESET);

			printMenuOption("1", "로그인 진행");
			printMenuOption("2", "아이디 / 비밀번호 찾기");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 화면으로");

			System.out.println(INDENT + CLR_GRY + "║" + RESET);
			printDrawLine("╚", LN_DOUBLE, "╝", LN_DOUBLE, 40);

			printInputTag("진행 선택");

			try {
				int secMenuNo = Integer.parseInt(br.readLine());
				if(secMenuNo == 1) {
					// 실제 로그인 입력 폼
					System.out.println("\n" + INDENT + CLR_GRY + LN_SINGLE.repeat(42) + RESET);
					printInputTag("아이디(ID)");
					String user_id = br.readLine();
					printInputTag("비밀번호(PW)");
					String user_pw = br.readLine();
					System.out.println(INDENT + CLR_GRY + LN_SINGLE.repeat(42) + RESET);

					cust_id = dao.userLogin(user_id, user_pw);

					if(cust_id != null && !cust_id.equals("0")) {
						login = true;
						role = dao.getUserRole(cust_id);
						field = dao.getUserField(cust_id);
						printSuccess(cust_id + "님, 환영합니다. 시스템에 접속합니다.");
						return true; // 로그인 성공
					} else {
						printError("인증에 실패했습니다. ID와 PW를 확인하세요.");
					}
				} else if(secMenuNo == 2) {
					findIdPwMenu();
				} else if(secMenuNo == 0) {
					return false; // 뒤로 가기
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력하세요.");
			}
		}
	}

	// 4. 로그인 후 권한별 메뉴 처리 (레이아웃 개선)
	private void processRoleMenu() throws IOException {
		while(login) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + BOLD + "========================================");
			System.out.println(INDENT + "   USER SESSION 활성화 [" + role + "]");
			System.out.println(INDENT + "========================================" + RESET);

			// 기존 리스너/게스트 메뉴 호출 (DAO쪽 UI도 이 스타일로 맞추는 것을 추천)
			//  if(role.equals("REV")) {
			if("REV".equals(role)) {
				boolean isLogout = revdao.callReviewerMenu(cust_id, role, field);
				if(isLogout) {
					login = false;
					cust_id = null;
					role = null;
					field = null;
					break;
				}
				//  } else if(role.equals("GST")) {
			} else if("GST".equals(role)) {
				boolean isLogout = callGuestMenu(cust_id, role, field);
				if(isLogout) {
					login = false;
					cust_id = null;
					role = null;
					field = null;
					break;
				}
				//	} else if(role.equals("AGY")) {
			} else if("AGY".equals(role)) {
				RR_KRDAdminMain agyMain = new RR_KRDAdminMain();
				agyMain.callMenu();

				//	}else if(role.equals("RESO")) {
			} else if("RESO".equals(role)) {
				HYJ_KRDRESOUserMain resoMain = new HYJ_KRDRESOUserMain();
				resoMain.callMenu();

				//	}else if(role.equals("RESI")) {
			} else if("RESI".equals(role)) {
				RESIUserMain resiMain = new RESIUserMain();
				resiMain.callMenu();

				//	}else if(role.equals("ADM")) {
			} else if("ADM".equals(role)) {
			    USH_KRDAdminMain admMain = new USH_KRDAdminMain(br, cust_id);
			    boolean isLogout = admMain.callMenu();

			    if(isLogout) {
			        login = false;
			        cust_id = null;
			        role = null;
			        field = null;
			        break;
			    }
			} else {
				printError("정의되지 않은 권한입니다. 관리자에게 문의하세요.");
				login = false;
				break;
			}

			// DAO 메뉴에서 돌아왔을 때 로그인 상태 확인
			if(!login) {
				break;
			}
		}
	}

	// 5. 아이디/비밀번호 찾기 메뉴 (깔끔하게 정리)
	public void findIdPwMenu() {
		while(true) {
			try {
				System.out.println("\n");
				printDrawLine("┌", LN_SINGLE, "┐", LN_SINGLE, 35);
				System.out.println(INDENT + "┃  " + CLR_WHT + BOLD + "계정 정보 찾기" + RESET);
				printDrawLine("├", LN_SINGLE, "┤", LN_SINGLE, 35);
				System.out.println(INDENT + "┃");
				printMenuOption("1", "아이디(ID) 찾기");
				printMenuOption("2", "비밀번호 재설정");
				System.out.println(INDENT + CLR_GRY + "┃" + RESET);
				printMenuOption("0", "이전 화면으로");
				System.out.println(INDENT + "┃");
				printDrawLine("└", LN_SINGLE, "┘", LN_SINGLE, 35);

				printInputTag("선택");

				int menu = Integer.parseInt(br.readLine());

				if(menu == 1) {
					dao.findUserId();
				} else if(menu == 2) {
					dao.resetPassword();
				} else if(menu == 0) {
					return; // 뒤로가기
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력해주세요.");
			} catch(Exception e) {
				printError("오류가 발생했습니다.");
			}
		}
	}

	// 2. 일반회원 화면 (추가된 부분 UI 개선)
	public boolean callGuestMenu(String myCust_id, String myRole, String myField) {
		//	public void callGuestMenu(String myCust_id, String myRole, String myField) { true -> 실제 로그아웃 false -> 상위메뉴로 돌아감
		this.cust_id = myCust_id;
		this.role = myRole;
		this.field = myField;

		while(true) {
			System.out.println("\n\n" + INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + BOLD + "   KRD Hubs | " + RESET + CLR_WHT + "일반회원 메인메뉴" + RESET);
			System.out.println(INDENT + CLR_PRIMARY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + CLR_GRY + "  접속 계정: " + RESET + cust_id + CLR_GRY + " | 등급: 일반회원" + RESET);
			System.out.println();

			printMenuOption("1", "공고 목록 조회 (Announcements)");
			printMenuOption("2", "권한 신청 (Request Role)");
			printMenuOption("3", "내 정보 관리 (My Profile)");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("4", "시스템 로그아웃 (Logout)");
			printMenuOption("5", "프로그램 종료 (Exit)");

			printInputTag("메뉴 선택");
			try {
				int gst_choose = Integer.parseInt(br.readLine());
				if(gst_choose == 1) {
					printGuestAnnouncementList();
				} else if(gst_choose == 2) {
					dao.applyRole(cust_id);
				} else if(gst_choose == 3) {
					callGuestMyPageMenu();
				} else if(gst_choose == 4) {
					printInputTag("로그아웃 하시겠습니까? (Y/N)");
					String logoutInput = br.readLine();
					if("Y".equalsIgnoreCase(logoutInput)) {
						boolean logoutResult = dao.logout();
						if(!logoutResult) {
							// printError("로그아웃 처리 중 문제가 발생했습니다."); (DAO 로직에 따라 활성화 필요)
						}
						printSuccess("안전하게 로그아웃 되었습니다.");
						return true; // 로그아웃 처리
					} else if("N".equalsIgnoreCase(logoutInput)) {
						continue;
					} else {
						printError("Y 또는 N만 입력하세요.");
					}
				} else if(gst_choose == 5) {
					System.out.println("\n" + INDENT + "프로그램을 종료합니다.");
					System.exit(0);
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(Exception e) {
				printError("입력 오류가 발생했습니다.");
			}
		}
	}

	//일반회원 메뉴 - 공고 목록 조회
	private void printGuestAnnouncementList() {
	    List<USH_AnnSummaryVO> list = annDao.findAnnSummaryList();

	    printSubTitle("공고 목록 조회 (Announcements)");

	    if(list == null || list.isEmpty()) {
	        System.out.println(INDENT + CLR_GRY + "  조회 가능한 공고가 없습니다." + RESET);
	        return;
	    }

	    // 표 헤더 생성 (String.format으로 간격 고정)
	    System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-5s | %-15s | %-8s | %-20s | %-10s", 
	    		"번호", "공고명", "상태", "진행 기간", "기관명") + RESET);
	    System.out.println(INDENT + CLR_GRY + "-----------------------------------------------------------------------" + RESET);

	    for(USH_AnnSummaryVO ann : list) {
	    	// 공고명이 너무 길 경우 잘라주기 (UI 깨짐 방지)
	    	String shortTitle = ann.title;
	    	if(shortTitle != null && shortTitle.length() > 12) {
	    		shortTitle = shortTitle.substring(0, 10) + "..";
	    	}
	    	
	    	// 상태별 색상 부여
	    	String statusStr = String.valueOf(ann.statusCd);
	    	String statusColor = statusStr.equals("ACTIVE") ? CLR_SUC : CLR_PRIMARY;

	        System.out.println(INDENT + String.format("%-6d | %-15s | " + statusColor + "%-8s" + RESET + " | %-10s ~ %-10s | %-10s", 
	                ann.annId, 
	                shortTitle, 
	                statusStr, 
	                ann.startDt, 
	                ann.endDt, 
	                ann.agencyName));
	    }
	    System.out.println();
	}

	//일반회원 메뉴 - 내 정보 관리
	private void callGuestMyPageMenu() throws IOException {
		while(true) {
			printSubTitle("내 정보 관리 (My Profile)");
			
			printMenuOption("1", "내 정보 상세 조회");
			printMenuOption("2", "내 정보 수정 (이메일/연락처 등)");
			printMenuOption("3", "권한 신청 진행 현황 조회");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 화면으로 돌아가기");

			printInputTag("메뉴 선택");

			try {
				int menu = Integer.parseInt(br.readLine());

				if(menu == 1) {
					dao.showMyInfo(cust_id);
				} else if(menu == 2) {
					callGuestMyInfoEditMenu();
				} else if(menu == 3) {
					dao.showRoleApplicationStatus(cust_id);
				} else if(menu == 0) {
					return;
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력해주세요.");
			}
		}
	}

	//일반 회원 메뉴 - 내 정보 관리 - 내 정보 변경
	private void callGuestMyInfoEditMenu() throws IOException {
		while(true) {
			printSubTitle("내 정보 변경 (Edit Profile)");
			
			printMenuOption("1", "이메일(E-mail) 주소 변경");
			printMenuOption("2", "연락처(Phone) 번호 변경");
			printMenuOption("3", "비밀번호(Password) 변경");
			System.out.println(INDENT + CLR_GRY + "┃" + RESET);
			printMenuOption("0", "이전 화면으로 돌아가기");

			printInputTag("메뉴 선택");

			try {
				int menu = Integer.parseInt(br.readLine());

				if(menu == 1) {
					dao.updateEmail(cust_id);
				} else if(menu == 2) {
					dao.updatePhone(cust_id);
				} else if(menu == 3) {
					dao.changeMyPassword(cust_id);
				} else if(menu == 0) {
					return;
				} else {
					printError("잘못된 선택입니다.");
				}
			} catch(NumberFormatException e) {
				printError("숫자를 입력해주세요.");
			}
		}
	}

	public static void main(String[] args) {
		// ANSI Escape 코드를 지원하지 않는 일부 윈도우 CMD를 위한 설정이 필요할 수 있으나,
		// 최신 터미널(VSCode, IntelliJ, Windows Terminal)에서는 대개 잘 작동합니다.
		new UserMain();
	}
}