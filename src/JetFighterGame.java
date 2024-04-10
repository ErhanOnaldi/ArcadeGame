import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.*;
import java.util.List;


public class JetFighterGame extends JFrame implements Runnable {
    private boolean isLoggedIn = false;
    private String currentUsername = "";
    public JetFighterGame() {
        super("Jet Fighter Game");

        String imagePath = "background.jpg";

        setContentPane(new JPanel() {
            private Image backgroundImage = new ImageIcon(imagePath).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageObserver observer = this;
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), observer);
            }
        });

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        JMenuItem registerItem = new JMenuItem("Register");
        JMenuItem loginItem = new JMenuItem("Login");
        JMenuItem playGameItem = new JMenuItem("Play Game");
        JMenuItem scoreTableItem = new JMenuItem("Score Table");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(registerItem);
        fileMenu.add(loginItem);
        fileMenu.add(playGameItem);
        fileMenu.add(scoreTableItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        registerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        loginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });
        playGameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playGame();
            }
        });

        scoreTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showScoreTable();
            }
        });
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });

        setSize(1600,900);
        setResizable(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }
    private void showScoreTable() {
        updateScore(currentUsername,getHighScore(currentUsername));
        List<String> scoreRecords = readScoreTableFromFile();

        if (!scoreRecords.isEmpty()) {
            StringBuilder scoreTableText = new StringBuilder();
            for (String record : scoreRecords) {
                scoreTableText.append(record).append("\n");
            }

            JTextArea scoreTableTextArea = new JTextArea(scoreTableText.toString());
            scoreTableTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(scoreTableTextArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(300, 200));

            JOptionPane.showMessageDialog(this, scrollPane, "Score Table", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No scores available.", "Score Table", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public String getCurrentUsername()
    {
        return currentUsername;
    }
    public static void updateScore(String username, int newScore) {
        File scoreFile = new File("Scoretable.txt");
        Map<String, Integer> scores = new HashMap<>();

        try (Scanner scanner = new Scanner(scoreFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String user = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    scores.put(user, score);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Integer currentHighScore = scores.get(username);
        if (currentHighScore == null || currentHighScore < newScore) {
            scores.put(username, newScore);

            try (PrintWriter writer = new PrintWriter(scoreFile)) {
                for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                    writer.printf("%s : %d%n", entry.getKey(), entry.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private List<String> readScoreTableFromFile() {
        List<String> scoreRecords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("Scoretable.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scoreRecords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(scoreRecords, Collections.reverseOrder());

        return scoreRecords;
    }

    private void playGame() {
        if (isLoggedIn) {
            showGameScreen();
        } else {
            loginUser();
            if (isLoggedIn) {
                showGameScreen();
            }
        }
    }

    private void showGameScreen() {
        this.getContentPane().removeAll();
        CharacterShootingGUI gamePanel = new CharacterShootingGUI();
        this.getContentPane().add(gamePanel);
        this.revalidate();
        this.repaint();
        gamePanel.requestFocusInWindow();
    }

    private void loginUser() {
        if (isLoggedIn) {
            JOptionPane.showMessageDialog(this, "You are already logged in as: " + currentUsername);
        } else {
            String username = JOptionPane.showInputDialog(this, "Enter username:");
            String password = JOptionPane.showInputDialog(this, "Enter password:");

            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                if (checkCredentials(username, password)) {
                    isLoggedIn = true;
                    currentUsername = username;
                    JOptionPane.showMessageDialog(this, "Login successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter valid username and password");
            }
        }
    }

    private boolean checkCredentials(String enteredUsername, String enteredPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader("Logins.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String usernameFromFile = parts[0];
                    String passwordFromFile = parts[1];
                    if (usernameFromFile.equals(enteredUsername) && passwordFromFile.equals(enteredPassword)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username:");
        String password = JOptionPane.showInputDialog(this, "Enter password:");

        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            if (isUsernameAlreadyRegistered(username)) {
                JOptionPane.showMessageDialog(this, "Username '" + username + "' is already registered. Please choose a different username.");
            } else {

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("Logins.txt", true))) {
                    writer.write(username + "," + password);
                    writer.newLine();
                    writer.close();
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error during registration");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter valid username and password");
        }
    }

    private boolean isUsernameAlreadyRegistered(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("Logins.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String usernameFromFile = parts[0];
                    if (usernameFromFile.equals(username)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAboutDialog() {
        String aboutMessage = "Jet Fighter Game\n\n"
                + "Welcome to my Java project.\n"
                + "Have fun playing!\n\n"
                + "Version: 1.0\n"
                + "Developed by: Erhan Onaldi\n\n";

        if (isLoggedIn) {
            aboutMessage += "Current User: " + currentUsername + "\n";
        }

        aboutMessage += "High Score: " + getHighScore(currentUsername);

        JOptionPane.showMessageDialog(this, aboutMessage, "About Jet Fighter Game", JOptionPane.INFORMATION_MESSAGE);
    }

    private int getHighScore(String username) {
        int highScore = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("Scoretable.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String user = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    if (user.equals(username) && score > highScore) {
                        highScore = score;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return highScore;
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> new JetFighterGame());
    }
}

