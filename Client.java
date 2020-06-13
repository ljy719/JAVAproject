package ZeroGame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Client extends JFrame {
	private JPanel panel;
	
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	
	private JTextField textfield1;
	private JTextField textfield2;
	private JTextField textfield3;
	
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	
	private JLabel[] hpbtn;
	
	private String tmpstr = "";
	
	private Component component = this;
	
	private String nickname;
	
	private Socket c_Socket;
	private OutputStream oos;
	private BufferedOutputStream bos ;
	private ObjectOutputStream out = null;
	private InputStream is;
	private BufferedInputStream bis;
	private ObjectInputStream in = null;
    
	private Player a;
    
	int HP;
	int myturn = 0;
	
	boolean waiting = true;
	boolean isStart = false;
	int fingerNum = -1, suggestionNum = -1;
	
	static String[] PlayerList;
	
	public Client() throws ClassNotFoundException, IOException {
	    setTitle("Zero Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		panel = new JPanel(null);
			
		contentPane.add(panel);
			
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
			
		label1.setText("Wellcome to ZeroGame");
		label2.setText("Write your nickname and press enter");
			
		textfield1 = new JTextField();
		textfield2 = new JTextField();
		textfield3 = new JTextField();
			
		button1 = new JButton();
		button2 = new JButton("Ready");
		button3 = new JButton("입력");
		button4 = new JButton("입력");
		
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nickname = textfield1.getText(); 	
				boolean suc = false;
				try {
					c_Socket = new Socket("127.0.0.1", 8888);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} //LocalHost
				suc = true;
	            JOptionPane.showMessageDialog(component, "당신의 닉네임은 " + nickname + " 입니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
				
				if(suc) {
					panel.removeAll();
		            	
		            label1.setText("Are You Ready?");
		            label1.setBounds(170, 100, 350, 50);
		        	label1.setFont(label1.getFont().deriveFont(30.0f));
		            	
		        	button2.setBounds(240, 170, 80, 30);
		        		
		        	panel.add(label1);
		        	panel.add(button2);
		        		
		        	panel.updateUI();	        	
				}
			}
		});
		
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(component, "서버 응답 대기중", "알림", JOptionPane.INFORMATION_MESSAGE);
				boolean suc = false;
				
				try {
					oos = new DataOutputStream(c_Socket.getOutputStream());
					bos = new BufferedOutputStream(oos);
					out = new ObjectOutputStream(bos);
					int _whattodo; int count = 0;
					out.writeObject(new Player(nickname,0)); //mydata를 쏘아보냄.
					out.flush();
					is = new DataInputStream(c_Socket.getInputStream());
					bis = new BufferedInputStream(is);
					in = new ObjectInputStream(bis);
					// a = (Player)in.readObject();
					// if(a.whatToDo == 13){
					//System.out.println("same NickName");
					// return;
					//  }
					//System.out.print("Are You Ready?(yes:1): ");
					//_whattodo = scan.nextInt();
					//_whattodo = 1;
					//while(_whattodo != 1){
					//    System.out.println("when you ready.. type 1");
					//    System.out.print("Are You ready?(yes:1): ");
					//_whattodo = scan.nextInt();
					//}
					_whattodo = 1;
					out.writeObject(new Player(nickname,_whattodo+1));
					out.flush();
	        		
					//System.out.println("서버 응답 대기중");
					a = (Player)in.readObject();
					if(a.whatToDo == 6){
						//System.out.println("Already Game Start");
						JOptionPane.showMessageDialog(component, "Already Game Start", "경고", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if(a.whatToDo == 13){
						//System.out.println("same NickName");
						JOptionPane.showMessageDialog(component, "Same Nickname", "경고", JOptionPane.WARNING_MESSAGE);
						return;
					}
					Thread.sleep(100);
					//***************************************
					//System.out.println("Wait2...");
					
					while(true){ //
						out.writeObject(new Player(nickname,3)); //모두 준비 되었나?
						//return으로 몇명, 준비 여부, 받고 싶어!
						out.flush();
						a = (Player)in.readObject();
						if(a.whatToDo == 1){ //2명이상이고.. ready되었다..!
							//너의 HP 포인트는 이때 할당된다.
							HP = a.HP; //내 HP임....
							break;
						}
						Thread.sleep(150);
					}
					System.out.println("GameSeesion Start!"); //게임 시작!
					System.out.println("HP : " + HP);
					int fingerNum; int suggestionNum;
					showMakeList(readMakeList(a.playerList));
					
					suc = true;
				} catch(SocketException ex){
					//System.out.println("서버가 종료되었습니다."); //서버가 닫혔을때..
					JOptionPane.showMessageDialog(component, "서버가 종료되었습니다.", "경고", JOptionPane.WARNING_MESSAGE);
					return;
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
				
				hpbtn = new JLabel[PlayerList.length];
				if(suc) {
					isStart = true;
					
					panel.removeAll();
	            	
	            	label1.setText("접속 완료");
	            	label1.setBounds(10, 10, 350, 50);
	        		label1.setFont(label1.getFont().deriveFont(15.0f));
	        		
	        		label2.setText("Please give me What Number");
	            	label2.setBounds(10, 60, 400, 50);
	        		label2.setFont(label1.getFont().deriveFont(15.0f));
	        		
	        		label3.setText("you suggest : up to 2 * UserNum");
	            	label3.setBounds(10, 80, 400, 50);
	        		label3.setFont(label1.getFont().deriveFont(15.0f));
	        		
	        		textfield2.setBounds(10, 120, 200, 30);
	        		
	        		button3.setBounds(10, 150, 150, 30);
	        		
	        		label4.setText("Please give me fingerNum : (0~2)");
	            	label4.setBounds(10, 210, 350, 50);
	        		label4.setFont(label1.getFont().deriveFont(15.0f));
	        		
	        		textfield3.setBounds(10, 250, 200, 30);
	        		
	        		button4.setBounds(10, 280, 150, 30);
	        		
	        		for(int i=0; i<hpbtn.length; i=i+2) {
	        			if(!nickname.equals(PlayerList[i])) {
	        				hpbtn[i] = new JLabel(PlayerList[i] + " : " + PlayerList[i+1]);
	        				hpbtn[i].setBounds(380, 30 + (20*i), 150, 50);
	        				hpbtn[i].setFont(label1.getFont().deriveFont(20.0f));
	        				panel.add(hpbtn[i]);
	        			}
	        		}
	        		
	        		label5.setText("HP : " + Integer.toString(HP));
	            	label5.setBounds(380, 230, 150, 50);
	        		label5.setFont(label1.getFont().deriveFont(30.0f));
	        		
	        		panel.add(label1);
	        		panel.add(label2);
	        		panel.add(label3);
	        		panel.add(textfield2);
	        		panel.add(button3);
	        		panel.add(label4);
	        		panel.add(textfield3);
	        		panel.add(button4);
	        		panel.add(label5);
	        		
	        		panel.updateUI();
				}
			}
		});
		
		panel.add(label1);
		panel.add(label2);
		panel.add(textfield1);
		panel.add(button1);
			
		label1.setBounds(120, 40, 350, 50);
		label1.setFont(label1.getFont().deriveFont(30.0f));
		
		label2.setBounds(110, 120, 350, 50);
		label2.setFont(label2.getFont().deriveFont(20.0f));
			
		textfield1.setBounds(185, 180, 200, 30);
		
		button1.setText("Enter");
		button1.setBounds(245, 240, 80, 30);
			
		setSize(600, 400);
		setVisible(true);
			
        //Scanner scan = new Scanner(System.in); //키보드로 받는 입력
        //먼저 자신의 닉네임을 정해준다. 서버에 전달할 것임.
        //System.out.println("본인의 닉네임을 입력해주세요.");
        //nickname = scan.nextLine();
        // myNickName += "\n";
        //System.out.println("당신의 닉네임은 " + nickname + " 입니다!");
        // 입장거부 되면 기다려야 하는데.. //예외중에 사람이 가득찼다! 도 있겠네..
        
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				suggestionNum = Integer.parseInt(textfield2.getText());
				JOptionPane.showMessageDialog(null, "입력 완료", "알림", JOptionPane.INFORMATION_MESSAGE);
				waiting = false;
			}
		});
    	
    	button4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fingerNum = Integer.parseInt(textfield3.getText());
				if(0<= fingerNum && fingerNum <=2)
					waiting = false;
				else
					JOptionPane.showMessageDialog(null, "you only have 2 thumbs", "경고", JOptionPane.WARNING_MESSAGE);
			}
		});
    	
    	while(true) {
    		System.out.println(isStart);
	    	while(isStart) {
				a = (Player)in.readObject(); //버퍼
				//System.out.println("asd");
				if(a.whatToDo == 8){
					//System.out.println("You Win");
					JOptionPane.showMessageDialog(null, "You Win", "알림", JOptionPane.INFORMATION_MESSAGE);
					showMakeList(readMakeList(a.playerList));
					return;
				}
				//System.out.println(hpbtn.length);
				//for(int i=0; i<hpbtn.length; i=i+2) {
        		//	if(!nickname.equals(PlayerList[i])) {
        		//		hpbtn[i].setText(PlayerList[i] + " : " + PlayerList[i+1]);
        		//	}
        		//}
				
				out.writeObject(new Player(nickname, 4)); //내차례야?
				out.flush();
				a = (Player)in.readObject();
				Player tmp;
				if (a.NickName.equals(nickname)) { //내차례..
					myturn = 1;
					//내 차례면.. 내가 제시할 점수와, 내가 올릴 손가락 개수를 센다.
					System.out.print("It's your Turn! Please give me fingerNum : (0~2)");
					label1.setText("My Turn!!");
					textfield2.setEnabled(true);
					button3.setEnabled(true);
					//while(true){
						//fingerNum = scan.nextInt();
					//	if(0<= fingerNum && fingerNum <=2){
					//		break;
					//	}
					//	System.out.println("you only have 2 thumbs");
					//}
					System.out.print("Please give me What Number you suggest : up to 2 * UserNum"); //my suggestion 제한
	                //suggestionNum = scan.nextInt();
					while(waiting) { System.out.println(waiting); }
					waiting = true;
					tmp = new Player(nickname,fingerNum, suggestionNum, 5);
				} else { //내차례 아님..
					myturn = 0;
					label1.setText(a.NickName + "'s Turn!!");
					textfield2.setEnabled(false);
					button3.setEnabled(false);
					System.out.print("not your Turn. Please give me fingerNum : ");
					//fingerNum = scan.nextInt();
					while(waiting) { System.out.println(waiting); }
					waiting = true;
					tmp = new Player(nickname,fingerNum, -1, 5);
				}
				System.out.println("Wait1...");
				while (true) {
					//System.out.println("aaaaaaaa");
					out.writeObject(tmp); out.flush();
					a = (Player)in.readObject();
					if (a.whatToDo == 11) {
						break;
					}
				}
				//hp 깎는거임!
				int damage = 0;
				if(myturn == 0){
					if(a.HP == -1){ //턴 인사람이 짐
						//System.out.println(a.NickName + " 's turn and he LOSE,you have no damage");
						JOptionPane.showMessageDialog(null, "You Win", "알림", JOptionPane.INFORMATION_MESSAGE);
					}
					else if(a.HP == 0){ //턴 인사람이 0으로 이김
						//System.out.println("all man shouted zero... " + a.NickName + " completely win!!!!!");
						JOptionPane.showMessageDialog(null, "all man shouted zero... " + a.NickName + " completely win!!!!!", "알림", JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						//System.out.println(a.NickName + " 's turn and he WIN, your damage is " + a.HP);
						JOptionPane.showMessageDialog(null, a.NickName + " 's turn and he WIN, your damage is " + a.HP, "알림", JOptionPane.INFORMATION_MESSAGE);
						damage = a.HP;
					}
					int a1 = HP - damage;
					System.out.println("HP : " + HP + " -> " + a1);
					HP = a1;
					label5.setText("HP : " + HP);
					if( HP <= 0){
						System.out.println("you loose");
						out.writeObject(new Player(nickname, 8)); //  나 죽은거임
						out.flush();
						break;
					}
				}
				else{
					if(a.HP == -1){ //턴 인사람이 짐
	                //System.out.println("you cannot type correct num");
	                JOptionPane.showMessageDialog(null, "you cannot type correct num", "알림", JOptionPane.INFORMATION_MESSAGE);
					}
					else if(a.HP == 0){ //턴 인사람이 0으로 이김
	                //System.out.println("all man shouted zero... you completely win!!!!!");
	                JOptionPane.showMessageDialog(null, "all man shouted zero... you completely win!!!!!", "알림", JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						//System.out.println( " you WIN with your input");
						JOptionPane.showMessageDialog(null, " you WIN with your input", "알림", JOptionPane.INFORMATION_MESSAGE);
						damage = 0;
					}
					System.out.println("HP : " + HP + " -> " + HP);
				}
	
				myturn = 0;
				out.writeObject(new Player(nickname, 7,"",HP)); // 다음차례
				out.flush();
				
				for(int i=0; i<hpbtn.length; i=i+2) {
        			if(!nickname.equals(PlayerList[i])) {
        				hpbtn[i].setText(PlayerList[i] + " : " + PlayerList[i+1]);
        			}
        		}
			} //
	    	isStart = false;
	    }
	}
	
    static String[] readMakeList(String toReturn){
        PlayerList = toReturn.split("&~");
        return PlayerList;
    }
    static void showMakeList(String[] PlayerList){
        int size = PlayerList.length;
        System.out.println("-------------");
        for( int i = 0; i < size; i++){
            System.out.print(PlayerList[i] + " ");
        }
        System.out.println("\n-------------");
    }
    static void showGameStatus(int HP, String[] PlayerList){
        System.out.println("my HP : " + HP);
    }
    //감정표현 기능 추가.
    public static void main(String args[]) throws ClassNotFoundException, IOException {
    	new Client();
    }
}
