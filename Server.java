package ZeroGame;
//구현 안된 기능.. 배틀룸, 동일 닉네임 검사, 감정표햔 -> 서버 푸쉬 개발의 어려움..
//몇초이상 입력이 없으면 튕기는 것도 구현하자.
//서버 푸쉬의 개념으로 통신했으면 훨씬더 효율적으로 할 수 있었을것
import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
class Player implements Serializable { //직렬화 -> 소켓통신으로 보낼 수 있게 처리하는 인터페이스
    String NickName; //유저이름
    int HP; //각 유저가 갖고있는 생명력이다. 게임 참가 인원수에 따라서 게임 시작시 인원에 맞춰 초기화됨.
    int ready;
    int whatToDo; // 1이면 setname, 2면 게임 나감, 3이면 ready 4면 not ready, ...
    int myFinger;
    String playerList; //0으로 구분을 할 것임!
    int mySuggestion;
    //************************
    Player(){
        this.NickName = null;
        this.HP = 0;
        this.ready = 0;
        this.myFinger = 0;
        this.mySuggestion = 0;
        this.playerList = null;
        this.whatToDo = 0;// 1이면 setname, 2면 게임 나감, 3이면 ready 4면 not ready, ... 5면 감정표현1 6이면 감정표현2 등등..
    }
    Player(String name,int _Whattodo) {
        this.NickName = name;
        this.HP = 0;
        this.ready = 0;
        this.myFinger = 0;
        this.mySuggestion = 0;
        this.playerList = "";
        this.whatToDo = _Whattodo;// 1이면 setname, 2면 게임 나감, 3이면 ready 4면 not ready, ... 5면 감정표현1 6이면 감정표현2 등등..
    }
    Player(String name,int _Whattodo,int _Myfinger) {
        this.NickName = name;
        this.HP = 0;
        this.ready = 0;
        this.myFinger = _Myfinger;
        this.mySuggestion = 0;
        this.playerList = "";
        this.whatToDo = _Whattodo;// 1이면 setname, 2면 게임 나감, 3이면 ready 4면 not ready, ... 5면 감정표현1 6이면 감정표현2 등등..
    }
    
