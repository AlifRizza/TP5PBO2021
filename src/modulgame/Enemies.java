/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 *
 */
public class Enemies extends GameObject{
    int speed;
    public Enemies(int x, int y, ID id){
        super(x, y, id);
       
        speed = 5;
    }

    @Override
    public void tick() {
        x += vel_x;
        y += vel_y;
        
        x = Game.clamp(x, 0, Game.WIDTH - 60);
        y = Game.clamp(y, 0, Game.HEIGHT - 80);
    }
    

    public void move(int pX, int pY){
        int diffX = pX - x;
        int diffY = pY - y;

        float angle = (float)Math.atan2(diffY, diffX);

        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.decode("#0f0f0f"));
        g.fillRect(x, y, 50, 50);
    }
}
