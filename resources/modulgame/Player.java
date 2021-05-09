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
 * @author Fauzan
 */
public class Player extends GameObject{
    
    public int n;
    public Player(int x, int y, ID id, int nt){
        super(x, y, id);
        n = nt;
        //speed = 1;
    }

    @Override
    public void tick() {
        x += vel_x;
        y += vel_y;
        
        x = Game.clamp(x, 0, Game.WIDTH - 60);
        y = Game.clamp(y, 0, Game.HEIGHT - 80);

    }
    
    public int getN() {
        return n;
    }

    @Override
    public void render(Graphics g) {
        String colorCode;
        if(n == 1) {
            colorCode = "#3283a8";
        } else {
            colorCode = "#32a855";
        }
        g.setColor(Color.decode(colorCode));
        g.fillRect(x, y, 50, 50);
    }
}
