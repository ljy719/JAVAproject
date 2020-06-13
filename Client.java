package ZeroGame;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class Client {
    static String[] readMakeList(String toReturn){
        String[] PlayerList = toReturn.split("&~");
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
    public static void main(String args[]) {
        OutputStream oos;
        BufferedOutputStream bos ;
        ObjectOutputStream out ;
        InputStream is;
        BufferedInputStream bis;
        ObjectInputStream in;

        String nickname;
        int HP;
        int myturn = 0;
        Scanner scan = new Scanner(System.in); //키보드로 받는 입력
        //먼저 자신의 닉네임을 정해준다. 서버에 전달할 것임.
        System.out.println("본인의 닉네임을 입력해주세요.");
        nickname = scan.nextLine();
        // myNickName += "\n";
        System.out.println("당신의 닉네임은 " + nickname + " 입니다!");
        // 입장거부 되면 기다려야 하는데.. //예외중에 사람이 가득찼다! 도 있겠네..
        try {
            //Socket c_Socket = new Socket("192.168.43.216", 8888); // x1carbon IP
            // Socket c_Socket = new Socket("192.168.0.7", 8888); //Desktop IP
            Socket c_Socket = new Socket("127.0.0.1", 8888); //LocalHost

            try { Player a;
                oos = new DataOutputStream(c_Socket.getOutputStream());
                bos = new BufferedOutputStream(oos);
                out = new ObjectOutputStream(bos);
                int _whattodo; int count = 0;
                out.writeObject(new Player(nickname,0,-1)); //mydata를 쏘아보냄.
                out.flush();
                is = new DataInputStream(c_Socket.getInputStream());
                bis = new BufferedInputStream(is);
                in = new ObjectInputStream(bis);
               // a = (Player)in.readObject();
               // if(a.whatToDo == 13){
                    //System.out.println("same NickName");
                  // return;
              //  }
                System.out.print("Are You Ready?(yes:1): ");
                _whattodo = scan.nextInt();
                while(_whattodo != 1){
                    System.out.println("when you ready.. type 1");
                    System.out.print("Are You ready?(yes:1): ");
                    _whattodo = scan.nextInt();
                }
                out.writeObject(new Player(nickname,_whattodo+1));
                out.flush();
                System.out.println("서버 응답 대기중");
                a = (Player)in.readObject();
                if(a.whatToDo == 6){
                    System.out.println("Already Game Start");
                    return;
                }
                if(a.whatToDo == 13){
                    System.out.println("same NickName");
                     return;
                }
                Thread.sleep(100);
                //***************************************
                System.out.println("Wait2...");
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

                while(true) {
                    a = (Player)in.readObject(); //버퍼
                    System.out.println("asd");
                    if(a.whatToDo == 8){
                        System.out.println("You Win");
                        showMakeList(readMakeList(a.playerList));
                        return;
                    }
                    out.writeObject(new Player(nickname, 4)); //내차례야?
                    out.flush();
                    a = (Player)in.readObject();
                    Player tmp;
                    if (a.NickName.equals(nickname)) { //내차례..
                        myturn = 1;
                        //내 차례면.. 내가 제시할 점수와, 내가 올릴 손가락 개수를 센다.
                        System.out.print("It's your Turn! Please give me fingerNum : (0~2)");
                        while(true){
                            fingerNum = scan.nextInt();
                            if(0<= fingerNum && fingerNum <=2){
                                break;
                            }
                            System.out.println("you only have 2 thumbs");
                        }
                        System.out.print("Please give me What Number you suggest : up to 2 * UserNum"); //my suggestion 제한
                            suggestionNum = scan.nextInt();
                        tmp = new Player(nickname,fingerNum, suggestionNum, 5);
                    } else { //내차례 아님..
                        myturn = 0;
                        System.out.print("not your Turn. Please give me fingerNum : ");
                        while(true){
                            fingerNum = scan.nextInt();
                            if(0<= fingerNum && fingerNum <=2){
                                break;
                            }
                            System.out.println("you only have 2 thumbs");
                        }
                        tmp = new Player(nickname,fingerNum, -1, 5);
                    }
                    System.out.println("Wait1...");
                    while (true) {
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
                            System.out.println(a.NickName + " 's turn and he LOSE,you have no damage");
                        }
                        else if(a.HP == 0){ //턴 인사람이 0으로 이김
                            System.out.println("all man shouted zero... " + a.NickName + " completely win!!!!!");
                            damage = 10; //0으로 지면... 그냥 다 죽음!!
                        }
                        else{
                            System.out.println(a.NickName + " 's turn and he WIN, your damage is " + a.HP);
                            damage = a.HP;
                        }
                        int a1 = HP - damage;
                        System.out.println("HP : " + HP + " -> " + a1);
                        HP = a1;
                        if( HP <= 0){
                            System.out.println("you loose");
                            out.writeObject(new Player(nickname, 8)); //  나 죽은거임
                            out.flush();
                            break;
                        }
                    }
                    else{
                        if(a.HP == -1){ //턴 인사람이 짐
                            System.out.println("you cannot type correct num");
                        }
                        else if(a.HP == 0){ //턴 인사람이 0으로 이김
                            System.out.println("all man shouted zero... you completely win!!!!!");
                        }
                        else{
                            System.out.println( " you WIN with your input");
                            damage = 0;
                        }
                        System.out.println("HP : " + HP + " -> " + HP);
                    }

                    myturn = 0;
                    out.writeObject(new Player(nickname, 7,"",HP)); // 다음차례
                    out.flush();

                }

            }catch(SocketException e){
                System.out.println("서버가 종료되었습니다."); //서버가 닫혔을때..
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        catch(SocketException e){
            System.out.println("서버가 종료되었습니다."); //서버가 닫혔을때..
            return;
        }
        catch (IOException e) {
            System.out.println("Fail to connect");
            e.printStackTrace();
        }
    }
}
