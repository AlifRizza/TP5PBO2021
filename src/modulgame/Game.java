/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Random;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import modulgame.dbConnection;

/**
 *
 *
 */
public class Game extends Canvas implements Runnable{
    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private int score = 0;
    
    private int time = 10;
    
    private int totalTime = 0;
    
    private int speed = 50;
    
    private char mode = 's';
    
    private String username = "";
    
    private Thread thread;
    private boolean running = false;
    
    private Handler handler;
    
    public enum STATE{
        Game,
        GameOver
    };
    
    public STATE gameState = STATE.Game;
    
    public Game(){
        window = new Window(WIDTH, HEIGHT, "Modul praktikum 5", this);
        
        handler = new Handler();
        
        this.addKeyListener(new KeyInput(handler, this));
        
        if(gameState == STATE.Game){
            handler.addObject(new Items(100,150, ID.Item));
            handler.addObject(new Items(200,350, ID.Item));
            handler.addObject(new Player(200,200, ID.Player, 1));
            if (mode == 'n') {
                handler.addObject(new Player(200,500, ID.Player, 2));
            }
            handler.addObject(new Enemies(500,500, ID.Enemy));
        }
    }
    
    public Game(char n, int t, int sp){
        mode = n;
        time = t;
        speed = sp;
        window = new Window(WIDTH, HEIGHT, "Modul praktikum 5", this);
        
        handler = new Handler();
        
        this.addKeyListener(new KeyInput(handler, this));
        
        if(gameState == STATE.Game){
            handler.addObject(new Items(50,50, ID.Item));
            handler.addObject(new Items(300,400, ID.Item));
            handler.addObject(new Player(700,100, ID.Player, 1));
            if (mode == 'n') {
                handler.addObject(new Player(100,200, ID.Player, 2));
            }
            handler.addObject(new Enemies(450,50, ID.Enemy));
        }
    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        long timer2 = System.currentTimeMillis();
        int frames = 0;
        
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running){
                render();
                frames++;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                frames = 0;
                if(gameState == STATE.Game){
                    if(time>0){
                        time--;
                        totalTime++;
                    }else{
                        gameState = STATE.GameOver;
                        addScore();
                    }
                    
                }  
            }
            
