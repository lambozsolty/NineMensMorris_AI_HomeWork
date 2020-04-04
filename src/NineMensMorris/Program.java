
package NineMensMorris;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Program extends JFrame {
    private BoardState currentGame;
    private NineMensMorrisBoard boardPanel;
    private JPanel controls;
    private JButton newGameButton;
    private JTextField maxTimeTextField;
    private JTextField maxDepthTextField;
    private JLabel statusLabel;
    private AlphaBetaPruning solver;
    private volatile MoveExecutorCallback moveExecutor;

    private class MoveExecutor implements MoveExecutorCallback {

        private boolean terminate = false;

        public synchronized void terminate() {
            this.terminate = true;
            solver.terminateSearch();
        }

        @Override
        public synchronized void makeMove(Move move) {
            if (terminate) {
                return;
            }

            currentGame.makeMove(move);
            boardPanel.repaint();

            if (currentGame.hasCurrentPlayerLost()) {
                if (currentGame.getCurrentPlayer() == 1) {
                    statusLabel.setText("You won!");
                } else {
                    statusLabel.setText("You lost!");
                }
            } else if (currentGame.getCurrentPlayer() == 1) {
                statusLabel.setText("Making move...");

                int maxDepth = Integer.parseInt(maxDepthTextField.getText());
                int maxTime = Integer.parseInt(maxTimeTextField.getText()) * 1000;

                solver.setMaxDepth(maxDepth);
                solver.setMaxTime(maxTime);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Move move = solver.searchForBestMove();
                        MoveExecutor.this.makeMove(move);
                    }
                }).start();
            } else {
                statusLabel.setText("Your move");
                boardPanel.makeMove();
            }
        }
    }

    private void startNewGame() {
        if (moveExecutor != null) {
            moveExecutor.terminate();
        }
        currentGame = new BoardState();
        moveExecutor = new MoveExecutor();
        boardPanel.setBoard(currentGame, moveExecutor);
        statusLabel.setText("Your move");

        int maxDepth = Integer.parseInt(maxDepthTextField.getText());
        int maxTime = Integer.parseInt(maxTimeTextField.getText()) * 1000;
        solver = new AlphaBetaPruning(currentGame, maxDepth, maxTime);
        boardPanel.makeMove();
    }
    
        
    private void setMaxTimeTextField(String maxTimeValue)
    {
        maxTimeTextField.setText(maxTimeValue);
    }
    
    private void setMaxDepthTextField(String maxDepthValue)
    {
        maxDepthTextField.setText(maxDepthValue);
    }

    public Program(int maxDepth, int maxTime) {
        super("Nine Men's Morris");

        boardPanel = new NineMensMorrisBoard();

        add(boardPanel, BorderLayout.CENTER);

        controls = new JPanel();
        controls.setLayout(new FlowLayout());
        newGameButton = new JButton("New game");
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });
        controls.add(newGameButton);
        controls.add(new JLabel("Max move time:"));
        maxTimeTextField = new JTextField(3);
        maxTimeTextField.setText("30");
        controls.add(maxTimeTextField);
        controls.add(new JLabel("Max searching depth:"));
        maxDepthTextField = new JTextField(3);
        maxDepthTextField.setText("15");
        controls.add(maxDepthTextField);
        controls.add(new JLabel("Status:"));
        statusLabel = new JLabel("Your move");
        controls.add(statusLabel);

        add(controls, BorderLayout.SOUTH);
        
        setMaxDepthTextField(String.valueOf(maxDepth));
        setMaxTimeTextField(String.valueOf(maxTime));


        startNewGame();
    }

    public static void main(String[] args) {
       int maxDepth=5;
       int maxTime=5;
      	if(args.length!=4)
        {
	    
            System.out.println("Error at number of parameters\nYou have to give 4 parameter: e.g. -d N -t N");
            if(args.length>=4)
            {
                 System.out.println("Too much parameter");
                 System.exit(0);
            }
           
        }
        if(args.length<=4){
        
            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-d")) {
                       if (i == args.length - 1) {
                            System.out.println("You must give the maxdepth value!");
                            System.exit(1);
                        }
                        else
                        {
                            maxDepth= Integer.parseInt(args[i+1]);
                            System.out.println("maxdepth=" + maxDepth);
                        }
                    }
                    if (args[i].equals("-t")) {
                       if (i == args.length - 1) {
                            System.out.println("You must the value of thinking time!");
                            System.exit(1);
                        }
                        else
                        {   
                            
                            maxTime= Integer.parseInt(args[i+1]);
                            System.out.println("maxTime=" + maxTime);
                        }
                    }
                }
            }
        }
        if(args.length==4){
        JFrame game = new Program(maxDepth,maxTime);
        game.setSize(600, 700);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setVisible(true);
        }
    }
}