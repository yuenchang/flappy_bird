import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.io.*;
import javax.sound.sampled.*;

public class Game extends JFrame implements ActionListener, MouseListener
{
	private int mapX = 16;
	private int mapY = 16;
	private int bombTotal = 30;
	private int bombFlag = 0;
	
	private Clip lobbyClip;
	private Clip bangClip;
	private Clip vicClip;
	
	private JButton newBtn; //the button to start a new game
	private JPanel mapPanel; //the map is group in a panel
	private JButton map[][]; //the map is made of buttons by 9 * 9
	private JLabel message; //game message
	
	private Random rand;
	
	private int[][] bombSurround; //the number of the surrounding bombs
	private boolean[][] isBomb; //check if the block is a bomb
	private boolean gameOver; //check if the game is over
	private boolean[][] isPressed; //check if the block has been pressed
	private boolean gameStart; //to start the game immediately by pressing a block
	private boolean isBang;
	private boolean [][] isFlag;
	
	private Game()
	{
		//set the size and some basic elements
		setSize(680,680);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		//create a new button to start a new game
	    newBtn = new JButton();
		newBtn.setBackground(Color.GRAY);
		newBtn.setIcon(new ImageIcon("resource/bro.jpg"));
		newBtn.addActionListener(this);
		newBtn.setActionCommand("New");
		add(newBtn, BorderLayout.NORTH);
		
		//create a map and UI
		message = new JLabel();
		message.setText("Bomb Left: " + bombTotal);
		message.setSize(getPreferredSize());
		add(message, BorderLayout.SOUTH);
		
		mapPanel = new JPanel();
		mapPanel.setLayout(new GridLayout(mapX, mapY));
		map = new JButton[mapX][mapY];
		
		for(int i = 0; i < mapX; i++)
		{
			for(int j = 0; j < mapY; j++)
			{
				map[i][j] = new JButton();
				map[i][j].addMouseListener(this);
				map[i][j].setBackground(Color.WHITE);
				map[i][j].setActionCommand(	i + " " + j);
				mapPanel.add(map[i][j]);
			}
		}
		add(mapPanel, BorderLayout.CENTER);
		
		gameStart = false;
		
		//add BGM
		File lobbyFile = new File("resource/SF.wav");
		
		try
		{
			lobbyClip = AudioSystem.getClip();
			lobbyClip.open(AudioSystem.getAudioInputStream(lobbyFile));
			lobbyClip.start();
			lobbyClip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		catch(Exception e)
		{
		}
	}
	
	private void newGame()
	{
		gameOver = false;
        bombSurround = new int[mapX][mapY];
        isBomb = new boolean[mapX][mapY];
        isPressed = new boolean[mapX][mapY];
        isFlag = new boolean[mapX][mapY];
        
        for(int i = 0; i < mapX; i++)
        	for(int j = 0; j < mapY; j++)
        		isFlag[i][j] = false;
        
        int bombCount = 0;
        while(bombCount < bombTotal)
        {
        	rand = new Random();
            int x = rand.nextInt(mapX);
            int y = rand.nextInt(mapY);
            
            if(!isBomb[x][y])
            {
            	//place a bomb in a random block
            	bombCount++; 
            	isBomb[x][y] = true;
            		
            	//tell the surrounding blocks that there is a bomb
            	for(int i = x - 1; i <= x + 1; i++)
            		for(int j = y - 1; j <= y + 1; j++)
            			if(i >= 0 && i < mapX && j >= 0 && j < mapY)
            				bombSurround[i][j]++;
            }
        }
	}
	
	private void press(int x, int y)
	{
		//if the block is a bomb
		 if(isBomb[x][y])
	     {
	         gameOver = true;
	         message.setText("Boom! You're dead!");
	         
	         for(int i = 0; i < mapX; i++)
	        	 for(int j = 0; j < mapY; j++)
	        		 if(isBomb[i][j])
	        			 map[i][j].setIcon(new ImageIcon("resource/bang.jpg"));
	         
	         //add BGM
	 		 try
	 		 {
	 			File bangFile = new File("resource/Bang.wav");
	 			bangClip = AudioSystem.getClip();
				bangClip.open(AudioSystem.getAudioInputStream(bangFile));
	 			lobbyClip.stop();
	 			bangClip.start();
	 		 }
	 		 catch(Exception e)
	 		 {
	 		 }
	         
	         return;
	     }
	     else
	     {
	    	 if(bombSurround[x][y] != 0)
	         {
	    		 map[x][y].setBackground(Color.LIGHT_GRAY);
	    		 
	    	     	if(bombSurround[x][y] > 0)
	    	     		map[x][y].setText(Integer.toString(bombSurround[x][y]));
	    	        
	    	     isPressed[x][y] = true;
	         }
	         else
	         {
	        	 int[][] surround = {{-1,0},{0,-1},{1,0},{0,1},{-1,-1},{1,1},{-1,1},{1,-1}}; // 8 directions
	             int[] queueX = new int[mapX * mapY];
	             int[] queueY = new int[mapX * mapY];
	             int pop = 0, push = 0;
	            
	             //fill in the queue
	             queueX[push] = x;
	             queueY[push] = y;
	             push++;
	            
	             while(pop < push)
	             {
	            	 int tempX = queueX[pop];
	                 int tempY = queueY[pop];
	                
	                 //if there is nothing in the block, spread the chosen one
	                 if(bombSurround[tempX][tempY] == 0)
	                 {
	                	 for(int i = 0; i < 8; i++)
	                	 {
	                		 int chosenX = tempX + surround[i][0];
	                		 int chosenY = tempY + surround[i][1];
	                    
	                		 if((chosenX >= 0 && chosenX < mapX && chosenY >= 0 && chosenY < mapY) 
	                			 && !isPressed[chosenX][chosenY])
	                		 {
	                			 isPressed[chosenX][chosenY] = true;
	                			 queueX[push] = chosenX;
	                			 queueY[push] = chosenY;
	                			 push++;
	                		 }
	                	 }
	                 }   
	                 pop++;
	             }
	            
	             for(int i = 0; i < push; i++)
	             {
	            	 map[queueX[i]][queueY[i]].setBackground(Color.LIGHT_GRAY);
		    		 
		    	     	if(bombSurround[queueX[i]][queueY[i]] > 0)
		    	     		map[queueX[i]][queueY[i]].setText(Integer.toString(bombSurround[queueX[i]][queueY[i]]));
		    	        
		    	     isPressed[queueX[i]][queueY[i]] = true;
	             }
	         }
	     }
	}
	private void flag(int x, int y)
	{
		if(!isFlag[x][y] && !isPressed[x][y])
		{
			bombFlag++;
			map[x][y].setIcon(new ImageIcon("resource/flag.jpg"));
			isPressed[x][y] = true;
			isFlag[x][y] = true;
		}
		else if(isFlag[x][y])
		{
			bombFlag--;
			map[x][y].setIcon(null);
			isPressed[x][y] = false;
			isFlag[x][y] = false;
		}
		if(bombTotal - bombFlag > 0)
			message.setText("Bomb Left: " + (bombTotal - bombFlag));
		else
			message.setText("Bomb Left: 0");
	}
	
	private boolean checkVic()
	{
		//check if the player wins the game
        int correctCount = 0;
        for(int i = 0; i < mapX; i++)
        	for(int j = 0; j < mapY; j++)
        		if(map[i][j].getBackground() == Color.LIGHT_GRAY)
        			correctCount++;
        
        if(correctCount == (mapX * mapY - bombTotal))
        {
        	gameOver = true;
        	message.setText("You win!");
        	for(int i = 0; i < mapX; i++)
        		for(int j = 0; j < mapY; j++)
        			if(map[i][j].getBackground() == Color.WHITE)
        				map[i][j].setIcon(new ImageIcon("resource/flag.jpg"));
        	
        	try
    		{
    			File vicFile = new File("resource/win.wav");
    			vicClip = AudioSystem.getClip();
    			vicClip.open(AudioSystem.getAudioInputStream(vicFile));
    			lobbyClip.stop();
    			vicClip.start();
    		}
    		catch(Exception e)
    		{
    		}
        	
        	
        	return true;
        }
        else
        	return false;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("New"))
		{
			//initialize the map
			for(int i = 0; i < mapX; i++)
			{
	            for(int j = 0; j < mapY; j++)
	            {
	                map[i][j].setText("");
	                map[i][j].setIcon(null);
	                map[i][j].setBackground(Color.WHITE);
	            }
			}
			
			gameStart = false;
			message.setText("Bomb Left: " + bombTotal);
			
			try
			{
				if(isBang)
					vicClip.stop();
				else
					bangClip.stop();
			
				lobbyClip.start();
				lobbyClip.loop(Clip.LOOP_CONTINUOUSLY);
			}
			catch(Exception ex)
			{
			}
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		//the command is made of a string of i + " " + j
		String[] selected = ((JButton)e.getSource()).getActionCommand().split(" ");
		int x = Integer.parseInt(selected[0]);
		int y = Integer.parseInt(selected[1]);
		
		if(e.getButton() == MouseEvent.BUTTON3)
		{
			if(!gameStart)
			{
				newGame();
				gameStart = true;
			}
			if(!gameOver)
			{
				flag(x,y);
			}
		}
		else if(e.getButton() == MouseEvent.BUTTON1)
		{
            //check if the player has just started the game
            if(!gameStart)
            {
                newGame();
                gameStart = true;
            }
            
            //check if the game is over, if not, press the button
            if(!isPressed[x][y] && !gameOver)
            {	
                press(x,y);
                isPressed[x][y] = true;
            }
            isBang = checkVic();
		}
	}
	public void mouseExited(MouseEvent e)
	{
		
	}
	public void mouseEntered(MouseEvent e)
	{
		
	}
	public void mousePressed(MouseEvent e)
	{
		
	}
	public void mouseReleased(MouseEvent e)
	{
		
	}
	public static void main(String[] args)
	{
		Game game = new Game();
		game.setVisible(true);
	}
}