            if(System.currentTimeMillis() - timer2 > speed){
                timer2 += speed;
                if(gameState == STATE.Game){
                    if(time>0){
                        enemyMove();
                    }
                }
            }
        }
        stop();
    }
    
    public void addScore(){
        dbConnection dbcon = new dbConnection();
        int player_score = score;
        score = score + totalTime;
        int prevScore = dbcon.getScore(username);
        if(prevScore == -1){
            System.out.println("New");
            dbcon.uploadScore(username, score, 0);
        }
        else if(prevScore < score){
            System.out.println("Update");
            dbcon.uploadScore(username, score, 1);
        }
        totalTime = 0;
    }
    
    public void enemyMove(){
        GameObject playerObject = null;
        int x1 = -1, x2 = -1, y1 = -1, y2 = -1;
        for(int i=0;i< handler.object.size(); i++){
            if(mode == 's'){
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                   x1 = playerObject.getX();
                   y1 = playerObject.getY();
                }
                if(handler.object.get(i).getId() == ID.Enemy && (x1 != -1 && y1 != -1)){
                   Enemies en = (Enemies) handler.object.get(i);
                   en.move(x1, y1);

                }
            }
            else{
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                   Player tempObj = (Player) handler.object.get(i);
                   if(tempObj.getN() == 1){
                    x1 = playerObject.getX();
                    y1 = playerObject.getY();
                   }
                   else{
                    x2 = playerObject.getX();
                    y2 = playerObject.getY();
                   }
                }
                if(handler.object.get(i).getId() == ID.Enemy && (x1 != -1 && y1 != -1)){
                    int eX, eY;
                    eX = handler.object.get(i).getX();
                    eY = handler.object.get(i).getY();
                    double d1 = distance(eX, eY, x1, y1);
                    double d2 = distance(eX, eY, x2, y2);
                    Enemies en = (Enemies) handler.object.get(i);
                    if(d1 < d2){
                        en.move(x1, y1);
                    }
                    else{
                        en.move(x2, y2);
                    }
                }
            }
        }
    }
    
    // Menghitung jarak antara 2 object
    public double distance(int x1, int y1, int x2, int y2){
        double res = Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((y2-y1), 2));
        return res;
    }
    
    public void setDelay(int del){
        speed = del;
    }
    
    public void setTime(int t){
        time = t;
    }
    
    public void setMode(char m){
        mode = m;
    }
    
    private void tick(){
        handler.tick();
        if(gameState == STATE.Game){
            GameObject playerObject1 = null;
            GameObject playerObject2 = null;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Player){
                   Player temp = (Player) handler.object.get(i);
                   if(temp.getN() == 1) {
                       playerObject1 = handler.object.get(i);
                   } else {
                       playerObject2 = handler.object.get(i);
                   }
                }
            }
            if(playerObject1 != null){
                for(int i=0;i< handler.object.size(); i++){
                    if(mode == 's') {
                        if(handler.object.get(i).getId() == ID.Item){
                            if(checkCollision(playerObject1, handler.object.get(i), 0)){
                                playSound("/Eat.wav");
                                handler.removeObject(handler.object.get(i));
                                Random rand = new Random();
                                score = score + (rand.nextInt(10) + 1);
                                time = time + 5;
                                handler.addObject(new Items(rand.nextInt(550),rand.nextInt(550), ID.Item));
                                break;
                            }
                        } else if(handler.object.get(i).getId() == ID.Enemy){
                            if(checkCollision(playerObject1, handler.object.get(i), 1)){
                                playSound("/Eat.wav");
                                gameState = STATE.GameOver;
                                addScore();
                            }
                        }
                    } else {
                        if(handler.object.get(i).getId() == ID.Item){
                            if(checkCollision(playerObject1, handler.object.get(i), 0) || (checkCollision(playerObject2, handler.object.get(i), 0) && mode == 'n')){
                                playSound("/Eat.wav");
                                handler.removeObject(handler.object.get(i));
                                Random rand = new Random();
                                score = score + (rand.nextInt(10) + 1);
                                time = time + 5;
                                handler.addObject(new Items(rand.nextInt(550),rand.nextInt(550), ID.Item));
                                break;
                            } 
                        } else if(handler.object.get(i).getId() == ID.Enemy){
                            if(checkCollision(playerObject1, handler.object.get(i), 1) || (checkCollision(playerObject2, handler.object.get(i), 1) && mode == 'n')){
                                playSound("/Eat.wav");
                                gameState = STATE.GameOver;
                                addScore();
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static boolean checkCollision(GameObject player, GameObject item, int type){
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeItem = 20;
        if(type == 1) {
            sizeItem = 50;
        }
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int itemLeft = item.x;
        int itemRight = item.x + sizeItem;
        int itemTop = item.y;
        int itemBottom = item.y + sizeItem;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);
                
        
        
        if(gameState ==  STATE.Game){
            handler.render(g);
            
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), 20, 20);

            g.setColor(Color.BLACK);
            g.drawString("Time: " +Integer.toString(time), WIDTH-120, 20);
        }else{
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH/2 - 120, HEIGHT/2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), WIDTH/2 - 50, HEIGHT/2 - 10);
            
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH/2 - 100, HEIGHT/2 + 30);
        }
                
        g.dispose();
        bs.show();
    }
    
    public static int clamp(int var, int min, int max){
        if(var >= max){
            return var = max;
        }else if(var <= min){
            return var = min;
        }else{
            return var;
        }
    }
    
    public void setUsername(String uname){
        username = uname;
    }
    
    public void close(){
        window.CloseWindow();
    }
    
    public void playSound(String filename){
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } catch (LineUnavailableException e) {
           e.printStackTrace();
        }
    
    }
}