    Player(String name,int _myFinger, int _mySuggestion , int _whattodo) {
        this.NickName = name;
        this.myFinger = _myFinger;
        this.HP = 0;
        this.ready = 0;
        this.mySuggestion = _mySuggestion;
        this.playerList = "";
        this.whatToDo = _whattodo;
    }
    Player(String name, int _whattodo,String _PL, int _HP) {
        this.NickName = name;
        this.myFinger = 0;
        this.mySuggestion = 0;
        this.HP = _HP;
        this.ready = 0;
        this.whatToDo = _whattodo;
        this.playerList = _PL; // 이름 + 0 +이름 + 0... +로 구분할 것임..!
    }
    Player(int finger, int _whattodo, String _PL, int _HP) {
        this.myFinger = finger;
        this.mySuggestion = 0;
        this.NickName = "";
        this.HP = _HP;
        this.ready = 0;
        this.whatToDo = _whattodo;
        this.playerList = _PL; // 이름 + 0 +이름 + 0... +로 구분할 것임..!
    }
    @Override
    public String toString() {
        return String.format("name=%s= HP=%d= ready=%d= whattodo=%d= myFinger=%d= playerList=%s= mySuggestion=%d=", NickName, HP,ready, whatToDo, myFinger,playerList,mySuggestion) ;
    }
}
abstract class UserVariable{
    static HashMap<String,Player> map;
    static HashMap<String,ObjectOutputStream> Socketmap;
    static BlockingQueue <Player> queue;
    static ServerSocket s_socket;
    static int userNum; //몇명의 유저가 지금 게임을 하고 있는지.
    static int gameStart; //게임 시작됨?
    static int whoTurn;
}
    class Receive extends UserVariable implements Runnable{ //각 유저마다 하나씩 생성한다.
        ObjectInputStream thisstream;
        ObjectOutputStream output;
        Player a;
        Receive(){};
        Receive( ObjectInputStream tmp,ObjectOutputStream tmp2){thisstream = tmp; output = tmp2;};
        public void run(){
            try{
                while(true){
                    a = (Player)thisstream.readObject();

                    if(a.whatToDo == 0){ //set name
                        if(UserVariable.gameStart == 1){
                            output.writeObject(new Player(" ",6)); output.flush();
                            return;
                        }
                        for(String key : map.keySet()){
                            Player am = (Player)map.get(key);
                           if (am.NickName.equals(a.NickName)){
                               System.out.println("same nickname..");
                               output.writeObject(new Player(" ",13)); output.flush();
                               return;
                           }
                        }
                        System.out.println("새로운 플레이어 ");
                        userNum += 1;
                        output.writeObject(a); output.flush();
                        Socketmap.put(a.NickName,output);
                        map.put(a.NickName,a);
                    }
                    else{
                            queue.put(a);
                    }
                }
            }catch (IOException e) { //중간에 나갔을때..
                System.out.println("연결이 해제되었습니다.");
                userNum -=1; //유저 인원수 관리.
                UserVariable.gameStart = 0;
                Socketmap.remove(a.NickName);
                map.remove(a.NickName); // 나갔다..
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
class AcceptThread extends  UserVariable implements Runnable{
    OutputStream oos;
    BufferedOutputStream bos ;
    ObjectOutputStream out ;
    AcceptThread(){}
    public void run(){
        while(true){
            System.out.println("연결 대기중...");
            try{
                Socket connected_socket = s_socket.accept();
                InputStream is = new DataInputStream(connected_socket.getInputStream());
                BufferedInputStream bis = new BufferedInputStream(is);
                ObjectInputStream in = new ObjectInputStream(bis);
                oos = new DataOutputStream(connected_socket.getOutputStream());
                bos = new BufferedOutputStream(oos);
                out = new ObjectOutputStream(bos);

                    Thread tmp = new Thread(new Receive(in,out));tmp.start();
                    //connected socket을 새로 만들어진 thread와 연결짓는다.
                    //맵에 넣은 client에게 push받는 쓰레드 생성.

            } catch (IOException e) {
               System.out.println("acceptthread 문제 발생");
               // e.printStackTrace();
            }
        }
    }
}
public class Server{
    public static void main (String args[]){
        try{
            UserVariable.map = new HashMap<String,Player>(); //map 인스턴스 생성. 여기에 유저정보가 저장될 것임.
            UserVariable.Socketmap = new HashMap<String,ObjectOutputStream>();
            UserVariable.queue = new ArrayBlockingQueue<Player>(500);
            UserVariable.s_socket = new ServerSocket(8888);// 할 일을 저장하는 queue
            UserVariable.userNum = 0; UserVariable.gameStart = 0; UserVariable.whoTurn = 0;
            List<String> UserOrder = null;
            int suggestedNum = 0;   int allSubmit = 1; int fingerNum = 0; int endUsers = 0;
            int alreadyAddSN = 0;   int isTurnGo = 0;
            System.out.println("Server Open Success.");
           Thread accept_user = new Thread(new AcceptThread()); accept_user.start();
            //무한 루프를 돌며 clients를 accept하는 Thread 받은 친구는 맵에 저장한다.
            // 무한 루프를 돌며 받아온 친구들을 큐에 푸쉬한다.
            Player tmp;
            while(true){
                    tmp = UserVariable.queue.take();
                    if(tmp.whatToDo == 2){ //ready
                        System.out.println(tmp.NickName + " get ready");
                        UserVariable.map.get(tmp.NickName).ready = 1;
                        UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player(" ",2));
                        UserVariable.Socketmap.get(tmp.NickName).flush();
                    }
                    else if(tmp.whatToDo == 3){ //all ready? when all peoples are not yet ready, return Players INFO via PL arguments in PLAYER
                        int OK = 1;
                        if(UserVariable.map.size() == 1) OK = 0; //혼자이면 안된다.
                        String toReturn = "";
                        for( String key : UserVariable.map.keySet() ){
                            UserVariable.map.get(key).HP = UserVariable.userNum * 5;
                            if(UserVariable.map.get(key).ready == 0){
                                OK = 0; //아직 모두 수락하지 않았다.
                            }
                            toReturn = toReturn.concat(key);
                            toReturn = toReturn.concat("&~"); //구분자로
                            toReturn = toReturn.concat(Integer.toString(UserVariable.map.get(key).HP));
                            toReturn = toReturn.concat("&~"); //구분자로

                        }
                        if(OK == 1){
                            UserVariable.gameStart = 1; //게임이 시작됨을 알림
                            //첫 시작사람을 설정한다...!
                            UserOrder = new ArrayList<String>(UserVariable.map.keySet());
                            UserOrder.get(0);
                        }
                            UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player(0,OK,toReturn,UserVariable.userNum * 5));
                            UserVariable.Socketmap.get(tmp.NickName).flush();

                        Thread.sleep(100);
                    }
                    else if(tmp.whatToDo == 4){
                            //is my turn??
                            System.out.println(tmp.NickName + " ask for whether their turn or not");
                            String a = UserOrder.get(UserVariable.whoTurn);
                            System.out.println("turn is " + a);
                            UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player(a,4,"Please",1));
                            UserVariable.Socketmap.get(tmp.NickName).flush();

                    }
                    else if(tmp.whatToDo == 5){ // how is the result?\
                        UserOrder = new ArrayList<String>(UserVariable.map.keySet());
                        UserVariable.map.get(tmp.NickName).myFinger = tmp.myFinger; //받았으면 이걸로 내 핑거 설정해주고.
                        if(tmp.mySuggestion != -1 && alreadyAddSN == 0){
                            suggestedNum = tmp.mySuggestion;
                            alreadyAddSN = 1;
                        } // 차례인 player의 suggestedNum으로 이번턴 게임 넘버를 셋..!
                        for(String key:UserOrder){
                            if(UserVariable.map.get(key).myFinger == 0){
                               allSubmit = 0;
                            }
                            fingerNum += UserVariable.map.get(key).myFinger;
                        }

                       if(allSubmit == 0){ //아직!
                           UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player("",10));
                           UserVariable.Socketmap.get(tmp.NickName).flush();
                           allSubmit = 1; fingerNum = 0;
                       }
                       else if(allSubmit != 0){ //다냈다! whattodo를 11로 보내 줄 것임!
                           System.out.println("다냈다!");
                           System.out.println(fingerNum + " vs " + suggestedNum);
                            if(fingerNum == suggestedNum){ //플레이어의 게임 승리!
                                System.out.println("Player Win");
                                isTurnGo = 0;
                                fingerNum = 0;
                                UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player(UserOrder.get(UserVariable.whoTurn),11,"",suggestedNum)); // 아직..
                                UserVariable.Socketmap.get(tmp.NickName).flush();
                            }
                            else{ //다음 턴으로 넘김.. hp 그대로..!
                                System.out.println("Player lose. next man!");
                                isTurnGo = 1;
                                fingerNum = 0;
                                UserVariable.Socketmap.get(tmp.NickName).writeObject(new Player(UserOrder.get(UserVariable.whoTurn),11,"",-1)); // 아직..
                                UserVariable.Socketmap.get(tmp.NickName).flush();
                            }
                       }
                    }
                    else if(tmp.whatToDo == 7){ // how is the result?
                        UserVariable.map.get(tmp.NickName).HP = tmp.HP; //HP업데이트.
                        endUsers += 1;
                        if(endUsers >= UserVariable.map.size()){
                            System.out.println("계산 완료됨.. 모두 다음 턴을 위한 준비 완료");
                            if(isTurnGo == 1){
                                UserVariable.whoTurn = (UserVariable.whoTurn + 1) % UserOrder.size();
                            }
                            isTurnGo = 0;
                            for(String key:UserOrder){
                                UserVariable.map.get(key).myFinger = 0; //다시 초기화..!
                            }
                            allSubmit = 1;  alreadyAddSN = 0;
                            endUsers = 0; //다시 0으로..!
                            for(String key:UserOrder){
                                UserVariable.Socketmap.get(key).writeObject(new Player());
                                UserVariable.Socketmap.get(key).flush();
                            }
                        }
                    }
                    else if(tmp.whatToDo == 8){ //somebody dead
                        String toReturn = "";
                        for( String key : UserVariable.map.keySet()){
                            toReturn = toReturn.concat(key);
                            toReturn = toReturn.concat("&~"); //구분자로
                            toReturn = toReturn.concat(Integer.toString(UserVariable.map.get(key).HP));
                            toReturn = toReturn.concat("&~"); //구분자로
                        }
                        UserOrder = new ArrayList<String>(UserVariable.map.keySet());
                        if(UserVariable.map.size() == 1){
                            UserVariable.gameStart = 0;
                        }
                        for(String key:UserOrder){
                            UserVariable.Socketmap.get(key).writeObject(new Player(UserOrder.get(UserVariable.whoTurn),8,toReturn,-1));
                            UserVariable.Socketmap.get(key).flush();
                        }

                    }

            }
        }
        catch(IOException e){
            System.out.println("Fail to open Server");
            System.out.println("May not correct IP Address");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//변경 내용.. 1대1 제로게임..!





}
