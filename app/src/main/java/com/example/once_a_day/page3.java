package com.example.once_a_day;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/*
 **    该界面为游戏界面，游戏主体在surfaceview里，这里包括一些参数的传递和处理
 *     主要功能：开始游戏前传输参数，游戏进行中时的暂停界面功能，结束游戏后结算奖励
 **
 */
public class page3 extends AppCompatActivity {
    private static final String TAG = "page3";
    private MySQLite helper2;
    private SQLiteDatabase mydb2;
    private Button button1,button2;
    private int x;
    private int player,dif,str,dex,dex_area,hp,skill;//全局变量：属性
    private String message1;
    private static Timer timer;//用于后台监听游戏结束
    private static TimerTask task;
    private Handler handler1;//用于更新监听线程？
    private boolean istimer;//定时器是否反应
    private boolean only_dialog;//确保弹窗的唯一性
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            //加入获取装备信息并修改surface里的参数
            message1 = bundle.getString("stage");

            SharedPreferences sp=this.getSharedPreferences("zks", MODE_PRIVATE);
            player=sp.getInt("player", 0);
            dif=sp.getInt("dif", 0);
            /*
            player=bundle.getInt("player");
            dif=bundle.getInt("dif");
            str=bundle.getInt("str");
            dex=bundle.getInt("dex");
            dex_area=bundle.getInt("dex_area");
            hp=bundle.getInt("hp");
            skill=bundle.getInt("skill");
            */
            x=Integer.parseInt( message1 );//用于后面的结算
            //int x=bundle.getInt("stage");
            if(message1.equals("1"))        {setContentView(R.layout.content_stage1);}
            //if(message1.equals("1"))        {Stage1_SurfaceView.player=player;Stage1_SurfaceView.dif=dif;Stage1_SurfaceView.str=str;Stage1_SurfaceView.dex=dex;Stage1_SurfaceView.dex_area=dex_area;Stage1_SurfaceView.hp=hp;Stage1_SurfaceView.skill=skill;setContentView(Stage1_SurfaceView.Init(this));}
            else if(message1.equals("2"))   {setContentView(R.layout.content_stage2);}
            else if(message1.equals("3"))   {setContentView(R.layout.content_stage3);}
            else if(message1.equals("4"))   {setContentView(R.layout.content_stage4);}
            else if(message1.equals("5"))   {setContentView(R.layout.content_stage5);}
            else if(message1.equals("10"))   {setContentView(R.layout.content_stage_a);}
            //setContentView(new Stage1_SurfaceView(page3.this));基本版本
            //setContentView(R.layout.content_stage1);XML版本
            //setContentView(Stage1_SurfaceView.Init(this));单例版本
        }
        //Stage5_SurfaceView.rec=0;//重置
        //!!setcontentView要在这些之前！
        button1 = (Button) findViewById(R.id.button01);//暂停功能
        istimer=false;
        //button2 = (Button) findViewById(R.id.button02);//返回+结算功能
        this.initDB();
        //this.initHandler();
        this.initTimerTask();
        //this.initProperty();
        this.initListeners();
    }
    private void initDB() {
        helper2 = new MySQLite(page3.this);
        mydb2 = helper2.getWritableDatabase();
    }

    private void initHandler() {
        handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1) show_menu();
            }
        };
    }

    //监听游戏结束 标志1：玩家失败  标志2：玩家胜利并开箱 后可以自动弹出菜单 TODO （更新加入了按键，已改成开箱秒发放奖励）
    private void initTimerTask(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {//这个run 的使用就是开启了一个新的线程，在这个子线程中是无法更新UI 的
                switch (x){
                    case 1:if(Stage1_SurfaceView.isEnd2) {get_award();timer.cancel();}break;
                    case 2:if(Stage2_SurfaceView.isEnd2) {get_award();timer.cancel();}break;
                    case 3:if(Stage3_SurfaceView.isEnd2) {get_award();timer.cancel();}break;
                    case 4:if(Stage4_SurfaceView.isEnd2) {get_award();timer.cancel();}break;
                    case 5:if(Stage5_SurfaceView.isEnd2) {get_award();timer.cancel();}break;
                    case 10:if(StageA2_SV.isEnd2) {get_award();timer.cancel();}break;
                    default: break;
                }
                /*
                if(!istimer){
                    switch (x){
                        case 1:
                            if(Stage1_SurfaceView.rec>0) istimer=true;
                            else if(Stage1_SurfaceView.isEnd) istimer=true;
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        default: break;
                    }
                    if(istimer){
                        Message msg=new Message();
                        msg.what=1;
                        handler1.sendMessage(msg);
                    }
                }
                * */
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void updateData(int id,int n) {//更新-物品数量 + n
        //String sql = "UPDATE " + helper.TABLE_NAME + " SET major=" + major + " WHERE id=" + id;
        String sql = "UPDATE " + helper2.ITEM_NAME + " SET number=number+n WHERE id=" + id;
        mydb2.execSQL(sql);
    }
    private void updateData(int id) {//更新-物品数量 固定为1
        //String sql = "UPDATE " + helper.TABLE_NAME + " SET major=" + major + " WHERE id=" + id;
        String sql = "UPDATE " + helper2.ITEM_NAME + " SET number=1 WHERE id=" + id;
        mydb2.execSQL(sql);
    }

    //暂停并显示游戏菜单
    private void show_menu(){
        if(!only_dialog){//确保对话框的唯一
            only_dialog=true;
            switch(x) {//控制暂停
                case 1: { Stage1_SurfaceView.stop = 1;break; }
                case 2: { Stage2_SurfaceView.stop = 1;break; }
                case 3: { Stage3_SurfaceView.stop = 1;break; }
                case 4: { Stage4_SurfaceView.stop = 1;break; }
                case 5: { Stage5_SurfaceView.stop = 1;break; }
                case 10: { StageA2_SV.stop = 1;break; }
                default: break;
            }
            AlertDialog.Builder alert=new AlertDialog.Builder(this,1);//默认5
            //View view = LayoutInflater.from(this.getBaseContext()).inflate(R.layout.mydialog,null,false);
            //alert.setView(view);//自定义对话框
            alert.setTitle("少女折寿中......")
                    .setMessage("操作说明："+
                            "\n     1:单指触屏控制移动"+
                            "\n     2:双指触屏使用技能"+
                            "\n 温馨提示："+
                            "\n     1:每擦弹10次能回蓝"+
                            "\n     2:退出前记得开箱");
            alert.setNegativeButton("结束游戏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Process.killProcess(Process.myPid());
                    only_dialog=false;
                    switch (x){
                        case 1:Stage1_SurfaceView.end();break;
                        case 2:Stage2_SurfaceView.end();break;
                        case 3:Stage3_SurfaceView.end();break;
                        case 4:Stage4_SurfaceView.end();break;
                        case 5:Stage5_SurfaceView.end();break;
                        case 10:Stage5_SurfaceView.end();break;
                        default:break;
                    }
                    finish();
                }
            });

            alert.setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //istimer=false;
                    only_dialog=false;
                    switch(x) {//控制暂停
                        case 1: { Stage1_SurfaceView.stop = 0;break; }
                        case 2: { Stage2_SurfaceView.stop = 0;break; }
                        case 3: { Stage3_SurfaceView.stop = 0;break; }
                        case 4: { Stage4_SurfaceView.stop = 0;break; }
                        case 5: { Stage5_SurfaceView.stop = 0;break; }
                        case 10: { StageA2_SV.stop = 0;break; }
                        default: break;
                    }
                }
            });
            alert.setCancelable(false);//弹窗时即使BACK也不关闭
            alert.create().show();
        }
    }

    //按钮触发界面
    private void initListeners() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_menu();
            }
        });

    }

    //BACK按键触发界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            show_menu();
        }
        return false;
    }

    //切后台时弹窗并暂停
    @Override
    public void onStop(){
        super.onStop();
        show_menu();
    }

    //结算并获得奖励
    private void get_award(){
        int id=0;//id值范围为0-20 0表示未成功开箱
        //子线程直接弹Toast的处理方法
        Looper.prepare();
        switch(x){
            case 1: {
                id=Stage1_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(dif==2)  Toast.makeText(page3.this, "你征服了这个关卡！", Toast.LENGTH_SHORT).show();
                else if(id>10){
                    switch (dif){
                        case 0:
                            updateData(10);Toast.makeText(page3.this, "已获得力Ⅰ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(11);Toast.makeText(page3.this, "已获得力Ⅱ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else Toast.makeText(page3.this, "硬币反面朝上，没有奖励", Toast.LENGTH_SHORT).show();
                Stage1_SurfaceView.rec=0;//重置
                break;
            }
            case 2:{
                id=Stage2_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(dif==2)  Toast.makeText(page3.this, "你征服了这个关卡！", Toast.LENGTH_SHORT).show();
                else if(id>10){
                    switch (dif){
                        case 0:
                            updateData(20);Toast.makeText(page3.this, "已获得敏Ⅰ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(21);Toast.makeText(page3.this, "已获得敏Ⅱ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else Toast.makeText(page3.this, "硬币反面朝上，没有奖励", Toast.LENGTH_SHORT).show();
                Stage2_SurfaceView.rec=0;//重置
                break;
            }
            case 3:{
                id=Stage3_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(dif==2)  Toast.makeText(page3.this, "你征服了这个关卡！", Toast.LENGTH_SHORT).show();
                else if(id>10){
                    switch (dif){
                        case 0:
                            updateData(30);Toast.makeText(page3.this, "已获得体Ⅰ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(31);Toast.makeText(page3.this, "已获得体Ⅱ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else Toast.makeText(page3.this, "硬币反面朝上，没有奖励", Toast.LENGTH_SHORT).show();
                Stage3_SurfaceView.rec=0;//重置
                break;
            }
            case 4:{
                id=Stage4_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(dif==2)  Toast.makeText(page3.this, "你征服了这个关卡！", Toast.LENGTH_SHORT).show();
                else if(id>10){
                    switch (dif){
                        case 0:
                            updateData(40);Toast.makeText(page3.this, "已获得全Ⅰ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(41);Toast.makeText(page3.this, "已获得全Ⅱ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else Toast.makeText(page3.this, "硬币反面朝上，没有奖励", Toast.LENGTH_SHORT).show();
                Stage4_SurfaceView.rec=0;//重置
                break;
            }
            case 5:{
                id=Stage5_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(dif==2)  Toast.makeText(page3.this, "你征服了这个关卡！", Toast.LENGTH_SHORT).show();
                else if(id>15){
                    switch (dif){
                        case 0:
                            updateData(50);Toast.makeText(page3.this, "已获得技Ⅰ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(52);Toast.makeText(page3.this, "已获得技Ⅲ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else if(id>10){
                    switch (dif){
                        case 0:
                            updateData(51);Toast.makeText(page3.this, "已获得技Ⅱ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            updateData(53);Toast.makeText(page3.this, "已获得技Ⅳ(唯一)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else Toast.makeText(page3.this, "硬币反面朝上，没有奖励", Toast.LENGTH_SHORT).show();
                Stage5_SurfaceView.rec=0;//重置
                //Stage5_SurfaceView.stop=0;//surfaceview的destory有延迟，这个语句后覆盖执行了一次，此处相当于无效了
                break;
            }
            case 10:{
                id=StageA2_SV.rec;//调用surfaceview的参数
                if(id==0) break;
                else Toast.makeText(page3.this, "WIN", Toast.LENGTH_SHORT).show();
                StageA2_SV.rec=0;//重置
                //Stage5_SurfaceView.stop=0;//surfaceview的destory有延迟，这个语句后覆盖执行了一次，此处相当于无效了
                break;
            }
            default:break;
        }
        /*旧版掉落概率
        switch(x){
            case 1: {
                id=Stage1_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(id>10){
                    switch (dif){
                        case 2:
                            if(id>18)   {updateData(14);Toast.makeText(page3.this, "已获得N型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(9);Toast.makeText(page3.this, "已获得I型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                        case 1:
                            if(id>18)   {updateData(9);Toast.makeText(page3.this, "已获得I型源码", Toast.LENGTH_SHORT).show();}//10%
                            else if((int)(Math.random()*100)>=90)   {updateData(14);Toast.makeText(page3.this, "已获得N型源码", Toast.LENGTH_SHORT).show();}//1%
                            else        {updateData(5);Toast.makeText(page3.this, "已获得E型源码", Toast.LENGTH_SHORT).show();}//39%
                            break;
                        default:
                            if(id>18)   {updateData(5);Toast.makeText(page3.this, "已获得E型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(1);Toast.makeText(page3.this, "已获得A型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                    }

                    //if(id==20) {updateData(14);Toast.makeText(page3.this, "已获得N型源码", Toast.LENGTH_SHORT).show();}
                    //else if(id>17) {updateData(9);Toast.makeText(page3.this, "已获得I型源码", Toast.LENGTH_SHORT).show();}
                    //else if(id>14) {updateData(5);Toast.makeText(page3.this, "已获得E型源码", Toast.LENGTH_SHORT).show();}
                    //else           {updateData(1);Toast.makeText(page3.this, "已获得A型源码", Toast.LENGTH_SHORT).show();}

                }
                else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                Stage1_SurfaceView.rec=0;//重置
                //Stage1_SurfaceView.stop=0;
                break;
            }
            case 2:{
                id=Stage2_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;

                if(id>10){
                    switch (dif){
                        case 2:
                            if(id>18)   {updateData(15);Toast.makeText(page3.this, "已获得O型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(10);Toast.makeText(page3.this, "已获得J型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                        case 1:
                            if(id>18)   {updateData(10);Toast.makeText(page3.this, "已获得J型源码", Toast.LENGTH_SHORT).show();}//10%
                            else if((int)(Math.random()*100)>=90)   {updateData(15);Toast.makeText(page3.this, "已获得O型源码", Toast.LENGTH_SHORT).show();}//1%
                            else        {updateData(6);Toast.makeText(page3.this, "已获得F型源码", Toast.LENGTH_SHORT).show();}//39%
                            break;
                        default:
                            if(id>18)   {updateData(6);Toast.makeText(page3.this, "已获得F型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(2);Toast.makeText(page3.this, "已获得B型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                    }
                }
                else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                //else new AlertDialog.Builder(page3.this).setTitle("结算").setMessage("谢谢参与！").show();
                Stage2_SurfaceView.rec=0;//重置
                //Stage2_SurfaceView.stop=0;
                break;
            }
            case 3:{
                id=Stage3_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(id>10){
                    switch (dif){
                        case 2:
                            if(id>18)   {updateData(16);Toast.makeText(page3.this, "已获得P型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(11);Toast.makeText(page3.this, "已获得K型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                        case 1:
                            if(id>18)   {updateData(11);Toast.makeText(page3.this, "已获得K型源码", Toast.LENGTH_SHORT).show();}//10%
                            else if((int)(Math.random()*100)>=90)   {updateData(16);Toast.makeText(page3.this, "已获得P型源码", Toast.LENGTH_SHORT).show();}//1%
                            else        {updateData(7);Toast.makeText(page3.this, "已获得G型源码", Toast.LENGTH_SHORT).show();}//39%
                            break;
                        default:
                            if(id>18)   {updateData(7);Toast.makeText(page3.this, "已获得G型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(3);Toast.makeText(page3.this, "已获得C型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                    }
                }
                else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                Stage3_SurfaceView.rec=0;//重置
                //Stage3_SurfaceView.stop=0;
                break;
            }
            case 4:{
                id=Stage4_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                if(id>10){
                    switch (dif){
                        case 2:
                            if(id>18)   {updateData(17);Toast.makeText(page3.this, "已获得Q型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(12);Toast.makeText(page3.this, "已获得L型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                        case 1:
                            if(id>18)   {updateData(12);Toast.makeText(page3.this, "已获得L型源码", Toast.LENGTH_SHORT).show();}//10%
                            else if((int)(Math.random()*100)>=90)   {updateData(17);Toast.makeText(page3.this, "已获得Q型源码", Toast.LENGTH_SHORT).show();}//1%
                            else        {updateData(8);Toast.makeText(page3.this, "已获得H型源码", Toast.LENGTH_SHORT).show();}//39%
                            break;
                        default:
                            if(id>18)   {updateData(8);Toast.makeText(page3.this, "已获得H型源码", Toast.LENGTH_SHORT).show();}//10%
                            else        {updateData(4);Toast.makeText(page3.this, "已获得D型源码", Toast.LENGTH_SHORT).show();}//40%
                            break;
                    }
                }
                else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                Stage4_SurfaceView.rec=0;//重置
                //Stage4_SurfaceView.stop=0;
                break;
            }
            case 5:{
                id=Stage5_SurfaceView.rec;//调用surfaceview的参数
                if(id==0) break;
                switch (dif){
                    case 2:
                        if(id>18) {updateData(18);Toast.makeText(page3.this, "已获得R型源码", Toast.LENGTH_SHORT).show();}//10%
                        else if(id>10) {updateData(13);Toast.makeText(page3.this, "已获得M型源码", Toast.LENGTH_SHORT).show();}//40%
                        else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        if(id>16) {//20%
                            if((int)(Math.random()*100)>=95) {updateData(18);Toast.makeText(page3.this, "已获得R型源码", Toast.LENGTH_SHORT).show();}//20% * 5% = 1%
                            else {updateData(13);Toast.makeText(page3.this, "已获得M型源码", Toast.LENGTH_SHORT).show();}//19%
                        }
                        else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        if(id>18) {updateData(13);Toast.makeText(page3.this, "已获得N型源码", Toast.LENGTH_SHORT).show();}//10%
                        else Toast.makeText(page3.this, "谢谢参与", Toast.LENGTH_SHORT).show();
                        break;
                }
                Stage5_SurfaceView.rec=0;//重置
                //Stage5_SurfaceView.stop=0;//surfaceview的destory有延迟，这个语句后覆盖执行了一次，此处相当于无效了
                break;
            }
            default:break;
        }
        * */
        Looper.loop();
    }

    @Override
    public void onDestroy(){

        //button1.setVisibility(View.VISIBLE);
        //跳转(不用？)
        //SoundPoolHelper xxx=new SoundPoolHelper(this);
        //xxx.release();
        timer.cancel();
        finish();
        super.onDestroy();
        //Log.d(TAG,"onDestroy");
    }
}